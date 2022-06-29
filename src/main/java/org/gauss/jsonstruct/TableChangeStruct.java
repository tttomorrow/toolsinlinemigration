/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

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

    public static class Table {
        private List<String> primaryKeyColumnNames;
        private List<PrimaryKeyColumnChange> primaryKeyColumnChanges = new ArrayList<>();
        private List<IndexColumn> uniqueColumns = new ArrayList<>();
        private List<CheckColumn> checkColumns= new ArrayList<>();
        private List<ForeignKeyColumn> foreignKeyColumns = new ArrayList<>();
        private List<column> columns;

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
        private String  constraintName;
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
