package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.util.*;

public class ApartmentDAO {

    // =============================================
    // GENERATE CODE
    // =============================================
    private String generateApartmentCode() {
        Random r = new Random();
        String code;

        do {
            code = String.valueOf(100000 + r.nextInt(900000));
        } while (codeExists(code));

        return code;
    }

    private boolean codeExists(String code) {
        String sql = "SELECT 1 FROM apartments WHERE apartment_code=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            return ps.executeQuery().next();

        } catch (Exception e) {
            return false;
        }
    }

    // =============================================
    // ADD APARTMENT (FINAL MATCHED VERSION)
    // =============================================
    public void addApartment(String apartmentCode,
                             String name,
                             String tin,
                             int floors,
                             int roomsPerFloor,
                             double rent,
                             double down,
                             String paymentMethod,
                             String description,
                             String policy,
                             String barangay,
                             String street,
                             String electricity,
                             String water,
                             String internet,
                             String contact,
                             String email,
                             String social,
                             String emergency,
                             String profileImage,
                             int ownerId) {

        if (apartmentCode == null || apartmentCode.isEmpty()) {
            apartmentCode = generateApartmentCode();
        }

        int totalRooms = floors * roomsPerFloor;

        String sql = "INSERT INTO apartments(" +
                "apartment_code, apartment_name, owner_id, tin_no, " +
                "floors, rooms_per_floor, rooms_available, " +
                "rent_per_room, down_payment, payment_method, description, policy, " +
                "barangay, street, electricity, water, internet, " +
                "contact_number, email, social_media, emergency_number, profile_image) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apartmentCode);
            ps.setString(2, name);
            ps.setInt(3, ownerId);
            ps.setString(4, tin);

            ps.setInt(5, floors);
            ps.setInt(6, roomsPerFloor);
            ps.setInt(7, totalRooms);

            ps.setDouble(8, rent);
            ps.setDouble(9, down);
            ps.setString(10, paymentMethod);
            ps.setString(11, description);
            ps.setString(12, policy);

            ps.setString(13, barangay);
            ps.setString(14, street);
            ps.setString(15, electricity);
            ps.setString(16, water);
            ps.setString(17, internet);

            ps.setString(18, contact);
            ps.setString(19, email);
            ps.setString(20, social);
            ps.setString(21, emergency);
            ps.setString(22, profileImage);

            ps.executeUpdate();

            System.out.println("Added: " + name);

        } catch (Exception e) {
            System.out.println("Apartment Error: " + e.getMessage());
        }
    }

    // =============================================
    // SEARCH
    // =============================================
    public List<String> searchApartmentsWithAvailableRooms(String barangay) {

        List<String> list = new ArrayList<>();

        String sql = "SELECT apartment_name, rooms_available FROM apartments WHERE barangay=? AND rooms_available>0";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("apartment_name") +
                        " | Rooms: " + rs.getInt("rooms_available"));
            }

        } catch (Exception e) {
            System.out.println("Search Error: " + e.getMessage());
        }

        return list;
    }

    // =============================================
    // BARANGAYS
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
            System.out.println("Barangay Error: " + e.getMessage());
        }

        return list;
    }

    // =============================================
    // SUGGESTION
    // =============================================
    public Map<String, Object> searchWithSuggestion(String barangay) {

        Map<String, Object> map = new HashMap<>();

        List<String> result = searchApartmentsWithAvailableRooms(barangay);

        map.put("found", !result.isEmpty());
        map.put("apartments", result);

        if (result.isEmpty()) {
            map.put("suggestedBarangay", "None");
        }

        return map;
    }
}