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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DDLSqlParser{

    private final static String TABLE_ALTER = "ALTER";
    private final static String TABLE_CREATE = "CREATE";
    private final static String TABLE_PRIMARY_KEY_ADD = "ADD";
    private final static String TABLE_PRIMARY_KEY_DROP = "DROP";
    public static final char COMMA = ',';
    public static final char DOT = '.';
    public static final char QUO = '\"';
    public static final char TAB = '\t';
    public static final char BRACKETS_START = '(';
    public static final char BRACKETS_ENDT = ')';
    public static final char SEMICOLON = ';';

    public String parse(DDLValueStruct struct) {
        List<TableChangeStruct> tableChanges = struct.getPayload().getTableChanges();

        boolean isNeedParseSql =
            tableChanges.stream().anyMatch(tableChangeStruct -> isCreateSql(tableChangeStruct) || isAlterNeedParseSql(tableChangeStruct));

        if (isNeedParseSql) {
            List<String> openGaussSqlList =
                tableChanges.stream()
                    .map(tableChangeStruct -> convertSqlToOpenGaussSql(tableChangeStruct, struct.getPayload().getSource()))
                    .filter(openGaussSql -> StringUtils.isNotEmpty(openGaussSql))
                    .collect(Collectors.toList());
            return StringUtils.join(openGaussSqlList, SEMICOLON);
        }

        return struct.getPayload().getDdl();
    }

    private String convertSqlToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        if (isCreateSql(tableChangeStruct)) {
            return convertCreateSqlToOpenGaussSql(tableChangeStruct, source);
        }
        else if (isAlterNeedParseSql(tableChangeStruct)) {
            return convertAlterToOpenGaussSql(tableChangeStruct, source);
        }
        return null;
    }

    private boolean isCreateSql(TableChangeStruct tableChangeStruct){
        return StringUtils.equals(tableChangeStruct.getType(), TABLE_CREATE);
    }

    private boolean isAlterNeedParseSql(TableChangeStruct tableChangeStruct){
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (table != null && StringUtils.equals(tableChangeStruct.getType(), TABLE_ALTER)) {
            return (isNotEmpty(table.getPrimaryKeyColumnChanges()))
                || isNotEmpty(getColumnChanges(table.getColumns()));
        }
        return Boolean.FALSE;
    }

    private List<TableChangeStruct.column> getColumnChanges(List<TableChangeStruct.column> columns){
        return columns.stream().filter(column -> isNotEmpty(column.getModifyKeys())).collect(Collectors.toList());
    }

    private String convertCreateSqlToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        List<String> columnSqls = tableChangeStruct.getTable().getColumns().stream().map(column -> getColumnSqls(column))
            .collect(Collectors.toList());

        String primaryKeySql = getPrimaryKeySql(tableChangeStruct.getTable().getPrimaryKeyColumnNames());

        List<String> foreignKeySqls = tableChangeStruct.getTable().getForeignKeyColumns().stream()
            .map(foreignKeyColumn -> getForeignKeySql(foreignKeyColumn)).collect(Collectors.toList());

        List<String> uniqueColumnSqls = tableChangeStruct.getTable().getUniqueColumns().stream()
            .map(uniqueColumn -> getUniqueSql(uniqueColumn)).collect(Collectors.toList());

        List<String> checkColumnSqls = tableChangeStruct.getTable().getCheckColumns().stream()
            .map(checkColumn -> getCheckSql(checkColumn)).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append(getTableTitleSql(source))
            .append(BRACKETS_START);

        sb.append(StringUtils.join(columnSqls, getColumnJoinStr()));

        sb.append(StringUtils.isNotEmpty(primaryKeySql)? COMMA : StringUtils.EMPTY);
        sb.append(primaryKeySql);

        sb.append(isNotEmpty(uniqueColumnSqls)? COMMA : StringUtils.EMPTY);
        sb.append(StringUtils.join(uniqueColumnSqls, getColumnJoinStr()));

        sb.append(isNotEmpty(checkColumnSqls)? COMMA : StringUtils.EMPTY);
        sb.append(StringUtils.join(checkColumnSqls, getColumnJoinStr()));

        sb.append(isNotEmpty(foreignKeySqls)? COMMA : StringUtils.EMPTY);
        sb.append(StringUtils.join(foreignKeySqls, getColumnJoinStr()));

        sb.append(BRACKETS_ENDT);
        return sb.toString();
    }

    private String convertAlterToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (isNotEmpty(table.getPrimaryKeyColumnChanges())) {
            return  StringUtils.join(primaryKeyColumnChangeSql(table.getPrimaryKeyColumnChanges(), source.getTable(),
                    getTableAlterTitleSql(source)), SEMICOLON);
        }
        List<TableChangeStruct.column> columnChanges = getColumnChanges(table.getColumns());
        if (isNotEmpty(getColumnChanges(table.getColumns()))) {
            List<String> columnChangeSqls = columnChanges.stream()
                .flatMap(columnChange -> getColumnChangeSql(columnChange, getTableAlterTitleSql(source)).stream())
                .collect(Collectors.toList());
            return StringUtils.join(columnChangeSqls, SEMICOLON);
        }
        return StringUtils.EMPTY;
    }

    private List<String> getColumnChangeSql(TableChangeStruct.column column, String alterTitleSql) {
        return column.getModifyKeys().stream()
            .map(modifyKey -> {
                if (StringUtils.equals("defaultValueExpression", modifyKey)) {
                    return getDefaultChangeSql(column, alterTitleSql);
                }
                if (StringUtils.equals("optional", modifyKey)) {
                    return getOptionalSql(column, alterTitleSql);
                }
                return null;})
            .filter(changeSql -> StringUtils.isNotEmpty(changeSql))
            .collect(Collectors.toList());
    }

    private String getOptionalSql(TableChangeStruct.column column, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append("ALTER COLUMN ");
        sb.append(addQuo(column.getName())).append(StringUtils.SPACE);
        if (column.isOptional()) {
            sb.append("DROP NOT NULL");
        }
        else {
            sb.append("SET NOT NULL");
        }

        return sb.toString();
    }

    private String getDefaultChangeSql(TableChangeStruct.column column, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append("ALTER COLUMN ");
        sb.append(addQuo(column.getName())).append(StringUtils.SPACE);
        sb.append("SET DEFAULT ");
        sb.append(column.getDefaultValueExpression());

        return sb.toString();
    }


    private String getTableAlterTitleSql(SourceStruct source){
        StringBuilder sb = new StringBuilder();
        sb.append(TABLE_ALTER)
            .append(StringUtils.SPACE)
            .append("TABLE")
            .append(StringUtils.SPACE)
            .append(addQuo(source.getSchema()))
            .append(DOT)
            .append(addQuo(source.getTable()));

        return sb.toString();
    }
    private List<String> primaryKeyColumnChangeSql(List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChangeColumns, String tableName, String alterTitleSql){
        return primaryKeyColumnChangeColumns.stream()
            .map(primaryKeyColumnChangeColumn ->
                StringUtils.equals(primaryKeyColumnChangeColumn.getAction(), TABLE_PRIMARY_KEY_ADD) ?
                    getPrimaryKeyAddSqL(primaryKeyColumnChangeColumn.getColumnName(), alterTitleSql) :
                    getPrimaryKeyDropSqL(tableName, primaryKeyColumnChangeColumn.getColumnName(), alterTitleSql))
            .collect(Collectors.toList());
    }

    private String getPrimaryKeyAddSqL(String columnName, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        sb.append("PRIMARY KEY");
        sb.append(addBrackets(addQuo(columnName)));
        return sb.toString();
    }

    private String getPrimaryKeyDropSqL(String tableName, String columnName, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(TABLE_PRIMARY_KEY_DROP).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ");
        sb.append(addQuo(tableName + "_pkey"));
        return sb.toString();
    }

    private String getUniqueSql(TableChangeStruct.IndexColumn uniqueColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("CONSTRAINT ");
        sb.append(addQuo(uniqueColumn.getIndexName())).append(StringUtils.SPACE);
        sb.append("UNIQUE ");
        sb.append(StringUtils.SPACE)
            .append(addBrackets(addQuo(uniqueColumn.getColumnName())));
        return sb.toString();
    }

    private String getCheckSql(TableChangeStruct.CheckColumn checkColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("CONSTRAINT ");
        sb.append(addQuo(checkColumn.getIndexName())).append(StringUtils.SPACE);
        sb.append("CHECK ");
        sb.append(StringUtils.SPACE)
            .append(addBrackets(checkColumn.getCondition()));
        return sb.toString();
    }

    private String getForeignKeySql(TableChangeStruct.ForeignKeyColumn foreignKeyColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("CONSTRAINT ");
        sb.append(addQuo(foreignKeyColumn.getFkName())).append(StringUtils.SPACE);
        sb.append("FOREIGN KEY ");
        sb.append(StringUtils.SPACE)
            .append(addBrackets(addQuo(foreignKeyColumn.getFkColumnName())))
            .append(StringUtils.SPACE);
        sb.append(StringUtils.LF);
        sb.append(TAB);
        sb.append("REFERENCES ");
        sb.append(StringUtils.SPACE)
            .append(addQuo(foreignKeyColumn.getPktableSchem()))
            .append(DOT)
            .append(addQuo(foreignKeyColumn.getPktableName()))
            .append(StringUtils.SPACE)
            .append(addBrackets(addQuo(foreignKeyColumn.getPkColumnName())));
        return sb.toString();
    }

    private String getPrimaryKeySql(List<String> primaryKeys) {
        Set<String> primaryKeySet = primaryKeys.stream().map(primaryKey -> addQuo(primaryKey)).collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();
        if (!primaryKeySet.isEmpty()) {
            sb.append(StringUtils.LF);
            sb.append(TAB);
            sb.append("PRIMARY KEY ");
            sb.append(addBrackets(StringUtils.join(primaryKeySet, COMMA)));
            sb.append(StringUtils.LF);
        }
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
        if (StringUtils.isNotEmpty(column.getDefaultValueExpression())){
            sb.append("DEFAULT ").append(column.getDefaultValueExpression()).append(StringUtils.SPACE);
        }
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

    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isNotEmpty(Collection coll) {
        return !isEmpty(coll);
    }
}
