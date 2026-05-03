package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class ApartmentDAO {

    private static final Logger LOGGER = Logger.getLogger(ApartmentDAO.class.getName());

    private String generateApartmentCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // MODIFIED: Accepts List for rooms per floor and List of lists for individual room prices
    public void addApartment(String apartmentCode, String name, String tin, int floors, 
                               List<Integer> roomsPerFloorList, List<List<Double>> rentPricesPerFloor,
                               double down, String paymentMethod, String description, String policy,
                               String barangay, String street, String electricity, String water, String internet,
                               String contact, String email, String social, String emergency, String profileImage,
                               int ownerId) {

        if (apartmentCode == null || apartmentCode.isEmpty()) {
            apartmentCode = generateApartmentCode();
        }

        // Calculate total rooms by summing the list values
        int totalRooms = 0;
        for (int count : roomsPerFloorList) {
            totalRooms += count;
        }

        String sql = "INSERT INTO apartments(" +
                "apartment_code, apartment_name, owner_id, tin_no, floors, total_rooms, rooms_available, " +
                "down_payment, payment_method, description, policy, barangay, street, electricity, water, internet, " +
                "contact_number, email, social_media, emergency_number, profile_image) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, apartmentCode);
            ps.setString(2, name);
            ps.setInt(3, ownerId);
            ps.setString(4, tin);
            ps.setInt(5, floors);
            ps.setInt(6, totalRooms);
            ps.setInt(7, totalRooms);
            ps.setDouble(8, down);
            ps.setString(9, paymentMethod);
            ps.setString(10, description);
            ps.setString(11, policy);
            ps.setString(12, barangay);
            ps.setString(13, street);
            ps.setString(14, electricity);
            ps.setString(15, water);
            ps.setString(16, internet);
            ps.setString(17, contact);
            ps.setString(18, email);
            ps.setString(19, social);
            ps.setString(20, emergency);
            ps.setString(21, profileImage);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newApartmentId = rs.getInt(1);
                // Call the new custom generator logic
                generateCustomRooms(conn, newApartmentId, roomsPerFloorList, rentPricesPerFloor);
            }

            LOGGER.info("Added Apartment: " + name + " (Generated " + totalRooms + " custom rooms)");

        } catch (Exception e) {
            LOGGER.severe("Apartment Error: " + e.getMessage());
        }
    }

    // MODIFIED: Logic handles varying rooms per floor and specific pricing per room
    private void generateCustomRooms(Connection conn, int apartmentId, 
                                     List<Integer> roomsPerFloorList, 
                                     List<List<Double>> rentPricesPerFloor) {
        
        String roomSql = "INSERT INTO rooms(apartment_id, room_number, rent_amount) VALUES(?, ?, ?)";
        try (PreparedStatement psRoom = conn.prepareStatement(roomSql)) {
            
            // Loop through floors
            for (int f = 0; f < roomsPerFloorList.size(); f++) {
                int floorNum = f + 1;
                int roomsOnThisFloor = roomsPerFloorList.get(f);
                List<Double> pricesForThisFloor = rentPricesPerFloor.get(f);

                // Loop through rooms on this specific floor
                for (int r = 1; r <= roomsOnThisFloor; r++) {
                    String roomNum = String.format("%d%02d", floorNum, r);
                    
                    // Get specific price if provided, otherwise default to 0.0
                    double price = (r <= pricesForThisFloor.size()) ? pricesForThisFloor.get(r-1) : 0.0;

                    psRoom.setInt(1, apartmentId);
                    psRoom.setString(2, roomNum);
                    psRoom.setDouble(3, price);
                    psRoom.addBatch();
                }
            }
            psRoom.executeBatch();
        } catch (Exception e) {
            LOGGER.severe("Room Generation Error: " + e.getMessage());
        }
    }

    public List<String> searchApartmentsWithAvailableRooms(String barangay) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT apartment_name, total_rooms, rooms_available FROM apartments WHERE barangay=? AND rooms_available>0";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("apartment_name") + " | Total Rooms: " + rs.getInt("total_rooms") + " | Available: " + rs.getInt("rooms_available"));
            }
        } catch (Exception e) {
            LOGGER.severe("Search Error: " + e.getMessage());
        }
        return list;
    }

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
            LOGGER.severe("Barangay Error: " + e.getMessage());
        }
        return list;
    }
}