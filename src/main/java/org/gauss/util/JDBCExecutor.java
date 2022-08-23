/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.MigrationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;

public class JDBCExecutor {

    private final Logger logger = LoggerFactory.getLogger(JDBCExecutor.class);
    private Connection connection;

    public JDBCExecutor() {
        try {
            String url = MigrationConfig.getDatabaseUrl();
            String user = MigrationConfig.getDatabaseUser();
            String password = MigrationConfig.getDatabasePassword();
            Class.forName(MigrationConfig.getDriverName());
            connection = DriverManager.getConnection(url, user, password);

            // This is a long time connection, we close session timout here.
            Statement timeoutStmt = connection.createStatement();
            timeoutStmt.execute("set session_timeout = 0;");
            timeoutStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeDDL(String ddlSql) {
        try {
            logger.info("execute ddl: {}", ddlSql);
            try (Statement statement = connection.createStatement()) {
                statement.execute(ddlSql);
            }
        } catch (SQLException exception) {
            logger.error("execute ddl error", exception);
        }
    }

    public void executeDDL(List<String> ddlSql) {
        for (String ddl : ddlSql) {
            try {
                logger.info("execute ddl: {}", ddlSql);
                try (Statement statement = connection.createStatement()) {
                    statement.execute(ddl);
                }
            } catch (SQLException e) {
                logger.error("execute ddl error", e);
            }
        }
    }

    public void executeDML(PreparedStatement statement) {
        try {
            statement.executeUpdate();
        } catch (SQLException exception) {
            logger.error(exception.getMessage(), exception);
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("close statement error", e);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
