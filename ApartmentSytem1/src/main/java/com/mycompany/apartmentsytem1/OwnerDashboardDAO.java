package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OwnerDashboardDAO {

    private static final Logger LOGGER = Logger.getLogger(OwnerDashboardDAO.class.getName());

    // 1. Core Architecture: Link Owner to their Apartment[cite: 17]
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

    // 2. Apt Dashboard Tab[cite: 17]
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

    // 3. Expenses Tab (Reads from room_bills)[cite: 17]
    public double[] getExpensesSummation(int apartmentId) {
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
    }

    // 4. Rooms Tab (Reads combined data from rooms and room_bills)[cite: 17]
    public List<String[]> getOwnerRooms(int apartmentId) {
        List<String[]> rooms = new ArrayList<>();
        String sql = "SELECT r.room_number, a.internet_type, rb.rent_amount, rb.electricity_amount, rb.water_amount, rb.internet_amount " +
                     "FROM rooms r JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "LEFT JOIN room_bills rb ON r.room_number = rb.room_number AND rb.apartment_id = r.apartment_id " +
                     "WHERE r.apartment_id = ? ORDER BY r.room_number ASC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                boolean hasNet = !rs.getString("internet_type").equalsIgnoreCase("None");
                rooms.add(new String[]{
                    rs.getString("room_number"),
                    String.valueOf(rs.getDouble("rent_amount")),
                    String.valueOf(rs.getDouble("electricity_amount")),
                    String.valueOf(rs.getDouble("water_amount")),
                    String.valueOf(rs.getDouble("internet_amount")),
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

    // 5. To Do's Tab[cite: 17]
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

    // 6. Inquiries Tab[cite: 17]
    public List<String[]> getPendingRoomViewings(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT schedule_id, tenant_name, room_number, schedule_date FROM viewing_schedule WHERE apartment_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new String[]{String.valueOf(rs.getInt("schedule_id")), rs.getString("tenant_name"), rs.getString("room_number"), rs.getString("schedule_date")});
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

    // 7. History Tab[cite: 17]
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

    public List<String> getNotificationHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT title, message, date_posted FROM announcements WHERE apartment_id = ? ORDER BY date_posted DESC LIMIT 10";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId); ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(rs.getString("title") + " - " + rs.getString("date_posted") + "\n" + rs.getString("message"));
        } catch (Exception e) {} return list;
    }
}