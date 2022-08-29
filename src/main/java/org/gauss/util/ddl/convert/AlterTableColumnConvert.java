package org.gauss.util.ddl.convert;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gauss.converter.ColumnConvert;
import org.gauss.converter.ColumnTypeConverter;
import org.gauss.converter.OracleToOpenGaussKeywordsConvert;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description implements alter table  add column/ modify column /drop column
 * @date 2022/6/10
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class AlterTableColumnConvert extends BaseConvert implements DDLConvert {
    public static final String DEFAULT_VALUE_EXPRESSION = "defaultValueExpression";
    public static final String OPTIONAL = "optional";
    private final Logger logger = LoggerFactory.getLogger(AlterTableColumnConvert.class);

    private final Map<String, OracleToOpenGaussKeywordsConvert> keywordMapping = new HashMap<String, OracleToOpenGaussKeywordsConvert>() {{
        put("SYSTIMESTAMP", new OracleToOpenGaussKeywordsConvert("SYSTIMESTAMP", "CURRENT_TIMESTAMP", "SYSTIMESTAMP", "系统时间戳"));
        put("SYS_GUID()", new OracleToOpenGaussKeywordsConvert("SYS_GUID", "", "SYS_GUID\\(\\)", "GUID," + "PG不支持"));
    }};

    /**
     * 根据 ddlValueStruct 内的值,生成OPENGAUSS SQL 语句
     *
     * @param ddlValueStruct debezium captured DDL record
     * @return list of  ddl
     */
    @Override
    public List<String> convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        SourceStruct source = ddlValueStruct.getPayload().getSource();
        List<TableChangeStruct> tableChanges = ddlValueStruct.getPayload().getTableChanges();
        TableChangeStruct tableChangeStruct = tableChanges.stream().findFirst().orElse(null);
        if (null == tableChangeStruct) {
            logger.info("tableChangeStruct is null, but get DDL from topic is: {}", ddlValueStruct.getPayload().getDdl());
            return Collections.singletonList(ddlValueStruct.getPayload().getDdl());
        }
        List<String> ddlList = new ArrayList<>();
        // only null,not null change on column
        if (tableChangeStruct.getTable().getChangeColumns() != null && tableChangeStruct.getTable().getChangeColumns().hasChangeColumn()) {
            /*
             * when on opengauss we can use ddl like
             * alter table tab_1
             *      add col_7 varchar (10) default 'aaa',
             *      add col_8 varchar(32) not null,
             *      modify col_5 varchar(32),
             *      drop col_1;
             * to send one ddl sql to change multiple column
             */
            StringBuilder alterSql = new StringBuilder(getTableAlterTitleSql(source));
            List<String> columnChange = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(tableChangeStruct.getTable().getChangeColumns().getAddColumn())) {
                columnChange.addAll(buildAddColumnSql(source, tableChangeStruct));
            }
            if (CollectionUtils.isNotEmpty(tableChangeStruct.getTable().getChangeColumns().getModifyColumn())) {
                columnChange.addAll(buildModifyColumnSql(source, tableChangeStruct));
            }
            if (CollectionUtils.isNotEmpty(tableChangeStruct.getTable().getChangeColumns().getDropColumn())) {
                columnChange.addAll(buildDropColumnSql(source, tableChangeStruct));
            }
            if (CollectionUtils.isNotEmpty(columnChange)) {
                alterSql.append(String.join(OpenGaussConstant.COMMA, columnChange));
                ddlList.add(alterSql.toString());
            }
        }
        ddlList.addAll(tableChangeStruct.getTable()
                                        .getColumns()
                                        .stream()
                                        .filter(column -> CollectionUtils.isNotEmpty(column.getModifyKeys()))
                                        .map(sourceColumn -> getNullCheckAndDefaultValueSql(sourceColumn, source))
                                        .filter(CollectionUtils::isNotEmpty)
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toList()));
        return ddlList;
    }

    /**
     * build add column sql
     * like  add col_7 varchar(20),add col_8 varchar(30);
     *
     * @param source
     * @param tableChangeStruct
     * @return
     */
    private List<String> buildAddColumnSql(SourceStruct source, TableChangeStruct tableChangeStruct) {

        return tableChangeStruct.getTable().getChangeColumns().getAddColumn().stream().filter(StringUtils::isNotBlank).map(addColumnName -> {
            StringBuilder addColumnSql = new StringBuilder();
            logger.info("get add column, columnName: {}", addColumnName);
            Optional<TableChangeStruct.column> columnOptional = tableChangeStruct.getTable()
                                                                                 .getColumns()
                                                                                 .stream()
                                                                                 .filter(s -> s.getName().equals(addColumnName))
                                                                                 .findFirst();
            if (columnOptional.isPresent()) {
                TableChangeStruct.column sourceColumn = columnOptional.get();
                addColumnSql.append(OpenGaussConstant.TABLE_PRIMARY_KEY_ADD).append(StringUtils.SPACE).append(buildColumnSql(sourceColumn));
                if (StringUtils.isNotBlank(sourceColumn.getDefaultValueExpression())) {
                    addColumnSql.append(StringUtils.SPACE)
                                .append(OpenGaussConstant.DEFAULT)
                                .append(StringUtils.SPACE)
                                .append(rewriteKeyWord(sourceColumn.getDefaultValueExpression()));
                }
                if (!sourceColumn.isOptional()) {
                    addColumnSql.append(StringUtils.SPACE).append(" NOT NULL");
                }
                return addColumnSql.toString();
            }
            return null;
        }).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    private String buildColumnSql(TableChangeStruct.column sourceColumn) {
        StringBuilder columnBuilder = new StringBuilder();
        columnBuilder.append(wrapQuote(sourceColumn.getName())).append(StringUtils.SPACE);
        String targetTypeName = ColumnTypeConverter.convertTypeName(sourceColumn.getTypeName());
        if (null == targetTypeName) {
            logger.error("source column :{} type {} not support in openGauss yet! convert to character varying now ",
                         sourceColumn.getName(),
                         sourceColumn.getTypeName());
        }
        ColumnConvert columnConvert = ColumnConvert.convertToColumnConvert(sourceColumn.getTypeName());
        String columnDdlSql = columnConvert.getConvertFunction().apply(sourceColumn);
        columnBuilder.append(columnDdlSql);
        return columnBuilder.toString();
    }

    /**
     * build modify column sql
     * for now  in OPENGAUSS grammar MODIFY ddl sql not support change  column_type, default, null check in same time
     * so it will be split more than one  ALTER TABLE sql
     * ALTER TABLE [ IF EXISTS ] table_name
     * MODIFY ( { column_name data_type | column_name [ CONSTRAINT constraint_name ] NOT NULL [ ENABLE ] | column_name [ CONSTRAINT constraint_name ] NULL } [, ...] );
     * <p>
     * | ALTER [ COLUMN ] column_name [ SET DATA ] TYPE data_type [ COLLATE collation ] [ USING expression ]
     * | ALTER [ COLUMN ] column_name { SET DEFAULT expression | DROP DEFAULT }
     * | ALTER [ COLUMN ] column_name { SET | DROP } NOT NULL
     *
     * @param source
     * @param tableChangeStruct
     * @return
     */
    private List<String> buildModifyColumnSql(SourceStruct source, TableChangeStruct tableChangeStruct) {
        List<String> sql = new ArrayList<>();
        TableChangeStruct.ChangeColumn changeColumns = tableChangeStruct.getTable().getChangeColumns();
        if (CollectionUtils.isNotEmpty(changeColumns.getModifyColumn())) {
            changeColumns.getModifyColumn().stream().filter(StringUtils::isNotBlank).forEach(modifyColumn -> {
                StringBuilder modifyColumnSql = new StringBuilder();
                logger.info("get modify column, columnName: {}", modifyColumn);
                Optional<TableChangeStruct.column> columnOptional = tableChangeStruct.getTable()
                                                                                     .getColumns()
                                                                                     .stream()
                                                                                     .filter(s -> s.getName().equals(modifyColumn))
                                                                                     .findFirst();
                if (columnOptional.isPresent()) {
                    TableChangeStruct.column sourceColumn = columnOptional.get();
                    modifyColumnSql.append(OpenGaussConstant.MODIFY).append(StringUtils.SPACE).append(buildColumnSql(sourceColumn));
                    sql.add(modifyColumnSql.toString());
                }
            });
        }
        return sql;
    }


    private List<String> getNullCheckAndDefaultValueSql(TableChangeStruct.column column, SourceStruct source) {
        if (CollectionUtils.isNotEmpty(column.getModifyKeys())) {
            return column.getModifyKeys().stream().map(modifyKey -> {
                if (StringUtils.equals(DEFAULT_VALUE_EXPRESSION, modifyKey)) {
                    return getDefaultChangeSql(column, source);
                }
                if (StringUtils.equals(OPTIONAL, modifyKey)) {
                    return getOptionalSql(column, source);
                }
                return null;
            }).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * modify for  NULL check
     *
     * @param column
     * @param
     * @return
     */
    private String getOptionalSql(TableChangeStruct.column column, SourceStruct source) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTableAlterTitleSql(source)).append(StringUtils.SPACE);
        sb.append(OpenGaussConstant.TABLE_ALTER).append(StringUtils.SPACE).append(OpenGaussConstant.COLUMN);
        sb.append(wrapQuote(column.getName())).append(StringUtils.SPACE);
        if (column.isOptional()) {
            sb.append(OpenGaussConstant.TABLE_PRIMARY_KEY_DROP).append(StringUtils.SPACE).append(OpenGaussConstant.NOT_NULL);
        } else {
            sb.append(OpenGaussConstant.SET).append(StringUtils.SPACE).append(OpenGaussConstant.NOT_NULL);
        }
        return sb.toString();
    }

    /**
     * modify for default value
     *
     * @param column
     * @param
     * @return
     */
    private String getDefaultChangeSql(TableChangeStruct.column column, SourceStruct source) {
        return getTableAlterTitleSql(source) + StringUtils.SPACE + OpenGaussConstant.TABLE_ALTER + StringUtils.SPACE + OpenGaussConstant.COLUMN +
                wrapQuote(column.getName()) + StringUtils.SPACE + OpenGaussConstant.SET + StringUtils.SPACE + OpenGaussConstant.DEFAULT +
                StringUtils.SPACE + rewriteKeyWord(column.getDefaultValueExpression());
    }


    /**
     * build drop column sql
     *
     * @param source
     * @param tableChangeStruct
     * @return
     */
    private List<String> buildDropColumnSql(SourceStruct source, TableChangeStruct tableChangeStruct) {
        List<String> sql = new ArrayList<>();
        for (String eachDropColumn : tableChangeStruct.getTable().getChangeColumns().getDropColumn()) {
            if (StringUtils.isNotBlank(eachDropColumn)) {
                String columnName = eachDropColumn;
                String[] split = eachDropColumn.split(";");
                if(split.length > 1) {
                    columnName = split[0];
                }
                logger.info("get drop column, columnName: {}", eachDropColumn);
                String dropColumnSql = OpenGaussConstant.TABLE_PRIMARY_KEY_DROP + StringUtils.SPACE + OpenGaussConstant.COLUMN + StringUtils.SPACE +
                        wrapQuote(columnName) + (split.length > 1 ? StringUtils.SPACE + split[1] : StringUtils.SPACE);
                sql.add(dropColumnSql);
            }
        }
        return sql;
    }

    private String rewriteKeyWord(final String columnDefault) {
        List<Map.Entry<String, OracleToOpenGaussKeywordsConvert>> matchingKey = keywordMapping.entrySet()
                                                                                              .stream()
                                                                                              .filter(entry -> columnDefault.contains(entry.getKey()))
                                                                                              .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(matchingKey)) {
            String resultText = columnDefault;
            for (Map.Entry<String, OracleToOpenGaussKeywordsConvert> entry : matchingKey) {
                resultText = entry.getValue().replaceKeyWord(resultText);
            }
            return resultText;
        }
        return columnDefault;
    }

    @Override
    public boolean needCacheSql() {
        return true;
    }
}
