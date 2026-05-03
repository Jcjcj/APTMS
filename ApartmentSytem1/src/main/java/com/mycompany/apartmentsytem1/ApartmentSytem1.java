package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApartmentSytem1 {

    public static void main(String[] args) {

        System.out.println("Adding data to database...");
        clearAllTables();
        DatabaseSetup.createTables();

        // 1. Setup Prerequisites (Owner & Apartment)
        OwnerDAO ownerDAO = new OwnerDAO();
        ownerDAO.registerOwner("Carlos Mendoza", "09171234567", "carlos@rental.com", "Cebu", "09991112233", "id_1.jpg", "carlos_m", "owner123");
        int ownerId1 = getId("SELECT owner_id FROM owners WHERE username=?", "carlos_m");

        ApartmentDAO aptDAO = new ApartmentDAO();
        List<Integer> sunriseRooms = Arrays.asList(4, 4, 4, 4, 4);
        List<List<Double>> prices = new ArrayList<>();
        List<List<Double>> downs = new ArrayList<>();
        List<List<Double>> deposits = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            prices.add(Arrays.asList(8500.0, 8500.0, 8500.0, 8500.0));
            downs.add(Arrays.asList(5000.0, 5000.0, 5000.0, 5000.0));
            deposits.add(Arrays.asList(8500.0, 8500.0, 8500.0, 8500.0));
        }

        aptDAO.addApartment(null, "Sunrise Residences", "123-456", 5, 
                sunriseRooms, prices, downs, deposits, 1000000.0, "Bank Transfer", 
                "Spacious", "No pets", "Lahug", "Gov. Cuenco", "Fixed", "Metered", "Fiber", 
                "09181234567", "sunrise@apt.com", "fb/sunrise", "09171234568", "sunrise.jpg", ownerId1);
        int aptId1 = getId("SELECT apartment_id FROM apartments WHERE apartment_name=?", "Sunrise Residences");

        // 2. Register Tenants & Assign Rooms
        TenantDAO tenantDAO = new TenantDAO();
        RoomOccupancyDAO occDAO = new RoomOccupancyDAO();

        tenantDAO.registerTenant("Maria Santos", "09162345678", "maria@email.com", "Lahug", "09171234570", "maria_s", "pass456", "id1.jpg");
        int mariaId = getId("SELECT tenant_id FROM registered_tenants WHERE username=?", "maria_s");
        
        tenantDAO.registerTenant("Juan Dela Cruz", "09199998888", "juan@email.com", "Mandaue", "09188887777", "juan_dc", "pass789", "id2.jpg");
        int juanId = getId("SELECT tenant_id FROM registered_tenants WHERE username=?", "juan_dc");

        if (mariaId != -1 && aptId1 != -1) {
            ownerDAO.updateTenantStatus(mariaId, "APPROVED"); 
            occDAO.assignTenantToRoom(aptId1, "101", mariaId);
        }
        if (juanId != -1 && aptId1 != -1) {
            ownerDAO.updateTenantStatus(juanId, "APPROVED");
            occDAO.assignTenantToRoom(aptId1, "102", juanId);
        }

        // =================================================================
        // 3. ADDING DATA TO MAINTENANCE TABLE (NO CONSOLE PRINTING)
        // =================================================================
        MaintenanceDAO maintDAO = new MaintenanceDAO();
        
        // Inserting Maria's data
        maintDAO.submitRequest(aptId1, "101", mariaId, "Water pipe burst in bathroom!", "EMERGENCY");
        maintDAO.submitRequest(aptId1, "101", mariaId, "Loose doorknob", "LOW");
        maintDAO.submitRequest(aptId1, "101", mariaId, "Aircon is not cooling", "MEDIUM");
        
        // Inserting Juan's data
        maintDAO.submitRequest(aptId1, "102", juanId, "Electrical outlet sparking", "EMERGENCY");
        maintDAO.submitRequest(aptId1, "102", juanId, "Kitchen sink is leaking", "HIGH");
        maintDAO.submitRequest(aptId1, "102", juanId, "Busted lightbulb in hallway", "LOW");
        
        System.out.println("Successfully added maintenance data to the database.");
    }

    private static void clearAllTables() {
        String[] tables = {
                "maintenance_requests", "viewing_schedule", "room_occupancy",
                "rooms", "apartments", "registered_tenants", "owners", "barangays", "super_admins"
        };
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF;");
            for (String t : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE IF EXISTS " + t);
                } catch (SQLException e) {}
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (Exception e) {}
    }

    private static int getId(String sql, String val) {
        int id = -1;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if(sql.contains("?")) ps.setString(1, val);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) id = rs.getInt(1);
            }
        } catch (Exception e) { }
        return id;
    }
}