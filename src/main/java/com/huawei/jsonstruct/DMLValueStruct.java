package com.huawei.jsonstruct;

import java.util.Map;

public class DMLValueStruct {
    public static class PayloadStruct {
        private Map<String, Object> before;
        private Map<String, Object> after;
        private SourceStruct source;
        private String op;
        private Long ts_ms;

        public Map<String, Object> getBefore() {
            return before;
        }

        public void setBefore(Map<String, Object> before) {
            this.before = before;
        }

        public Map<String, Object> getAfter() {
            return after;
        }

        public void setAfter(Map<String, Object> after) {
            this.after = after;
        }

        public SourceStruct getSource() {
            return source;
        }

        public void setSource(SourceStruct source) {
            this.source = source;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public Long getTs_ms() {
            return ts_ms;
        }

        public void setTs_ms(Long ts_ms) {
            this.ts_ms = ts_ms;
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
