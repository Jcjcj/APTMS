package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApartmentsSystem1 {

    public static void main(String[] args) {

        System.out.println("========== INITIALIZING DATABASE ==========");
        clearAllTables();
        DatabaseSetup.createTables();

        System.out.println("\n========== 1. TESTING OWNERS ==========");
        OwnerDAO ownerDAO = new OwnerDAO();

        ownerDAO.registerOwner("Carlos Mendoza", "09171234567", "carlos@rental.com", "Cebu", "carlos_m", "owner123");
        ownerDAO.registerOwner("Luzviminda Rivera", "09181234568", "luz@apartments.com", "Mabolo", "luz_r", "rental456");

        int ownerId1 = getId("SELECT owner_id FROM owners WHERE username=?", "carlos_m");
        int ownerId2 = getId("SELECT owner_id FROM owners WHERE username=?", "luz_r");


        System.out.println("\n========== 2. TESTING APARTMENTS & ROOM GENERATION ==========");
        ApartmentDAO aptDAO = new ApartmentDAO();

        // SUNRISE RESIDENCES: 5 floors, 4 rooms per floor, 8500.0 rent each.
        List<Integer> sunriseRoomsPerFloor = Arrays.asList(4, 4, 4, 4, 4);
        List<List<Double>> sunrisePrices = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            sunrisePrices.add(Arrays.asList(8500.0, 8500.0, 8500.0, 8500.0));
        }

        aptDAO.addApartment(null, "Sunrise Residences", "123-456", 5, 
                sunriseRoomsPerFloor, sunrisePrices, 5000.0, "Bank Transfer", 
                "Spacious units", "No pets", "Lahug", "Gov. Cuenco", "Fixed", "Metered", "Fiber", 
                "09181234567", "sunrise@apt.com", "fb/sunrise", "09171234568", "sunrise.jpg", ownerId1);

        // GREENFIELD TOWERS: 3 floors, 3 rooms per floor, 12000.0 rent each.
        List<Integer> greenfieldRoomsPerFloor = Arrays.asList(3, 3, 3);
        List<List<Double>> greenfieldPrices = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            greenfieldPrices.add(Arrays.asList(12000.0, 12000.0, 12000.0));
        }

        aptDAO.addApartment(null, "Greenfield Towers", "789-012", 3, 
                greenfieldRoomsPerFloor, greenfieldPrices, 8000.0, "GCash", 
                "Modern studios", "Quiet hours", "Banilad", "Banilad Road", "Included", "Included", "Included", 
                "09181234569", "greenfield@apt.com", "ig/greenfield", "09171234569", "greenfield.jpg", ownerId2);

        int aptId1 = getId("SELECT apartment_id FROM apartments WHERE apartment_name=?", "Sunrise Residences");


        System.out.println("\n========== 3. TENANTS & OCCUPANCY ==========");
        TenantDAO tenantDAO = new TenantDAO();
        
        // Register Maria
        tenantDAO.registerTenant("Maria Santos", "09162345678", "maria@email.com", "Lahug", "09171234570", "maria_s", "pass456", "id1.jpg");
        int mariaId = getId("SELECT tenant_id FROM registered_tenants WHERE username=?", "maria_s");
        
        // Approve Maria
        ownerDAO.updateTenantStatus(mariaId, "APPROVED"); 
        System.out.println("Maria registered and approved.");
        
        RoomOccupancyDAO occDAO = new RoomOccupancyDAO();
        // Assign Maria to Room 101
        occDAO.assignTenantToRoom(aptId1, "101", mariaId);
        System.out.println("Assigned Maria to Room 101.");


        System.out.println("\n========== 4. TESTING MAINTENANCE SYSTEM ==========");
        MaintenanceDAO maintDAO = new MaintenanceDAO();
        
        maintDAO.submitRequest(aptId1, "101", mariaId, "Water pipe burst in bathroom!", "EMERGENCY");
        maintDAO.submitRequest(aptId1, "101", mariaId, "Loose doorknob", "LOW");
        
        maintDAO.printActiveRequests(aptId1);
        
        System.out.println("\nOwner fixes the pipe...");
        int emergencyTicketId = getId("SELECT request_id FROM maintenance_requests WHERE priority_level=?", "EMERGENCY");
        maintDAO.updateRequestStatus(emergencyTicketId, "RESOLVED");


        System.out.println("\n========== 5. TESTING ANALYTICS SYSTEM ==========");
        AnalyticsDAO analyticsDAO = new AnalyticsDAO();
        analyticsDAO.printOccupancyStats(aptId1);
        analyticsDAO.printPopularBarangays();


        System.out.println("\n========== 6. POPULATING VIEWING SCHEDULE ==========");
        // Adding data to the viewing_schedule table
        String scheduleSql = "INSERT INTO viewing_schedule(apartment_id, tenant_name, contact_number, schedule_date, start_time, end_time) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(scheduleSql)) {
            ps.setInt(1, aptId1);
            ps.setString(2, "Juan Dela Cruz");
            ps.setString(3, "09998887776");
            ps.setString(4, "2026-05-10");
            ps.setString(5, "02:00 PM");
            ps.setString(6, "03:00 PM");
            ps.executeUpdate();
            System.out.println("✓ Viewing schedule entry created for Juan Dela Cruz.");
        } catch (Exception e) {
            System.out.println("Schedule Error: " + e.getMessage());
        }

        System.out.println("\n========== SYSTEM TEST COMPLETE: ALL TABLES POPULATED ==========");
    }

    private static void clearAllTables() {
        String[] tables = {
                "maintenance_requests", "viewing_schedule", "room_occupancy",
                "rooms", "apartments", "registered_tenants", "owners", "barangays"
        };

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = OFF;");
            for (String t : tables) {
                try {
                    stmt.executeUpdate("DELETE FROM " + t);
                    stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='" + t + "'");
                } catch (SQLException e) {
                    // Ignore errors if table doesn't exist yet
                }
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
            System.out.println("✓ Cleared old database records successfully.");

        } catch (Exception e) {
            System.out.println("Clear Error: " + e.getMessage());
        }
    }

    private static int getId(String sql, String val) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, val);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { 
            System.out.println("GetID Error: " + e.getMessage());
        }
        return -1;
    }
}