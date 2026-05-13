package com.mycompany.apartmentsytem1;

import java.sql.*; 
import java.util.logging.Logger;

public class TenantDAO {
    private static final Logger LOGGER = Logger.getLogger(TenantDAO.class.getName());

    // =========================================================================
    // 1. DIRECT ROOM RESERVATION (THE FIX FOR THE POPUP ERROR)
    // =========================================================================
    public boolean directReserveRoom(String name, String contact, String email, String username, String password, int aptId, String roomNum, String ref, double dpAmount) {
        
        String insertTenant = "INSERT INTO registered_tenants (name, contact_number, email, username, password, target_apartment_id, target_room_number, approval_status, valid_id) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING_RESERVATION', 'no_id.png')";
                              
        String insertPayment = "INSERT INTO payment_transactions (apartment_id, tenant_id, room_number, payment_method, reference_no, date_paid, status) " +
                               "VALUES (?, ?, ?, ?, ?, date('now'), 'PENDING')";

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); // Start transaction! If payment fails, account creation rolls back.
            
            // 1. Create the tenant account
            try (PreparedStatement psTenant = conn.prepareStatement(insertTenant, Statement.RETURN_GENERATED_KEYS)) {
                psTenant.setString(1, name);
                psTenant.setString(2, contact);
                psTenant.setString(3, email);
                psTenant.setString(4, username);
                psTenant.setString(5, PasswordUtil.hashPassword(password));
                psTenant.setInt(6, aptId);
                psTenant.setString(7, roomNum);
                
                psTenant.executeUpdate();
                ResultSet rs = psTenant.getGeneratedKeys();
                
                if (rs.next()) {
                    int newTenantId = rs.getInt(1);
                    
                    // 2. Save the down payment (Packing the amount into the method string since there is no amount column!)
                    try (PreparedStatement psPayment = conn.prepareStatement(insertPayment)) {
                        psPayment.setInt(1, aptId);
                        psPayment.setInt(2, newTenantId);
                        psPayment.setString(3, roomNum);
                        psPayment.setString(4, "Down Payment (₱" + dpAmount + ")"); 
                        psPayment.setString(5, ref);
                        psPayment.executeUpdate();
                    }
                    
                    conn.commit(); // Save everything!
                    LOGGER.info("Direct Reservation successful for: " + username);
                    return true;
                }
            } catch (Exception e) {
                conn.rollback();
                LOGGER.severe("Reservation Insert Error: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.severe("DB Connection Error: " + e.getMessage());
        }
        return false;
    }

    // =========================================================================
    // 2. STANDARD TENANT REGISTRATION (From SignUp.java)
    // =========================================================================
    public boolean registerTenant(String name, String contact, String email, String address, String emergency,
                                  String username, String password, String validId,
                                  String aptName, String roomNum, String moveInDate, String occupantsStr) {
        
        int occupants = occupantsStr.isEmpty() ? 1 : Integer.parseInt(occupantsStr);
        int targetAptId = -1;

        try (Connection conn = DBConnection.connect()) {
            
            // Look up the Apartment ID based on the name the tenant typed
            String findAptSql = "SELECT apartment_id FROM apartments WHERE apartment_name = ?";
            try (PreparedStatement psApt = conn.prepareStatement(findAptSql)) {
                psApt.setString(1, aptName);
                ResultSet rs = psApt.executeQuery();
                if (rs.next()) {
                    targetAptId = rs.getInt("apartment_id");
                } else {
                    LOGGER.warning("Tenant tried to register for an unknown apartment: " + aptName);
                    return false; 
                }
            }

            String sql = "INSERT INTO registered_tenants(name, contact_number, email, address, emergency_contact, " +
                         "username, password, valid_id, approval_status, target_apartment_id, target_room_number, move_in_date, occupants) " +
                         "VALUES(?,?,?,?,?,?,?,?, 'PENDING', ?, ?, ?, ?)";
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, contact);
                ps.setString(3, email);
                ps.setString(4, address);
                ps.setString(5, emergency);
                ps.setString(6, username);
                ps.setString(7, PasswordUtil.hashPassword(password)); 
                ps.setString(8, validId);
                ps.setInt(9, targetAptId);
                ps.setString(10, roomNum);
                ps.setString(11, moveInDate);
                ps.setInt(12, occupants);
                
                ps.executeUpdate();
                LOGGER.info("Tenant registered successfully and is PENDING: " + username);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe("Tenant Registration Error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 3. CHANGE PASSWORD
    // =========================================================================
    public boolean changePassword(int tenantId, String newPassword) {
        String sql = "UPDATE registered_tenants SET password = ? WHERE tenant_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword)); 
            ps.setInt(2, tenantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Tenant Password Update Error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 4. REGISTER PERMANENT TENANT (RESTORED!)
    // =========================================================================
    public boolean registerPermanentTenant(String name, String address, String contact, String email, 
                                           String emergencyContact, String apartmentName, String roomNumber, 
                                           String moveInDate, int occupants, String username, String rawPassword) {

        String hashedInputPassword = PasswordUtil.hashPassword(rawPassword);
        int apartmentId = -1;

        try (Connection conn = DBConnection.connect()) {
            
            String findApt = "SELECT apartment_id FROM apartments WHERE apartment_name = ?";
            try (PreparedStatement psApt = conn.prepareStatement(findApt)) {
                psApt.setString(1, apartmentName);
                ResultSet rsApt = psApt.executeQuery();
                if (rsApt.next()) {
                    apartmentId = rsApt.getInt("apartment_id");
                }
            }

            if (apartmentId == -1) {
                LOGGER.warning("Registration Failed: Apartment not found - " + apartmentName);
                return false; 
            }

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
    
    public boolean finalizeViewingTenant(String username,
                                     String name,
                                     String contact,
                                     String email,
                                     String address,
                                     String emergency,
                                     String moveInDate,
                                     String occupantsStr) {

    int occupants = (occupantsStr == null || occupantsStr.trim().isEmpty())
            ? 1
            : Integer.parseInt(occupantsStr.trim());

    String sql = "UPDATE registered_tenants " +
                 "SET name = ?, contact_number = ?, email = ?, address = ?, " +
                 "    emergency_contact = ?, move_in_date = ?, occupants = ?, " +
                 "    approval_status = 'PENDING' " +  // <-- move into owner approval queue
                 "WHERE username = ?";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, name);
        ps.setString(2, contact);
        ps.setString(3, email);
        ps.setString(4, address);
        ps.setString(5, emergency);
        ps.setString(6, moveInDate);
        ps.setInt(7, occupants);
        ps.setString(8, username);

        return ps.executeUpdate() > 0;
    } catch (Exception e) {
        System.out.println("Finalize Viewing Tenant Error: " + e.getMessage());
        return false;
    }
}


}