package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class ApartmentSytem1 {

    public static void main(String[] args) {

        System.out.println("--- Starting System Initialization ---");
        
        // 1. Wipe the old database clean
        System.out.println("1. Clearing old tables...");
        clearAllTables();
        
        // 2. Build the fresh tables
        System.out.println("2. Building new tables...");
        DatabaseSetup.createTables();

        // 3. AUTOMATICALLY INJECT ALL MOCK DATA
        DataBaseSeeder.seedMassiveData();

        // 4. Launch your UI (Replace this line with your actual UI launch code later)
        // new LoginUI().setVisible(true); 
    }

    /**
     * Wipes all tables clean so the seeder can start fresh without duplicate errors.
     */
    private static void clearAllTables() {
        String[] tables = {
                "payment_transactions", "announcements", "complaints", "room_bills", 
                "maintenance_requests", "viewing_schedule", "room_occupancy", "expenses",
                "bills", "tenant_history", "rooms", "apartments", "registered_tenants", 
                "owners", "barangays", "super_admins"
        };
        
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
            
            // Turn off foreign keys temporarily so we can delete tables in any order
            stmt.execute("PRAGMA foreign_keys = OFF;");
            
            for (String t : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE IF EXISTS " + t);
                } catch (SQLException e) {
                    System.out.println("Could not drop table: " + t);
                }
            }
            
            // Turn foreign keys back on
            stmt.execute("PRAGMA foreign_keys = ON;");
            
        } catch (Exception e) {
            System.out.println("Clear Tables Error: " + e.getMessage());
        }
    }
}