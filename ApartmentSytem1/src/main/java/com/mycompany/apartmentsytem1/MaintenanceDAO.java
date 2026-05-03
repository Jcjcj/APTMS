package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.logging.Logger;

public class MaintenanceDAO {

    private static final Logger LOGGER = Logger.getLogger(MaintenanceDAO.class.getName());

    public boolean submitRequest(int apartmentId, String roomNumber, int tenantId,
                                 String issueDescription, String priorityLevel) {

        String sql = "INSERT INTO maintenance_requests("
                + "apartment_id, room_number, tenant_id, issue_description, priority_level, date_reported, date_updated) "
                + "VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setInt(3, tenantId);
            ps.setString(4, issueDescription);
            ps.setString(5, priorityLevel.toUpperCase());
            ps.setString(6, LocalDate.now().toString());
            ps.setString(7, LocalDate.now().toString());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.severe("Submit Request Error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateRequestStatus(int requestId, String newStatus) {
        return updateRequestStatus(requestId, newStatus, null);
    }

    public boolean updateRequestStatus(int requestId, String newStatus, String rejectionReason) {

        String sql = "UPDATE maintenance_requests "
                + "SET status = ?, rejection_reason = ?, date_resolved = ?, date_updated = ? "
                + "WHERE request_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String status = newStatus.toUpperCase();
            ps.setString(1, status);

            if (status.equals("REJECTED")) {
                ps.setString(2, rejectionReason);
                ps.setString(3, null); 
            } else {
                ps.setString(2, null);
                if (status.equals("APPROVED") || status.equals("RESOLVED")) {
                    ps.setString(3, LocalDate.now().toString()); 
                } else {
                    ps.setString(3, null);
                }
            }

            ps.setString(4, LocalDate.now().toString());
            ps.setInt(5, requestId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LOGGER.severe("Update Status Error: " + e.getMessage());
            return false;
        }
    }

    public void printActiveRequests(int apartmentId) {
        String sql = "SELECT * FROM maintenance_requests WHERE apartment_id = ? "
                   + "AND (status IS NULL OR status NOT IN ('RESOLVED', 'APPROVED', 'REJECTED')) " 
                   + "ORDER BY date_reported DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();

            boolean hasRequests = false;
            
            while (rs.next()) {
                hasRequests = true;
                String currentStatus = rs.getString("status");
                if(currentStatus == null) currentStatus = "PENDING (Default)";

                System.out.println(
                        "Req ID: " + rs.getInt("request_id")
                        + " | Room: " + rs.getString("room_number")
                        + " | Issue: " + rs.getString("issue_description")
                        + " | Priority: " + rs.getString("priority_level")
                        + " | Status: " + currentStatus
                );
            }
            
            if (!hasRequests) {
                System.out.println("   -> No active requests found in database.");
            }

        } catch (Exception e) {
            LOGGER.severe("Print Active Requests Error: " + e.getMessage());
        }
    }
}