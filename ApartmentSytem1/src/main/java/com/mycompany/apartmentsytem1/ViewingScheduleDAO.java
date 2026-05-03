package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Logger;

public class ViewingScheduleDAO {
    private static final Logger LOGGER = Logger.getLogger(ViewingScheduleDAO.class.getName());

    // 1. Tenant schedules a viewing and receives a Temporary Account
    public String[] scheduleViewing(int apartmentId, String tenantName, String contact, String date, String startTime, String endTime) {
        String tempUser = "view_" + UUID.randomUUID().toString().substring(0, 5);
        String tempPass = UUID.randomUUID().toString().substring(0, 6);

        String sql = "INSERT INTO viewing_schedule(apartment_id, tenant_name, contact_number, schedule_date, start_time, end_time, status, temp_username, temp_password) " +
                     "VALUES(?,?,?,?,?,?,'PENDING',?,?)";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ps.setString(2, tenantName);
            ps.setString(3, contact);
            ps.setString(4, date);
            ps.setString(5, startTime);
            ps.setString(6, endTime);
            ps.setString(7, tempUser);
            ps.setString(8, PasswordUtil.hashPassword(tempPass)); 
            
            ps.executeUpdate();
            LOGGER.info("Viewing Scheduled. Temp Account Created: " + tempUser);
            
            return new String[]{tempUser, tempPass}; 
            
        } catch (Exception e) {
            LOGGER.severe("Schedule Error: " + e.getMessage());
            return null;
        }
    }

    // 2. Temp Account Login to check Status
    public String checkViewingStatus(String tempUsername, String rawPassword) {
        String sql = "SELECT temp_password, status FROM viewing_schedule WHERE temp_username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tempUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (PasswordUtil.checkPassword(rawPassword, rs.getString("temp_password"))) {
                    return rs.getString("status");
                }
                return "INVALID_PASSWORD";
            }
            return "USER_NOT_FOUND";
        } catch (Exception e) { 
            return "ERROR"; 
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