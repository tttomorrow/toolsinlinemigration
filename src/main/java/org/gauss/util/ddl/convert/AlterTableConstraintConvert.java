package org.gauss.util.ddl.convert;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description 对主键约束类型的转换操作
 * @date 2022/5/24
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class AlterTableConstraintConvert extends BaseConvert implements DDLConvert {
    /**
     * @param struct debezium captured DDL record
     * @return list of ddl
     */
    @Override
    public List<String> convertToOpenGaussDDL(DDLValueStruct struct) {
        List<TableChangeStruct> tableChanges = struct.getPayload().getTableChanges();
        List<String> openGaussSqlList = tableChanges.stream()
                                                    .map(tableChangeStruct -> convertAlterToOpenGaussSql(tableChangeStruct,
                                                                                                         struct.getPayload().getSource()))
                                                    .filter(CollectionUtils::isNotEmpty)
                                                    .flatMap(Collection::stream)
                                                    .collect(Collectors.toList());
        return Collections.singletonList(StringUtils.join(openGaussSqlList, OpenGaussConstant.SEMICOLON));
    }

    private List<String> convertAlterToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        List<String> ddl = new ArrayList<>();
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (CollectionUtils.isNotEmpty(table.getPrimaryKeyColumnChanges())) {
            ddl.add(StringUtils.join(primaryKeyColumnChangeSql(table.getPrimaryKeyColumnChanges(),
                                                               source.getTable(),
                                                               getTableAlterTitleSql(source),
                                                               table.getColumns()), OpenGaussConstant.SEMICOLON));
        }
        if (CollectionUtils.isNotEmpty(table.getUniqueColumns())) {
            ddl.add(StringUtils.join(uniqueColumnChangeSql(table.getUniqueColumns(), getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON));
        }
        if (CollectionUtils.isNotEmpty(table.getCheckColumns())) {
            ddl.add(StringUtils.join(checkChangeSql(table.getCheckColumns(), getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON));
        }

        if (CollectionUtils.isNotEmpty(table.getForeignKeyColumns())) {
            ddl.add(StringUtils.join(foreignKeySql(table.getForeignKeyColumns(), getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON));
        }

        List<TableChangeStruct.column> columnChanges = getColumnChanges(table.getColumns());
        if (CollectionUtils.isNotEmpty(columnChanges)) {
            List<String> columnChangeSqls = columnChanges.stream()
                                                         .flatMap(columnChange -> getColumnChangeSql(columnChange,
                                                                                                     getTableAlterTitleSql(source)).stream())
                                                         .collect(Collectors.toList());
            ddl.add(StringUtils.join(columnChangeSqls, OpenGaussConstant.SEMICOLON));
        }
        return ddl;
    }


    private List<String> checkChangeSql(List<TableChangeStruct.CheckColumn> checkColumns, String alterTitleSql) {
        return checkColumns.stream().map(checkColumn -> getCheckAlterSqL(checkColumn, alterTitleSql)).collect(Collectors.toList());
    }

    private String getCheckAlterSqL(TableChangeStruct.CheckColumn checkColumn, String alterTitleSql) {
        return alterTitleSql + StringUtils.SPACE + OpenGaussConstant.TABLE_PRIMARY_KEY_ADD + StringUtils.SPACE + "CONSTRAINT " +
                wrapQuote(checkColumn.getIndexName()) + StringUtils.SPACE + "CHECK " +
                addBrackets(replaceExpression(checkColumn.getCondition(), Arrays.asList(checkColumn.getIncludeColumn().split(","))));
    }

    private List<TableChangeStruct.column> getColumnChanges(List<TableChangeStruct.column> columns) {
        return columns.stream().filter(column -> CollectionUtils.isNotEmpty(column.getModifyKeys())).collect(Collectors.toList());
    }

    /**
     * this code move to AlterTableColumnConvert.class
     *
     * @param column
     * @param alterTitleSql
     * @return
     */
    private List<String> getColumnChangeSql(TableChangeStruct.column column, String alterTitleSql) {
        return Collections.emptyList();
    }

    private String getPrimaryKeyDropSqL(String tableName, TableChangeStruct.PrimaryKeyColumnChange primaryKeyColumnChange,
                                        String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_DROP).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ");
        if (StringUtils.isNotEmpty(primaryKeyColumnChange.getConstraintName())) {
            sb.append(wrapQuote(primaryKeyColumnChange.getConstraintName()));
        } else {
            sb.append(wrapQuote(tableName + "_pkey"));
        }
        if (StringUtils.isNotEmpty(primaryKeyColumnChange.getCascade())) {
            sb.append(StringUtils.SPACE).append(primaryKeyColumnChange.getCascade());
        }
        return sb.toString();
    }

    private String getPrimaryKeyAddSqL(String tableName, List<String> columnNameList, String alterTitleSql, String constraintName,
                                       String usingIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        if (StringUtils.isNotBlank(usingIndex)) {
            if (StringUtils.isBlank(constraintName)) {
                constraintName = tableName + "_pkey";
            }
        }
        if (StringUtils.isNotEmpty(constraintName)) {
            sb.append("CONSTRAINT ").append(wrapQuote(constraintName)).append(StringUtils.SPACE);
        }
        sb.append("PRIMARY KEY ");
        if (StringUtils.isNotBlank(usingIndex)) {
            sb.append("USING INDEX ").append(wrapQuote(usingIndex));
        } else {
            sb.append(addBrackets(StringUtils.join(columnNameList, OpenGaussConstant.COMMA)));
        }
        return sb.toString();
    }

    private List<String> primaryKeyColumnChangeSql(List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChangeColumns, String tableName,
                                                   String alterTitleSql, List<TableChangeStruct.column> columns) {

        Map<String, TableChangeStruct.column> columnMapKeyByColumnName = columns.stream()
                                                                                .collect(Collectors.toMap(TableChangeStruct.column::getName,
                                                                                                          s -> s,
                                                                                                          (s1, s2) -> s1));

        Map<String, List<TableChangeStruct.PrimaryKeyColumnChange>> primaryKeyGroup = primaryKeyColumnChangeColumns.stream()
                                                                                                                   .collect(Collectors.groupingBy(
                                                                                                                           TableChangeStruct.PrimaryKeyColumnChange::getAction));

        List<String> primaryKeySqlList = new ArrayList<>();

        for (Map.Entry<String, List<TableChangeStruct.PrimaryKeyColumnChange>> entry : primaryKeyGroup.entrySet()) {
            String action = entry.getKey();
            List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChanges = entry.getValue();
            List<String> primaryKeyAddColumnNames = primaryKeyColumnChanges.stream()
                                                                           .map(primaryKeyColumnChangeColumn -> wrapQuote(primaryKeyColumnChangeColumn.getColumnName()))
                                                                           .collect(Collectors.toList());
            Optional<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChange = primaryKeyColumnChanges.stream()
                                                                                                               .filter(primaryKeyColumnChangeColumn -> StringUtils.isNotEmpty(
                                                                                                                       primaryKeyColumnChangeColumn.getConstraintName()))
                                                                                                               .findAny();
            String usingIndex = primaryKeyColumnChanges.stream()
                                                       .map(TableChangeStruct.PrimaryKeyColumnChange::getUsingIndex)
                                                       .filter(StringUtils::isNotBlank)
                                                       .findAny()
                                                       .orElse(StringUtils.EMPTY);

            if (StringUtils.equals(action.toUpperCase(), OpenGaussConstant.TABLE_PRIMARY_KEY_ADD)) {
                if (primaryKeyColumnChange.isPresent()) {
                    primaryKeySqlList.add(getPrimaryKeyAddSqL(tableName,primaryKeyAddColumnNames,
                                                              alterTitleSql,
                                                              primaryKeyColumnChange.get().getConstraintName(),usingIndex));
                } else {
                    primaryKeySqlList.add(getPrimaryKeyAddSqL(tableName,primaryKeyAddColumnNames, alterTitleSql, null,usingIndex));
                }

            } else {
                primaryKeySqlList.addAll(primaryKeyColumnChanges.stream()
                                                                .map(primaryKeyColumnChangeColumn -> getPrimaryKeyDropSqL(tableName,
                                                                                                                          primaryKeyColumnChangeColumn,
                                                                                                                          alterTitleSql))
                                                                .collect(Collectors.toSet()));
                if (primaryKeyColumnChange.isPresent() && StringUtils.isNotEmpty(primaryKeyColumnChange.get().getConstraintName())) {
                    if (StringUtils.equals(primaryKeyColumnChange.get().getType(), NumberUtils.INTEGER_ONE.toString())) {
                        primaryKeySqlList.addAll(primaryKeyAddColumnNames.stream()
                                                                         .filter(columnName -> columnMapKeyByColumnName.containsKey(columnName) &&
                                                                                 columnMapKeyByColumnName.get(columnName).isOptional())
                                                                         .map(columnName -> getColumnNullSql(columnName, alterTitleSql))
                                                                         .collect(Collectors.toSet()));
                    }
                } else {
                    primaryKeySqlList.addAll(primaryKeyAddColumnNames.stream()
                                                                     .filter(columnName -> columnMapKeyByColumnName.containsKey(columnName) &&
                                                                             columnMapKeyByColumnName.get(columnName).isOptional())
                                                                     .map(columnName -> getColumnNullSql(columnName, alterTitleSql))
                                                                     .collect(Collectors.toSet()));
                }

            }
        }
        return primaryKeySqlList;
    }

    private String getColumnNullSql(String columnName, String alterTitleSql) {
        return alterTitleSql + StringUtils.SPACE + "ALTER COLUMN " + columnName + StringUtils.SPACE + "DROP NOT NULL";
    }

    private List<String> uniqueColumnChangeSql(List<TableChangeStruct.UniqueColumn> uniqueColumns, String alterTitleSql) {
        List<String> uniqueColumnChangeSqlList = new ArrayList<>();
        Map<String, List<TableChangeStruct.UniqueColumn>> collect = uniqueColumns.stream()
                                                                                 .filter(uniqueColumn -> StringUtils.isNotBlank(uniqueColumn.getIndexName()))
                                                                                 .collect(Collectors.groupingBy(TableChangeStruct.UniqueColumn::getIndexName));
        for (Map.Entry<String, List<TableChangeStruct.UniqueColumn>> stringListEntry : collect.entrySet()) {
            String usingIndex = stringListEntry.getValue()
                                               .stream()
                                               .map(TableChangeStruct.UniqueColumn::getUsingIndex)
                                               .filter(StringUtils::isNotBlank)
                                               .findAny()
                                               .orElse(StringUtils.EMPTY);

            List<String> uniqueColumnNames = stringListEntry.getValue()
                                                            .stream()
                                                            .map(uniqueColumn -> wrapQuote(uniqueColumn.getColumnName()))
                                                            .collect(Collectors.toList());
            String uniqueAlterSql = getUniqueAlterSqL(uniqueColumnNames, alterTitleSql, stringListEntry.getKey(), usingIndex);
            uniqueColumnChangeSqlList.add(uniqueAlterSql);
        }
        return uniqueColumnChangeSqlList;
    }

    private String getUniqueAlterSqL(List<String> columnNameList, String alterTitleSql, String constraintName, String usingIndex) {
        if (StringUtils.isNotBlank(usingIndex)) {
            return alterTitleSql + StringUtils.SPACE + OpenGaussConstant.TABLE_PRIMARY_KEY_ADD + StringUtils.SPACE + "CONSTRAINT " +
                    wrapQuote(constraintName) + StringUtils.SPACE + "UNIQUE USING INDEX " + wrapQuote(usingIndex);
        } else {
            return alterTitleSql + StringUtils.SPACE + OpenGaussConstant.TABLE_PRIMARY_KEY_ADD + StringUtils.SPACE + "CONSTRAINT " +
                    wrapQuote(constraintName) + StringUtils.SPACE + "UNIQUE " +
                    addBrackets(StringUtils.join(columnNameList, OpenGaussConstant.COMMA));
        }
    }

    @Override
    public boolean needCacheSql() {
        return true;
    }

    private List<String> foreignKeySql(List<TableChangeStruct.ForeignKeyColumn> foreignKeyColumns, String alterTitleSql) {
        return foreignKeyColumns.stream().map(foreignKeyColumn -> getForeignKeySql(foreignKeyColumn, alterTitleSql)).collect(Collectors.toList());
    }

    private String getForeignKeySql(TableChangeStruct.ForeignKeyColumn foreignKeyColumn, String alterTitleSql) {

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
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ");
        sb.append(wrapQuote(foreignKeyColumn.getFkName())).append(StringUtils.SPACE);
        sb.append("FOREIGN KEY ");
        sb.append(StringUtils.SPACE).append(addBrackets(fkColumnNameStr)).append(StringUtils.SPACE);
        sb.append(StringUtils.LF);
        sb.append(OpenGaussConstant.TAB);
        sb.append("REFERENCES ");
        sb.append(StringUtils.SPACE)
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
}
