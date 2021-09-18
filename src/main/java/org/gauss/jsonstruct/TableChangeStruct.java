/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

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
    }

    public static class Table {
        private List<String> primaryKeyColumnNames;
        private List<column> columns;

        public List<String> getPrimaryKeyColumnNames() {
            return primaryKeyColumnNames;
        }

        public void setPrimaryKeyColumnNames(List<String> primaryKeyColumnNames) {
            this.primaryKeyColumnNames = primaryKeyColumnNames;
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
