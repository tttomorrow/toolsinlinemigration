package com.huawei.jsonstruct;

public class TransactionValueStruct {
    public static class PayloadStruct {
        private String status;
        private String id;
        private int event_count;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getEvent_count() {
            return event_count;
        }

        public void setEvent_count(int event_count) {
            this.event_count = event_count;
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
