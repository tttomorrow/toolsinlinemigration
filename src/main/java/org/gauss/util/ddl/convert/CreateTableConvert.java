package org.gauss.util.ddl.convert;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gauss.converter.ColumnConvert;
import org.gauss.converter.ColumnTypeConverter;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description
 * @date 2022/4/20
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class CreateTableConvert extends BaseConvert implements DDLConvert {
    private static final Logger logger = LoggerFactory.getLogger(CreateTableConvert.class);

    public String parse(DDLValueStruct struct) {
        List<TableChangeStruct> tableChanges = struct.getPayload().getTableChanges();
        List<String> openGaussSqlList = tableChanges.stream()
                                                    .map(tableChangeStruct -> convertSqlToOpenGaussSql(tableChangeStruct,
                                                                                                       struct.getPayload().getSource()))
                                                    .collect(Collectors.toList());
        return StringUtils.join(openGaussSqlList, OpenGaussConstant.SEMICOLON);
    }

    private String convertSqlToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        return convertCreateSqlToOpenGaussSql(tableChangeStruct, source);
    }

    private String convertCreateSqlToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        List<String> columnSqls = tableChangeStruct.getTable().getColumns().stream().map(this::getColumnSqls).collect(Collectors.toList());

        String primaryKeySql = getPrimaryKeySql(tableChangeStruct.getTable());

        List<String> foreignKeySqls = tableChangeStruct.getTable()
                                                       .getForeignKeyColumns()
                                                       .stream()
                                                       .map(this::getForeignKeySql)
                                                       .collect(Collectors.toList());

        List<TableChangeStruct.UniqueColumn> uniqueColumns = tableChangeStruct.getTable().getUniqueColumns();
        List<String> uniqueColumnSqls = getUniqueColumnSqls(uniqueColumns);

        List<String> checkColumnSqls = tableChangeStruct.getTable().getCheckColumns().stream()
                .filter(checkcolumn-> !checkcolumn.getCondition().contains("IS JSON"))
                .map(this::getCheckSql)
                .collect(Collectors.toList());

        return getTableTitleSql(source) + OpenGaussConstant.BRACKETS_START + StringUtils.join(columnSqls, getColumnJoinStr()) +
                (StringUtils.isNotEmpty(primaryKeySql) ? OpenGaussConstant.COMMA : StringUtils.EMPTY) + primaryKeySql +
                (CollectionUtils.isNotEmpty(uniqueColumnSqls) ? OpenGaussConstant.COMMA : StringUtils.EMPTY) +
                StringUtils.join(uniqueColumnSqls, getColumnJoinStr()) +
                (CollectionUtils.isNotEmpty(checkColumnSqls) ? OpenGaussConstant.COMMA : StringUtils.EMPTY) +
                StringUtils.join(checkColumnSqls, getColumnJoinStr()) +
                (CollectionUtils.isNotEmpty(foreignKeySqls) ? OpenGaussConstant.COMMA : StringUtils.EMPTY) +
                StringUtils.join(foreignKeySqls, getColumnJoinStr()) + OpenGaussConstant.BRACKETS_ENDT;
    }

    private List<String> getUniqueColumnSqls(List<TableChangeStruct.UniqueColumn> uniqueColumns) {
        List<String> sql = new ArrayList<>();
        Map<String, List<TableChangeStruct.UniqueColumn>> uniqueConstraintGroupByConstraintName = uniqueColumns.stream()
                                                                                                               .filter(uniqueColumn -> StringUtils.isNotBlank(
                                                                                                                      uniqueColumn.getIndexName()))
                                                                                                               .collect(Collectors.groupingBy(
                                                                                                                       TableChangeStruct.UniqueColumn::getIndexName));
        for (Map.Entry<String, List<TableChangeStruct.UniqueColumn>> eachUniqueConstraint : uniqueConstraintGroupByConstraintName.entrySet()) {
            List<String> uniqueColumnNames = eachUniqueConstraint.getValue()
                                                                 .stream()
                                                                 .map(uniqueColumn -> wrapQuote(uniqueColumn.getColumnName()))
                                                                 .collect(Collectors.toList());
            String uniqueAlterSql = getUniqueSql(eachUniqueConstraint.getKey(), uniqueColumnNames);
            sql.add(uniqueAlterSql);
        }

        return sql;
    }


    private String getUniqueSql(String uniqueKeyName, List<String> columnNames) {
        return StringUtils.LF + OpenGaussConstant.TAB + OpenGaussConstant.CONSTRAINT + StringUtils.SPACE + wrapQuote(uniqueKeyName) +
                StringUtils.SPACE + OpenGaussConstant.UNIQUE + StringUtils.SPACE + StringUtils.SPACE +
                addBrackets(String.join(OpenGaussConstant.COMMA, columnNames));
    }

    private String getCheckSql(TableChangeStruct.CheckColumn checkColumn) {
        return StringUtils.LF + OpenGaussConstant.TAB + OpenGaussConstant.CONSTRAINT + StringUtils.SPACE + wrapQuote(checkColumn.getIndexName()) +
                StringUtils.SPACE + OpenGaussConstant.CHECK + StringUtils.SPACE + StringUtils.SPACE +
                addBrackets(replaceExpression(checkColumn.getCondition(), Arrays.asList(checkColumn.getIncludeColumn().split(","))));
    }

    private String getForeignKeySql(TableChangeStruct.ForeignKeyColumn foreignKeyColumn) {

        String fkColumnNameStr = wrapQuote(foreignKeyColumn.getFkColumnName());
        if (foreignKeyColumn.getFkColumnName().contains(OpenGaussConstant.COMMA)) {
            fkColumnNameStr = StringUtils.join(Arrays.stream(foreignKeyColumn.getFkColumnName().split(OpenGaussConstant.COMMA))
                                                     .map(this::wrapQuote)
                                                     .collect(Collectors.toList()), OpenGaussConstant.COMMA);
        }

        String pkColumnNameStr = wrapQuote(foreignKeyColumn.getPkColumnName());
        if (foreignKeyColumn.getFkColumnName().contains(OpenGaussConstant.COMMA)) {
            pkColumnNameStr = StringUtils.join(Arrays.stream(foreignKeyColumn.getPkColumnName().split(OpenGaussConstant.COMMA))
                                                     .map(this::wrapQuote)
                                                     .collect(Collectors.toList()), OpenGaussConstant.COMMA);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.LF);
        sb.append(OpenGaussConstant.TAB);
        sb.append(OpenGaussConstant.CONSTRAINT).append(StringUtils.SPACE);
        sb.append(wrapQuote(foreignKeyColumn.getFkName())).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.FOREIGN_KEY).append(StringUtils.SPACE);
        sb.append(StringUtils.SPACE).append(addBrackets(fkColumnNameStr)).append(StringUtils.SPACE);
        sb.append(StringUtils.LF);
        sb.append(OpenGaussConstant.TAB);
        sb.append(OpenGaussConstant.REFERENCES)
          .append(StringUtils.SPACE)
          .append(wrapQuote(foreignKeyColumn.getPktableSchem()))
          .append(OpenGaussConstant.DOT)
          .append(wrapQuote(foreignKeyColumn.getPktableName()))
          .append(StringUtils.SPACE)
          .append(addBrackets(pkColumnNameStr));
        if (StringUtils.isNotEmpty(foreignKeyColumn.getCascade())) {
            sb.append(StringUtils.SPACE).append(foreignKeyColumn.getCascade());
        }
        return sb.toString();
    }

    private String getPrimaryKeySql(TableChangeStruct.Table table) {
        List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChanges = table.getPrimaryKeyColumnChanges();
        if (CollectionUtils.isNotEmpty(primaryKeyColumnChanges)) {
            Set<String> primaryKeyAddColumnNames = primaryKeyColumnChanges.stream()
                                                                          .map(primaryKeyColumnChangeColumn -> wrapQuote(primaryKeyColumnChangeColumn.getColumnName()))
                                                                          .collect(Collectors.toSet());
            Optional<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChange = primaryKeyColumnChanges.stream()
                                                                                                               .filter(primaryKeyColumnChangeColumn -> StringUtils.isNotEmpty(
                                                                                                                       primaryKeyColumnChangeColumn.getConstraintName()))
                                                                                                               .findAny();
            if (primaryKeyColumnChange.isPresent()) {
                return getPrimaryKeyAddByConstraintName(primaryKeyAddColumnNames, primaryKeyColumnChange.get().getConstraintName());
            }
        }
        return getPrimaryKeySqlByNoConstraintName(table.getPrimaryKeyColumnNames());

    }

    private String getPrimaryKeySqlByNoConstraintName(List<String> primaryKeys) {
        Set<String> primaryKeySet = primaryKeys.stream().map(this::wrapQuote).collect(Collectors.toSet());
        StringBuilder sb = new StringBuilder();
        if (!primaryKeySet.isEmpty()) {
            sb.append(StringUtils.LF);
            sb.append(OpenGaussConstant.TAB);
            sb.append(OpenGaussConstant.PRIMARY_KEY).append(StringUtils.SPACE);
            sb.append(addBrackets(StringUtils.join(primaryKeySet, OpenGaussConstant.COMMA)));
            sb.append(StringUtils.LF);
        }
        return sb.toString();
    }

    private String getPrimaryKeyAddByConstraintName(Set<String> columnNameList, String constraintName) {
        return StringUtils.LF + OpenGaussConstant.TAB + OpenGaussConstant.CONSTRAINT + StringUtils.SPACE + wrapQuote(constraintName) +
                StringUtils.SPACE + OpenGaussConstant.PRIMARY_KEY + StringUtils.SPACE +
                addBrackets(StringUtils.join(columnNameList, OpenGaussConstant.COMMA)) + StringUtils.LF;
    }

    private String getTableTitleSql(SourceStruct source) {
        return StringUtils.LF + OpenGaussConstant.TABLE_CREATE + StringUtils.SPACE + OpenGaussConstant.TABLE + StringUtils.SPACE +
                wrapQuote(source.getSchema()) + OpenGaussConstant.DOT + wrapQuote(source.getTable()) + StringUtils.LF;
    }

    private String getColumnSqls(TableChangeStruct.column column) {
        StringBuilder sb = new StringBuilder();
        sb.append(OpenGaussConstant.TAB);
        sb.append(wrapQuote(column.getName())).append(StringUtils.SPACE);
        ColumnTypeConverter ColumnTypeConverter = new ColumnTypeConverter();
        String targetTypeName = ColumnTypeConverter.convertTypeName(column.getTypeName());
        if (null == targetTypeName) {
            logger.error("source column :{} type {} not support in openGauss yet! convert to character varying now ",
                         column.getName(),
                         column.getTypeName());
        }
        ColumnConvert columnConvert = ColumnConvert.convertToColumnConvert(column.getTypeName());
        String columnDdlSql = columnConvert.getConvertFunction().apply(column);
        sb.append(columnDdlSql).append(StringUtils.SPACE);
        if (StringUtils.isNotEmpty(column.getDefaultValueExpression())) {
            sb.append(OpenGaussConstant.DEFAULT).append(StringUtils.SPACE).append(column.getDefaultValueExpression()).append(StringUtils.SPACE);
        }
        sb.append(column.isOptional() ? StringUtils.EMPTY : OpenGaussConstant.NOT_NULL);
        return sb.toString();
    }

    @Override
    public List<String> convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        return Collections.singletonList(parse(ddlValueStruct));
    }
}
