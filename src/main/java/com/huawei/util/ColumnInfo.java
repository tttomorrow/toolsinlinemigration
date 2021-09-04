package com.huawei.util;

import java.util.Map;

public class ColumnInfo {
    private String name;
    private String literalType;
    private String semanticType;
    private Map<String, String> parameters;

    public ColumnInfo(String name, String literalType, String semanticType, Map<String, String> parameters) {
        this.name = name;
        this.literalType = literalType;
        this.semanticType = semanticType;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLiteralType() {
        return literalType;
    }

    public void setLiteralType(String literalType) {
        this.literalType = literalType;
    }

    public String getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
