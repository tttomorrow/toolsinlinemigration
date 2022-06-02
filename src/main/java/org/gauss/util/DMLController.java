/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DMLValueStruct;
import org.gauss.jsonstruct.KeyStruct;
import org.gauss.jsonstruct.SourceStruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class DMLController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLProcessor.class);

    private static final long POLL_DURATION = 100;

    // topicMapper is use to parse topic json
    private final ObjectMapper topicMapper = new ObjectMapper();

    // executor connect destination database and execute SQL
    private final JDBCExecutor executor = new JDBCExecutor();

    // Stores the table processors. Each table owns a processor.
    private final Map<String, DMLProcessor> tableProcessors = new HashMap<>();

    // We use a queue to cache DML topics in next transaction.
    private final Queue<ConsumerRecord> remainingRecords = new LinkedList<>();

    // The transaction ID.
    private String txnId = null;

    // Record counts in a transaction.
    private int recordsInTxn = 0;

    // Should do commit?
    private boolean needCommit = false;

    public DMLController() {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            executor.getConnection().setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Poll DML topic record and replay it.
     *
     * @param DMLConsumer consumer that polls DML topic records
     * @param eventCount eventCount = -1 when txn starts, but is set to be not -1 when txn ends.
     */
    public void work(KafkaConsumer DMLConsumer, int eventCount) {
        // Process the records that are cached.
        while (!remainingRecords.isEmpty()) {
            ConsumerRecord<String, String> record = remainingRecords.peek();
            process(record, false);
            if (needCommit) {
                // This is the new record in next transaction, we can't remove this record and just exit.
                return;
            } else {
                remainingRecords.remove();
            }
        }

        while (true) {
            ConsumerRecords<String, String> records = DMLConsumer.poll(Duration.ofMillis(POLL_DURATION));

            // isEmpty() means there are not new records or production rate is slow.
            if (records.isEmpty()) {
                if (eventCount == -1 || eventCount == recordsInTxn) {
                    // eventCount == -1 means transaction start, but this time it poll nothing.
                    // We can exit and next time we poll left records before transaction ends.
                    // eventCount == recordsInTxn means all records in transaction are polled.
                    // We can exit too.
                    return;
                } else if (eventCount > recordsInTxn) {
                    // There are left records to be polled before transaction end.
                    continue;
                }
            }

            for (ConsumerRecord<String, String> record : records) {
                if (needCommit) {
                    // Cache records. process them in next transaction.
                    remainingRecords.add(record);
                } else {
                    process(record, true);
                }
            }

            DMLConsumer.commitSync();

            if (needCommit) {
                return;
            }
        }
    }

    public void process(ConsumerRecord<String, String> record, boolean needCacheInQueue) {
        if (record.value() == null) {
            LOGGER.info("This is a topic after a DELETE topic, should ignore it.");
            return;
        }

        KeyStruct key = null;
        DMLValueStruct value = null;
        try {
            if (record.key() != null) {
                // The table does not have key column;
                key = topicMapper.readValue(record.key(), KeyStruct.class);
            }
            value = topicMapper.readValue(record.value(), DMLValueStruct.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error occurs when parsing record.");
        }

        SourceStruct source = value.getPayload().getSource();
        String recordTxid = source.getTxId();
        if (StringUtils.isBlank(recordTxid)) {
            LOGGER.info("This is a record from snapshot,maybe ignore it now");
            return;
        }
        if (!recordTxid.equals(txnId)) {
            if (needCacheInQueue) {
                remainingRecords.add(record);
            }
            needCommit = true;
        } else {
            String table = source.getTable();
            if (!tableProcessors.containsKey(table)) {
                tableProcessors.put(table, new DMLProcessor(table, executor));
            }
            DMLProcessor processor = tableProcessors.get(table);
            processor.process(key, value);
            needCommit = false;
            recordsInTxn++;
        }
    }

    public void commit(KafkaConsumer DMLConsumer, int eventInTxnCount) {
        if (eventInTxnCount > recordsInTxn) {
            LOGGER.info("Fetch left {} records before commit.", eventInTxnCount - recordsInTxn);
            work(DMLConsumer, eventInTxnCount);
        }

        try {
            executor.getConnection().commit();
            needCommit = false;
            recordsInTxn = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTransactionId(String txnId) {
        this.txnId = txnId;
    }
}
