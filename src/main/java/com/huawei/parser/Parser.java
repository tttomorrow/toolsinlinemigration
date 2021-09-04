package com.huawei.parser;

import java.util.Map;

public interface Parser {
    Object parse(Map<String, String> params, Object value);
}
