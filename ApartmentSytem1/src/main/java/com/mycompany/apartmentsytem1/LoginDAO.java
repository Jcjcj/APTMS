package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class LoginDAO {
    private static final Logger LOGGER = Logger.getLogger(LoginDAO.class.getName());

    public boolean loginOwner(String username, String password) {
        String sql = "SELECT password FROM owners WHERE username=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (PasswordUtil.checkPassword(password, rs.getString("password"))) return true;
            }
            return false;
        } catch (Exception e) { return false; }
    }

    public String loginTenant(String username, String password) {
        // We added tenant_id to the SELECT query
        String sql = "SELECT tenant_id, password, approval_status FROM registered_tenants WHERE username=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (PasswordUtil.checkPassword(password, rs.getString("password"))) {
                    if (rs.getString("approval_status").equals("APPROVED")) {
                        // Return SUCCESS + the actual ID (e.g., "SUCCESS:5")
                        return "SUCCESS:" + rs.getInt("tenant_id"); 
                    } else {
                        return "Your registration is " + rs.getString("approval_status");
                    }
                }
                return "Invalid password.";
            }
            return "Username not found.";
        } catch (Exception e) { return "System error."; }
    }
}