package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDAO {

    public void loginOwner(String username, String password) {
        
        // add ang log in formmm mwuahhh
        //// BUTTON:
        // btnLogin → calls loginOwner() 
        // para mo run ang backend

        String sql = "SELECT * FROM owners WHERE username=? AND password=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid login!");
            }

        } catch (Exception e) {
            System.out.println("Login Error: " + e.getMessage());
        }
    }
}