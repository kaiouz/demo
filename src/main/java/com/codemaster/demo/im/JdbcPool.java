package com.codemaster.demo.im;

import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcPool {

    public static final Logger logger = LoggerFactory.getLogger(JdbcPool.class);

    private MariaDbPoolDataSource dataSource;

    public JdbcPool(String url) {
       dataSource = new MariaDbPoolDataSource(url);
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("获取数据库连接失败", e);
           return null;
        }
    }

    public void close() {
        dataSource.close();
    }
}
