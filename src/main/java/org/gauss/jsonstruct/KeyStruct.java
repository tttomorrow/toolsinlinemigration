/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

import java.util.Map;

public class KeyStruct {
    private SchemaStruct schema;
    private Map<String, Object> payload;

    public SchemaStruct getSchema() {
        return schema;
    }

    public void setSchema(SchemaStruct schema) {
        this.schema = schema;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
