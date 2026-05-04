package com.mycompany.apartmentsytem1;

// THIS IS THE FIX: The * imports everything, including ResultSet!
import java.sql.*; 
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
            ps.setString(7, PasswordUtil.hashPassword(password)); // Hashes the password on registration
            ps.setString(8, validId);
            ps.executeUpdate();
            LOGGER.info("Tenant registered successfully and is PENDING: " + username);

        } catch (Exception e) {
            LOGGER.severe("Tenant Error: " + e.getMessage());
        }
    }

    // Allows a tenant to change their password securely
    public boolean changePassword(int tenantId, String newPassword) {
        String sql = "UPDATE registered_tenants SET password = ? WHERE tenant_id = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // We use PasswordUtil here so it matches the security of your register method!
            ps.setString(1, PasswordUtil.hashPassword(newPassword)); 
            ps.setInt(2, tenantId);
            
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (Exception e) {
            LOGGER.severe("Tenant Password Update Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Registers a permanent tenant from the Move-In UI and links them to the requested room.
     * Automatically looks up the apartment_id based on the apartment name.
     */
    public boolean registerPermanentTenant(String name, String address, String contact, String email, 
                                           String emergencyContact, String apartmentName, String roomNumber, 
                                           String moveInDate, int occupants, String username, String rawPassword) {

        // 1. Hash the password for maximum security
        String hashedInputPassword = PasswordUtil.hashPassword(rawPassword);
        int apartmentId = -1;

        try (Connection conn = DBConnection.connect()) {
            
            // 2. Look up the Apartment ID based on the text box in your UI
            String findApt = "SELECT apartment_id FROM apartments WHERE apartment_name = ?";
            try (PreparedStatement psApt = conn.prepareStatement(findApt)) {
                psApt.setString(1, apartmentName);
                
                // The error is gone because we imported java.sql.* at the top!
                ResultSet rsApt = psApt.executeQuery();
                if (rsApt.next()) {
                    apartmentId = rsApt.getInt("apartment_id");
                }
            }

            // If they typed an apartment name that doesn't exist, stop and return false
            if (apartmentId == -1) {
                LOGGER.warning("Registration Failed: Apartment not found - " + apartmentName);
                return false; 
            }

            // 3. Save everything to the updated registered_tenants table!
            String sql = "INSERT INTO registered_tenants(name, address, contact_number, email, " +
                         "emergency_contact, target_apartment_id, target_room_number, move_in_date, occupants, " +
                         "username, password, approval_status) " +
                         "VALUES(?,?,?,?,?,?,?,?,?,?,?,'PENDING')";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, address);
                ps.setString(3, contact);
                ps.setString(4, email);
                ps.setString(5, emergencyContact);
                ps.setInt(6, apartmentId);
                ps.setString(7, roomNumber);
                ps.setString(8, moveInDate);
                ps.setInt(9, occupants);
                ps.setString(10, username);
                ps.setString(11, hashedInputPassword);

                int rowsUpdated = ps.executeUpdate();
                
                if (rowsUpdated > 0) {
                    LOGGER.info("Permanent Tenant Registered (Pending Approval): " + username);
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Tenant Registration Error: " + e.getMessage());
        }
        return false;
    }
}