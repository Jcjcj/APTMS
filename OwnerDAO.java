package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OwnerDAO {

    public void registerOwner(String name, String contactNumber,
                              String email, String address,
                              String username, String password) {
        
        //// add ang register form here
        /// 
        /// dito lang jud
        ///// BUTTON:
        // btnAddApartment → calls this method 
        // para daw na mo run ang backend

        String sql = "INSERT INTO owners(name, contact_number, email, address, username, password) "
                   + "VALUES(?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, contactNumber);
            pstmt.setString(3, email);
            pstmt.setString(4, address);
            pstmt.setString(5, username);
            pstmt.setString(6, password);

            pstmt.executeUpdate();
            System.out.println("Owner saved successfully!");

        } catch (Exception e) {
            System.out.println("Register Error: " + e.getMessage());
        }
    }
}