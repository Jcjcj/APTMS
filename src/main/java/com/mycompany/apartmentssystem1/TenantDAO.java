package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class TenantDAO {

    public void registerTenant(String name,
                               String contact,
                               String email,
                               String address,
                               String emergency,
                               String username,
                               String password,
                               String validId) {

        String sql = "INSERT INTO tenants_base(name, contact_number, email, address, emergency_contact, username, password, valid_id, moved_out_date) "
                   + "VALUES(?,?,?,?,?,?,?,?,NULL)";

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

        } catch (Exception e) {
            System.out.println("Tenant Error: " + e.getMessage());
        }
    }
}