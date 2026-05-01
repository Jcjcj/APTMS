package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.time.LocalDate;

public class RoomOccupancyDAO {

    public void assignTenantToRoom(int apartmentId, String roomNumber, int tenantId) {

        try (Connection conn = DBConnection.connect()) {

            conn.setAutoCommit(false);

            try {
                // mark old as past
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE room_occupancy SET status='Past', move_out_date=? "
                      + "WHERE apartment_id=? AND room_number=? AND status='Current'");

                ps1.setString(1, LocalDate.now().toString());
                ps1.setInt(2, apartmentId);
                ps1.setString(3, roomNumber);
                ps1.executeUpdate();

                // insert new
                PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO room_occupancy(apartment_id, room_number, tenant_id, move_in_date, move_out_date, status) "
                      + "VALUES(?,?,?,?,NULL,'Current')");

                ps2.setInt(1, apartmentId);
                ps2.setString(2, roomNumber);
                ps2.setInt(3, tenantId);
                ps2.setString(4, LocalDate.now().toString());
                ps2.executeUpdate();

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}