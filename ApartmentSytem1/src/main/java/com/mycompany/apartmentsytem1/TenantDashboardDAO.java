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
     * Array Index: [0]RentAmt, [1]RentDate, [2]ElecAmt, [3]ElecDate, [4]WaterAmt, [5]WaterDate, [6]NetAmt, [7]NetDate, [8]PenaltyText
     */
    public Object[] getMyBills(int apartmentId, String roomNumber) {
        // METER-AWARE LOGIC: Perfectly mirrors the Owner's side!
        String latestBillDueDate =
                     "(SELECT b.due_date " +
                     " FROM room_occupancy ro2 " +
                     " JOIN bills b ON b.tenant_id = ro2.tenant_id AND b.apartment_id = ro2.apartment_id " +
                     " WHERE ro2.apartment_id = r.apartment_id " +
                     "   AND ro2.room_number = r.room_number " +
                     "   AND ro2.status = 'Current' " +
                     "   AND b.paid = 0 " +
                     " ORDER BY b.bill_id DESC LIMIT 1)";

        String automaticDueDate =
                     "(SELECT date(ro3.move_in_date, '+1 month') " +
                     " FROM room_occupancy ro3 " +
                     " WHERE ro3.apartment_id = r.apartment_id " +
                     "   AND ro3.room_number = r.room_number " +
                     "   AND ro3.status = 'Current' " +
                     " ORDER BY ro3.occupancy_id DESC LIMIT 1)";

        String latestPenalty =
                     "(SELECT b.penalty " +
                     " FROM room_occupancy ro4 " +
                     " JOIN bills b ON b.tenant_id = ro4.tenant_id AND b.apartment_id = ro4.apartment_id " +
                     " WHERE ro4.apartment_id = r.apartment_id " +
                     "   AND ro4.room_number = r.room_number " +
                     "   AND ro4.status = 'Current' " +
                     "   AND b.paid = 0 " +
                     " ORDER BY b.bill_id DESC LIMIT 1)";

        String sql = "SELECT " +
                     "COALESCE(rb.rent_amount, r.rent_amount) as rent_amt, " +
                     "COALESCE(" + latestBillDueDate + ", rb.rent_due_date, " + automaticDueDate + ", 'N/A') as rent_date, " +
                     // Electricity Logic: Fixed = Base Rate, Metered = 0.0 until Owner updates
                     "CASE WHEN rb.electricity_amount IS NOT NULL THEN rb.electricity_amount " +
                     "     WHEN a.electricity_type = 'Fixed' THEN a.elec_rate ELSE 0.0 END as elec_amt, " +
                     "COALESCE(" + latestBillDueDate + ", rb.electricity_due_date, " + automaticDueDate + ", 'N/A') as elec_date, " +
                     // Water Logic: Fixed = Base Rate, Metered = 0.0 until Owner updates
                     "CASE WHEN rb.water_amount IS NOT NULL THEN rb.water_amount " +
                     "     WHEN a.water_type = 'Fixed' THEN a.water_rate ELSE 0.0 END as water_amt, " +
                     "COALESCE(" + latestBillDueDate + ", rb.water_due_date, " + automaticDueDate + ", 'N/A') as water_date, " +
                     "COALESCE(rb.internet_amount, a.internet_rate, 0.0) as net_amt, " +
                     "COALESCE(" + latestBillDueDate + ", rb.internet_due_date, " + automaticDueDate + ", 'N/A') as net_date, " +
                     "COALESCE(" + latestPenalty + ", 0.0) as penalty_amt " +
                     "FROM rooms r " +
                     "JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "LEFT JOIN room_bills rb ON rb.bill_id = (" +
                     "    SELECT MAX(rb2.bill_id) FROM room_bills rb2 " +
                     "    WHERE rb2.room_number = r.room_number AND rb2.apartment_id = r.apartment_id" +
                     ") " +
                     "WHERE r.apartment_id = ? AND r.room_number = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Object[] {
                    rs.getDouble("rent_amt"), rs.getString("rent_date"),
                    rs.getDouble("elec_amt"), rs.getString("elec_date"),
                    rs.getDouble("water_amt"), rs.getString("water_date"),
                    rs.getDouble("net_amt"), rs.getString("net_date"),
                    rs.getDouble("penalty_amt") > 0.0 ? String.format("PHP %,.2f", rs.getDouble("penalty_amt")) : "N/A"
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Meter-Aware Get Bills Error: " + e.getMessage());
        }
        return new Object[] {0.0, "N/A", 0.0, "N/A", 0.0, "N/A", 0.0, "N/A", "N/A"};
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
        // FIXED: Changed 'issue' to 'issue_description' to match your database
        String sql = "INSERT INTO maintenance_requests(apartment_id, room_number, issue_description, status, date_reported, date_updated) VALUES(?,?,?,'PENDING',date('now'),date('now'))";
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
        String sql = "SELECT issue_description, status, COALESCE(date_reported, date_updated, 'N/A') AS request_date " +
                     "FROM maintenance_requests WHERE apartment_id = ? AND room_number = ? ORDER BY request_id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add("[" + rs.getString("status") + "] " + rs.getString("request_date") + "\n" + rs.getString("issue_description"));
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
    public List<String> getNotificationFeed(int apartmentId, String tenantUsername) {
        List<String> list = new ArrayList<>();
        String sql =
                "SELECT title, message, created_at FROM (" +
                "  SELECT title, message, date_posted AS created_at, announcement_id AS sort_id " +
                "  FROM announcements WHERE apartment_id = ? " +
                "  UNION ALL " +
                "  SELECT title, message, date_created AS created_at, notification_id AS sort_id " +
                "  FROM notifications WHERE target_username = ? " +
                ") ORDER BY created_at DESC, sort_id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, tenantUsername);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("title") + "\n" + rs.getString("message") + " (" + rs.getString("created_at") + ")");
            }
        } catch (Exception e) {
            LOGGER.severe("Get Notification Feed Error: " + e.getMessage());
        }
        return list;
    }
 // --- TENANT HISTORY TAB METHODS ---

    // 1. Fetch Tenant's Personal Bill History (Only Paid)
    public List<String> getMyBillHistory(int tenantId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT month, total, payment_date FROM bills WHERE tenant_id = ? AND paid = 1 ORDER BY payment_date DESC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add("Month: " + rs.getString("month") + " | PHP " + String.format("%,.2f", rs.getDouble("total")) + " | Paid: " + rs.getString("payment_date"));
            }
        } catch (Exception e) { LOGGER.severe("Tenant Bill History Error: " + e.getMessage()); }
        return list;
    }

    // 2. Fetch Tenant's Past Complaints/Suggestions
    public List<String> getMyComplaintsHistory(int apartmentId, String roomNumber) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT message, date_submitted FROM complaints WHERE apartment_id = ? AND room_number = ? ORDER BY date_submitted DESC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("date_submitted") + ": " + rs.getString("message"));
            }
        } catch (Exception e) { LOGGER.severe("Tenant Complaints History Error: " + e.getMessage()); }
        return list;
    }
    
    // --- FETCH OWNER PAYMENT DETAILS (NO BANKS) ---
    public String getOwnerPaymentDetails(int apartmentId) {
        String details = "<html><b>Online Payment</b><br><br>No payment details provided by owner.</html>";
        
        String sql = "SELECT o.gcash_no, o.gcash_name, o.paymaya_no, o.paymaya_name " +
                     "FROM owners o JOIN apartments a ON o.owner_id = a.owner_id " +
                     "WHERE a.apartment_id = ?";
                     
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Formatting it perfectly for the UI text box
                details = "<html><b>Online Payment</b><br><br>" +
                          "<b>GCash</b><br>" + 
                          rs.getString("gcash_no") + "<br>(" + rs.getString("gcash_name") + ")<br><br>" +
                          "<b>Paymaya</b><br>" + 
                          rs.getString("paymaya_no") + "<br>(" + rs.getString("paymaya_name") + ")" +
                          "</html>";
            }
        } catch (Exception e) {
            System.out.println("Payment Details Fetch Error: " + e.getMessage());
        }
        return details;
    }
}
