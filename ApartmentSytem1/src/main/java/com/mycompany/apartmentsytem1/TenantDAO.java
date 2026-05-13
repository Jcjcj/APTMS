package com.mycompany.apartmentsytem1;

// THIS IS THE FIX: The * imports everything, including ResultSet!
import java.sql.*; 
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

public class TenantDAO {
    private static final Logger LOGGER = Logger.getLogger(TenantDAO.class.getName());

    public boolean registerTenant(String name, String contact, String email, String address, String emergency,
                                  String username, String password, String validId,
                                  String aptName, String roomNum, String moveInDate, String occupantsStr) {
        
        int occupants;
        String normalizedMoveInDate;
        try {
            occupants = occupantsStr == null || occupantsStr.isBlank() ? 1 : Integer.parseInt(occupantsStr.trim());
            if (occupants <= 0) return false;

            normalizedMoveInDate = normalizeDate(moveInDate);
            if (normalizedMoveInDate.isEmpty()) return false;
        } catch (Exception e) {
            LOGGER.warning("Invalid tenant registration number/date input: " + e.getMessage());
            return false;
        }

        int targetAptId = -1;

        try (Connection conn = DBConnection.connect()) {
            
            // 1. Look up the Apartment ID based on the name the tenant typed
            String findAptSql = "SELECT apartment_id FROM apartments WHERE apartment_name = ?";
            try (PreparedStatement psApt = conn.prepareStatement(findAptSql)) {
                psApt.setString(1, aptName);
                ResultSet rs = psApt.executeQuery();
                if (rs.next()) {
                    targetAptId = rs.getInt("apartment_id");
                } else {
                    LOGGER.warning("Tenant tried to register for an unknown apartment: " + aptName);
                    return false; // Fail if apartment doesn't exist
                }
            }

            // 2. Save the full profile WITH the target apartment and room
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
                ps.setString(11, normalizedMoveInDate);
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

    private String normalizeDate(String value) {
        String normalized = value != null ? value.trim().replace("/", "-") : "";
        if (normalized.isEmpty()) return "";
        return LocalDate.parse(normalized).toString();
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

    public boolean canEditMoveInDate(int tenantId) {
        String sql = "SELECT move_in_date FROM registered_tenants WHERE tenant_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String moveInDate = rs.getString("move_in_date");
                if (moveInDate == null || moveInDate.isBlank()) {
                    return true;
                }

                return LocalDate.now().isBefore(LocalDate.parse(moveInDate));
            }
        } catch (DateTimeParseException e) {
            LOGGER.warning("Invalid move-in date format for tenant " + tenantId + ": " + e.getMessage());
            return true;
        } catch (Exception e) {
            LOGGER.severe("Move-in Date Rule Check Error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTenantAccountProfile(int tenantId, String name, String contact, String email,
                                              String emergencyContact, String address, String moveInDate) {
        if (name == null || name.isBlank() || contact == null || contact.isBlank()
                || email == null || email.isBlank()) {
            return false;
        }

        String normalizedMoveIn = moveInDate != null ? moveInDate.trim() : "";
        if (!normalizedMoveIn.isEmpty()) {
            try {
                LocalDate.parse(normalizedMoveIn);
            } catch (DateTimeParseException e) {
                LOGGER.warning("Invalid requested move-in date: " + normalizedMoveIn);
                return false;
            }
        }

        boolean canEditMoveIn = canEditMoveInDate(tenantId);
        String tenantSql = canEditMoveIn
                ? "UPDATE registered_tenants SET name = ?, contact_number = ?, email = ?, emergency_contact = ?, address = ?, move_in_date = ? WHERE tenant_id = ?"
                : "UPDATE registered_tenants SET name = ?, contact_number = ?, email = ?, emergency_contact = ?, address = ? WHERE tenant_id = ?";
        String occupancySql = "UPDATE room_occupancy SET move_in_date = ? WHERE tenant_id = ? AND status = 'Current'";

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(tenantSql)) {
                ps.setString(1, name.trim());
                ps.setString(2, contact.trim());
                ps.setString(3, email.trim());
                ps.setString(4, emergencyContact != null ? emergencyContact.trim() : "");
                ps.setString(5, address != null ? address.trim() : "");

                if (canEditMoveIn) {
                    ps.setString(6, normalizedMoveIn);
                    ps.setInt(7, tenantId);
                } else {
                    ps.setInt(6, tenantId);
                }

                if (ps.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            if (canEditMoveIn && !normalizedMoveIn.isEmpty()) {
                try (PreparedStatement psOcc = conn.prepareStatement(occupancySql)) {
                    psOcc.setString(1, normalizedMoveIn);
                    psOcc.setInt(2, tenantId);
                    psOcc.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            LOGGER.severe("Tenant Account Profile Update Error: " + e.getMessage());
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

    public boolean directReserveRoom(String name, String contact, String email, String username, String rawPassword,
                                     int apartmentId, String roomNumber, String paymentReference, double downPaymentAmount) {
        ViewingDAO viewingDAO = new ViewingDAO();
        return viewingDAO.bookRoomReservationWithPayment(
                apartmentId,
                roomNumber,
                name,
                contact,
                email,
                username,
                rawPassword,
                paymentReference,
                downPaymentAmount
        );
    }
}
