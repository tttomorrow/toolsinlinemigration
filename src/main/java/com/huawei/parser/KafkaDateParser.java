package com.huawei.parser;

import java.sql.Date;
import java.util.Map;

public class KafkaDateParser implements Parser {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    @Override
    public Object parse(Map<String, String> params, Object value) {
        Date date = new Date((int)value * MILLIS_PER_DAY);
        return date;
    }
}
