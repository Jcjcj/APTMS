package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SuperAdminDAO {
    
    private static final Logger LOGGER = Logger.getLogger(SuperAdminDAO.class.getName());

    // ==========================================
    // YOUR EXISTING METHODS (Kept safe!)
    // ==========================================

    public boolean login(String username, String password) {
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

    // ==========================================
    // NEW METHODS FOR THE UI DASHBOARDS
    // ==========================================

    /**
     * UPGRADED: Super Admin verifies the QR payment and approves the apartment.
     * Hooks up to the Green Checkmark and Red X buttons.
     * Automatically sets the first billing due date 30 days from now!
     */
    public boolean updateApartmentApprovalStatus(int apartmentId, String status) {
        String dueDate = null;
        if (status.equals("APPROVED")) {
            dueDate = java.time.LocalDate.now().plusMonths(1).toString(); // e.g., "2026-06-04"
        }

        String sql = "UPDATE apartments SET approval_status = ?, next_billing_date = ? WHERE apartment_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, dueDate);
            ps.setInt(3, apartmentId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                LOGGER.info("Super Admin updated Apartment ID " + apartmentId + " to " + status);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe("Update Status Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * For IMAGE 4 (Inquiries): Gets a list of all apartments waiting for approval.
     */
    public List<String[]> getPendingApartmentInquiries() {
        List<String[]> pendingList = new ArrayList<>();
        String sql = "SELECT a.apartment_id, a.apartment_name, o.name AS owner_name, o.contact_number " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'PENDING'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pendingList.add(new String[]{
                    String.valueOf(rs.getInt("apartment_id")),
                    rs.getString("apartment_name"),
                    rs.getString("owner_name"),
                    rs.getString("contact_number")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Pending Inquiries Error: " + e.getMessage());
        }
        return pendingList;
    }

    /**
     * For IMAGE 3 (Apartment Owners): Gets active owners and counts their active tenants.
     */
    public List<String[]> getActiveOwnersSummary() {
        List<String[]> ownersList = new ArrayList<>();
        String sql = "SELECT a.apartment_name, o.name AS owner_name, " +
                     "(SELECT COUNT(*) FROM registered_tenants t WHERE t.target_apartment_id = a.apartment_id AND t.approval_status = 'APPROVED') AS tenant_count " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'APPROVED'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ownersList.add(new String[]{
                    rs.getString("apartment_name"),
                    rs.getString("owner_name"),
                    rs.getInt("tenant_count") + " Active Tenants"
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Active Owners Error: " + e.getMessage());
        }
        return ownersList;
    }

    /**
     * For IMAGE 2 & 6 (Notifications): Gets owners who have upcoming bills.
     */
    public List<String[]> getBillingNotifications() {
        List<String[]> billingList = new ArrayList<>();
        String sql = "SELECT a.apartment_name, o.name AS owner_name, o.contact_number, a.next_billing_date " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'APPROVED' AND a.next_billing_date IS NOT NULL " +
                     "ORDER BY a.next_billing_date ASC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                billingList.add(new String[]{
                    rs.getString("apartment_name"),
                    rs.getString("owner_name"),
                    rs.getString("contact_number"),
                    "Due " + rs.getString("next_billing_date")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Billing Notifications Error: " + e.getMessage());
        }
        return billingList;
    }
}