package com.huawei.util;

import com.huawei.jsonstruct.TransactionValueStruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionProcessor extends Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProcessor.class);

    // topicMapper is used to parse topic json
    private final ObjectMapper topicMapper = new ObjectMapper();

    // DMLConsumer poll the DML topic records
    private final KafkaConsumer<String, String> DMLConsumer;

    // DMLWorker controls the DMLConsumer and replay transaction.
    private final DMLController DMLWorker = new DMLController();

    // Used to record cost time
    private long txnStart = 0;
    private long txnEnd = 0;

    public TransactionProcessor(KafkaConsumer<String, String> DMLConsumer) {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.DMLConsumer = DMLConsumer;
    }

    @Override
    public void process(ConsumerRecord<String, String> record) {
        long timestamp = System.currentTimeMillis();

        TransactionValueStruct txnValue = null;
        try {
            txnValue = topicMapper.readValue(record.value(), TransactionValueStruct.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TransactionValueStruct.PayloadStruct payload = txnValue.getPayload();
        String status = payload.getStatus();
        String txId = payload.getId();
        int eventCount = payload.getEvent_count();

        if (status.equals("BEGIN")) {
            LOGGER.info("Start transaction.");

            txnStart = timestamp;

            DMLWorker.setTransactionId(txId);
            DMLWorker.work(DMLConsumer, -1);
        } else if (status.equals("END")){
            DMLWorker.commit(DMLConsumer, eventCount);

            txnEnd = System.currentTimeMillis();

            LOGGER.info("Commit transaction.");
            LOGGER.info("Transaction cost time: {} ms.", txnEnd - txnStart);
        }
    }
}
