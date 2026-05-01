package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ViewingDAO {

    public void scheduleViewing(int apartmentId,
                                String name,
                                String contact,
                                String date,
                                String time) {

        //  (TENANT VIEW PAGE)
        //
        // calendarDatePicker
        // timeDropdown
        // txtName
        // txtContact
        //
        // BUTTON:
        // btnScheduleViewing → CALL THIS METHOD

        String sql = "INSERT INTO viewing_schedule(apartment_id, tenant_name, contact_number, schedule_date, schedule_time) "
                   + "VALUES(?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ps.setString(2, name);
            ps.setString(3, contact);
            ps.setString(4, date);
            ps.setString(5, time);

            ps.executeUpdate();
            System.out.println("Viewing scheduled!");

        } catch (Exception e) {
            System.out.println("Schedule Error: " + e.getMessage());
        }
    }
}