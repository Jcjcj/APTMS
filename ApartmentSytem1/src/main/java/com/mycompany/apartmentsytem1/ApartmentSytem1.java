package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApartmentSytem1 {

    public static void main(String[] args) {

        System.out.println("========== INITIALIZING DATABASE ==========");
        clearAllTables();
        DatabaseSetup.createTables();

        System.out.println("\n========== 1. TESTING OWNERS ==========");
        OwnerDAO ownerDAO = new OwnerDAO();

        ownerDAO.registerOwner("Carlos Mendoza", "09171234567", "carlos@rental.com", "Cebu", "09991112233", "id_1.jpg", "carlos_m", "owner123");
        ownerDAO.registerOwner("Luzviminda Rivera", "09181234568", "luz@apartments.com", "Mabolo", "09994445566", "id_2.jpg", "luz_r", "rental456");

        int ownerId1 = getId("SELECT owner_id FROM owners WHERE username=?", "carlos_m");
        int ownerId2 = getId("SELECT owner_id FROM owners WHERE username=?", "luz_r");

        System.out.println("\n========== 2. TESTING APARTMENTS & ROOM GENERATION ==========");
        ApartmentDAO aptDAO = new ApartmentDAO();

        // SUNRISE RESIDENCES: 5 floors, 4 rooms per floor
        List<Integer> sunriseRoomsPerFloor = Arrays.asList(4, 4, 4, 4, 4);
        
        List<List<Double>> sunrisePrices = new ArrayList<>();
        List<List<Double>> sunriseDowns = new ArrayList<>();
        List<List<Double>> sunriseDeposits = new ArrayList<>();
        
        for(int i = 0; i < 5; i++) {
            sunrisePrices.add(Arrays.asList(8500.0, 8500.0, 8500.0, 8500.0));
            sunriseDowns.add(Arrays.asList(5000.0, 5000.0, 5000.0, 5000.0));
            sunriseDeposits.add(Arrays.asList(8500.0, 8500.0, 8500.0, 8500.0));
        }

        aptDAO.addApartment(null, "Sunrise Residences", "123-456", 5, 
                sunriseRoomsPerFloor, sunrisePrices, sunriseDowns, sunriseDeposits, 1000000.0, "Bank Transfer", 
                "Spacious units", "No pets", "Lahug", "Gov. Cuenco", "Fixed", "Metered", "Fiber", 
                "09181234567", "sunrise@apt.com", "fb/sunrise", "09171234568", "sunrise.jpg", ownerId1);

        // GREENFIELD TOWERS: 3 floors, 3 rooms per floor
        List<Integer> greenfieldRoomsPerFloor = Arrays.asList(3, 3, 3);
        
        List<List<Double>> greenPrices = new ArrayList<>();
        List<List<Double>> greenDowns = new ArrayList<>();
        List<List<Double>> greenDeposits = new ArrayList<>();
        
        for(int i = 0; i < 3; i++) {
            greenPrices.add(Arrays.asList(12000.0, 12000.0, 12000.0));
            greenDowns.add(Arrays.asList(8000.0, 8000.0, 8000.0));
            greenDeposits.add(Arrays.asList(12000.0, 12000.0, 12000.0));
        }

        aptDAO.addApartment(null, "Greenfield Towers", "789-012", 3, 
                greenfieldRoomsPerFloor, greenPrices, greenDowns, greenDeposits, 2500000.0, "GCash", 
                "Modern studios", "Quiet hours", "Banilad", "Banilad Road", "Included", "Included", "Included", 
                "09181234569", "greenfield@apt.com", "ig/greenfield", "09171234569", "greenfield.jpg", ownerId2);

        int aptId1 = getId("SELECT apartment_id FROM apartments WHERE apartment_name=?", "Sunrise Residences");

        System.out.println("\n========== 3. TENANTS & OCCUPANCY ==========");
        TenantDAO tenantDAO = new TenantDAO();
        tenantDAO.registerTenant("Maria Santos", "09162345678", "maria@email.com", "Lahug", "09171234570", "maria_s", "pass456", "id1.jpg");
        int mariaId = getId("SELECT tenant_id FROM registered_tenants WHERE username=?", "maria_s");
        
        ownerDAO.updateTenantStatus(mariaId, "APPROVED"); 
        
        RoomOccupancyDAO occDAO = new RoomOccupancyDAO();
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
        ViewingScheduleDAO viewDAO = new ViewingScheduleDAO();
        String[] tempCreds = viewDAO.scheduleViewing(aptId1, "Juan Dela Cruz", "09998887776", "2026-05-10", "02:00 PM", "03:00 PM");
        System.out.println("✓ Schedule added. Temp Username for Juan: " + tempCreds[0]);

        System.out.println("\n========== SYSTEM TEST COMPLETE ==========");
    }

    private static void clearAllTables() {
        String[] tables = {
                "super_admins", "maintenance_requests", "viewing_schedule", "room_occupancy",
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
                    // Safe to ignore if table does not exist on first run
                }
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
            System.out.println("✓ Cleared old database records successfully.");

        } catch (Exception e) {
            System.out.println("Critical Error Clearing Tables: " + e.getMessage());
        }
    }

    private static int getId(String sql, String val) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, val);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (Exception e) {
            System.out.println("Error fetching ID: " + e.getMessage());
        }
        return -1;
    }
}