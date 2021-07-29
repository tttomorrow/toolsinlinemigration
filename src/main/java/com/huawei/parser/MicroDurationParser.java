package com.huawei.parser;

import org.postgresql.util.PGInterval;

import java.util.Map;

public class MicroDurationParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        // microseconds -> seconds
        long seconds = (long)value / 1000000;
        PGInterval interval = null;
        try {
            interval = new PGInterval(String.format("%d secondes", seconds));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return interval;
    }
}
