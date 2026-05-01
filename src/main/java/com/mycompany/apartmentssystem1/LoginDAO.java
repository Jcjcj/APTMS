package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDAO {

    // OWNER LOGIN
    public boolean loginOwner(String username, String password) {

        String sql = "SELECT password FROM owners WHERE username=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (PasswordUtil.checkPassword(password, storedHash)) {
                    System.out.println("Owner login success!");
                    return true;
                }
            }

            System.out.println("Invalid login!");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TENANT LOGIN
    public boolean loginTenant(String username, String password) {

        String sql = "SELECT password FROM tenants_base WHERE username=? AND is_active=1";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                if (PasswordUtil.checkPassword(password, storedHash)) {
                    System.out.println("Tenant login success!");
                    return true;
                }
            }

            System.out.println("Invalid or inactive account!");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}