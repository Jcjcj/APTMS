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
    
    // Returns only rooms that are "Available" AND have at least 1 free timeslot on the requested date
    public List<String> getAvailableRoomsOnDate(int apartmentId, String scheduleDate) {
        List<String> rooms = new ArrayList<>();
        
        // This query hides any room that appears in the viewing_schedule table 7 or more times for the selected date
        String sql = "SELECT r.room_number, r.rent_amount, r.capacity_text " +
                     "FROM rooms r " +
                     "WHERE r.apartment_id = ? AND r.status = 'Available' " +
                     "AND r.room_number NOT IN (" +
                     "    SELECT room_number FROM viewing_schedule " +
                     "    WHERE apartment_id = ? AND schedule_date = ? AND status != 'REJECTED' " +
                     "    GROUP BY room_number " +
                     "    HAVING COUNT(viewing_time) >= 7" + // 7 is the total number of slots in TimeSlotHelper
                     ") ORDER BY r.room_number ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, apartmentId);
            ps.setInt(2, apartmentId);
            ps.setString(3, scheduleDate);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                rooms.add("Room: " + rs.getString("room_number") + 
                          " | Rent: PHP " + rs.getDouble("rent_amount") + 
                          " | Capacity: " + rs.getString("capacity_text"));
            }
        } catch (Exception e) {
            System.out.println("Get Available Rooms Error: " + e.getMessage());
        }
        
        return rooms;
    }
}