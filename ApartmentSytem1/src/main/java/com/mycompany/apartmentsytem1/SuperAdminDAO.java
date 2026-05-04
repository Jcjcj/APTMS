package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class SuperAdminDAO {
    private static final Logger LOGGER = Logger.getLogger(SuperAdminDAO.class.getName());

    public boolean login(String username, String password) {
        // DATA VALIDATION
        if (username == null || password == null || username.trim().isEmpty()) return false;

        String sql = "SELECT password FROM super_admins WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return PasswordUtil.checkPassword(password, rs.getString("password"));
            }
        } catch (Exception e) { 
            LOGGER.severe("Admin Login Error: " + e.getMessage()); 
        }
        return false;
    }

    // NEW: Super Admin disables an owner account (Soft Delete)
    public boolean deactivateOwner(int ownerId) {
        String sql = "UPDATE owners SET is_active = 0 WHERE owner_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                LOGGER.info("Super Admin deactivated Owner ID: " + ownerId);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe("Deactivation Error: " + e.getMessage());
        }
        return false;
    }

    public void printSystemOverview() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("\n--- SUPER ADMIN OVERVIEW ---");
            
            // Only count active accounts
            ResultSet rsOwners = stmt.executeQuery("SELECT COUNT(*) FROM owners WHERE is_active = 1");
            System.out.println("Total Active Apartment Owners: " + rsOwners.getInt(1));
            
            ResultSet rsApts = stmt.executeQuery("SELECT COUNT(*) FROM apartments WHERE is_active = 1");
            System.out.println("Total Active Apartments Listed: " + rsApts.getInt(1));
            
            ResultSet rsTenants = stmt.executeQuery("SELECT COUNT(*) FROM registered_tenants WHERE approval_status = 'APPROVED' AND is_active = 1");
            System.out.println("Total Active Tenants: " + rsTenants.getInt(1));
            
        } catch (Exception e) { 
            LOGGER.severe("Overview Error: " + e.getMessage()); 
        }
    }
    
    // NEW: Super Admin verifies the QR payment and approves the apartment
    public boolean approveApartmentRegistration(int apartmentId) {
        String sql = "UPDATE apartments SET approval_status = 'APPROVED' WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                LOGGER.info("Super Admin approved payment for Apartment ID: " + apartmentId);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe("Approval Error: " + e.getMessage());
        }
        return false;
    }
}