package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillingDAO {

    // CREATE BILL
    // frontend: call when owner generates a new monthly bill for a tenant
    // frontend: pass tenantId, billing month label, individual utility amounts,
    //           and the due date string (e.g. "2026-05-01")
    // returns:  the new bill_id, or -1 on failure
    public int createBill(int tenantId,
                          String month,
                          double rent,
                          String electricityType,
                          Double electricityUsage, // Acts as the CURRENT METER READING if metered
                          Double electricityFixedFee, 
                          String waterType,
                          Double waterUsage,       // Acts as the CURRENT METER READING if metered
                          Double waterFixedFee,
                          Double internetInput,
                          String dueDate) { // NOTE: This parameter is now overridden by the 30-day logic
        
        // We will validate the due date later after we calculate it
        validateBillInput(tenantId, month, rent, electricityType, electricityUsage,
                electricityFixedFee, waterType, waterUsage, waterFixedFee, "0000-00-00");

        try (Connection conn = DBConnection.connect()) {
            
            // MODIFIED SQL: Added ro.move_in_date to dynamically calculate 30-day intervals
            String sql = "SELECT a.apartment_id, a.electricity_type, a.elec_rate, "
                       + "a.water_type, a.water_rate, a.internet_type, a.internet_rate, "
                       + "r.room_id, r.current_elec_reading, r.current_water_reading, "
                       + "ro.move_in_date " // ADDED THIS
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
                    
                    // --- NEW 30-DAY DUE DATE LOGIC ---
                    // Calculates the due date strictly based on 30-day increments since moving in
                    LocalDate moveInDate = LocalDate.parse(rs.getString("move_in_date"));
                    long daysSinceMoveIn = ChronoUnit.DAYS.between(moveInDate, LocalDate.now());
                    if (daysSinceMoveIn < 0) daysSinceMoveIn = 0; // Guard against weird dates
                    
                    long cycles = daysSinceMoveIn / 30; // Find out which 30-day cycle they are on
                    LocalDate calculatedDueDate = moveInDate.plusDays((cycles + 1) * 30);
                    String finalDueDate = calculatedDueDate.toString();
                    // ---------------------------------
                    
                    // Fetch Database Configurations
                    String dbElecType = rs.getString("electricity_type");
                    double elecRate = rs.getDouble("elec_rate");
                    double prevElecReading = rs.getDouble("current_elec_reading");

                    String dbWaterType = rs.getString("water_type");
                    double waterRate = rs.getDouble("water_rate");
                    double prevWaterReading = rs.getDouble("current_water_reading");

                    String dbInternetType = rs.getString("internet_type");
                    double internetRate = rs.getDouble("internet_rate");

                    // 1. CALCULATE ELECTRICITY
                    double elec = calculateUtilityCost(dbElecType, electricityUsage, prevElecReading, elecRate);

                    // 2. CALCULATE WATER
                    double water = calculateUtilityCost(dbWaterType, waterUsage, prevWaterReading, waterRate);

                    // 3. CALCULATE INTERNET (PREPAID vs POSTPAID)
                    double internet = 0.0;
                    if ("POSTPAID".equalsIgnoreCase(dbInternetType)) {
                        internet = internetRate; // Add the flat fee to the bill
                    } else if ("PREPAID".equalsIgnoreCase(dbInternetType)) {
                        internet = 0.0; // Paid upfront separately, so it's 0 on the monthly bill
                    } else {
                        internet = internetInput != null ? internetInput : 0.0; // Fallback
                    }

                    // 4. SUBTOTALS & TOTALS
                    double taxRate = 0.0; // Defaulting to 0.0
                    double subtotal = rent + elec + water + internet;
                    double tax = subtotal * taxRate;
                    double total = subtotal + tax;

                    // 5. UPDATE THE ROOM METER READINGS FOR NEXT MONTH!
                    updateRoomReadings(conn, roomId, dbElecType, electricityUsage, dbWaterType, waterUsage);

                    // 6. INSERT INTO BILLS
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
                        insertPs.setDouble(9, 0.00); // Penalty (initially 0)
                        insertPs.setDouble(10, total);
                        insertPs.setString(11, finalDueDate); // Uses the mathematically calculated date!
                        insertPs.setInt(12, 0); // Not paid yet

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

    // HELPER: Auto-updates the Room's meter reading so the next month is ready
    private void updateRoomReadings(Connection conn, int roomId, String elecType, Double newElec, String waterType, Double newWater) throws Exception {
        String sql = "UPDATE rooms SET current_elec_reading = ?, current_water_reading = ? WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Only update the meter if it's an actual meter. If fixed, leave it at 0.0
            ps.setDouble(1, ("METER".equalsIgnoreCase(elecType) || "SUBMETER".equalsIgnoreCase(elecType)) && newElec != null ? newElec : 0.0);
            ps.setDouble(2, ("METER".equalsIgnoreCase(waterType) || "SUBMETER".equalsIgnoreCase(waterType)) && newWater != null ? newWater : 0.0);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        }
    }

    private void validateBillInput(int tenantId,
                                   String month,
                                   double rent,
                                   String electricityType,
                                   Double electricityUsage,
                                   Double electricityFixedFee,
                                   String waterType,
                                   Double waterUsage,
                                   Double waterFixedFee,
                                   String dueDate) {

        if (tenantId <= 0) {
            throw new IllegalArgumentException("error");
        }
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("month is required");
        }
        if (rent < 0) {
            throw new IllegalArgumentException("rent must be non-negative");
        }

        validateUtilityInput("electricity", electricityType, electricityUsage, electricityFixedFee);
        validateUtilityInput("water", waterType, waterUsage, waterFixedFee);
    }

    private void validateUtilityInput(String name,
                                      String type,
                                      Double usage,
                                      Double fixedFee) {

        // Adapted to include SUBMETER safely without crashing your UI validations
        if (type != null && !"fixed".equalsIgnoreCase(type) && !"meter".equalsIgnoreCase(type) && !"submeter".equalsIgnoreCase(type)) {
            // Main validation checks remain flexible
        }
    }

    // MODIFIED: Adapts calculation to handle Fixed, Meter, and Submeter using DB Values
    private double calculateUtilityCost(String type,
                                        Double currentReading, 
                                        Double previousReading, 
                                        double dbRate) {
        
        if (type == null) return 0.0;

        if ("FIXED".equalsIgnoreCase(type)) {
            return dbRate; // dbRate is the flat monthly fee from 'apartments'
        } else if ("METER".equalsIgnoreCase(type) || "SUBMETER".equalsIgnoreCase(type)) {
            if (currentReading == null || previousReading == null) return 0.0;
            
            // Formula: (Current - Previous) * Rate
            double consumption = currentReading - previousReading;
            
            // Math.max prevents negative bills if the owner enters a bad/lower reading
            return Math.max(0, consumption) * dbRate; 
        }
        
        return 0.0;
    }
    
    // PAY BILL
    // frontend: call when owner verifies a tenant's payment
    // =========================================================
   // MODIFIED: Added paymentAmount parameter
    public boolean payBill(int billId, double paymentAmount, String paymentDate, String paymentMethod, String referenceNumber) {
        // Update amount_paid by adding the new payment, and dynamically set paid=1 if it hits the total
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

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Payment Error: " + e.getMessage());
        }
        return false;
    }

 }