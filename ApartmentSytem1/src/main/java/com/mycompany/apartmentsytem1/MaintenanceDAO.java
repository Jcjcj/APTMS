package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.logging.Logger;

public class MaintenanceDAO {
    private static final Logger LOGGER = Logger.getLogger(MaintenanceDAO.class.getName());

    public boolean submitRequest(int apartmentId, String roomNumber, int tenantId, String issueDescription, String priorityLevel) {
        String sql = "INSERT INTO maintenance_requests(apartment_id, room_number, tenant_id, issue_description, priority_level, date_reported) "
                   + "VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setInt(3, tenantId);
            ps.setString(4, issueDescription);
            ps.setString(5, priorityLevel.toUpperCase()); // LOW, MEDIUM, HIGH, EMERGENCY
            ps.setString(6, LocalDate.now().toString());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Submit Request Error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRequestStatus(int requestId, String newStatus) {
        String sql = "UPDATE maintenance_requests SET status = ?, date_resolved = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase()); // IN_PROGRESS, RESOLVED
            
            // Only stamp a resolved date if it is actually resolved
            if(newStatus.equalsIgnoreCase("RESOLVED")) {
                ps.setString(2, LocalDate.now().toString());
            } else {
                ps.setString(2, null);
            }
            
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Update Status Error: " + e.getMessage());
            return false;
        }
    }

    public void printActiveRequests(int apartmentId) {
        String sql = "SELECT * FROM maintenance_requests WHERE apartment_id = ? AND status != 'RESOLVED' ORDER BY priority_level ASC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            System.out.println("--- ACTIVE MAINTENANCE REQUESTS ---");
            while (rs.next()) {
                System.out.println("Req ID: " + rs.getInt("request_id") +
                                   " | Room: " + rs.getString("room_number") +
                                   " | Issue: " + rs.getString("issue_description") +
                                   " | Priority: " + rs.getString("priority_level") +
                                   " | Status: " + rs.getString("status"));
            }
        } catch (Exception e) {
            LOGGER.severe("Print Requests Error: " + e.getMessage());
        }
    }
}