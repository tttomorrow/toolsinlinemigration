/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

import org.gauss.util.ddl.DDLProcessor;
import org.gauss.util.Processor;
import org.gauss.util.TransactionProcessor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRunner.class);

    // Consumer is used to pool topic message
    private final KafkaConsumer<String, String> consumer;

    // Processor is used to process each message.
    private final Processor processor;

    /**
     * Consumer thread.
     * @param props Properties used to configure consumer.
     * @param topic The topic want to subscribe.
     * @param fromBeginning Consume the topic from beginning.
     * @param DMLConsumer If not null, it is used for transaction consumer. It is null for DDL consumer.
     */
    public ConsumerRunner(Properties props, String topic, boolean fromBeginning,
                          KafkaConsumer<String, String> DMLConsumer) {
        String groupId = props.getProperty(GROUP_ID_CONFIG);
        if (DMLConsumer != null) {
            // Create the transaction group for transaction consumer.
            props.setProperty(GROUP_ID_CONFIG, groupId + "_Transaction");
        } else {
            // Create the DDL group for DDL consumer.
            props.setProperty(GROUP_ID_CONFIG, groupId + "_DDL");
        }

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        // Set the group id to origin one
        props.setProperty(GROUP_ID_CONFIG, groupId);

        if (fromBeginning) {
            consumer.poll(0);
            consumer.seekToBeginning(consumer.assignment());
        }

        if (DMLConsumer != null) {
            processor = new TransactionProcessor(DMLConsumer);
        } else {
            processor = new DDLProcessor();
        }
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            int count = 0;
            for (ConsumerRecord<String, String> record : records) {
                count++;
                processor.process(record);
            }
            if (count > 0) {
                consumer.commitSync();
            }
        }
    }
}
