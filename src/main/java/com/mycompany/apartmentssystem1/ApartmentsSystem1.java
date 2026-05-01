package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ApartmentsSystem1 {

    public static void main(String[] args) {

        clearAllTables();

        DBConnection.connect();
        DatabaseSetup.createTables();

        System.out.println("\n========== INSERTING DATA ==========\n");

        // =============================================
        // OWNERS
        // =============================================
        OwnerDAO ownerDAO = new OwnerDAO();

        ownerDAO.registerOwner(
                "Carlos Mendoza",
                "09171234567",
                "carlos@rental.com",
                "Cebu Business Park",
                "carlos_m",
                "owner123"
        );

        ownerDAO.registerOwner(
                "Luzviminda Rivera",
                "09181234568",
                "luz@apartments.com",
                "Mabolo, Cebu City",
                "luz_r",
                "rental456"
        );

        int ownerId1 = getId("SELECT owner_id FROM owners WHERE username=?", "carlos_m");
        int ownerId2 = getId("SELECT owner_id FROM owners WHERE username=?", "luz_r");

        // =============================================
        // APARTMENTS (FIXED CALLS)
        // =============================================
        ApartmentDAO aptDAO = new ApartmentDAO();

        aptDAO.addApartment(
                null,
                "Sunrise Residences",
                "123-456",
                5,
                4,
                8500.0,
                5000.0,
                "Bank Transfer",
                "Spacious units",
                "No pets",
                "Lahug",
                "Gov. Cuenco",
                "Fixed",
                "Metered",
                "Fiber",
                "09181234567",
                "sunrise@apt.com",
                "fb/sunrise",
                "09171234568",
                "sunrise.jpg",
                ownerId1
        );

        aptDAO.addApartment(
                null,
                "Greenfield Towers",
                "789-012",
                3,
                3,
                12000.0,
                8000.0,
                "GCash",
                "Modern studios",
                "Quiet hours",
                "Banilad",
                "Banilad Road",
                "Included",
                "Included",
                "Included",
                "09181234569",
                "greenfield@apt.com",
                "ig/greenfield",
                "09171234569",
                "greenfield.jpg",
                ownerId1
        );

        int aptId1 = getId("SELECT apartment_id FROM apartments WHERE apartment_name=?", "Sunrise Residences");
        int aptId2 = getId("SELECT apartment_id FROM apartments WHERE apartment_name=?", "Greenfield Towers");

        // =============================================
        // TENANTS
        // =============================================
        TenantDAO tenantDAO = new TenantDAO();

        tenantDAO.registerTenant("Maria Santos", "09162345678", "maria@email.com",
                "Lahug", "09171234570", "maria_s", "pass456", "id1.jpg");

        tenantDAO.registerTenant("Jose Rizal", "09162345679", "jose@email.com",
                "Banilad", "09171234571", "jose_r", "pass789", "id2.jpg");

        int t1 = getId("SELECT tenant_id FROM tenants_base WHERE username=?", "maria_s");
        int t2 = getId("SELECT tenant_id FROM tenants_base WHERE username=?", "jose_r");

        // =============================================
        // ROOM OCCUPANCY
        // =============================================
        RoomOccupancyDAO occ = new RoomOccupancyDAO();

        occ.assignTenantToRoom(aptId1, "101", t1);
        occ.assignTenantToRoom(aptId2, "201", t2);

        // =============================================
        // VIEWING
        // =============================================
        ViewingDAO viewDAO = new ViewingDAO();

        viewDAO.scheduleViewing(
                aptId1,
                "Visitor A",
                "09170000000",
                LocalDate.now().plusDays(3).toString(),
                "09:00",
                "10:00"
        );

        // =============================================
        // DISPLAY
        // =============================================
        System.out.println("\n========== APARTMENTS ==========\n");

        print("apartments",
                "apartment_name",
                "barangay",
                "floors",
                "rooms_per_floor",
                "rooms_available"
        );

        System.out.println("\n========== BARANGAYS ==========\n");
        aptDAO.getAllBarangays().forEach(System.out::println);

        System.out.println("\n========== SEARCH TEST ==========\n");
        aptDAO.searchApartmentsWithAvailableRooms("Lahug")
                .forEach(System.out::println);

        System.out.println("\nDONE.");
    }

    // =============================================
    // CLEAR TABLES
    // =============================================
    private static void clearAllTables() {

        String[] tables = {
                "tenant_history",
                "viewing_schedule",
                "room_occupancy",
                "tenants_base",
                "apartments",
                "owners",
                "barangays"
        };

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t);
            }

        } catch (Exception e) {
            System.out.println("Clear Error: " + e.getMessage());
        }
    }

    // =============================================
    // HELPERS
    // =============================================
    private static int getId(String sql, String val) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, val);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    private static void print(String table, String... cols) {

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table)) {

            while (rs.next()) {

                StringBuilder sb = new StringBuilder();

                for (String c : cols) {
                    sb.append(c)
                            .append("=")
                            .append(rs.getString(c))
                            .append(" | ");
                }

                System.out.println(sb);
            }

        } catch (Exception e) {
            System.out.println("Print Error: " + e.getMessage());
        }
    }
}