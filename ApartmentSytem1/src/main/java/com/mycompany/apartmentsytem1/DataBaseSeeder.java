package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;

public class DataBaseSeeder {

    public static void seedMassiveData() {
        System.out.println("\n[WIPING OLD DATA AND SEEDING EXCLUSIVE SUPER ADMIN ACCOUNTS...]");
        
        // Hashing the shared password "admin123" for database security
        String sharedPassword = PasswordUtil.hashPassword("admin123");

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
             
            conn.setAutoCommit(false); 

            // 1. DELETE ANY DEFAULT ADMINS (To ensure ONLY your 6 admins exist)
            stmt.addBatch("DELETE FROM super_admins");

            // 2. INSERT YOUR SPECIFIC SUPER ADMINS
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('hilaryjanz', '" + sharedPassword + "')");
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('shynjhy', '" + sharedPassword + "')");
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('myles', '" + sharedPassword + "')");
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('cj', '" + sharedPassword + "')");
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('jerome', '" + sharedPassword + "')");
            stmt.addBatch("INSERT INTO super_admins (username, password) VALUES ('yeasha', '" + sharedPassword + "')");

            stmt.executeBatch();
            conn.commit();
            
            System.out.println("SUCCESS! Database is cleared. Only the 6 authorized Super Admins exist.");

        } catch (Exception e) {
            System.err.println("Super Admin Seeding Error: " + e.getMessage());
        }
    }
}