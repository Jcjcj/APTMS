package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public class OwnerDAO {
    private static final Logger LOGGER = Logger.getLogger(OwnerDAO.class.getName());

    public void registerOwner(String name, String contactNumber, String email, String address, String username, String password) {
        String sql = "INSERT INTO owners(name, contact_number, email, address, username, password) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, contactNumber);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, username);
            ps.setString(6, PasswordUtil.hashPassword(password));
            ps.executeUpdate();
            LOGGER.info("Owner registered successfully: " + username);
        } catch (Exception e) {
            LOGGER.severe("Owner Register Error: " + e.getMessage());
        }
    }

    // THIS IS THE METHOD APARTMENTSSYSTEM1 IS LOOKING FOR
    public boolean updateTenantStatus(int tenantId, String status) {
        String sql = "UPDATE registered_tenants SET approval_status = ? WHERE tenant_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase());
            ps.setInt(2, tenantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}