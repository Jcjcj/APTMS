package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    public static Connection connect() {
        Connection conn = null;

        try {
            String url = "jdbc:sqlite:apartment.db";

            Properties props = new Properties();
            props.setProperty("busy_timeout", "10000");

            conn = DriverManager.getConnection(url, props);

        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }

        return conn;
    }
}