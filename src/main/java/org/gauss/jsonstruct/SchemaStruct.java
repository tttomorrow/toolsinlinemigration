/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

import java.util.List;

public class SchemaStruct {
    private String type;
    private boolean optional;
    private String name;
    private List<FieldStruct> fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FieldStruct> getFields() {
        return fields;
    }

    public void setFields(List<FieldStruct> fields) {
        this.fields = fields;
    }
}
