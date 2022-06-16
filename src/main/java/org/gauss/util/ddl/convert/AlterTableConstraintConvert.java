package org.gauss.util.ddl.convert;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;

import java.util.ArrayList;
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
    @Override
    public String convertToOpenGaussDDL(DDLValueStruct struct) {
        List<TableChangeStruct> tableChanges = struct.getPayload().getTableChanges();
        List<String> openGaussSqlList = tableChanges.stream()
                                                    .map(tableChangeStruct -> convertAlterToOpenGaussSql(tableChangeStruct,
                                                                                                         struct.getPayload().getSource()))
                                                    .filter(StringUtils::isNotEmpty)
                                                    .collect(Collectors.toList());
        return StringUtils.join(openGaussSqlList, OpenGaussConstant.SEMICOLON);
    }

    private String convertAlterToOpenGaussSql(TableChangeStruct tableChangeStruct, SourceStruct source) {
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (CollectionUtils.isNotEmpty(table.getPrimaryKeyColumnChanges())) {
            return  StringUtils.join(primaryKeyColumnChangeSql(table.getPrimaryKeyColumnChanges(), source.getTable(),
                                                               getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON);
        }
        if (CollectionUtils.isNotEmpty(table.getUniqueColumns())) {
            return  StringUtils.join(uniqueColumnChangeSql(table.getUniqueColumns(),
                                                           getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON);
        }
        if (CollectionUtils.isNotEmpty(table.getCheckColumns())) {
            return  StringUtils.join(checkChangeSql(table.getCheckColumns(),
                                                    getTableAlterTitleSql(source)), OpenGaussConstant.SEMICOLON);
        }

        List<TableChangeStruct.column> columnChanges = getColumnChanges(table.getColumns());
        if (CollectionUtils.isNotEmpty(getColumnChanges(table.getColumns()))) {
            List<String> columnChangeSqls = columnChanges.stream()
                                                         .flatMap(columnChange -> getColumnChangeSql(columnChange, getTableAlterTitleSql(source)).stream())
                                                         .collect(Collectors.toList());
            return StringUtils.join(columnChangeSqls, OpenGaussConstant.SEMICOLON);
        }
        return StringUtils.EMPTY;
    }


    private List<String> checkChangeSql(List<TableChangeStruct.CheckColumn> checkColumns, String alterTitleSql){
        return checkColumns.stream().map(checkColumn -> getCheckAlterSqL(checkColumn, alterTitleSql)).collect(Collectors.toList());
    }

    private String getCheckAlterSqL(TableChangeStruct.CheckColumn checkColumn, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ").append(checkColumn.getIndexName()).append(StringUtils.SPACE);
        sb.append("CHECK ");
        sb.append(addBrackets(checkColumn.getCondition()));
        return sb.toString();
    }

    private List<TableChangeStruct.column> getColumnChanges(List<TableChangeStruct.column> columns){
        return columns.stream().filter(column -> CollectionUtils.isNotEmpty(column.getModifyKeys())).collect(Collectors.toList());
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
                     .filter(StringUtils::isNotEmpty)
                     .collect(Collectors.toList());
    }

    private String getOptionalSql(TableChangeStruct.column column, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append("ALTER COLUMN ");
        sb.append(wrapQuote(column.getName())).append(StringUtils.SPACE);
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
        sb.append(wrapQuote(column.getName())).append(StringUtils.SPACE);
        sb.append("SET DEFAULT ");
        sb.append(column.getDefaultValueExpression());

        return sb.toString();
    }

    private String getTableAlterTitleSql(SourceStruct source) {
        StringBuilder sb = new StringBuilder();
        sb.append(OpenGaussConstant.TABLE_ALTER)
          .append(StringUtils.SPACE)
          .append("TABLE")
          .append(StringUtils.SPACE)
          .append(wrapQuote(source.getSchema()))
          .append(OpenGaussConstant.DOT)
          .append(wrapQuote(source.getTable()));
        return sb.toString();
    }

    private String getPrimaryKeyDropSqL(String tableName, TableChangeStruct.PrimaryKeyColumnChange primaryKeyColumnChangeColumn, String alterTitleSql) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_DROP).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ");
        if (StringUtils.isNotEmpty(primaryKeyColumnChangeColumn.getConstraintName())){
            sb.append(primaryKeyColumnChangeColumn.getConstraintName());
        } else {
            sb.append(wrapQuote(tableName + "_pkey"));
        }
        if (StringUtils.isNotEmpty(primaryKeyColumnChangeColumn.getCascade())) {
            sb.append(StringUtils.SPACE).append(primaryKeyColumnChangeColumn.getCascade());
        }
        return sb.toString();
    }

    private String getPrimaryKeyAddSqL(List<String>  columnNameList, String alterTitleSql, String constraintName) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        if (StringUtils.isNotEmpty(constraintName)) {
            sb.append("CONSTRAINT ").append(constraintName).append(StringUtils.SPACE);
        }
        sb.append("PRIMARY KEY ");
        sb.append(addBrackets(StringUtils.join(columnNameList, OpenGaussConstant.COMMA)));
        return sb.toString();
    }

    private List<String> primaryKeyColumnChangeSql(List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChangeColumns, String tableName, String alterTitleSql){

        Map<String, List<TableChangeStruct.PrimaryKeyColumnChange>> primaryKeyGroup = primaryKeyColumnChangeColumns.stream()
                                                                                                                   .collect(Collectors.groupingBy(TableChangeStruct.PrimaryKeyColumnChange::getAction));

        List<String> primaryKeySqlList = new ArrayList<>();

        for (Map.Entry<String, List<TableChangeStruct.PrimaryKeyColumnChange>> entry : primaryKeyGroup.entrySet()) {
            String action = entry.getKey();
            List<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChanges = entry.getValue();
            List<String> primaryKeyAddColumnNames = primaryKeyColumnChanges.stream()
                                                                           .map(primaryKeyColumnChangeColumn -> wrapQuote(primaryKeyColumnChangeColumn.getColumnName()))
                                                                           .collect(Collectors.toList());
            Optional<TableChangeStruct.PrimaryKeyColumnChange> primaryKeyColumnChange = primaryKeyColumnChanges.stream().filter(
                    primaryKeyColumnChangeColumn -> StringUtils
                            .isNotEmpty(primaryKeyColumnChangeColumn.getConstraintName())).findAny();

            if (StringUtils.equals(action.toUpperCase(), OpenGaussConstant.TABLE_PRIMARY_KEY_ADD)) {
                if (primaryKeyColumnChange.isPresent()) {
                    primaryKeySqlList.add(getPrimaryKeyAddSqL(primaryKeyAddColumnNames, alterTitleSql,
                                                              primaryKeyColumnChange.get().getConstraintName()));
                } else {
                    primaryKeySqlList.add(getPrimaryKeyAddSqL(primaryKeyAddColumnNames, alterTitleSql, null));
                }

            } else {
                primaryKeySqlList.addAll(primaryKeyColumnChanges.stream().map(
                        primaryKeyColumnChangeColumn -> getPrimaryKeyDropSqL(tableName, primaryKeyColumnChangeColumn,
                                                                             alterTitleSql)).collect(Collectors.toSet()));
                if (primaryKeyColumnChange.isPresent() && StringUtils.isNotEmpty(primaryKeyColumnChange.get().getConstraintName())) {
                    if (StringUtils.equals(primaryKeyColumnChange.get().getType(), NumberUtils.INTEGER_ONE.toString())){
                        primaryKeySqlList.addAll(primaryKeyAddColumnNames.stream().map(columnName -> getColumnNullSql(columnName, alterTitleSql)).collect(
                                Collectors.toSet()));
                    }
                } else {
                    primaryKeySqlList.addAll(primaryKeyAddColumnNames.stream().map(columnName -> getColumnNullSql(columnName, alterTitleSql)).collect(
                            Collectors.toSet()));
                }

            }
        }
        return primaryKeySqlList;
    }

    private String getColumnNullSql(String columnName, String alterTitleSql){
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append("ALTER COLUMN ");
        sb.append(columnName).append(StringUtils.SPACE);
        sb.append("DROP NOT NULL");
        return sb.toString();
    }

    private List<String> uniqueColumnChangeSql(List<TableChangeStruct.IndexColumn> uniqueColumns, String alterTitleSql){
        List<String> uniqueColumnNames = uniqueColumns.stream()
                                                      .map(uniqueColumn -> wrapQuote(uniqueColumn.getColumnName()))
                                                      .collect(Collectors.toList());
        Optional<TableChangeStruct.IndexColumn> uniqueColumnIndex =
                uniqueColumns.stream().filter(uniqueColumn -> StringUtils.isNotEmpty(uniqueColumn.getIndexName()))
                             .findAny();
        return uniqueColumnIndex.map(indexColumn -> Lists.newArrayList(getUniqueAlterSqL(uniqueColumnNames,
                                                                                         alterTitleSql,
                                                                                         indexColumn.getIndexName()))).orElseGet(Lists::newArrayList);
    }

    private String getUniqueAlterSqL(List<String>  columnNameList, String alterTitleSql, String constraintName) {
        StringBuilder sb = new StringBuilder();
        sb.append(alterTitleSql).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE);
        sb.append("CONSTRAINT ").append(constraintName).append(StringUtils.SPACE);
        sb.append("UNIQUE ");
        sb.append(addBrackets(StringUtils.join(columnNameList, OpenGaussConstant.COMMA)));
        return sb.toString();
    }
}
