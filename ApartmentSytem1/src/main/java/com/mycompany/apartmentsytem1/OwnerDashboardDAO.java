package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OwnerDashboardDAO {

    private static final Logger LOGGER = Logger.getLogger(OwnerDashboardDAO.class.getName());

    // =====================================================================
    // 1. CORE ARCHITECTURE & LOCKOUT STATUS
    // =====================================================================

    public int getApartmentIdByOwner(int ownerId) {
        String sql = "SELECT apartment_id FROM apartments WHERE owner_id = ? LIMIT 1";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("apartment_id");
        } catch (Exception e) { LOGGER.severe("Get Apt ID Error: " + e.getMessage()); }
        return -1;
    }

    // Checks if the apartment is currently SUSPENDED (Unpaid) for the Lockout UI
    public boolean isApartmentActive(int apartmentId) {
        String sql = "SELECT is_active FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("is_active") == 1;
        } catch (Exception e) { LOGGER.severe("Active Check Error: " + e.getMessage()); }
        return false;
    }

    // =====================================================================
    // 2. DASHBOARD & ROOM STATS
    // =====================================================================

    public int[] getRoomOccupancyStats(int apartmentId) {
        int[] stats = {0, 0, 0}; // Occupied, Vacant, Total
        String sql = "SELECT total_rooms, rooms_available FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats[2] = rs.getInt("total_rooms");
                stats[1] = rs.getInt("rooms_available");
                stats[0] = stats[2] - stats[1]; 
            }
        } catch (Exception e) { LOGGER.severe("Occupancy Error: " + e.getMessage()); }
        return stats;
    }

    // =====================================================================
    // 3. EXPENSES & PLATFORM FEE CALCULATIONS
    // =====================================================================

    /*public double[] getExpensesSummation(int apartmentId) {
        double[] expenses = {0.0, 0.0, 0.0}; // Elec, Water, Net
        String sql = "SELECT SUM(electricity_amount) as e, SUM(water_amount) as w, SUM(internet_amount) as i FROM room_bills WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                expenses[0] = rs.getDouble("e"); expenses[1] = rs.getDouble("w"); expenses[2] = rs.getDouble("i");
            }
        } catch (Exception e) { LOGGER.severe("Expenses Sum Error: " + e.getMessage()); }
        return expenses;
    }*/

    // Fetches active rooms, their specific rent, and calculates the 2% fee dynamically
    public List<String[]> getActiveRoomsForServiceFee(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT r.room_number, COALESCE(rb.rent_amount, r.rent_amount) as rent " +
                     "FROM rooms r " +
                     "JOIN room_occupancy ro ON r.room_number = ro.room_number AND r.apartment_id = ro.apartment_id " +
                     "LEFT JOIN room_bills rb ON r.room_number = rb.room_number AND r.apartment_id = rb.apartment_id " +
                     "WHERE r.apartment_id = ? AND ro.status = 'Current'";
                     
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                double rent = rs.getDouble("rent");
                double fee = rent * 0.02; // 2% calculation
                list.add(new String[]{rs.getString("room_number"), String.valueOf(rent), String.valueOf(fee)});
            }
        } catch(Exception e) { LOGGER.severe("Service Fee Math Error: " + e.getMessage()); }
        return list;
    }

    // Submits the payment info so the Super Admin can verify it
    public boolean submitPlatformFeePayment(int apartmentId, String tin, String method, String date, String refNo) {
        String sql = "UPDATE apartments SET tin_no = ?, payment_method = ? WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tin);
            ps.setString(2, method + " (Ref: " + refNo + ") - " + date); 
            ps.setInt(3, apartmentId);
            return ps.executeUpdate() > 0;
        } catch(Exception e) { LOGGER.severe("Payment Submission Error: " + e.getMessage()); }
        return false;
    }

    // =====================================================================
    // 4. ROOM PRICING MANAGEMENT
    // =====================================================================

    public List<String[]> getOwnerRooms(int apartmentId) {
        List<String[]> rooms = new ArrayList<>();

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
        
        // METER-AWARE FIX: Automatically populates Fixed rates, but leaves Metered rates at 0.00 for manual input!
        String sql = "SELECT r.room_number, a.internet_type, " +
                     "COALESCE(rb.rent_amount, r.rent_amount) as display_rent, " +
                     "CASE WHEN rb.electricity_amount IS NOT NULL THEN rb.electricity_amount " +
                     "     WHEN a.electricity_type = 'Fixed' THEN a.elec_rate ELSE 0.0 END as e_amt, " +
                     "CASE WHEN rb.water_amount IS NOT NULL THEN rb.water_amount " +
                     "     WHEN a.water_type = 'Fixed' THEN a.water_rate ELSE 0.0 END as w_amt, " +
                     "COALESCE(rb.internet_amount, a.internet_rate, 0.0) as i_amt, " +
                     "COALESCE(" + latestBillDueDate + ", rb.rent_due_date, " + automaticDueDate + ", 'N/A') as due_date " +
                     "FROM rooms r JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "LEFT JOIN room_bills rb ON r.room_number = rb.room_number AND rb.apartment_id = r.apartment_id " +
                     "WHERE r.apartment_id = ? ORDER BY r.room_number ASC";
        
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Check if internet is offered
                String internetType = rs.getString("internet_type");
                boolean hasNet = internetType != null && !internetType.equalsIgnoreCase("None");
                
                rooms.add(new String[]{
                    rs.getString("room_number"),
                    String.valueOf(rs.getDouble("display_rent")),
                    String.valueOf(rs.getDouble("e_amt")),
                    String.valueOf(rs.getDouble("w_amt")),
                    String.valueOf(rs.getDouble("i_amt")),
                    rs.getString("due_date"),
                    String.valueOf(hasNet)
                });
            }
        } catch (Exception e) { LOGGER.severe("Get Rooms Error: " + e.getMessage()); }
        return rooms;
    }

    public boolean updateRoomUtilities(int apartmentId, String roomNo, double rent, double elec, double water, double net) {
        String checkSql = "SELECT bill_id FROM room_bills WHERE apartment_id = ? AND room_number = ?";
        String insertSql = "INSERT INTO room_bills (rent_amount, electricity_amount, water_amount, internet_amount, apartment_id, room_number) VALUES (?,?,?,?,?,?)";
        String updateSql = "UPDATE room_bills SET rent_amount=?, electricity_amount=?, water_amount=?, internet_amount=? WHERE apartment_id=? AND room_number=?";
        
        try (Connection conn = DBConnection.connect()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, apartmentId); psCheck.setString(2, roomNo);
                exists = psCheck.executeQuery().next();
            }
            String finalSql = exists ? updateSql : insertSql;
            try (PreparedStatement psFinal = conn.prepareStatement(finalSql)) {
                psFinal.setDouble(1, rent); psFinal.setDouble(2, elec); psFinal.setDouble(3, water); psFinal.setDouble(4, net);
                psFinal.setInt(5, apartmentId); psFinal.setString(6, roomNo);
                return psFinal.executeUpdate() > 0;
            }
        } catch (Exception e) { return false; }
    }

    // =====================================================================
    // 5. TO DO'S & COMMUNICATION
    // =====================================================================

    public List<String[]> getPendingMaintenance(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT request_id, room_number, issue_description FROM maintenance_requests WHERE apartment_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new String[]{String.valueOf(rs.getInt("request_id")), rs.getString("room_number"), rs.getString("issue_description")});
        } catch (Exception e) {} return list;
    }

    public boolean markMaintenanceDone(int requestId) {
        String sql = "UPDATE maintenance_requests SET status = 'COMPLETED', date_resolved = date('now') WHERE request_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean sendAnnouncement(int apartmentId, String message) {
        String sql = "INSERT INTO announcements (apartment_id, title, message, date_posted) VALUES (?, 'Owner Announcement', ?, date('now'))";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId); ps.setString(2, message); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public List<String[]> getRecentComplaints(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT room_number, message, date_submitted FROM complaints WHERE apartment_id = ? ORDER BY complaint_id DESC LIMIT 10";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new String[]{rs.getString("room_number"), rs.getString("message"), rs.getString("date_submitted")});
        } catch (Exception e) {} return list;
    }

    // =====================================================================
    // 6. INQUIRIES & REGISTRATIONS
    // =====================================================================

    public List<String[]> getPendingRoomViewings(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT schedule_id, tenant_name, room_number, schedule_date, viewing_time " +
                     "FROM viewing_schedule WHERE apartment_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String type = "RESERVE_NOW".equalsIgnoreCase(rs.getString("viewing_time")) ? "Reservation" : "Viewing";
                list.add(new String[]{
                    String.valueOf(rs.getInt("schedule_id")),
                    rs.getString("tenant_name"),
                    rs.getString("room_number"),
                    rs.getString("schedule_date"),
                    type
                });
            }
        } catch (Exception e) {} return list;
    }

    public List<String[]> getPendingTenants(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT tenant_id, name, target_room_number, move_in_date FROM registered_tenants WHERE target_apartment_id = ? AND approval_status = 'PENDING'";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new String[]{String.valueOf(rs.getInt("tenant_id")), rs.getString("name"), rs.getString("target_room_number"), rs.getString("move_in_date")});
        } catch (Exception e) {} return list;
    }

    // =====================================================================
    // 7. HISTORICAL RECORDS
    // =====================================================================

    public List<String> getBillsHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.room_number, b.total, b.payment_date FROM bills b WHERE b.apartment_id = ? AND b.paid = 1 ORDER BY b.payment_date DESC LIMIT 10";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId); ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add("Room " + rs.getString("room_number") + " | PHP " + rs.getDouble("total") + " | Paid: " + rs.getString("payment_date"));
        } catch (Exception e) {} return list;
    }

    public List<String> getMaintenanceHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT room_number, issue_description, date_resolved FROM maintenance_requests WHERE apartment_id = ? AND status = 'COMPLETED' ORDER BY date_resolved DESC LIMIT 10";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId); ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add("Room " + rs.getString("room_number") + " | " + rs.getString("issue_description") + " | Resolved: " + rs.getString("date_resolved"));
        } catch (Exception e) {} return list;
    }

    public List<String> getNotificationHistory(int apartmentId, String ownerUsername) {
        List<String> list = new ArrayList<>();
        String sql =
                "SELECT title, message, created_at FROM (" +
                "  SELECT title, message, date_posted AS created_at, announcement_id AS sort_id " +
                "  FROM announcements WHERE apartment_id = ? " +
                "  UNION ALL " +
                "  SELECT title, message, date_created AS created_at, notification_id AS sort_id " +
                "  FROM notifications WHERE target_username = ? " +
                ") ORDER BY created_at DESC, sort_id DESC LIMIT 10";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, ownerUsername);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(rs.getString("title") + " - " + rs.getString("created_at") + "\n" + rs.getString("message"));
        } catch (Exception e) {} return list;
    }

    // =====================================================================
    // 8. TENANT MANAGEMENT & ACCOUNT CREATION
    // =====================================================================

    public List<String[]> getActiveTenantDetails(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT rt.tenant_id, rt.name, COALESCE(ro.room_number, rt.target_room_number) AS room_number, " +
                     "rt.contact_number, rt.email, COALESCE(ro.move_in_date, rt.move_in_date) AS joined_date " +
                     "FROM registered_tenants rt " +
                     "LEFT JOIN room_occupancy ro ON ro.tenant_id = rt.tenant_id AND ro.apartment_id = rt.target_apartment_id AND ro.status = 'Current' " +
                     "WHERE rt.target_apartment_id = ? AND rt.approval_status = 'APPROVED' AND rt.is_active = 1";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("tenant_id")), 
                    rs.getString("name"),
                    rs.getString("room_number"), 
                    rs.getString("contact_number"),
                    rs.getString("email"), 
                    rs.getString("joined_date")
                });
            }
        } catch (Exception e) { LOGGER.severe("Get Active Tenants Error: " + e.getMessage()); }
        return list;
    }

    public String[] getTenantRegistrationDetails(int tenantId) {
        String sql = "SELECT rt.name, rt.contact_number, rt.email, rt.address, rt.emergency_contact, " +
                     "a.apartment_name, " +
                     "COALESCE(ro.room_number, rt.target_room_number) AS room_number, " +
                     "COALESCE(ro.move_in_date, rt.move_in_date) AS joined_date, " +
                     "rt.occupants, rt.valid_id " +
                     "FROM registered_tenants rt " +
                     "LEFT JOIN apartments a ON a.apartment_id = rt.target_apartment_id " +
                     "LEFT JOIN room_occupancy ro ON ro.tenant_id = rt.tenant_id AND ro.status = 'Current' " +
                     "WHERE rt.tenant_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("emergency_contact"),
                    rs.getString("apartment_name"),
                    rs.getString("room_number"),
                    rs.getString("joined_date"),
                    rs.getString("occupants"),
                    rs.getString("valid_id")
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Get Tenant Registration Details Error: " + e.getMessage());
        }
        return null;
    }

    public String[] getOwnerApartmentProfile(int ownerId, int apartmentId) {
        String sql = "SELECT " +
                     "o.name AS owner_name, o.contact_number AS owner_contact, o.email AS owner_email, " +
                     "o.address AS owner_address, o.emergency_number AS owner_emergency, " +
                     "o.gcash_no, o.gcash_name, o.paymaya_no, o.paymaya_name, " +
                     "a.apartment_name, a.tin_no, a.description, a.policy, a.barangay, a.street, " +
                     "a.contact_number AS apartment_contact, a.email AS apartment_email, " +
                     "a.emergency_number AS apartment_emergency " +
                     "FROM owners o JOIN apartments a ON o.owner_id = a.owner_id " +
                     "WHERE o.owner_id = ? AND a.apartment_id = ?";

        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setInt(2, apartmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[] {
                    rs.getString("owner_name"),
                    rs.getString("owner_contact"),
                    rs.getString("owner_email"),
                    rs.getString("owner_address"),
                    rs.getString("owner_emergency"),
                    rs.getString("gcash_no"),
                    rs.getString("gcash_name"),
                    rs.getString("paymaya_no"),
                    rs.getString("paymaya_name"),
                    rs.getString("apartment_name"),
                    rs.getString("tin_no"),
                    rs.getString("description"),
                    rs.getString("policy"),
                    rs.getString("barangay"),
                    rs.getString("street"),
                    rs.getString("apartment_contact"),
                    rs.getString("apartment_email"),
                    rs.getString("apartment_emergency")
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Get Owner Apartment Profile Error: " + e.getMessage());
        }
        return null;
    }

    public boolean updateOwnerApartmentProfile(
            int ownerId,
            int apartmentId,
            String ownerName,
            String ownerContact,
            String ownerEmail,
            String ownerAddress,
            String ownerEmergency,
            String gcashNo,
            String gcashName,
            String paymayaNo,
            String paymayaName,
            String apartmentName,
            String tinNo,
            String description,
            String policy,
            String barangay,
            String street,
            String apartmentContact,
            String apartmentEmail,
            String apartmentEmergency) {
        String ownerSql = "UPDATE owners SET name = ?, contact_number = ?, email = ?, address = ?, emergency_number = ?, " +
                          "gcash_no = ?, gcash_name = ?, paymaya_no = ?, paymaya_name = ? WHERE owner_id = ?";
        String apartmentSql = "UPDATE apartments SET apartment_name = ?, tin_no = ?, description = ?, policy = ?, barangay = ?, street = ?, " +
                              "contact_number = ?, email = ?, emergency_number = ? WHERE apartment_id = ? AND owner_id = ?";

        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOwner = conn.prepareStatement(ownerSql);
                 PreparedStatement psApartment = conn.prepareStatement(apartmentSql)) {
                psOwner.setString(1, ownerName);
                psOwner.setString(2, ownerContact);
                psOwner.setString(3, ownerEmail);
                psOwner.setString(4, ownerAddress);
                psOwner.setString(5, ownerEmergency);
                psOwner.setString(6, gcashNo);
                psOwner.setString(7, gcashName);
                psOwner.setString(8, paymayaNo);
                psOwner.setString(9, paymayaName);
                psOwner.setInt(10, ownerId);
                psOwner.executeUpdate();

                psApartment.setString(1, apartmentName);
                psApartment.setString(2, tinNo);
                psApartment.setString(3, description);
                psApartment.setString(4, policy);
                psApartment.setString(5, barangay);
                psApartment.setString(6, street);
                psApartment.setString(7, apartmentContact);
                psApartment.setString(8, apartmentEmail);
                psApartment.setString(9, apartmentEmergency);
                psApartment.setInt(10, apartmentId);
                psApartment.setInt(11, ownerId);
                psApartment.executeUpdate();

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.severe("Update Owner Apartment Profile Error: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Update Owner Apartment Profile Error: " + e.getMessage());
            return false;
        }
    }

    public boolean evictTenant(int tenantId, int apartmentId) {
        String evictSql = "UPDATE registered_tenants SET is_active = 0, approval_status = 'EVICTED' WHERE tenant_id = ?";
        String updateAptSql = "UPDATE apartments SET rooms_available = rooms_available + 1 WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement ps1 = conn.prepareStatement(evictSql);
                 PreparedStatement ps2 = conn.prepareStatement(updateAptSql)) {
                
                ps1.setInt(1, tenantId);
                ps1.executeUpdate();
                
                ps2.setInt(1, apartmentId);
                ps2.executeUpdate();
                
                conn.commit(); 
                return true;
            } catch (Exception e) {
                conn.rollback();
                return false;
            }
        } catch (Exception e) { return false; }
    }
    public List<String[]> getPendingPayments(int apartmentId) {
        List<String[]> list = new java.util.ArrayList<>();
        String sql = "SELECT transaction_id, tenant_id, room_number, payment_method, reference_no, date_paid " +
                     "FROM payment_transactions WHERE apartment_id = ? AND status = 'PENDING'";
        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("transaction_id")),
                    String.valueOf(rs.getInt("tenant_id")),
                    rs.getString("room_number"),
                    rs.getString("payment_method"),
                    rs.getString("reference_no"),
                    rs.getString("date_paid")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

}
