/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import org.postgresql.util.PGInterval;
import static io.debezium.time.MicroDuration.DAYS_PER_MONTH_AVG;

import java.util.Map;

public class MicroDurationParser implements Parser {
    public static final double SECONDS_PER_MONTH_AVG = 24 * 60 * 60 * DAYS_PER_MONTH_AVG;

    @Override
    public Object parse(Map<String, String> params, Object value) {
        // microseconds -> seconds
        double numberOfSeconds = ((Long)value).doubleValue() / 1000000;

        // DAYS_PER_MONTH_AVG is double data, so the decimal part of numberOfSeconds if derived
        // from numOfMonths. We can subtract this part and and convert remain data type to int type
        // safely. For more details, see th implementation of io.debezium.time.MicroDuration.
        int numOfMonths = (int)(numberOfSeconds / SECONDS_PER_MONTH_AVG);
        int years = numOfMonths / 12;
        int months = numOfMonths % 12;
        int remainSeconds = (int)(numberOfSeconds - numOfMonths * SECONDS_PER_MONTH_AVG);
        int seconds = remainSeconds % 60;
        int numOfMinutes = (remainSeconds - seconds) / 60;
        int minutes = (numOfMinutes % 60);
        int numOfHours = (numOfMinutes - minutes) / 60;
        int hours = numOfHours % 24;
        int days = (numOfHours - hours) / 24;

        PGInterval interval = null;
        try {
            interval = new PGInterval(years, months, days, hours, minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return interval;
    }
}
