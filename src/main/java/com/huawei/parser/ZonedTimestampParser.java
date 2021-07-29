package com.huawei.parser;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Map;
import java.sql.Timestamp;

public class ZonedTimestampParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        Timestamp timestamp = null;
        try {
            Date date = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse((String)value);
            timestamp = new Timestamp(date.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}
