// DONOT MODIFY!! 
// mag create ni siya ug database.... or mag open...

package com.mycompany.apartmentssystem1;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {
        Connection conn = null;

        
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:apartment.db");
            System.out.println("Connected to SQLite!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return conn;
    }
}