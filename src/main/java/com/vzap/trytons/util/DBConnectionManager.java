package com.vzap.trytons.util;

import com.vzap.trytons.config.DotEnvConfig;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionManager {
    private static final BasicDataSource dataSource;

    static {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername(DotEnvConfig.getRequired("DB_USERNAME"));
        dataSource.setPassword(DotEnvConfig.getRequired("DB_PASSWORD"));
        dataSource.setUrl(DotEnvConfig.getRequired("DB_URL"));

        dataSource.setMinIdle(DotEnvConfig.getRequiredInt("DB_MIN_IDLE"));
        dataSource.setMaxIdle(DotEnvConfig.getRequiredInt("DB_MAX_IDLE"));
        dataSource.setMaxTotal(DotEnvConfig.getRequiredInt("DB_MAX_TOTAL"));
        dataSource.setMaxWaitMillis(DotEnvConfig.getRequiredLong("DB_MAX_WAIT_MILLIS"));
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxOpenPreparedStatements(DotEnvConfig.getRequiredInt("DB_MAX_OPEN_PREPARED_STATEMENTS"));
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeDataSource() throws SQLException {
        dataSource.close();
    }
}
