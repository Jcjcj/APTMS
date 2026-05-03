package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public class TenantDAO {
    private static final Logger LOGGER = Logger.getLogger(TenantDAO.class.getName());

    public void registerTenant(String name, String contact, String email, String address, String emergency,
                               String username, String password, String validId) {

        String sql = "INSERT INTO registered_tenants(name, contact_number, email, address, emergency_contact, username, password, valid_id, approval_status, moved_out_date) "
                   + "VALUES(?,?,?,?,?,?,?,?,'PENDING',NULL)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, emergency);
            ps.setString(6, username);
            ps.setString(7, PasswordUtil.hashPassword(password));
            ps.setString(8, validId);
            ps.executeUpdate();
            LOGGER.info("Tenant registered successfully and is PENDING: " + username);

        } catch (Exception e) {
            LOGGER.severe("Tenant Error: " + e.getMessage());
        }
    }

    public boolean changePassword(int tenantId, String newPassword) {
        String sql = "UPDATE registered_tenants SET password = ? WHERE tenant_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, tenantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}