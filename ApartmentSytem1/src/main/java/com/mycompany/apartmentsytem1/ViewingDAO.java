package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ViewingDAO {

    public List<String[]> getAvailableSlots(int apartmentId, String date) {
        List<String[]> available = new ArrayList<>();
        List<String[]> allSlots = TimeSlotHelper.getAllSlots();

        try (Connection conn = DBConnection.connect()) {
            for (String[] slot : allSlots) {
                String start = slot[0];
                String sql = "SELECT * FROM viewing_schedule WHERE apartment_id=? AND schedule_date=? AND start_time=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, apartmentId);
                ps.setString(2, date);
                ps.setString(3, start);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { 
                    available.add(slot);
                }
            }
        } catch (Exception e) {
            System.out.println("Get Available Slots Error: " + e.getMessage());
        }
        return available;
    }

    public boolean scheduleViewing(int apartmentId, String name, String contact, String date, String startTime, String endTime) {
        try (Connection conn = DBConnection.connect()) {
            if (LocalDate.parse(date).isBefore(LocalDate.now())) return false;
            
            String insertSql = "INSERT INTO viewing_schedule(apartment_id, tenant_name, contact_number, schedule_date, start_time, end_time, status) "
                             + "VALUES(?,?,?,?,?,?,'SCHEDULED')";
            PreparedStatement ps = conn.prepareStatement(insertSql);
            ps.setInt(1, apartmentId);      
            ps.setString(2, name);          
            ps.setString(3, contact);       
            ps.setString(4, date);          
            ps.setString(5, startTime);     
            ps.setString(6, endTime);       
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // THIS IS THE METHOD APARTMENTSSYSTEM1 IS LOOKING FOR
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
}