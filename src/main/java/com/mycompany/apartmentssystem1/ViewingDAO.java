package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ViewingDAO {

    // =============================================
    // get available time slots for a given apartment and date
    // frontend: call when user selects date from calendar
    // returns list of {start, end} slots that are NOT yet booked
    // =============================================
    public List<String[]> getAvailableSlots(int apartmentId, String date) {
        List<String[]> available = new ArrayList<>();
        List<String[]> allSlots = TimeSlotHelper.getAllSlots(); // predefined 1‑hour slots

        try (Connection conn = DBConnection.connect()) {
            for (String[] slot : allSlots) {
                String start = slot[0];
                // check if this slot is already taken
                String sql = "SELECT * FROM viewing_schedule WHERE apartment_id=? AND schedule_date=? AND start_time=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, apartmentId);
                ps.setString(2, date);
                ps.setString(3, start);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { // not booked → available
                    available.add(slot);
                }
            }
        } catch (Exception e) {
            System.out.println("Get Available Slots Error: " + e.getMessage());
        }
        return available;
    }

    // =============================================
    // schedule a viewing
    // frontend: call when "Schedule" button clicked
    // returns true if successful, false otherwise (past date/time, double‑booking)
    // =============================================
    public boolean scheduleViewing(int apartmentId, String name, String contact,
                                   String date, String startTime, String endTime) {
        try (Connection conn = DBConnection.connect()) {

            // 1. no past dates allowed
            if (LocalDate.parse(date).isBefore(LocalDate.now())) {
                System.out.println("Cannot book past date.");
                return false;
            }

            // 2. if today, cannot book a time that already passed
            if (date.equals(LocalDate.now().toString())) {
                if (LocalTime.parse(startTime).isBefore(LocalTime.now())) {
                    System.out.println("Cannot book past time.");
                    return false;
                }
            }

            // 3. double‑booking check – ensure no existing booking for same slot
            String checkSql = "SELECT * FROM viewing_schedule WHERE apartment_id=? AND schedule_date=? AND start_time=?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setInt(1, apartmentId);
            psCheck.setString(2, date);
            psCheck.setString(3, startTime);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                System.out.println("Time slot already booked.");
                return false;
            }

            // 4. insert the booking
            String insertSql = "INSERT INTO viewing_schedule(apartment_id, tenant_name, contact_number, schedule_date, start_time, end_time) "
                             + "VALUES(?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(insertSql);
            ps.setInt(1, apartmentId);      // which apartment
            ps.setString(2, name);           // frontend: txtName
            ps.setString(3, contact);        // frontend: txtContact
            ps.setString(4, date);           // frontend: from calendar
            ps.setString(5, startTime);      // frontend: selected slot start
            ps.setString(6, endTime);        // frontend: selected slot end
            ps.executeUpdate();

            System.out.println("Viewing scheduled successfully.");
            return true;

        } catch (Exception e) {
            System.out.println("Schedule Viewing Error: " + e.getMessage());
            return false;
        }
    }
}