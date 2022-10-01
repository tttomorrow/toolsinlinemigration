package org.gauss.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.gauss.util.JDBCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName checker
 * @Description: This is a checker which provide a method to judge whether the database has installed postgis extension
 * @Author 段帅旗
 * @Date 2022/9/15
 */
public class checker {

    private final Logger logger = LoggerFactory.getLogger(checker.class);

    private boolean flag = false;

    private JDBCExecutor executor = new JDBCExecutor();

    private PreparedStatement statement = null;

    private ResultSet rs = null;

    private static final String checkSql = "select count(*) from pg_extension where extname = 'postgis'";

    public checker() {
    }

    /**
     * This method is used to judge whether the database has installed postgis extension
     *
     * @param /NULL
     *
     * @return true or false
     *
     */
    public Boolean postgis_check () {
        try {
            statement = executor.getConnection().prepareStatement(checkSql);
            rs = executor.executeQuery(statement);
            while (rs.next()) {
                int count = rs.getInt("count");
                if (count == 1) {
                    flag = true;
                }
            }
        } catch (SQLException exception) {
            logger.error(exception.getMessage(), exception);
        } finally {
            try {
                statement.close();
                rs.close();
            } catch (SQLException exception) {
                logger.error(exception.getMessage(), exception);
            }
        }
        return flag;
    }

    public static String getChecksql() {
        return checkSql;
    }

    public boolean isFlag() {
        return flag;
    }
    public PreparedStatement getStatement() {
        return statement;
    }
}
