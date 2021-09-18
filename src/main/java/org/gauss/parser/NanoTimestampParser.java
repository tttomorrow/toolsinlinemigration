/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import java.sql.Timestamp;
import java.util.Map;

public class NanoTimestampParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        // nanoseconds -> milliseconds
        Timestamp timestamp = new Timestamp((long)value / 1000000);
        return timestamp;
    }
}
