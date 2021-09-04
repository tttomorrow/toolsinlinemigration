package com.huawei.jsonstruct;

import java.util.List;
import java.util.Map;

public class FieldStruct {
    private String type;
    private boolean optional;
    private String name;
    private int version;
    private Map<String, String> parameters;
    private String field;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<FieldStruct> getFields() {
        return fields;
    }

    public void setFields(List<FieldStruct> fields) {
        this.fields = fields;
    }
}
