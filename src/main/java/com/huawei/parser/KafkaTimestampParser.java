package com.huawei.parser;

import java.sql.Timestamp;
import java.util.Map;

public class KafkaTimestampParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        // value is millisecond.
        Timestamp timestamp = new Timestamp((long)value);
        return timestamp;
    }
}
