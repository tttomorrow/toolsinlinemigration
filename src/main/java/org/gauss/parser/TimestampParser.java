/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import java.util.Map;
import java.sql.Timestamp;

public class TimestampParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        Timestamp timestamp = new Timestamp((long)value);
        return timestamp;
    }
}
