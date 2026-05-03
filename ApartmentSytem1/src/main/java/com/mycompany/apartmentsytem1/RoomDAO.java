package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RoomDAO {
    private static final Logger LOGGER = Logger.getLogger(RoomDAO.class.getName());

    public List<String> getRoomsByApartment(int apartmentId) {
        List<String> rooms = new ArrayList<>();
        
        String sql = "SELECT room_number, status, rent_amount, capacity_text, utilities_text, design_text, image_url " +
                     "FROM rooms WHERE apartment_id = ? ORDER BY room_number ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String roomDetails = String.format("Room: %s | Status: %s | Rent: PHP %.2f | Capacity: %s | Utilities: %s | Design: %s",
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("rent_amount"),
                        rs.getString("capacity_text"),
                        rs.getString("utilities_text"),
                        rs.getString("design_text")
                );
                rooms.add(roomDetails);
            }
        } catch (Exception e) {
            LOGGER.severe("Get Rooms Error: " + e.getMessage());
        }
        return rooms;
    }

    public boolean updateRoomDetails(int apartmentId, String roomNumber, String newCapacity, String newUtilities, String newDesign, String newImageUrl) {
        String sql = "UPDATE rooms SET capacity_text = ?, utilities_text = ?, design_text = ?, image_url = ? WHERE apartment_id = ? AND room_number = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newCapacity);
            ps.setString(2, newUtilities);
            ps.setString(3, newDesign);
            ps.setString(4, newImageUrl);
            ps.setInt(5, apartmentId);
            ps.setString(6, roomNumber);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Update Room Error: " + e.getMessage());
            return false;
        }
    }
}