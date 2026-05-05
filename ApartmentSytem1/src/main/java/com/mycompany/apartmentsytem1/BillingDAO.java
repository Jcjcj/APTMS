package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillingDAO {

    // =========================================================
    // EXISTING METHOD: CREATE BILL (Untouched to prevent conflicts)
    // =========================================================
    public int createBill(int tenantId,
                          String month,
                          double rent,
                          String electricityType,
                          Double electricityUsage, 
                          Double electricityFixedFee, 
                          String waterType,
                          Double waterUsage,       
                          Double waterFixedFee,
                          Double internetInput,
                          String dueDate) { 
        
        validateBillInput(tenantId, month, rent, electricityType, electricityUsage,
                electricityFixedFee, waterType, waterUsage, waterFixedFee, "0000-00-00");

        try (Connection conn = DBConnection.connect()) {
            
            String sql = "SELECT a.apartment_id, a.electricity_type, a.elec_rate, "
                       + "a.water_type, a.water_rate, a.internet_type, a.internet_rate, "
                       + "r.room_id, r.current_elec_reading, r.current_water_reading, "
                       + "ro.move_in_date " 
                       + "FROM registered_tenants t "
                       + "JOIN room_occupancy ro ON t.tenant_id = ro.tenant_id "
                       + "JOIN rooms r ON ro.room_number = r.room_number AND ro.apartment_id = r.apartment_id "
                       + "JOIN apartments a ON r.apartment_id = a.apartment_id "
                       + "WHERE t.tenant_id = ? AND ro.status = 'Current' "
                       + "ORDER BY ro.occupancy_id DESC LIMIT 1";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Tenant, active room, or apartment not found for tenantId=" + tenantId);
                    }

                    int apartmentId = rs.getInt("apartment_id");
                    int roomId = rs.getInt("room_id");
                    
                    LocalDate moveInDate = LocalDate.parse(rs.getString("move_in_date"));
                    long daysSinceMoveIn = ChronoUnit.DAYS.between(moveInDate, LocalDate.now());
                    if (daysSinceMoveIn < 0) daysSinceMoveIn = 0; 
                    
                    long cycles = daysSinceMoveIn / 30; 
                    LocalDate calculatedDueDate = moveInDate.plusDays((cycles + 1) * 30);
                    String finalDueDate = calculatedDueDate.toString();
                    
                    String dbElecType = rs.getString("electricity_type");
                    double elecRate = rs.getDouble("elec_rate");
                    double prevElecReading = rs.getDouble("current_elec_reading");

                    String dbWaterType = rs.getString("water_type");
                    double waterRate = rs.getDouble("water_rate");
                    double prevWaterReading = rs.getDouble("current_water_reading");

                    String dbInternetType = rs.getString("internet_type");
                    double internetRate = rs.getDouble("internet_rate");

                    double elec = calculateUtilityCost(dbElecType, electricityUsage, prevElecReading, elecRate);
                    double water = calculateUtilityCost(dbWaterType, waterUsage, prevWaterReading, waterRate);

                    double internet = 0.0;
                    if ("POSTPAID".equalsIgnoreCase(dbInternetType)) {
                        internet = internetRate; 
                    } else if ("PREPAID".equalsIgnoreCase(dbInternetType)) {
                        internet = 0.0; 
                    } else {
                        internet = internetInput != null ? internetInput : 0.0; 
                    }

                    double taxRate = 0.0; 
                    double subtotal = rent + elec + water + internet;
                    double tax = subtotal * taxRate;
                    double total = subtotal + tax;

                    updateRoomReadings(conn, roomId, dbElecType, electricityUsage, dbWaterType, waterUsage);

                    String insertSql = "INSERT INTO bills "
                                     + "(tenant_id, apartment_id, month, rent, electricity, water, "
                                     + "internet, tax, penalty, total, due_date, paid) "
                                     + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        insertPs.setInt(1, tenantId);
                        insertPs.setInt(2, apartmentId);
                        insertPs.setString(3, month);
                        insertPs.setDouble(4, rent);
                        insertPs.setDouble(5, elec);
                        insertPs.setDouble(6, water);
                        insertPs.setDouble(7, internet);
                        insertPs.setDouble(8, tax);
                        insertPs.setDouble(9, 0.00); 
                        insertPs.setDouble(10, total);
                        insertPs.setString(11, finalDueDate); 
                        insertPs.setInt(12, 0); 

                        insertPs.executeUpdate();

                        try (ResultSet keys = insertPs.getGeneratedKeys()) {
                            if (keys.next()) {
                                int newId = keys.getInt(1);
                                System.out.printf("Bill #%d created | Tenant: %d | Due: %s | Total: %.2f%n",
                                        newId, tenantId, finalDueDate, total);
                                return newId;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // =========================================================
    // EXISTING HELPERS (Untouched)
    // =========================================================
    private void updateRoomReadings(Connection conn, int roomId, String elecType, Double newElec, String waterType, Double newWater) throws Exception {
        String sql = "UPDATE rooms SET current_elec_reading = ?, current_water_reading = ? WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ("METER".equalsIgnoreCase(elecType) || "SUBMETER".equalsIgnoreCase(elecType)) && newElec != null ? newElec : 0.0);
            ps.setDouble(2, ("METER".equalsIgnoreCase(waterType) || "SUBMETER".equalsIgnoreCase(waterType)) && newWater != null ? newWater : 0.0);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        }
    }

    private void validateBillInput(int tenantId, String month, double rent, String electricityType, Double electricityUsage, Double electricityFixedFee, String waterType, Double waterUsage, Double waterFixedFee, String dueDate) {
        if (tenantId <= 0) throw new IllegalArgumentException("error");
        if (month == null || month.isBlank()) throw new IllegalArgumentException("month is required");
        if (rent < 0) throw new IllegalArgumentException("rent must be non-negative");
        validateUtilityInput("electricity", electricityType, electricityUsage, electricityFixedFee);
        validateUtilityInput("water", waterType, waterUsage, waterFixedFee);
    }

    private void validateUtilityInput(String name, String type, Double usage, Double fixedFee) {
        if (type != null && !"fixed".equalsIgnoreCase(type) && !"meter".equalsIgnoreCase(type) && !"submeter".equalsIgnoreCase(type)) {
        }
    }

    private double calculateUtilityCost(String type, Double currentReading, Double previousReading, double dbRate) {
        if (type == null) return 0.0;
        if ("FIXED".equalsIgnoreCase(type)) {
            return dbRate; 
        } else if ("METER".equalsIgnoreCase(type) || "SUBMETER".equalsIgnoreCase(type)) {
            if (currentReading == null || previousReading == null) return 0.0;
            double consumption = currentReading - previousReading;
            return Math.max(0, consumption) * dbRate; 
        }
        return 0.0;
    }
    
    // =========================================================
    // EXISTING METHOD: PAY BILL (Untouched)
    // =========================================================
  public boolean payBill(int billId, double paymentAmount, String paymentDate, String paymentMethod, String referenceNumber) {
        String sql = "UPDATE bills SET amount_paid = amount_paid + ?, "
                   + "payment_date = ?, payment_method = ?, reference_number = ?, "
                   + "paid = CASE WHEN (amount_paid + ?) >= total THEN 1 ELSE 0 END "
                   + "WHERE bill_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, paymentAmount);
            ps.setString(2, paymentDate);
            ps.setString(3, paymentMethod);
            ps.setString(4, referenceNumber);
            ps.setDouble(5, paymentAmount);
            ps.setInt(6, billId);

            // 1. Capture the result instead of returning it immediately
            int rowsAffected = ps.executeUpdate();

            // 2. If the payment was successfully saved to the database, trigger the alert!
            if (rowsAffected > 0) {
                triggerPaymentNotification(billId, paymentAmount); 
                return true;
            }

        } catch (Exception e) {
            System.out.println("Payment Error: " + e.getMessage());
        }
        return false;
    }

    // 3. Add this helper method directly below payBill
    private void triggerPaymentNotification(int billId, double amountPaid) {
        // Look up the missing details (tenant name, room, owner) using the billId
        String sql = "SELECT t.username, t.room_number, a.owner_username " +
                     "FROM bills b " +
                     "JOIN tenants t ON b.room_number = t.room_number AND b.apartment_id = t.apartment_id " +
                     "JOIN apartments a ON b.apartment_id = a.apartment_id " +
                     "WHERE b.bill_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String tenantName = rs.getString("username");
                String roomNumber = rs.getString("room_number");
                String ownerUsername = rs.getString("owner_username");
                
                // Send the alert to the owner!
                NotificationDAO notificationEngine = new NotificationDAO();
                notificationEngine.notifyOwnerTenantPaid(ownerUsername, tenantName, roomNumber, amountPaid);
            }
        } catch (Exception e) {
            System.out.println("Notification Error: " + e.getMessage());
        }
    }
    // =========================================================
    // NEW IMPROVEMENT 1: ARREARS TRACKER (No Conflicts)
    // =========================================================
    public double getOutstandingBalance(int tenantId) {
        double arrears = 0.0;
        // Calculates debt purely from the bills table using existing column names
        String sql = "SELECT SUM(total - amount_paid) AS total_debt FROM bills WHERE tenant_id = ? AND paid = 0";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                arrears = rs.getDouble("total_debt");
            }
        } catch (Exception e) {
            System.out.println("Arrears Calculation Error: " + e.getMessage());
        }
        return arrears;
    }

    // =========================================================
    // NEW IMPROVEMENT 2: AUTOMATED PENALTIES (No Conflicts)
    // =========================================================
    public void applyLatePenalties(String currentDate) {
        // Enforces your rule: penalty rate depends on the owner of the registered apartment.
        // It strictly updates the existing penalty columns in your bills table.
        String findOverdueSql = 
            "SELECT b.bill_id, b.total, a.penalty_rate " +
            "FROM bills b " +
            "JOIN apartments a ON b.apartment_id = a.apartment_id " +
            "WHERE b.paid = 0 AND b.due_date < ? AND b.penalty = 0";

        String updatePenaltySql = 
            "UPDATE bills SET penalty = ?, total = total + ?, penalty_applied_at = ? WHERE bill_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement findPs = conn.prepareStatement(findOverdueSql);
             PreparedStatement updatePs = conn.prepareStatement(updatePenaltySql)) {
            
            findPs.setString(1, currentDate);
            ResultSet rs = findPs.executeQuery();

            int updatedCount = 0;
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                double currentTotal = rs.getDouble("total");
                double penaltyRate = rs.getDouble("penalty_rate"); 
                
                double penaltyAmount = currentTotal * penaltyRate;

                updatePs.setDouble(1, penaltyAmount);
                updatePs.setDouble(2, penaltyAmount);
                updatePs.setString(3, currentDate);
                updatePs.setInt(4, billId);
                updatePs.addBatch();
                updatedCount++;
            }
            
            if (updatedCount > 0) {
                updatePs.executeBatch();
                System.out.println("System Check: Applied owner-specific penalties to " + updatedCount + " overdue bills.");
            }

        } catch (Exception e) {
            System.out.println("Penalty Automation Error: " + e.getMessage());
        }
    }
}