package com.huawei;

import static com.huawei.Constant.DATABASE_PASSWORD;
import static com.huawei.Constant.DATABASE_SERVER_NAME;
import static com.huawei.Constant.DATABASE_URL;
import static com.huawei.Constant.DATABASE_USER;
import static com.huawei.Constant.DRIVER_NAME;
import static com.huawei.Constant.SCN_FILE;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import java.util.Properties;

/**
 * Interface of migration config.
 */
public class MigrationConfig {

    private static final String PROP_ERROR = "%s property cant be null!";

    private static MigrationConfig INSTANCE = new MigrationConfig();

    private Properties fileProps;

    private boolean fromBeginning = false;

    private boolean writeSCN = false;

    private Properties consumerProps;

    private String consumerFilePath = null;

    private String schema = null;

    private MigrationConfig() {

    }

    public static boolean isWriteSCN() {
        return INSTANCE.writeSCN;
    }

    private static String getFileProp(String key) {
        return INSTANCE.fileProps.getProperty(key);
    }

    public static void writeSCN(boolean writeSCN) {
        INSTANCE.writeSCN = writeSCN;
    }

    public static String getConsumerFilePath() {
        return INSTANCE.consumerFilePath;
    }

    public static String getSchema() {
        return INSTANCE.schema;
    }

    public static String getDriverName() {
        return getFileProp(DRIVER_NAME);
    }

    public static String getDatabaseUrl() {
        return getFileProp(DATABASE_URL);
    }

    public static String getDatabaseUser() {
        return getFileProp(DATABASE_USER);
    }

    public static String getDatabasePassword() {
        return getFileProp(DATABASE_PASSWORD);
    }

    public static boolean isFromBeginning() {
        return INSTANCE.fromBeginning;
    }

    public static void consumerFilePath(String consumerFilePath) {
        INSTANCE.consumerFilePath = consumerFilePath;
    }

    public static void schema(String schema) {
        INSTANCE.schema = schema;
    }

    public static Properties getConsumerProps() {
        return INSTANCE.consumerProps;
    }

    public static void fromBeginning(boolean fromBeginning) {
        INSTANCE.fromBeginning = fromBeginning;
    }

    public static Properties getFileProps() {
        return INSTANCE.fileProps;
    }

    public static void fileProps(Properties fileProps) {
        INSTANCE.fileProps = fileProps;
        consumerProps(INSTANCE.fileProps);
    }

    public static String getDatabaseServerName() {
        return INSTANCE.fileProps.getProperty(DATABASE_SERVER_NAME);
    }

    public static String getScnFilePath() {
        return getFileProp(SCN_FILE);
    }

    public static void build() {
        if (INSTANCE.fileProps == null) {
            throw new RuntimeException("fileProps is null");
        }
        if (INSTANCE.consumerProps == null) {
            throw new RuntimeException("fileProps is null");
        }
        if (getFileProp(DATABASE_SERVER_NAME) == null) {
            throw new RuntimeException(String.format(PROP_ERROR, DATABASE_SERVER_NAME));
        }
        if (INSTANCE.writeSCN) {
            if (getFileProp(SCN_FILE) == null) {
                throw new RuntimeException(String.format("writeSCN needs %s property!", SCN_FILE));
            }
        } else {
            if (getFileProp(DRIVER_NAME) == null) {
                throw new RuntimeException(String.format(PROP_ERROR, DRIVER_NAME));
            }
            if (getFileProp(DATABASE_URL) == null) {
                throw new RuntimeException(String.format(PROP_ERROR, DATABASE_URL));
            }
            if (getFileProp(DATABASE_USER) == null) {
                throw new RuntimeException(String.format(PROP_ERROR, DATABASE_USER));
            }
            if (getFileProp(DATABASE_PASSWORD) == null) {
                throw new RuntimeException(String.format(PROP_ERROR, DATABASE_PASSWORD));
            }
        }
    }

    private static void consumerProps(Properties fileProps) {
        INSTANCE.consumerProps = new Properties();
        // Properties in
        String[] kafkaConfigName = {
                BOOTSTRAP_SERVERS_CONFIG, ENABLE_AUTO_COMMIT_CONFIG, AUTO_COMMIT_INTERVAL_MS_CONFIG,
                KEY_DESERIALIZER_CLASS_CONFIG, VALUE_DESERIALIZER_CLASS_CONFIG, GROUP_ID_CONFIG
        };
        for (String configName : kafkaConfigName) {
            if (!fileProps.containsKey(configName)) {
                throw new RuntimeException(String.format(PROP_ERROR, configName));
            } else {
                INSTANCE.consumerProps.put(configName, fileProps.getProperty(configName));
            }
        }
    }
}
