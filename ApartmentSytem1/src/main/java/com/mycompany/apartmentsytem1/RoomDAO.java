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
        // MODIFIED: Select rent_amount
        String sql = "SELECT room_number, status, rent_amount, description, image_url FROM rooms WHERE apartment_id = ? ORDER BY room_number ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rooms.add("Room: " + rs.getString("room_number") + 
                          " | Rent: " + rs.getDouble("rent_amount") + // NEW
                          " | Status: " + rs.getString("status") + 
                          " | Desc: " + rs.getString("description"));
            }
        } catch (Exception e) {
            LOGGER.severe("Get Rooms Error: " + e.getMessage());
        }
        return rooms;
    }

    public boolean updateRoomDetails(int apartmentId, String roomNumber, String newDescription, String newImageUrl) {
        String sql = "UPDATE rooms SET description = ?, image_url = ? WHERE apartment_id = ? AND room_number = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newDescription);
            ps.setString(2, newImageUrl);
            ps.setInt(3, apartmentId);
            ps.setString(4, roomNumber);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Update Room Error: " + e.getMessage());
            return false;
        }
    }
}