package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TenantDAO {

    //  REGISTER TENANT
    
    public void registerTenant(String name,
                               String contact,
                               String email,
                               String address,
                               String emergencyContact,
                               String username,
                               String password,
                               String validId) {

        // TENANT REGISTER FORM HERE
        
        // txtName
        // txtContact
        // txtEmail
        // txtAddress
        // txtEmergencyContact
        // txtUsername
        // txtPassword
        // uploadValidID
        //
        // BUTTON:
        // btnRegisterTenant ->CALL THIS METHOD para mo run ang back end 
        

        String sql = "INSERT INTO tenants(name, contact_number, email, address, emergency_contact, username, password, valid_id) "
                   + "VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, emergencyContact);
            ps.setString(6, username);
            ps.setString(7, password);
            ps.setString(8, validId);

            ps.executeUpdate();
            System.out.println("Tenant registered!");

        } catch (Exception e) {
            System.out.println("Tenant Register Error: " + e.getMessage());
        }
    }

    //  LOGIN TENANT
    public void loginTenant(String username, String password) {

        //  (TENANT LOGIN) HERE
        //
        // txtUsername
        // txtPassword
        //
        // BUTTON:
        // btnLoginTenant → CALL THIS METHOD

        String sql = "SELECT * FROM tenants WHERE username=? AND password=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Tenant login successful!");
            } else {
                System.out.println("Invalid tenant login!");
            }

        } catch (Exception e) {
            System.out.println("Tenant Login Error: " + e.getMessage());
        }
    }
}