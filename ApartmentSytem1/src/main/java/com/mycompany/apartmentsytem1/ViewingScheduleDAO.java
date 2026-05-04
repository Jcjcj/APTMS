package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Logger;

public class ViewingScheduleDAO {
    private static final Logger LOGGER = Logger.getLogger(ViewingScheduleDAO.class.getName());

 
    // MODIFIED FOR UI PAGE 4: Now requires roomNumber and a single specific viewing time
    public String[] scheduleViewing(int apartmentId, String roomNumber, String tenantName, String contact, String date, String time) {
        String tempUser = "view_" + UUID.randomUUID().toString().substring(0, 5);
        String tempPass = UUID.randomUUID().toString().substring(0, 6);

        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, contact_number, schedule_date, viewing_time, status, temp_username, temp_password) " +
                     "VALUES(?,?,?,?,?,?,'PENDING',?,?)";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, tenantName);
            ps.setString(4, contact);
            ps.setString(5, date);
            ps.setString(6, time);
            ps.setString(7, tempUser);
            ps.setString(8, PasswordUtil.hashPassword(tempPass)); 
            
            ps.executeUpdate();
            return new String[]{tempUser, tempPass}; 
            
        } catch (Exception e) {
            LOGGER.severe("Schedule Error: " + e.getMessage());
            return null;
        }
    }

    // MODIFIED FOR UI PAGE 5: Returns all Dashboard Text data instead of just status
    public String[] getTempUserDashboard(String tempUsername, String rawPassword) {
        String sql = "SELECT v.temp_password, v.tenant_name, v.room_number, a.apartment_name, a.apartment_address, v.schedule_date, v.viewing_time, v.status " +
                     "FROM viewing_schedule v " +
                     "JOIN apartments a ON v.apartment_id = a.apartment_id " +
                     "WHERE v.temp_username = ?";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tempUsername);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                if (PasswordUtil.checkPassword(rawPassword, rs.getString("temp_password"))) {
                    // Returns an array containing UI fields: [Name, Room, Apt Name, Location, Date, Time, Status]
                    return new String[] {
                        rs.getString("tenant_name"),
                        rs.getString("room_number"),
                        rs.getString("apartment_name"),
                        rs.getString("apartment_address"),
                        rs.getString("schedule_date"),
                        rs.getString("viewing_time"),
                        rs.getString("status")
                    };
                }
                return new String[]{"INVALID_PASSWORD"};
            }
            return new String[]{"USER_NOT_FOUND"};
        } catch (Exception e) { 
            return new String[]{"ERROR"}; 
        }
    }
    // 3. Owner Updates the Status from their dashboard
    public boolean updateScheduleStatus(int scheduleId, String newStatus) {
        String sql = "UPDATE viewing_schedule SET status = ? WHERE schedule_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase());
            ps.setInt(2, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            return false; 
        }
    }
}