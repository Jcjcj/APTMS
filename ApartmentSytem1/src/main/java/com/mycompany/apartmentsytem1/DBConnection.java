package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:apartment.db";
            Properties props = new Properties();
            props.setProperty("busy_timeout", "10000");

            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url, props);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            LOGGER.info("Connected to SQLite and Foreign Keys enabled!");

        } catch (Exception e) {
            LOGGER.severe("Connection Error: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
}