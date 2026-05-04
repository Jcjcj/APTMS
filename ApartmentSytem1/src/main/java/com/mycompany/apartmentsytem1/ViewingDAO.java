package com.mycompany.apartmentsytem1;

import java.sql.*; 
import java.util.logging.Logger;

public class ViewingDAO {
    
    private static final Logger LOGGER = Logger.getLogger(ViewingDAO.class.getName());

    /**
     * Books a room viewing and automatically generates temporary login credentials.
     * Returns an array holding [Username, Password] so the UI can display them on the success screen.
     */
    public String[] bookRoomViewing(int apartmentId, String roomNumber, String tenantName, 
                                    String contactNumber, String scheduleDate) {
        
        // Removes spaces from name and makes it lowercase
        String baseUsername = tenantName.replaceAll("\\s+", "").toLowerCase();
        
        // Adds a few random numbers to ensure the username is unique
        String tempUsername = baseUsername + (int)(Math.random() * 1000); 
        
        // Generates a random 10-digit number string for the password
        String tempRawPassword = String.format("%010d", (long)(Math.random() * 10000000000L));
        
        // Hash the password for database security
        String hashedTempPassword = PasswordUtil.hashPassword(tempRawPassword);

        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, " +
                     "contact_number, schedule_date, viewing_time, status, temp_username, temp_password) " +
                     "VALUES(?,?,?,?,?,'7:00 AM - 4:00 PM','PENDING',?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, tenantName);
            ps.setString(4, contactNumber);
            ps.setString(5, scheduleDate);
            ps.setString(6, tempUsername);
            ps.setString(7, hashedTempPassword);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                LOGGER.info("Room viewing booked successfully for: " + tenantName);
                return new String[] { tempUsername, tempRawPassword }; 
            }

        } catch (Exception e) {
            LOGGER.severe("Viewing Booking Error: " + e.getMessage());
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
    public String[] getTemporaryUserDashboard(String tempUsername, String rawTempPassword) {
        
        String hashedInputPassword = PasswordUtil.hashPassword(rawTempPassword);

        String sql = "SELECT v.room_number, a.apartment_name, a.apartment_address, " +
                     "v.schedule_date, v.viewing_time, v.status, v.tenant_name " +
                     "FROM viewing_schedule v " +
                     "JOIN apartments a ON v.apartment_id = a.apartment_id " +
                     "WHERE v.temp_username = ? AND v.temp_password = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tempUsername);
            ps.setString(2, hashedInputPassword);

            // The error is gone because we imported java.sql.* at the top!
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[] {
                    rs.getString("room_number"),       
                    rs.getString("apartment_name"),    
                    rs.getString("apartment_address"), 
                    rs.getString("schedule_date"),     
                    rs.getString("viewing_time"),      
                    rs.getString("status"),            
                    rs.getString("tenant_name")        
                };
            }
            
        } catch (Exception e) {
            LOGGER.severe("Temp User Login Error: " + e.getMessage());
        }
        
        return null; 
    }
}