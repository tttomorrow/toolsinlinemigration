/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.MigrationConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class JDBCExecutor {
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

    public void executeDDL(String DDL_SQL) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(DDL_SQL);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeDML(PreparedStatement statement) {
        try {
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
