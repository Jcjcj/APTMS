package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.time.LocalDate;

public class RoomOccupancyDAO {

    public void assignTenantToRoom(int apartmentId, String roomNumber, int tenantId) {

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); 

            try {
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE room_occupancy SET status='Past', move_out_date=? WHERE apartment_id=? AND room_number=? AND status='Current'");
                ps1.setString(1, LocalDate.now().toString());
                ps1.setInt(2, apartmentId);
                ps1.setString(3, roomNumber);
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO room_occupancy(apartment_id, room_number, tenant_id, move_in_date, move_out_date, status) VALUES(?,?,?,?,NULL,'Current')");
                ps2.setInt(1, apartmentId);
                ps2.setString(2, roomNumber);
                ps2.setInt(3, tenantId);
                ps2.setString(4, LocalDate.now().toString());
                ps2.executeUpdate();

                PreparedStatement ps3 = conn.prepareStatement(
                        "UPDATE rooms SET status='Occupied' WHERE apartment_id=? AND room_number=?");
                ps3.setInt(1, apartmentId);
                ps3.setString(2, roomNumber);
                ps3.executeUpdate();

                PreparedStatement ps4 = conn.prepareStatement(
                        "UPDATE apartments SET rooms_available = rooms_available - 1 WHERE apartment_id=?");
                ps4.setInt(1, apartmentId);
                ps4.executeUpdate();

                conn.commit(); 

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Transaction Rollback: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Assign Tenant Error: " + e.getMessage());
        }
    }
}