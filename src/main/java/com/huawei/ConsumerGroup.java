package com.huawei;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;

import com.huawei.common.ThreadPoolFactory;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * ConsumerGroup hold the DDL/DML/Transaction consumer.
 */
public class ConsumerGroup {
    // Thread pool holds multi consumer. In this version, it only hold DDL and Transaction consumer.
    private final ExecutorService executorService = ThreadPoolFactory.newBigThreadPool("MigrationConsumer");

    private ConsumerRunner DDLConsumer = null;
    private ConsumerRunner TransactionConsumer = null;
    private KafkaConsumer<String, String> DMLConsumer = null;

    // Used to listen if there is new topic created.
    KafkaAdminClient kafkaAdminClient;

    // Properties of consumer
    private final Properties props;

    // serverTopic consists of schema changes
    private final String serverTopic;

    // allTableTopic consists of all tables DML events
    private final String allTableTopic;

    // transactionTopic consists of transaction meta data
    private final String transactionTopic;

    // consumer topics from beginning
    private final boolean fromBeginning;

    private boolean hasCreateTxnConsumer = false;
    private boolean hasCreateDMLConsumer = false;

    public ConsumerGroup(Properties consumerProp, String server, String schema, boolean fromBegin) {
        props = consumerProp;
        fromBeginning = fromBegin;
        serverTopic = server;
        allTableTopic = server + "." + schema.toUpperCase(Locale.ROOT) + "." + "ALL_TABLES";
        transactionTopic = server + "." + "transaction";

        // Configure the topic listener
        Properties clientProp = new Properties();
        clientProp.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
            consumerProp.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        kafkaAdminClient = (KafkaAdminClient)AdminClient.create(clientProp);
    }

    public void createDDLConsumer() {
        DDLConsumer = new ConsumerRunner(props, serverTopic, fromBeginning, null);
        executorService.submit(DDLConsumer);
    }

    public void createDMLConsumer() {
        if (hasCreateDMLConsumer) {
            return;
        }
        ListTopicsResult listTopicResult = kafkaAdminClient.listTopics();
        try {
            for (String topic : listTopicResult.names().get()) {
                if (topic.equals(allTableTopic)) {
                    String groupId = props.getProperty(GROUP_ID_CONFIG);

                    // DML consumer groupId: customer-groupId_DML
                    props.setProperty(GROUP_ID_CONFIG, groupId + "_DML");

                    // Because DML consumer is invoked in transaction consumer, it
                    // doesn't poll topic every time. We set poll interval to be 5000 min,
                    // or it will throw a pool timeout error.
                    props.put(MAX_POLL_INTERVAL_MS_CONFIG, "300000000");

                    DMLConsumer = new KafkaConsumer<>(props);
                    DMLConsumer.subscribe(Collections.singletonList(allTableTopic));
                    if (fromBeginning) {
                        DMLConsumer.poll(0);
                        DMLConsumer.seekToBeginning(DMLConsumer.assignment());
                    }

                    // set the props to be origin one.
                    props.setProperty(GROUP_ID_CONFIG, groupId);
                    props.remove("MAX_POLL_INTERVAL_MS_CONFIG");

                    hasCreateDMLConsumer = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTransactionConsumer() {
        if (hasCreateTxnConsumer) {
            return;
        }
        ListTopicsResult listTopicResult = kafkaAdminClient.listTopics();
        try {
            if (DMLConsumer == null) {
                return;
            }

            for (String topic : listTopicResult.names().get()) {
                // DML consumer must be created before transaction consumer
                if (topic.equals(transactionTopic)) {
                    TransactionConsumer = new ConsumerRunner(props, transactionTopic, fromBeginning, DMLConsumer);
                    executorService.submit(TransactionConsumer);
                    hasCreateTxnConsumer = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasCreateDMLConsumer() {
        return hasCreateDMLConsumer;
    }

    public boolean hasCreateTxnConsumer() {
        return hasCreateTxnConsumer;
    }
}
