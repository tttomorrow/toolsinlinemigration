/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.gauss.parser.DDLSqlParser;

public class DDLProcessor extends Processor {
    private final ObjectMapper topicMapper = new ObjectMapper();
    private final JDBCExecutor executor = new JDBCExecutor();
    private final DDLSqlParser ddlSqlParser = new DDLSqlParser();

    public DDLProcessor() {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void process(ConsumerRecord<String, String> record) {
        DDLValueStruct value = null;
        try {
            value = topicMapper.readValue(record.value(), DDLValueStruct.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SourceStruct source = value.getPayload().getSource();
        String snapshot = source.getSnapshot();
        if (snapshot.equals("true") || snapshot.equals("last")) {
            return;
        }

        String ddl = ddlSqlParser.parse(value);

        // We don't do heavy work on DDL and just pass the origin DDL SQL to destination
        // database. We assume some compatibility plugins in destination database may
        // process these DDL SQL.
        executor.executeDDL(ddl);
    }
}
