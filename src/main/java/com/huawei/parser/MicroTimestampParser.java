package com.huawei.parser;

import java.sql.Timestamp;
import java.util.Map;

public class MicroTimestampParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        Timestamp timestamp = new Timestamp((long)value);
        return timestamp;
    }
}
