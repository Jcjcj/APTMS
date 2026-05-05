package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OwnerDAO {
    private static final Logger LOGGER = Logger.getLogger(OwnerDAO.class.getName());

   public int registerOwner(String name, String contactNumber, String email, String address, String emergency, String validId, String username, String password) {
        // DATA VALIDATION
        if (name == null || name.trim().isEmpty() || username == null || username.trim().isEmpty() || password == null || password.length() < 6) {
            LOGGER.warning("Validation Failed: Missing required owner details or password too short.");
            return -1; // -1 means failure
        }
        
        String sql = "INSERT INTO owners(name, contact_number, email, address, emergency_number, valid_id, username, password, is_active) VALUES(?,?,?,?,?,?,?,?,1)";
        
        try (Connection conn = DBConnection.connect();
             // CRITICAL: Request the generated keys!
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, name);
            ps.setString(2, contactNumber);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, emergency);
            ps.setString(6, validId);
            ps.setString(7, username);
            ps.setString(8, PasswordUtil.hashPassword(password));
            ps.executeUpdate();
            
            // Catch the new Owner ID and return it to the frontend
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newOwnerId = rs.getInt(1);
                LOGGER.info("Owner registered successfully: " + username + " | ID: " + newOwnerId);
                return newOwnerId; 
            }
        } catch (Exception e) {
            LOGGER.severe("Owner Register Error: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateTenantStatus(int tenantId, String status) {
        String sql = "UPDATE registered_tenants SET approval_status = ? WHERE tenant_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase());
            ps.setInt(2, tenantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String[] generateInviteForExistingTenant(String tenantName) {
        
        // DATA VALIDATION
        if (tenantName == null || tenantName.trim().isEmpty()) return null;

        String tempUser = "tenant_" + java.util.UUID.randomUUID().toString().substring(0, 5);
        String tempPass = java.util.UUID.randomUUID().toString().substring(0, 6);

        String sql = "INSERT INTO registered_tenants(name, username, password, approval_status, is_active) VALUES(?,?,?,'INVITED',1)";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, tenantName);
            ps.setString(2, tempUser);
            ps.setString(3, PasswordUtil.hashPassword(tempPass));
            ps.executeUpdate();
            
            LOGGER.info("Generated invite credentials for existing tenant: " + tenantName);
            return new String[]{tempUser, tempPass}; 
        } catch (Exception e) {
            LOGGER.severe("Invite Error: " + e.getMessage());
            return null;
        }
    }

    // The Owner-Controlled Move Out & Delete Account Logic
    public boolean removeAndDeactivateTenant(int tenantId, int apartmentId, String roomNumber) {
        String updateRoom = "UPDATE rooms SET status = 'Available' WHERE apartment_id = ? AND room_number = ?";
        String updateApt = "UPDATE apartments SET rooms_available = rooms_available + 1 WHERE apartment_id = ?";
        String updateOccupancy = "UPDATE room_occupancy SET status = 'Past', move_out_date = date('now') WHERE tenant_id = ? AND room_number = ? AND status = 'Current'";
        // Soft Delete the tenant account
        String deactivateTenant = "UPDATE registered_tenants SET is_active = 0, approval_status = 'MOVED_OUT', moved_out_date = date('now') WHERE tenant_id = ?";

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); // Transaction

            try (PreparedStatement psRoom = conn.prepareStatement(updateRoom);
                 PreparedStatement psApt = conn.prepareStatement(updateApt);
                 PreparedStatement psOcc = conn.prepareStatement(updateOccupancy);
                 PreparedStatement psTen = conn.prepareStatement(deactivateTenant)) {

                // 1. Vacate Room
                psRoom.setInt(1, apartmentId);
                psRoom.setString(2, roomNumber);
                psRoom.executeUpdate();

                // 2. Add Room Back to Building Capacity
                psApt.setInt(1, apartmentId);
                psApt.executeUpdate();

                // 3. Log History in Occupancy Table
                psOcc.setInt(1, tenantId);
                psOcc.setString(2, roomNumber);
                psOcc.executeUpdate();

                // 4. Deactivate Tenant
                psTen.setInt(1, tenantId);
                psTen.executeUpdate();

                conn.commit();
                LOGGER.info("Tenant " + tenantId + " moved out of Room " + roomNumber + " and account deactivated.");
                return true;

            } catch (Exception e) {
                conn.rollback();
                LOGGER.severe("Remove Tenant Transaction Failed: " + e.getMessage());
                return false;
            }
        } catch (Exception e) { 
            return false; 
        }
    }

    // NEW: Fetches the list of active tenants currently occupying rooms in a specific apartment
    public List<String> getActiveTenants(int apartmentId) {
        List<String> activeTenants = new ArrayList<>();
        
        String sql = "SELECT t.name, t.contact_number, o.room_number, o.move_in_date " +
                     "FROM registered_tenants t " +
                     "JOIN room_occupancy o ON t.tenant_id = o.tenant_id " +
                     "WHERE o.apartment_id = ? AND o.status = 'Current' AND t.is_active = 1 " +
                     "ORDER BY o.room_number ASC";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                activeTenants.add("Room: " + rs.getString("room_number") +
                                  " | Name: " + rs.getString("name") +
                                  " | Contact: " + rs.getString("contact_number") +
                                  " | Moved In: " + rs.getString("move_in_date"));
            }
            
        } catch (Exception e) {
            LOGGER.severe("Get Active Tenants Error: " + e.getMessage());
        }
        
        return activeTenants;
    }
    
    // NEW: Allows an owner to change their password
    public boolean changePassword(int ownerId, String newRawPassword) {
        // Always hash the new password before saving it!
        String hashedNewPassword = PasswordUtil.hashPassword(newRawPassword);
        
        String sql = "UPDATE owners SET password = ? WHERE owner_id = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, hashedNewPassword);
            ps.setInt(2, ownerId);
            
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
            
        } catch (Exception e) {
            // Optional: LOGGER.severe("Owner Password Update Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticates an owner for the main login screen.
     * Returns the owner_id if successful, or -1 if login fails.
     */
    /**
     * Authenticates an owner for the main login screen.
     * Returns the owner_id if successful, or -1 if login fails.
     */
    public int authenticateOwner(String username, String rawPassword) {
        // We only search for the username, and grab the stored password hash and ID
        String sql = "SELECT owner_id, password FROM owners WHERE username = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                
                // Let PasswordUtil securely compare the raw text to the saved hash
                if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                    return rs.getInt("owner_id"); // Login success!
                }
            }
        } catch (Exception e) {
            System.out.println("Owner Login Error: " + e.getMessage());
        }
        return -1; // Login failed (either username not found, or password didn't match)
    }
    
    // NEW: Allows the owner to edit a tenant's basic details (The Pencil Icon)
    public boolean editTenantDetails(int tenantId, String newName, String newContact, String newEmail, String newEmergency) {
        String sql = "UPDATE registered_tenants SET name = ?, contact_number = ?, email = ?, emergency_contact = ? WHERE tenant_id = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newName);
            ps.setString(2, newContact);
            ps.setString(3, newEmail);
            ps.setString(4, newEmergency);
            ps.setInt(5, tenantId);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            LOGGER.severe("Edit Tenant Error: " + e.getMessage());
            return false;
        }
    }
    
}