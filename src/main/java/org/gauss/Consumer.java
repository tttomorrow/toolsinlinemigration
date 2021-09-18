/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss;

import org.gauss.util.SCNProcessor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Main class of the online migration consumer.
 */
public class Consumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private static final long POLL_DURATION = 100;

    public static void main(String[] args) throws IOException {
        initParameters(args);
        // Only get scn from server topic.
        if (MigrationConfig.isWriteSCN()) {
            writeSCN();
            return;
        }

        final String databaseServerName = MigrationConfig.getDatabaseServerName();
        final String schema = MigrationConfig.getSchema();
        LOGGER.info("DML consumer start, server is {}, schema is {}", databaseServerName, schema);

        final Properties consumerProps = MigrationConfig.getConsumerProps();
        final String serverName = MigrationConfig.getDatabaseServerName();
        final boolean fromBeginning = MigrationConfig.isFromBeginning();
        ConsumerGroup consumerGroup = new ConsumerGroup(consumerProps, serverName, schema, fromBeginning);
        consumerGroup.createDDLConsumer();
        while (true) {
            // Listen the transaction and DML topics periodicity.
            // When they are published, create responding consumer.
            try {
                Thread.sleep(1000);

                // Create DMLConsumer if there is new topic.
                if (!consumerGroup.hasCreateDMLConsumer()) {
                    consumerGroup.createDMLConsumer();
                }
                if (!consumerGroup.hasCreateTxnConsumer()) {
                    consumerGroup.createTransactionConsumer();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write SCN captured in Debezium snapshot mode to SCNFile. SCN is stored in server topic.
     */
    public static void writeSCN() {
        final Properties consumerProps = MigrationConfig.getConsumerProps();
        final String serverName = MigrationConfig.getDatabaseServerName();

        KafkaConsumer<String, String> serverConsumer = new KafkaConsumer<>(consumerProps);
        serverConsumer.subscribe(Collections.singletonList(serverName));

        // poll(0) work but poll(Duration.ofMillis(0)) can't fetch beginning topic. Should check it in the future.
        serverConsumer.poll(0);
        serverConsumer.seekToBeginning(serverConsumer.assignment());
        ConsumerRecords<String, String> serverRecords = serverConsumer.poll(Duration.ofMillis(POLL_DURATION));
        for (ConsumerRecord<String, String> serverRecord : serverRecords) {
            // We only process the first record and write scn to file, then exit;
            String scn = SCNProcessor.getSCN(serverRecord.value());
            final String scnFilePath = MigrationConfig.getScnFilePath();
            SCNProcessor.write(scnFilePath, scn);
            serverConsumer.close();
            LOGGER.info("Write scn {} to {}.", scn, scnFilePath);
            return;
        }
        serverConsumer.close();
        LOGGER.info("read nothing from database!");
    }

    public static void initParameters(String[] args) throws IOException {
        if (args.length == 1) {
            if (args[0].equals("--help")) {
                printHelp();
                System.exit(0);
            }
        }
        boolean fromBeginning = false;
        boolean writeSCN = false;
        String schema = null;
        String consumerFilePath = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--from-beginning":
                    fromBeginning = true;
                    break;
                case "--schema":
                    if (i > args.length - 1) {
                        System.out.println("--schema needs parameter!");
                        System.exit(0);
                    }
                    schema = args[++i].toUpperCase();
                    break;
                case "--consumer-file-path":
                    if (i > args.length - 1) {
                        System.out.println("--consumer-file-path needs parameter!");
                        System.exit(0);
                    }
                    consumerFilePath = args[++i];
                    break;
                case "--write-scn":
                    writeSCN = true;
                    break;
                case "--help":
                    System.out.println("--help cant used together with other parameters!");
                default:
                    System.out.println("unrecognized config parameter: " + args[i] + "!");
                    System.exit(0);
            }
        }

        // set config
        final Properties properties = intiFileProp(consumerFilePath);
        MigrationConfig.fileProps(properties);
        MigrationConfig.schema(schema);
        MigrationConfig.consumerFilePath(consumerFilePath);
        MigrationConfig.writeSCN(writeSCN);
        MigrationConfig.fromBeginning(fromBeginning);
        MigrationConfig.build();
    }

    private static Properties intiFileProp(String path) throws IOException {
        final InputStream inStream = Constant.consumerProperties(path);
        Properties fileProps = new Properties();
        fileProps.load(inStream);
        return fileProps;
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("    java -jar OnlineMigration-1.0-SNAPSHOT.jar [OPTION]...");
        System.out.println();
        System.out.println("Options:");
        System.out.println("    --write-scn             write scn captured in debezium snapshot mode");
        System.out.println("    --schema                the schema want to migrate");
        System.out.println("    --from-beginning        consume the messages from beginning");
        System.out.println("    --consumer-file-path    the application property file");
        System.out.println("    --help                  print the help message");
    }
}
