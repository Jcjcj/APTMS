package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OwnerDashboardDAO {

    private static final Logger LOGGER = Logger.getLogger(OwnerDashboardDAO.class.getName());

    // ==========================================================
    // 1. MAIN DASHBOARD TAB (Image 2)
    // ==========================================================

    /**
     * Calculates Occupied vs Vacant rooms.
     * Returns an array: [OccupiedCount, VacantCount, TotalRooms]
     */
    public int[] getRoomOccupancyStats(int apartmentId) {
        int totalRooms = 0;
        int occupiedRooms = 0;

        try (Connection conn = DBConnection.connect()) {
            // Get Total Rooms from the apartment table
            String sqlTotal = "SELECT total_rooms FROM apartments WHERE apartment_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sqlTotal)) {
                ps1.setInt(1, apartmentId);
                ResultSet rs1 = ps1.executeQuery();
                if (rs1.next()) totalRooms = rs1.getInt("total_rooms");
            }

            // Count Occupied Rooms (Approved Tenants)
            String sqlOccupied = "SELECT COUNT(*) AS occupied FROM registered_tenants " +
                                 "WHERE target_apartment_id = ? AND approval_status = 'APPROVED'";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlOccupied)) {
                ps2.setInt(1, apartmentId);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) occupiedRooms = rs2.getInt("occupied");
            }

            int vacantRooms = totalRooms - occupiedRooms;
            return new int[] { occupiedRooms, vacantRooms, totalRooms };

        } catch (Exception e) {
            LOGGER.severe("Occupancy Stats Error: " + e.getMessage());
        }
        return new int[] {0, 0, 0};
    }

    /**
     * Gets the latest complaints for the dashboard.
     * Returns a list of strings: "Room 7: Did you not hear the NOISE?"
     */
    public List<String> getRecentComplaints(int apartmentId) {
        List<String> complaints = new ArrayList<>();
        String sql = "SELECT room_number, message FROM complaints WHERE apartment_id = ? ORDER BY complaint_id DESC LIMIT 5";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add("Room " + rs.getString("room_number") + "\n" + rs.getString("message"));
            }
        } catch (Exception e) {
            LOGGER.severe("Complaints Error: " + e.getMessage());
        }
        return complaints;
    }

    // ==========================================================
    // 2. EXPENSES & ROOMS TABS (Images 4 & 5)
    // ==========================================================

    /**
     * Sums up all Utilities for the Expenses screen.
     * Returns array: [TotalElectricity, TotalWater, TotalInternet]
     */
    public double[] getExpensesSummation(int apartmentId) {
        String sql = "SELECT SUM(electricity_amount) AS total_elec, SUM(water_amount) AS total_water, " +
                     "SUM(internet_amount) AS total_net FROM room_bills WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[] {
                    rs.getDouble("total_elec"),
                    rs.getDouble("total_water"),
                    rs.getDouble("total_net")
                };
            }
        } catch (Exception e) {
            LOGGER.severe("Expenses Summation Error: " + e.getMessage());
        }
        return new double[] {0.0, 0.0, 0.0};
    }

    /**
     * Updates the exact bills for a specific room when you click the "UPDATE" button on the Rooms tab.
     */
    public boolean updateRoomBills(int apartmentId, String roomNumber, double rent, double elec, double water, double internet) {
        // First check if a bill record exists for this room. If not, INSERT. If yes, UPDATE.
        String checkSql = "SELECT bill_id FROM room_bills WHERE apartment_id = ? AND room_number = ?";
        String updateSql = "UPDATE room_bills SET rent_amount=?, electricity_amount=?, water_amount=?, internet_amount=? WHERE apartment_id=? AND room_number=?";
        String insertSql = "INSERT INTO room_bills (rent_amount, electricity_amount, water_amount, internet_amount, apartment_id, room_number) VALUES (?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, apartmentId);
                psCheck.setString(2, roomNumber);
                ResultSet rs = psCheck.executeQuery();
                exists = rs.next();
            }

            String finalSql = exists ? updateSql : insertSql;
            try (PreparedStatement psFinal = conn.prepareStatement(finalSql)) {
                psFinal.setDouble(1, rent);
                psFinal.setDouble(2, elec);
                psFinal.setDouble(3, water);
                psFinal.setDouble(4, internet);
                psFinal.setInt(5, apartmentId);
                psFinal.setString(6, roomNumber);
                return psFinal.executeUpdate() > 0;
            }
        } catch (Exception e) {
            LOGGER.severe("Room Update Error: " + e.getMessage());
        }
        return false;
    }

    // ==========================================================
    // 3. TO DO'S TAB (Image 6)
    // ==========================================================

    /**
     * Marks a maintenance request as "COMPLETED" when the green DONE button is clicked.
     */
    public boolean markMaintenanceDone(int requestId) {
        String sql = "UPDATE maintenance_requests SET status = 'COMPLETED' WHERE request_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================================
    // 4. TENANTS TAB (Image 9)
    // ==========================================================

    /**
     * Removes/Evicts a tenant when the red Trash Can button is clicked.
     */
    public boolean evictTenant(int tenantId) {
        String sql = "UPDATE registered_tenants SET approval_status = 'EVICTED', is_active = 0, moved_out_date = ? WHERE tenant_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.time.LocalDate.now().toString());
            ps.setInt(2, tenantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Updates a single specific bill type (Rent, Electricity, Water, or Internet) 
     * from the individual modal pop-ups in the Owner's Rooms tab.
     * 
     * @param billType MUST be "rent", "electricity", "water", or "internet"
     */
    public boolean updateSpecificBill(int apartmentId, String roomNumber, String billType, double amount, String dueDate) {
        
        // Match the column names in the database
        String amountCol = billType + "_amount";
        String dateCol = billType + "_due_date";

        // First, check if this room already has a billing record
        String checkSql = "SELECT bill_id FROM room_bills WHERE apartment_id = ? AND room_number = ?";
        
        try (Connection conn = DBConnection.connect()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, apartmentId);
                psCheck.setString(2, roomNumber);
                ResultSet rs = psCheck.executeQuery();
                exists = rs.next();
            }

            if (exists) {
                // Update existing record
                String updateSql = "UPDATE room_bills SET " + amountCol + " = ?, " + dateCol + " = ? WHERE apartment_id = ? AND room_number = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setDouble(1, amount);
                    psUpdate.setString(2, dueDate);
                    psUpdate.setInt(3, apartmentId);
                    psUpdate.setString(4, roomNumber);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                // Insert brand new record for this room
                String insertSql = "INSERT INTO room_bills (apartment_id, room_number, " + amountCol + ", " + dateCol + ") VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setInt(1, apartmentId);
                    psInsert.setString(2, roomNumber);
                    psInsert.setDouble(3, amount);
                    psInsert.setString(4, dueDate);
                    return psInsert.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Specific Bill Update Error: " + e.getMessage());
        }
        return false;
    }
    
    // --- OWNER HISTORY TAB METHODS ---

    // 1. Fetch Bills History (Only Paid Bills)
    public List<String> getBillsHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT room_number, total, payment_date FROM bills JOIN registered_tenants ON bills.tenant_id = registered_tenants.tenant_id WHERE bills.apartment_id = ? AND bills.paid = 1 ORDER BY bills.payment_date DESC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add("Room " + rs.getString("room_number") + " | PHP " + rs.getDouble("total") + " | Paid: " + rs.getString("payment_date"));
            }
        } catch (Exception e) { LOGGER.severe("Bills History Error: " + e.getMessage()); }
        return list;
    }

    // 2. Fetch Maintenance History (Only Completed Requests)
    public List<String> getMaintenanceHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT room_number, issue_description, date_resolved FROM maintenance_requests WHERE apartment_id = ? AND status = 'COMPLETED' ORDER BY date_resolved DESC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add("Room " + rs.getString("room_number") + " | " + rs.getString("issue_description") + " | Resolved: " + rs.getString("date_resolved"));
            }
        } catch (Exception e) { LOGGER.severe("Maintenance History Error: " + e.getMessage()); }
        return list;
    }

    // 3. Fetch Notification History (Past Broadcasts)
    public List<String> getNotificationHistory(int apartmentId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT title, message, date_posted FROM announcements WHERE apartment_id = ? ORDER BY date_posted DESC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("title") + " - " + rs.getString("date_posted") + "\n" + rs.getString("message"));
            }
        } catch (Exception e) { LOGGER.severe("Notification History Error: " + e.getMessage()); }
        return list;
    }
    
    // NEW: Fetches the left side of the "To Do's" screen (Pending Maintenance)
    public List<String[]> getPendingMaintenance(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT request_id, room_number, issue_description FROM maintenance_requests WHERE apartment_id = ? AND status = 'PENDING' ORDER BY date_reported ASC";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // Returning array: [Request ID, Room Number, Issue]
                list.add(new String[] {
                    String.valueOf(rs.getInt("request_id")), 
                    rs.getString("room_number"), 
                    rs.getString("issue_description")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Pending Maintenance Error: " + e.getMessage());
        }
        return list;
    }
    
    
    // NEW: Replaces the slow 1-by-1 update with a massive batch update for the big green UPDATE button
    public boolean batchUpdateRoomBills(int apartmentId, List<String> roomNumbers, List<Double> rents, List<Double> elecs, List<Double> waters, List<Double> internets) {
        String updateSql = "UPDATE room_bills SET rent_amount=?, electricity_amount=?, water_amount=?, internet_amount=? WHERE apartment_id=? AND room_number=?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            
            for (int i = 0; i < roomNumbers.size(); i++) {
                ps.setDouble(1, rents.get(i));
                ps.setDouble(2, elecs.get(i));
                ps.setDouble(3, waters.get(i));
                ps.setDouble(4, internets.get(i));
                ps.setInt(5, apartmentId);
                ps.setString(6, roomNumbers.get(i));
                
                ps.addBatch(); // Queue it up!
            }
            
            int[] results = ps.executeBatch(); // Fire all at once
            return results.length > 0;
            
        } catch (Exception e) {
            LOGGER.severe("Batch Room Update Error: " + e.getMessage());
            return false;
        }
    }
    
    // NEW: Fetches pending room viewings to populate the left side of the Owner's Inquiry screen
    public List<String[]> getPendingRoomViewings(int apartmentId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT schedule_id, tenant_name, room_number, schedule_date FROM viewing_schedule WHERE apartment_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // Returns: [ID, Name, Room, Date]
                list.add(new String[] {
                    String.valueOf(rs.getInt("schedule_id")),
                    rs.getString("tenant_name"),
                    "Room " + rs.getString("room_number"),
                    rs.getString("schedule_date")
                });
            }
        } catch (Exception e) {
            LOGGER.severe("Get Viewings Error: " + e.getMessage());
        }
        return list;
    }
}