package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.util.*;

public class ApartmentDAO {

    // =============================================
    // generate random 6-digit unique apartment code
    // =============================================
    private String generateApartmentCode() {
        Random rand = new Random();
        String code;
        do {
            code = String.valueOf(100000 + rand.nextInt(900000)); // 100000-999999
        } while (codeExists(code));
        return code;
    }

    // check if code already exists in database
    private boolean codeExists(String code) {
        String sql = "SELECT 1 FROM apartments WHERE apartment_code=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if found
        } catch (Exception e) {
            return false;
        }
    }

    // =============================================
    // add apartment (auto‑generate code if null)
    // frontend: call when "Add Apartment" button clicked
    // =============================================
    public void addApartment(String apartmentCode, String name, String tin, int rooms,
                             double rent, double down, String paymentMethod,
                             String description, String policy, String barangay,
                             String street, String electricity, String water,
                             String internet, String contact, String email,
                             String social, String emergency, String profileImage,
                             int ownerId) {

        if (apartmentCode == null || apartmentCode.isEmpty()) {
            apartmentCode = generateApartmentCode(); // generate if not provided
        }

        String sql = "INSERT INTO apartments(apartment_code, apartment_name, owner_id, tin_no, rooms_available, "
                + "rent_per_room, down_payment, payment_method, description, policy, barangay, street, "
                + "electricity, water, internet, contact_number, email, social_media, emergency_number, profile_image) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // mapping from frontend fields
            ps.setString(1, apartmentCode);          // 6‑digit code
            ps.setString(2, name);                   // frontend: txtApartmentName
            ps.setInt(3, ownerId);                   // from logged‑in owner
            ps.setString(4, tin);                    // frontend: txtTIN
            ps.setInt(5, rooms);                     // frontend: txtRooms
            ps.setDouble(6, rent);                   // frontend: txtRent
            ps.setDouble(7, down);                   // frontend: txtDown
            ps.setString(8, paymentMethod);          // frontend: txtPaymentMethod
            ps.setString(9, description);            // frontend: txtDescription
            ps.setString(10, policy);                // frontend: txtPolicy
            ps.setString(11, barangay);              // frontend: txtBarangay
            ps.setString(12, street);                // frontend: txtStreet
            ps.setString(13, electricity);           // frontend: radioElectricity
            ps.setString(14, water);                 // frontend: radioWater
            ps.setString(15, internet);              // frontend: radioInternet
            ps.setString(16, contact);               // frontend: txtContact
            ps.setString(17, email);                 // frontend: txtEmail
            ps.setString(18, social);                // frontend: txtSocial
            ps.setString(19, emergency);             // frontend: txtEmergency
            ps.setString(20, profileImage);          // frontend: uploadProfileImage

            ps.executeUpdate();
            System.out.println("Apartment added! Code: " + apartmentCode);
        } catch (Exception e) {
            System.out.println("Add Apartment Error: " + e.getMessage());
        }
    }

    // =============================================
    // search apartments with available rooms > 0 (by barangay)
    // frontend: tenant searches by barangay
    // returns list of "apartment name | Available Rooms: X"
    // =============================================
    public List<String> searchApartmentsWithAvailableRooms(String barangay) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT apartment_name, rooms_available FROM apartments WHERE barangay=? AND rooms_available>0";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(rs.getString("apartment_name") + " | Available Rooms: " + rs.getInt("rooms_available"));
            }
        } catch (Exception e) {
            System.out.println("Search Error: " + e.getMessage());
        }
        return results;
    }

    // =============================================
    // search with nearest barangay suggestion
    // frontend: call this (instead of direct search) for better UX
    // returns Map: found (bool), apartments (List), suggestedBarangay (String)
    // =============================================
    public Map<String, Object> searchWithSuggestion(String barangay) {
        Map<String, Object> response = new HashMap<>();
        List<String> results = searchApartmentsWithAvailableRooms(barangay);
        response.put("found", !results.isEmpty());
        response.put("apartments", results);
        if (results.isEmpty()) {
            String nearest = findNearestBarangayWithRooms(barangay);
            response.put("suggestedBarangay", nearest);
        } else {
            response.put("suggestedBarangay", null);
        }
        return response;
    }

    // helper: find nearest barangay that has at least one apartment with available rooms
    private String findNearestBarangayWithRooms(String barangay) {
        String sql = "SELECT nearby_1, nearby_2, nearby_3 FROM barangays WHERE name=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] nearby = {rs.getString("nearby_1"), rs.getString("nearby_2"), rs.getString("nearby_3")};
                for (String nb : nearby) {
                    if (nb != null && !searchApartmentsWithAvailableRooms(nb).isEmpty()) {
                        return nb; // first nearby with available rooms
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Find Nearest Error: " + e.getMessage());
        }
        return "No nearby barangay with available rooms";
    }

    // =============================================
    // get all barangays for dropdown
    // frontend: call to populate search dropdown
    // =============================================
    public List<String> getAllBarangays() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM barangays ORDER BY name";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (Exception e) {
            System.out.println("Get Barangays Error: " + e.getMessage());
        }
        return list;
    }

    // =============================================
    // owner view current tenants (from room_occupancy)
    // frontend: owner dashboard → "My Tenants" button
    // returns list of strings with tenant info
    // =============================================
    public List<String> getCurrentTenantsByOwner(int ownerId) {
        List<String> tenantsList = new ArrayList<>();
        String sql = "SELECT t.tenant_id, t.name, t.contact_number, a.apartment_name, ro.room_number, ro.move_in_date "
                + "FROM tenants t JOIN room_occupancy ro ON t.tenant_id = ro.tenant_id "
                + "JOIN apartments a ON ro.apartment_id = a.apartment_id "
                + "WHERE a.owner_id=? AND ro.status='Current' AND t.is_active=1";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tenantsList.add("ID:" + rs.getInt("tenant_id") + " | " + rs.getString("name") +
                                " | Room: " + rs.getString("room_number") + " | Since: " + rs.getString("move_in_date"));
            }
        } catch (Exception e) {
            System.out.println("Get Tenants Error: " + e.getMessage());
        }
        return tenantsList;
    }

    // =============================================
    // room history (past tenants for a specific room)
    // frontend: owner clicks on a room → "View History"
    // =============================================
    public List<String> getTenantHistoryForRoom(int apartmentId, String roomNumber) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT name, move_in_date, move_out_date FROM tenant_history "
                + "WHERE apartment_id=? AND room_number=? ORDER BY move_out_date DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                history.add(rs.getString("name") + " | " + rs.getString("move_in_date") + " to " + rs.getString("move_out_date"));
            }
        } catch (Exception e) {
            System.out.println("History Error: " + e.getMessage());
        }
        return history;
    }

    // =============================================
    // NEW METHOD: get all current tenants grouped by apartment
    // frontend: owner dashboard → "View All Tenants" (grouped view)
    // returns a list of String arrays: [tenant_name, contact_number, apartment_name, apartment_code, room_number, email, address, move_in_date]
    // Grouped by apartment (all tenants of same apartment appear together)
    // =============================================
    public List<String[]> getTenantsGroupedByApartment() {
        List<String[]> grouped = new ArrayList<>();
        String sql = "SELECT t.name AS tenant_name, t.contact_number, a.apartment_name, a.apartment_code, "
                   + "ro.room_number, t.email, t.address, ro.move_in_date "
                   + "FROM room_occupancy ro "
                   + "JOIN tenants t ON ro.tenant_id = t.tenant_id "
                   + "JOIN apartments a ON ro.apartment_id = a.apartment_id "
                   + "WHERE ro.status = 'Current' AND t.is_active = 1 "
                   + "ORDER BY a.apartment_id, ro.room_number";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String[] row = new String[8];
                row[0] = rs.getString("tenant_name");
                row[1] = rs.getString("contact_number");
                row[2] = rs.getString("apartment_name");
                row[3] = rs.getString("apartment_code");
                row[4] = rs.getString("room_number");
                row[5] = rs.getString("email");
                row[6] = rs.getString("address");
                row[7] = rs.getString("move_in_date");
                grouped.add(row);
            }
        } catch (Exception e) {
            System.out.println("Group tenants error: " + e.getMessage());
        }
        return grouped;
    }
}