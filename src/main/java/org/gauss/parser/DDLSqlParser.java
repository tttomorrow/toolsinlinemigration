/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gauss.converter.ColumnTypeConverter;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import java.util.List;
import java.util.stream.Collectors;

public class DDLSqlParser{

    private final static String TABLE_CREATE = "CREATE";
    public static final char COMMA = ',';
    public static final char DOT = '.';
    public static final char QUO = '\"';
    public static final char TAB = '\t';
    public static final char BRACKETS_START = '(';
    public static final char BRACKETS_ENDT = ')';

    public String parse(DDLValueStruct struct) {
        List<TableChangeStruct> tableChanges = struct.getPayload().getTableChanges();

        boolean isCreateDDL =
            tableChanges.stream().anyMatch(tableChangeStruct -> StringUtils.equals(tableChangeStruct.getType(), TABLE_CREATE));

        if (isCreateDDL) {
            List<String> openGaussCreateSqlList =
                tableChanges.stream().map(tableChangeStruct -> convertToOpenGaussSql(tableChangeStruct, struct.getPayload()
                    .getSource()))
                    .collect(Collectors.toList());
            return StringUtils.join(openGaussCreateSqlList, StringUtils.SPACE);
        }

        return struct.getPayload().getDdl();
    }

    private String convertToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        List<String> columnSqls = tableChangeStruct.getTable().getColumns().stream().map(column -> getColumnSqls(column))
            .collect(Collectors.toList());

        List<String> primaryKeySqls = tableChangeStruct.getTable().getPrimaryKeyColumnNames().stream()
            .map(primaryKey -> getPrimaryKeySql(primaryKey)).collect(Collectors.toList());

        List<String> foreignKeySqls = tableChangeStruct.getTable().getForeignKeyColumns().stream()
            .map(foreignKeyColumns -> getForeignKeySql(foreignKeyColumns)).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append(getTableTitleSql(source))
            .append(BRACKETS_START);

        sb.append(StringUtils.join(columnSqls, getColumnJoinStr()));
        sb.append(primaryKeySqls != null && primaryKeySqls.size() > 0? COMMA : StringUtils.EMPTY);
        sb.append(StringUtils.join(primaryKeySqls, getColumnJoinStr()));
        sb.append(foreignKeySqls != null && foreignKeySqls.size() > 0? COMMA : StringUtils.EMPTY);
        sb.append(StringUtils.join(foreignKeySqls, getColumnJoinStr()));
        sb.append(BRACKETS_ENDT);
        return sb.toString();
    }

    private String getForeignKeySql(TableChangeStruct.ForeignKeyColumns foreignKeyColumns) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("CONSTRAINT ");
        sb.append(addQuo(foreignKeyColumns.getFkName())).append(StringUtils.SPACE);
        sb.append("FOREIGN KEY ");
        sb.append(StringUtils.SPACE)
            .append(addBrackets(addQuo(foreignKeyColumns.getFkColumnName())))
            .append(StringUtils.SPACE);
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("REFERENCES ");
        sb.append(StringUtils.SPACE)
            .append(addQuo(foreignKeyColumns.getPktableSchem()))
            .append(DOT)
            .append(addQuo(foreignKeyColumns.getPktableName()))
            .append(StringUtils.SPACE)
            .append(addBrackets(addQuo(foreignKeyColumns.getPkColumnName())));
        return sb.toString();
    }

    private String getPrimaryKeySql(String primaryKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("PRIMARY KEY ");
        sb.append(addBrackets(addQuo(primaryKey)));
        sb.append(StringUtils.LF);
        return sb.toString();
    }

    private String getTableTitleSql(SourceStruct source) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF)
            .append(TABLE_CREATE)
            .append(StringUtils.SPACE)
            .append("TABLE")
            .append(StringUtils.SPACE)
            .append(addQuo(source.getSchema()))
            .append(DOT)
            .append(addQuo(source.getTable()))
            .append(StringUtils.LF);
        return sb.toString();
    }

    private String getColumnSqls(TableChangeStruct.column column) {
        StringBuilder sb = new StringBuilder();
        sb.append(TAB);
        sb.append(addQuo(ColumnTypeConverter.convertTypeName(column.getName()))).append(StringUtils.SPACE);
        sb.append(column.getTypeName())
            .append(column.getLength() > NumberUtils.INTEGER_ZERO? addBrackets(column.getLength()) : StringUtils.EMPTY)
            .append(StringUtils.SPACE);
        sb.append(column.isOptional()? StringUtils.EMPTY : "NOT NULL");
        return sb.toString();
    }

    private String addBrackets(Object str) {
        StringBuilder sb = new StringBuilder();
        sb.append(BRACKETS_START)
            .append(str)
            .append(BRACKETS_ENDT);
        return sb.toString();
    }

    private String addQuo(Object str) {
        StringBuilder sb = new StringBuilder();
        sb.append(QUO)
            .append(str)
            .append(QUO);
        return sb.toString();
    }

    private String getColumnJoinStr(){
        StringBuilder sb = new StringBuilder();
        sb.append(COMMA).append(StringUtils.CR).append(StringUtils.LF);
        return sb.toString();
    }
}
