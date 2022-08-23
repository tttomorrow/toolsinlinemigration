/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class TableChangeStruct {
    public static class column {
        private String name;
        private int jdbcType;
        private int nativeType;
        private String typeName;
        private String typeExpression;
        private String charsetName;
        private int length;
        private int scale;
        private int position;
        private boolean optional;
        private boolean autoIncremented;
        private boolean generated;
        private List<String> modifyKeys;
        private String defaultValueExpression;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getJdbcType() {
            return jdbcType;
        }

        public void setJdbcType(int jdbcType) {
            this.jdbcType = jdbcType;
        }

        public int getNativeType() {
            return nativeType;
        }

        public void setNativeType(int nativeType) {
            this.nativeType = nativeType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeExpression() {
            return typeExpression;
        }

        public void setTypeExpression(String typeExpression) {
            this.typeExpression = typeExpression;
        }

        public String getCharsetName() {
            return charsetName;
        }

        public void setCharsetName(String charsetName) {
            this.charsetName = charsetName;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public boolean isAutoIncremented() {
            return autoIncremented;
        }

        public void setAutoIncremented(boolean autoIncremented) {
            this.autoIncremented = autoIncremented;
        }

        public boolean isGenerated() {
            return generated;
        }

        public void setGenerated(boolean generated) {
            this.generated = generated;
        }

        public List<String> getModifyKeys() {
            return modifyKeys;
        }

        public void setModifyKeys(List<String> modifyKeys) {
            this.modifyKeys = modifyKeys;
        }

        public String getDefaultValueExpression() {
            return defaultValueExpression;
        }

        public void setDefaultValueExpression(String defaultValueExpression) {
            this.defaultValueExpression = defaultValueExpression;
        }
    }

    public static class ChangeColumn {
        private List<String> addColumn = new ArrayList<>();
        private List<String> modifyColumn = new ArrayList<>();
        private List<String> dropColumn = new ArrayList<>();

        public boolean hasChangeColumn() {
            return CollectionUtils.isNotEmpty(addColumn) || CollectionUtils.isNotEmpty(modifyColumn) || CollectionUtils.isNotEmpty(dropColumn);
        }

        public List<String> getAddColumn() {
            return addColumn;
        }

        public void setAddColumn(List<String> addColumn) {
            this.addColumn = addColumn;
        }

        public List<String> getModifyColumn() {
            return modifyColumn;
        }

        public void setModifyColumn(List<String> modifyColumn) {
            this.modifyColumn = modifyColumn;
        }

        public List<String> getDropColumn() {
            return dropColumn;
        }

        public void setDropColumn(List<String> dropColumn) {
            this.dropColumn = dropColumn;
        }
    }

    public static class Table {
        private List<String> primaryKeyColumnNames;
        private List<String> primaryConstraintName = new ArrayList<>();
        private List<PrimaryKeyColumnChange> primaryKeyColumnChanges = new ArrayList<>();
        private List<IndexColumn> uniqueColumns = new ArrayList<>();
        private List<CheckColumn> checkColumns = new ArrayList<>();
        private List<ForeignKeyColumn> foreignKeyColumns = new ArrayList<>();
        private List<column> columns;
        /**
         * if alter table changing column, the column name will in this field
         */
        private ChangeColumn changeColumns = new ChangeColumn();
        private IndexChanges indexChanges;
        public IndexChanges getIndexChanges() {
            return indexChanges;
        }
        public void setIndexChanges(IndexChanges indexChanges) {
            this.indexChanges = indexChanges;
        }
        public ChangeColumn getChangeColumns() {
            return changeColumns;
        }
        public void setChangeColumns(ChangeColumn changeColumns) {
            this.changeColumns = changeColumns;
        }
        public List<String> getPrimaryKeyColumnNames() {
            return primaryKeyColumnNames;
        }
        public List<PrimaryKeyColumnChange> getPrimaryKeyColumnChanges() {
            return primaryKeyColumnChanges;
        }
        public void setPrimaryKeyColumnNames(List<String> primaryKeyColumnNames) {
            this.primaryKeyColumnNames = primaryKeyColumnNames;
        }
        public List<IndexColumn> getUniqueColumns() {
            return uniqueColumns;
        }
        public List<CheckColumn> getCheckColumns() {
            return checkColumns;
        }
        public List<ForeignKeyColumn> getForeignKeyColumns() {
            return foreignKeyColumns;
        }
        public List<column> getColumns() {
            return columns;
        }
    }
    public static class ForeignKeyColumn {
        private String pktableSchem;
        private String pktableName;
        private String pkColumnName;
        private String fkColumnName;
        private String fkName;
        private String cascade;

        public String getPktableSchem() {
            return pktableSchem;
        }

        public String getPktableName() {
            return pktableName;
        }

        public String getPkColumnName() {
            return pkColumnName;
        }

        public String getFkColumnName() {
            return fkColumnName;
        }

        public String getFkName() {
            return fkName;
        }

        public String getCascade() {
            return cascade;
        }
    }
    public static class IndexColumn {
        private String indexName;
        private String columnName;
        public String getIndexName() {
            return indexName;
        }
        public String getColumnName() {
            return columnName;
        }
    }

    public static class PrimaryKeyColumnChange {
        private String action;
        private String columnName;
        private String cascade;
        private String constraintName;
        private String type;
        public String getAction() {
            return action;
        }
        public String getColumnName() {
            return columnName;
        }
        public String getCascade() {
            return cascade;
        }
        public String getConstraintName() {
            return constraintName;
        }
        public String getType() {
            return type;
        }
    }

    public static class CheckColumn {
        private String indexName;
        private String condition;
        public String getIndexName() {
            return indexName;
        }
        public String getCondition() {
            return condition;
        }
    }

    public static class IndexChanges {
        /**
         * indexId
         * example: schema_name.index_name
         */
        private String indexId;
        private String indexName;
        /**
         * tableId
         * example: SCHEMA_NAME.TABLE_NAME
         */
        private String tableId;
        private String schemaName;
        private String tableName;
        public String getTableName() {
            return tableName;
        }
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        private boolean unique;
        public boolean isUnique() {
            return unique;
        }
        public void setUnique(boolean unique) {
            this.unique = unique;
        }
        private List<IndexColumnExpr> indexColumnExpr;
        public String getIndexId() {
            return indexId;
        }
        public void setIndexId(String indexId) {
            this.indexId = indexId;
        }
        public String getIndexName() {
            return indexName;
        }
        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }
        public String getTableId() {
            return tableId;
        }
        public void setTableId(String tableId) {
            this.tableId = tableId;
        }
        public String getSchemaName() {
            return schemaName;
        }
        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }
        public List<IndexColumnExpr> getIndexColumnExpr() {
            return indexColumnExpr;
        }
        public void setIndexColumnExpr(List<IndexColumnExpr> indexColumnExpr) {
            this.indexColumnExpr = indexColumnExpr;
        }
        public static class IndexColumnExpr {
            private String columnExpr;
            private boolean desc;
            private List<String> includeColumn;
            public String getColumnExpr() {
                return columnExpr;
            }
            public void setColumnExpr(String columnExpr) {
                this.columnExpr = columnExpr;
            }
            public boolean isDesc() {
                return desc;
            }
            public void setDesc(boolean desc) {
                this.desc = desc;
            }
            public List<String> getIncludeColumn() {
                return includeColumn;
            }
            public void setIncludeColumn(List<String> includeColumn) {
                this.includeColumn = includeColumn;
            }
        }
    }
    private String type;
    private String id;
    private Table table;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Table getTable() {
        return table;
    }
    public void setTable(Table table) {
        this.table = table;
    }
}
