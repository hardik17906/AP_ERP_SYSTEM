package edu.univ.erp.data.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages all database connections for the application using HikariCP.
 *
 * This class creates and manages two separate connection pools:
 * 1. For the 'auth_db' (Authentication)
 * 2. For the 'erp_db' (Main Application Data)
 *
 * It loads configuration from 'config.properties' file in the resources folder.
 */
public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String CONFIG_FILE = "/config.properties";

    private HikariDataSource authDataSource;
    private HikariDataSource erpDataSource;

    /**
     * Initializes the DatabaseManager by loading properties and setting up
     * both connection pools.
     */
    public void init() throws IOException {
        log.info("Initializing DatabaseManager...");
        Properties props = loadProperties(CONFIG_FILE);

        // 1. Configure Auth DB Pool
        HikariConfig authConfig = new HikariConfig();
        authConfig.setJdbcUrl(props.getProperty("auth.jdbcUrl"));
        authConfig.setUsername(props.getProperty("auth.username"));
        authConfig.setPassword(props.getProperty("auth.password"));
        authConfig.setPoolName("AuthDBPool");
        authConfig.addDataSourceProperty("cachePrepStmts", "true");
        authConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        authConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        authDataSource = new HikariDataSource(authConfig);
        log.info("AuthDB connection pool initialized.");

        // 2. Configure ERP DB Pool
        HikariConfig erpConfig = new HikariConfig();
        erpConfig.setJdbcUrl(props.getProperty("erp.jdbcUrl"));
        erpConfig.setUsername(props.getProperty("erp.username"));
        erpConfig.setPassword(props.getProperty("erp.password"));
        erpConfig.setPoolName("ErpDBPool");
        erpConfig.addDataSourceProperty("cachePrepStmts", "true");
        erpConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        erpConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        erpDataSource = new HikariDataSource(erpConfig);
        log.info("ErpDB connection pool initialized.");
    }

    /**
     * Loads properties from a file in the resources folder.
     */
    private Properties loadProperties(String fileName) throws IOException {
        log.debug("Loading properties from: {}", fileName);
        InputStream input = getClass().getResourceAsStream(fileName);
        if (input == null) {
            log.error("Unable to find " + fileName);
            throw new IOException("Resource file not found: " + fileName);
        }
        Properties props = new Properties();
        props.load(input);
        return props;
    }

    /**
     * Gets a connection to the 'auth_db'.
     *
     * @return a Connection from the AuthDBPool.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getAuthConnection() throws SQLException {
        return authDataSource.getConnection();
    }

    /**
     * Gets a connection to the 'erp_db'.
     *
     * @return a Connection from the ErpDBPool.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getErpConnection() throws SQLException {
        return erpDataSource.getConnection();
    }

    /**
     * Closes both data sources.
     * Call this when the application is shutting down.
     */
    public void close() {
        if (authDataSource != null) {
            authDataSource.close();
            log.info("AuthDB connection pool closed.");
        }
        if (erpDataSource != null) {
            erpDataSource.close();
            log.info("ErpDB connection pool closed.");
        }
    }
}