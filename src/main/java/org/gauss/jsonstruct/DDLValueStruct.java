/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

import java.util.List;

public class DDLValueStruct {
    public static class PayloadStruct {
        private SourceStruct source;
        private String databaseName;
        private String schemaName;
        private String ddl;
        List<TableChangeStruct> tableChanges;

        public SourceStruct getSource() {
            return source;
        }

        public void setSource(SourceStruct source) {
            this.source = source;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getDdl() {
            return ddl;
        }

        public void setDdl(String ddl) {
            this.ddl = ddl;
        }

        public List<TableChangeStruct> getTableChanges() {
            return tableChanges;
        }

        public void setTableChanges(List<TableChangeStruct> tableChanges) {
            this.tableChanges = tableChanges;
        }
    }

    private SchemaStruct schema;
    private PayloadStruct payload;

    public SchemaStruct getSchema() {
        return schema;
    }

    public void setSchema(SchemaStruct schema) {
        this.schema = schema;
    }

    public PayloadStruct getPayload() {
        return payload;
    }

    public void setPayload(PayloadStruct payload) {
        this.payload = payload;
    }
}
