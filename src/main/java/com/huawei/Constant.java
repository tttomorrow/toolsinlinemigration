package com.huawei;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Properties used in property file.
 */
public class Constant {
    public static final String PROP_FILE = "consumer_setting.properties";

    public static final String DEBUG_PROP_FILE = "src/main/resources/consumer_setting.properties";

    public static final String DATABASE_SERVER_NAME = "database.server.name";

    public static final String SCN_FILE = "scnfile";

    // database const
    public static final String DRIVER_NAME = "database.driver.classname";

    public static final String DATABASE_URL = "database.url";

    public static final String DATABASE_USER = "database.user";

    public static final String DATABASE_PASSWORD = "database.password";

    private static <T> boolean isStartupFromJar(Class<T> clazz) {
        File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.isFile();
    }

    public static InputStream consumerProperties(String filePath) throws IOException {
        // Reading from user's property file.
        if (filePath != null) {
            final File file = new File(filePath);
            if (!file.isFile()) {
                throw new FileNotFoundException("file not found: " + filePath);
            }
            return new FileInputStream(file);
        }
        // Reading from default property file.
        if (isStartupFromJar(Constant.class)) {
            final URL resource = Consumer.class.getClassLoader().getResource(Constant.PROP_FILE);
            if (resource == null) {
                throw new IOException("file not found!");
            }
            return resource.openStream();
        } else {
            return new FileInputStream(new File(DEBUG_PROP_FILE));
        }
    }
}
