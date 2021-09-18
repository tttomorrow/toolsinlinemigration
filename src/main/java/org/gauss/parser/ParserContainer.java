/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import java.util.HashMap;
import java.util.Map;

public class ParserContainer {
    public static Map<String, Parser> parsers = new HashMap<>();

    // References: https://debezium.io/documentation/reference/1.5/connectors/oracle.html
    static {
        // Numeric types
        parsers.put(io.debezium.data.VariableScaleDecimal.LOGICAL_NAME, new VariableScaleDecimalParser());
        parsers.put(org.apache.kafka.connect.data.Decimal.LOGICAL_NAME, new DecimalParser());

        // Temporal types
        parsers.put(io.debezium.time.Timestamp.SCHEMA_NAME, new TimestampParser());
        parsers.put(io.debezium.time.MicroDuration.SCHEMA_NAME, new MicroDurationParser());
        parsers.put(io.debezium.time.MicroTimestamp.SCHEMA_NAME, new MicroTimestampParser());
        parsers.put(io.debezium.time.NanoTimestamp.SCHEMA_NAME, new NanoTimestampParser());
        parsers.put(io.debezium.time.ZonedTimestamp.SCHEMA_NAME, new ZonedTimestampParser());
        parsers.put(org.apache.kafka.connect.data.Date.LOGICAL_NAME, new KafkaDateParser());
        parsers.put(org.apache.kafka.connect.data.Timestamp.LOGICAL_NAME, new KafkaTimestampParser());
    }
}
