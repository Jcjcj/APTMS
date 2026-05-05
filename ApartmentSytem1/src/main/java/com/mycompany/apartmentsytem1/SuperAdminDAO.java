package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    // Super Admin disables an owner account (Soft Delete)
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
    
    // Super Admin verifies the QR payment and approves the apartment
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
    
    // Fetches the full read-only profile of an apartment for the Super Admin
    public String[] getApartmentDetailsForReview(int apartmentId) {
        String sql = "SELECT a.apartment_name, a.apartment_address, a.tin_no, a.capital, a.rooms_available, o.name AS owner_name, o.contact_number " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.apartment_id = ?";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new String[] {
                    rs.getString("apartment_name"),
                    rs.getString("apartment_address"),
                    rs.getString("owner_name"),
                    rs.getString("contact_number"),
                    rs.getString("tin_no"),
                    String.valueOf(rs.getDouble("capital")),
                    String.valueOf(rs.getInt("rooms_available"))
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Admin View Error: " + e.getMessage());
        }
        return null;
    }
    
    // Fetches pending apartments to populate the Admin's Inquiry screen
    public List<String[]> getPendingApartments() {
        List<String[]> list = new ArrayList<>();
        // Joins with owners to get the owner's name and contact, matching the UI
        String sql = "SELECT a.apartment_id, a.apartment_name, o.name AS owner_name, o.contact_number " +
                     "FROM apartments a JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'PENDING'";
                     
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Returns: [ID, Apt Name, Owner Name, Contact Number]
                list.add(new String[] {
                    String.valueOf(rs.getInt("apartment_id")),
                    rs.getString("apartment_name"),
                    rs.getString("owner_name"),
                    rs.getString("contact_number")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Pending Apartments Error: " + e.getMessage());
        }
        return list;
    }

    // Handles the Red X button on the Admin's Inquiry screen
    // Handles the Red X button and saves the reason
    public boolean rejectApartmentRegistration(int apartmentId, String rejectionReason) {
        String sql = "UPDATE apartments SET approval_status = 'REJECTED', rejection_reason = ? WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, rejectionReason);
            ps.setInt(2, apartmentId);
            
            if (ps.executeUpdate() > 0) {
                // Automatically send a notification to the owner's dashboard
                String title = "Registration Rejected";
                String message = "Your apartment registration was rejected. Reason: " + rejectionReason;
                broadcastNotificationToSpecificApartment(apartmentId, title, message);
                return true;
            }
        } catch (Exception e) {
            LOGGER.severe("Rejection Error: " + e.getMessage());
        }
        return false;
    }

    // Helper method to notify just ONE apartment owner instead of everyone
    public boolean broadcastNotificationToSpecificApartment(int apartmentId, String title, String message) {
        String sql = "INSERT INTO announcements (apartment_id, title, message, date_posted) VALUES (?, ?, ?, CURRENT_DATE)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, title);
            ps.setString(3, message);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Fetches Active Owners for the Super Admin Dashboard
    public List<String[]> getActiveOwners() {
        List<String[]> list = new ArrayList<>();
        // Joins apartments and owners to get the exact data for the UI card
        String sql = "SELECT a.apartment_name, o.name, o.contact_number " +
                     "FROM apartments a JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'APPROVED' AND o.is_active = 1";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Returns: [Apartment Name, Owner Name, Contact Number]
                list.add(new String[] {
                    rs.getString("apartment_name"),
                    rs.getString("name"),
                    rs.getString("contact_number")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Active Owners Error: " + e.getMessage());
        }
        return list;
    }
    
    // Pulls the list of apartments for the Billing Tab overview
    public List<String[]> getBillingOverview() {
        List<String[]> list = new ArrayList<>();
        // Simple join to show who is currently registered
        String sql = "SELECT a.apartment_name, o.name, o.contact_number FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status = 'APPROVED'";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new String[] {
                    rs.getString("apartment_name"),
                    rs.getString("name"),
                    rs.getString("contact_number")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Billing Overview Error: " + e.getMessage());
        }
        return list;
    }
    
    // Only READS the existing dates to show warnings in the UI
    public List<String[]> getDueWarnings() {
        List<String[]> list = new ArrayList<>();
        // Fetches apartments where the stored due date is today or earlier
        String sql = "SELECT a.apartment_name, o.name, a.next_billing_date FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.next_billing_date <= CURRENT_DATE AND a.approval_status = 'APPROVED'";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new String[] {
                    rs.getString("apartment_name"),
                    rs.getString("name"),
                    rs.getString("next_billing_date") 
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Warning Retrieval Error: " + e.getMessage());
        }
        return list;
    }
    
    // Sends a broadcast to all owners by using apartment_id 0
    public boolean broadcastNotification(String title, String message) {
        String sql = "INSERT INTO announcements (apartment_id, title, message, date_posted) VALUES (0, ?, ?, CURRENT_DATE)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Broadcast Error: " + e.getMessage());
            return false;
        }
    }

    // NEW: Calculates the 2% subscription cut and the owner's true profit dynamically
    public double[] getFinancialProjections(int apartmentId) {
        // We let SQL do all the math using SUM() and basic multiplication
        String sql = "SELECT " +
                     "SUM(rent_amount) AS gross_income, " +
                     "SUM(rent_amount * 0.02) AS platform_fee, " +
                     "SUM(rent_amount * 0.98) AS net_profit " +
                     "FROM rooms " +
                     "WHERE apartment_id = ?";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Returns an array: [Gross Income, 2% Subscription Fee, Owner Profit]
                return new double[] {
                    rs.getDouble("gross_income"),
                    rs.getDouble("platform_fee"),
                    rs.getDouble("net_profit")
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Financial Projection Error: " + e.getMessage());
        }
        
        // Return zeros if the apartment has no rooms registered yet
        return new double[] {0.0, 0.0, 0.0}; 
    }
    
    // 1. Fetches the detailed transaction list for the new UI
    public List<String[]> getTransactionOverview() {
        List<String[]> list = new ArrayList<>();
        // We pull the apartment details and check if it's APPROVED (Paid) or SUSPENDED (Unpaid)
        String sql = "SELECT a.apartment_id, a.apartment_name, o.name, o.contact_number, " +
                     "a.total_rooms, COALESCE(a.next_billing_date, CURRENT_DATE) as b_date, " +
                     "a.approval_status, a.tin_no, a.payment_method " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.approval_status IN ('APPROVED', 'SUSPENDED')";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new String[] {
                    String.valueOf(rs.getInt("apartment_id")),
                    rs.getString("apartment_name"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    String.valueOf(rs.getInt("total_rooms")),
                    rs.getString("b_date"),
                    rs.getString("approval_status"),
                    rs.getString("tin_no"),
                    rs.getString("payment_method")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Toggles the PAID / UNPAID status
    // Setting is_active = 0 automatically hides it from searches!
    public boolean setApartmentPaymentStatus(int apartmentId, boolean isPaid) {
        String status = isPaid ? "APPROVED" : "SUSPENDED";
        int isActive = isPaid ? 1 : 0; 
        
        String sql = "UPDATE apartments SET approval_status = ?, is_active = ? WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, status);
            ps.setInt(2, isActive);
            ps.setInt(3, apartmentId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}