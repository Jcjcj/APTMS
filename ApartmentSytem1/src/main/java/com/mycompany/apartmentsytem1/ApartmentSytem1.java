package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class ApartmentSytem1 {

    public static void main(String[] args) {

        System.out.println("--- Starting System Initialization ---");
        
        // 1. Wipe the old database clean (including the new tables)
        System.out.println("1. Clearing old tables...");
        clearAllTables();
        
        // 2. Build the fresh tables with all the new columns we added today
        System.out.println("2. Building new tables...");
        DatabaseSetup.createTables();

       // 3. FIXED: Call the menu-based method in the Seeder
        DataBaseSeeder.runFullSystemTest();
        
        // ---> YOU CAN LAUNCH YOUR FIRST UI SCREEN (LOGIN MENU) HERE <---
    }

    /**
     * Wipes all tables clean. 
     * UPDATED to include the new tables: payment_transactions, announcements, complaints, and room_bills.
     */
    private static void clearAllTables() {
        String[] tables = {
                "payment_transactions", "announcements", "complaints", "room_bills", 
                "maintenance_requests", "viewing_schedule", "room_occupancy",
                "rooms", "apartments", "registered_tenants", "owners", "barangays", "super_admins"
        };
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = OFF;");
            
            for (String t : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE IF EXISTS " + t);
                } catch (SQLException e) {
                    System.out.println("Could not drop table: " + t);
                }
            }
            
            stmt.execute("PRAGMA foreign_keys = ON;");
            
        } catch (Exception e) {
            System.out.println("Clear Tables Error: " + e.getMessage());
        }
    }
}