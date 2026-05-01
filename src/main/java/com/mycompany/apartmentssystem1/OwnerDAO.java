package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OwnerDAO {

    public void registerOwner(String name,
                              String contactNumber,
                              String email,
                              String address,
                              String username,
                              String password) {

        String sql = "INSERT INTO owners(name, contact_number, email, address, username, password) "
                   + "VALUES(?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, contactNumber);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, username);

            // FIX: hashed password
            ps.setString(6, PasswordUtil.hashPassword(password));

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Owner Register Error: " + e.getMessage());
        }
    }
}