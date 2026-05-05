package com.mycompany.apartmentsytem1;

import java.sql.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ViewingDAO {
    
    private static final Logger LOGGER = Logger.getLogger(ViewingDAO.class.getName());

    /**
     * Books a room viewing and automatically generates temporary login credentials.
     * Returns an array holding [Username, Password] so the UI can display them on the success screen.
     */
   public String[] bookRoomViewing(int apartmentId, String roomNumber, String tenantName, 
                                     String contactNumber, String scheduleDate, String viewingTime) { // <-- Added viewingTime parameter
                                     
        String baseUsername = tenantName.replaceAll("\\s+", "").toLowerCase();
        String tempUsername = baseUsername + (int)(Math.random() * 1000); 
        String tempRawPassword = String.format("%010d", (long)(Math.random() * 10000000000L));
        String hashedTempPassword = PasswordUtil.hashPassword(tempRawPassword);

        // Fixed the SQL to insert the exact viewing_time parameter
        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, " +
                     "contact_number, schedule_date, viewing_time, status, temp_username, temp_password) " +
                     "VALUES(?,?,?,?,?,?, 'PENDING',?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, tenantName);
            ps.setString(4, contactNumber);
            ps.setString(5, scheduleDate);
            ps.setString(6, viewingTime); // <-- Saving the specific slot (e.g., "08:00 - 09:00")
            ps.setString(7, tempUsername);
            ps.setString(8, hashedTempPassword);

            if (ps.executeUpdate() > 0) {
                return new String[] { tempUsername, tempRawPassword }; 
            }
        } catch (Exception e) {
            System.out.println("Viewing Booking Error: " + e.getMessage());
        }
        return null; 
    }

    /**
     * Updates the status of a viewing (e.g., to "APPROVED" or "COMPLETED")
     */
    public boolean updateViewingStatus(int scheduleId, String status) {
        String sql = "UPDATE viewing_schedule SET status = ? WHERE schedule_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase());
            ps.setInt(2, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Authenticates a temporary user and retrieves all the data needed for their Dashboard.
     */
    /**
     * Authenticates a temporary user and retrieves all the data needed for their Dashboard.
     */
    /**
     * Authenticates a temporary user and retrieves all the data needed for their Dashboard.
     */
    public String[] getTemporaryUserDashboard(String tempUsername, String rawTempPassword) {
        
        // FIX: Replaced 'a.apartment_address' with 'a.street, a.barangay'
        String sql = "SELECT v.temp_password, v.room_number, a.apartment_name, a.street, a.barangay, " +
                     "v.schedule_date, v.viewing_time, v.status, v.tenant_name " +
                     "FROM viewing_schedule v " +
                     "JOIN apartments a ON v.apartment_id = a.apartment_id " +
                     "WHERE v.temp_username = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tempUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("temp_password");
                
                if (PasswordUtil.checkPassword(rawTempPassword, storedHash)) {
                    // Combine street and barangay into a single address string for the UI
                    String fullAddress = rs.getString("street") + ", " + rs.getString("barangay");
                    
                    return new String[] {
                        rs.getString("room_number"),       
                        rs.getString("apartment_name"),    
                        fullAddress,                       // <-- Send the combined address here
                        rs.getString("schedule_date"),     
                        rs.getString("viewing_time"),      
                        rs.getString("status"),            
                        rs.getString("tenant_name")        
                    };
                }
            }
            
        } catch (Exception e) {
            LOGGER.severe("Temp User Login Error: " + e.getMessage());
        }
        
        return null; 
    }
    
    /**
     * Rejects a room viewing and saves the reason so the temporary user can see it.
     */
    public boolean rejectViewing(int scheduleId, String reason) {
        String sql = "UPDATE viewing_schedule SET status = 'REJECTED', rejection_reason = ? WHERE schedule_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, reason);
            ps.setInt(2, scheduleId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.severe("Reject Viewing Error: " + e.getMessage());
            return false;
        }
    }
    
    // Fetches only the timeslots that are NOT yet booked for a specific room on a specific date
    public List<String> getAvailableTimeSlots(int apartmentId, String roomNumber, String scheduleDate) {
        List<String> availableSlots = new ArrayList<>();
        
        // 1. Get all 7 default slots from your helper
        for (String[] slot : TimeSlotHelper.getAllSlots()) {
            availableSlots.add(slot[0] + " - " + slot[1]);
        }

        // 2. Look up which slots are already taken on that date
        String sql = "SELECT viewing_time FROM viewing_schedule " +
                     "WHERE apartment_id = ? AND room_number = ? AND schedule_date = ? AND status != 'REJECTED'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, scheduleDate);
            
            ResultSet rs = ps.executeQuery();
            
            // 3. Remove the booked slots from the available list
            while (rs.next()) {
                availableSlots.remove(rs.getString("viewing_time"));
            }
        } catch (Exception e) {
            System.out.println("Timeslot Error: " + e.getMessage());
        }
        
        return availableSlots; // Sends only the remaining free slots to the dropdown
    }
    
    public List<String> getBookedTimes(int apartmentId, String roomNumber, String date) {
        List<String> bookedTimes = new ArrayList<>();
            // FIX: Added "AND status != 'REJECTED'" so rejected times become available again!
        String sql = "SELECT viewing_time FROM room_viewings WHERE apartment_id = ? AND room_number = ? AND viewing_date = ? AND status != 'REJECTED'";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookedTimes.add(rs.getString("viewing_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookedTimes;
    }
    
       // NEW: Deletes the temporary viewing record once the tenant officially moves in
    public void cleanupTemporaryAccount(int apartmentId, String tenantName) {
        String sql = "DELETE FROM viewing_schedule WHERE apartment_id = ? AND tenant_name = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, tenantName);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Cleanup Error: " + e.getMessage());
        }
    }
}