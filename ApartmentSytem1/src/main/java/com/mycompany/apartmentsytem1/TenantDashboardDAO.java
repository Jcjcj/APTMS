package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TenantDashboardDAO {

    private static final Logger LOGGER = Logger.getLogger(TenantDashboardDAO.class.getName());

    // ==========================================================
    // 1. DASHBOARD & EXPENSES TABS (Images 2, 3, & 8)
    // ==========================================================

    /**
     * Loads the specific Rent and Utility bills for the logged-in tenant's room.
     * Returns an array: [Rent, Electricity, Water, Internet]
     */
  /**
     * Loads the specific Rent and Utility bills AND their due dates for the tenant's room.
     * Returns an Object array so it can hold both numbers (amounts) and Strings (dates).
     * Array Index: [0]RentAmt, [1]RentDate, [2]ElecAmt, [3]ElecDate, [4]WaterAmt, [5]WaterDate, [6]NetAmt, [7]NetDate
     */
    public Object[] getMyBills(int apartmentId, String roomNumber) {
        String sql = "SELECT rent_amount, rent_due_date, electricity_amount, electricity_due_date, " +
                     "water_amount, water_due_date, internet_amount, internet_due_date " +
                     "FROM room_bills WHERE apartment_id = ? AND room_number = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Object[] {
                    rs.getDouble("rent_amount"), rs.getString("rent_due_date"),
                    rs.getDouble("electricity_amount"), rs.getString("electricity_due_date"),
                    rs.getDouble("water_amount"), rs.getString("water_due_date"),
                    rs.getDouble("internet_amount"), rs.getString("internet_due_date")
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Get Bills Error: " + e.getMessage());
        }
        // Return empty defaults if no bills exist yet
        return new Object[] {0.0, "N/A", 0.0, "N/A", 0.0, "N/A", 0.0, "N/A"};
    }
    /**
     * Submits the GCash/Paymaya transaction reference to the owner.
     */
    public boolean submitPayment(int apartmentId, int tenantId, String roomNumber, String method, String date, String refNo) {
        String sql = "INSERT INTO payment_transactions(apartment_id, tenant_id, room_number, payment_method, date_paid, reference_no, status) " +
                     "VALUES(?,?,?,?,?,?,'PENDING')";
                     
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setInt(2, tenantId);
            ps.setString(3, roomNumber);
            ps.setString(4, method);
            ps.setString(5, date);
            ps.setString(6, refNo);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Payment Submission Error: " + e.getMessage());
        }
        return false;
    }

    // ==========================================================
    // 2. MAINTENANCE TAB (Image 4)
    // ==========================================================

    /**
     * Submits a new maintenance request (e.g., "Broken Doorknob").
     */
    public boolean submitMaintenanceRequest(int apartmentId, String roomNumber, String issue) {
        String sql = "INSERT INTO maintenance_requests(apartment_id, room_number, issue, status) VALUES(?,?,?,'PENDING')";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, issue);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the list of past maintenance requests to display on the right side of Image 4.
     */
    public List<String> getMyMaintenanceRequests(int apartmentId, String roomNumber) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT issue FROM maintenance_requests WHERE apartment_id = ? AND room_number = ? ORDER BY request_id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("issue"));
            }
        } catch (Exception e) {
            LOGGER.severe("Get Maintenance Error: " + e.getMessage());
        }
        return list;
    }

    // ==========================================================
    // 3. INQUIRY TAB (Image 6)
    // ==========================================================

    /**
     * Submits a complaint/suggestion to the Owner's dashboard.
     */
    public boolean submitComplaint(int apartmentId, String roomNumber, String message) {
        String sql = "INSERT INTO complaints(apartment_id, room_number, message, date_submitted) VALUES(?,?,?,?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, message);
            ps.setString(4, java.time.LocalDate.now().toString());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================================
    // 4. NOTIFICATION TAB (Image 5)
    // ==========================================================

    /**
     * Gets broadcasts/announcements sent by the Owner (e.g., "Power Outage").
     */
    public List<String> getAnnouncements(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT title, message, date_posted FROM announcements WHERE apartment_id = ? ORDER BY announcement_id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("title") + "\n" + rs.getString("message") + " (" + rs.getString("date_posted") + ")");
            }
        } catch (Exception e) {
            LOGGER.severe("Get Announcements Error: " + e.getMessage());
        }
        return list;
    }
    
}