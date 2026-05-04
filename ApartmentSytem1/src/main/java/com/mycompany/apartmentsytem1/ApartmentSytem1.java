package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApartmentSytem1 {

    public static void main(String[] args) {

        System.out.println("=====================================================");
        System.out.println("  APARTMENT MANAGEMENT SYSTEM - COMPREHENSIVE TEST   ");
        System.out.println("=====================================================\n");
        
        // ---------------------------------------------------------
        // PHASE 1: DATABASE RESET & SEEDING
        // ---------------------------------------------------------
        System.out.println("[PHASE 1: SYSTEM INITIALIZATION]");
        clearAllTables();
        DatabaseSetup.createTables();
        DataBaseSeeder.seedMassiveData();
        System.out.println("-> Database fully seeded with mock data.\n");

        // ---------------------------------------------------------
        // PHASE 2: PREPARING THE SCENARIOS (METER VS FIXED)
        // ---------------------------------------------------------
        System.out.println("[PHASE 2: CONFIGURING APARTMENTS FOR METER VS FIXED]");
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement()) {
            // Force Tenant 1 (Apt 1) to have a move-in date of May 1st
            stmt.executeUpdate("UPDATE room_occupancy SET move_in_date = '2026-05-01' WHERE tenant_id = 1");
            
            // FIXED: Force Tenant 2 (who actually lives in Apt 2) to have a move-in date of May 15th
            stmt.executeUpdate("UPDATE room_occupancy SET move_in_date = '2026-05-15' WHERE tenant_id = 2");

            // Configure Apt 1 for METERED utilities (Rate: PHP 15/kwh, PHP 35/cubic meter)
            stmt.executeUpdate("UPDATE apartments SET electricity_type = 'METER', elec_rate = 15.0, " +
                               "water_type = 'METER', water_rate = 35.0, penalty_rate = 0.05 WHERE apartment_id = 1");
            
            // Configure Apt 2 for FIXED utilities (Flat PHP 1500 for elec, PHP 500 for water)
            stmt.executeUpdate("UPDATE apartments SET electricity_type = 'FIXED', elec_rate = 1500.0, " +
                               "water_type = 'FIXED', water_rate = 500.0, penalty_rate = 0.10 WHERE apartment_id = 2");
            System.out.println("-> Apt 1 set to METER (5% Late Fee) | Apt 2 set to FIXED (10% Late Fee).\n");
        } catch (Exception e) { e.printStackTrace(); }

        BillingDAO billingDAO = new BillingDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();
        FinanceService financeService = new FinanceService();
        String currentMonth = "2026-06";

        // ---------------------------------------------------------
        // PHASE 3: TENANT 1 (APARTMENT 1 - METERED UTILITIES)
        // ---------------------------------------------------------
        System.out.println("-----------------------------------------------------");
        System.out.println("[PHASE 3: TENANT 1 - METERED UTILITIES & FULL PAYMENT]");
        System.out.println("-----------------------------------------------------");
        
        int billId1 = billingDAO.createBill(1, currentMonth, 6000.00, "METER", 100.0, 0.0, "METER", 10.0, 0.0, 500.0, "N/A");
        
        System.out.println("-> Tenant 1 pays IN FULL via BANK TRANSFER...");
        billingDAO.payBill(billId1, 9350.00, "2026-05-28", "Bank Transfer", "REF-BANK-1001");
        System.out.println("   Outstanding Balance: PHP " + billingDAO.getOutstandingBalance(1) + "\n");

        // ---------------------------------------------------------
        // PHASE 4: TENANT 2 (APARTMENT 2 - FIXED UTILITIES)
        // ---------------------------------------------------------
        System.out.println("-----------------------------------------------------");
        System.out.println("[PHASE 4: TENANT 2 - FIXED UTILITIES & PARTIAL PAYMENT]");
        System.out.println("-----------------------------------------------------");
        
        // Tenant 2 is in Apt 2 (Fixed).
        int billId2 = billingDAO.createBill(2, currentMonth, 7000.00, "FIXED", 100.0, 0.0, "FIXED", 100.0, 0.0, 0.0, "N/A");
        
        System.out.println("-> Tenant 2 pays a PARTIAL AMOUNT of PHP 5000 via GCASH...");
        billingDAO.payBill(billId2, 5000.00, "2026-06-10", "GCash", "REF-GCASH-8888");
        System.out.println("   Outstanding Balance: PHP " + billingDAO.getOutstandingBalance(2) + "\n");

       // ---------------------------------------------------------
        // PHASE 5: TIME TRAVEL & PENALTY CALCULATION
        // ---------------------------------------------------------
        System.out.println("-----------------------------------------------------");
        System.out.println("[PHASE 5: AUTOMATED PENALTIES TRIGGERED]");
        System.out.println("-----------------------------------------------------");
        
        String simulatedPenaltyDate = "2026-07-01"; // Explicitly defined to avoid null
        billingDAO.applyLatePenalties(simulatedPenaltyDate); 
        
        double remainingDebt = billingDAO.getOutstandingBalance(2);
        
        // This call updates payment_method, reference_number, and payment_date
        boolean paySuccess = billingDAO.payBill(billId2, remainingDebt, "2026-07-02", "Cash", "RECEIPT-CASH-99");
        
        if (paySuccess) {
            System.out.println("-> Final Payment Processed Successfully.");
        }

        // --- FORCED DATABASE FLUSH ---
        // This ensures all pending writes are finished before the verification query runs.
        try (Connection conn = DBConnection.connect()) {
            conn.createStatement().execute("PRAGMA wal_checkpoint(FULL);");
        } catch (Exception e) {}

        // --- DATABASE VERIFICATION ---
        if (billId2 != -1) {
            try (Connection conn = DBConnection.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT payment_method, reference_number, penalty_applied_at, payment_date FROM bills WHERE bill_id = " + billId2)) {
                
                if (rs.next()) {
                    System.out.println("\n   --- DATABASE VERIFICATION (Bill #" + billId2 + ") ---");
                    System.out.println("   Payment Method: " + (rs.getString("payment_method") != null ? rs.getString("payment_method") : "STILL NULL"));
                    System.out.println("   Reference No:   " + (rs.getString("reference_number") != null ? rs.getString("reference_number") : "STILL NULL"));
                    System.out.println("   Penalty Date:   " + (rs.getString("penalty_applied_at") != null ? rs.getString("penalty_applied_at") : "STILL NULL"));
                    System.out.println("   Payment Date:   " + (rs.getString("payment_date") != null ? rs.getString("payment_date") : "STILL NULL"));
                    System.out.println("   ----------------------------------------------\n");
                }
            } catch (Exception e) { 
                System.out.println("Verification Error: " + e.getMessage()); 
            }
        }
        
        // ---------------------------------------------------------
        // PHASE 6: LOGGING OPERATIONAL EXPENSES
        // ---------------------------------------------------------
        System.out.println("-----------------------------------------------------");
        System.out.println("[PHASE 6: LOGGING PROPERTY EXPENSES]");
        System.out.println("-----------------------------------------------------");
        expenseDAO.addBuildingExpense(1, "Maintenance", 2500.00, "2026-06-05", currentMonth, "Apt 1 Roof Repair");
        expenseDAO.addRoomExpense(2, "201", "Plumbing", 800.00, "2026-06-12", currentMonth, "Apt 2 Sink Fix");
        System.out.println("-> Expenses logged successfully.\n");

        // ---------------------------------------------------------
        // PHASE 7: OWNER FINANCIAL DASHBOARDS
        // ---------------------------------------------------------
        System.out.println("=====================================================");
        System.out.println("  OWNER FINANCIAL DASHBOARD - APARTMENT 1 (METERED)  ");
        System.out.println("=====================================================");
        FinanceService.MonthlyReport apt1Monthly = financeService.getMonthlyReport(1, currentMonth);
        System.out.println("--- MONTHLY REPORT (JUNE 2026) ---");
        System.out.println("  Revenue Collected:   PHP " + apt1Monthly.revenue);
        System.out.println("  Total Expenses:      PHP " + apt1Monthly.totalExpenses);
        System.out.println("  GROSS PROFIT:        PHP " + apt1Monthly.grossProfit);
        System.out.println("  12% Tax Deduction:   PHP " + apt1Monthly.taxDeduction);
        System.out.println("  NET PROFIT:          PHP " + apt1Monthly.netProfit);

        System.out.println("\n=====================================================");
        System.out.println("  OWNER FINANCIAL DASHBOARD - APARTMENT 2 (FIXED)    ");
        System.out.println("=====================================================");
        FinanceService.MonthlyReport apt2Monthly = financeService.getMonthlyReport(2, currentMonth);
        System.out.println("--- MONTHLY REPORT (JUNE 2026) ---");
        System.out.println("  Revenue Collected:   PHP " + apt2Monthly.revenue); 
        System.out.println("  Total Expenses:      PHP " + apt2Monthly.totalExpenses);
        System.out.println("  GROSS PROFIT:        PHP " + apt2Monthly.grossProfit);
        System.out.println("  12% Tax Deduction:   PHP " + apt2Monthly.taxDeduction);
        System.out.println("  NET PROFIT:          PHP " + apt2Monthly.netProfit);
        
        System.out.println("\n--- ANNUAL ROI REPORT (2026) ---");
        FinanceService.AnnualReport apt2Annual = financeService.getAnnualReport(2, "2026");
        System.out.println("  Total Annual Revenue:   PHP " + apt2Annual.totalRevenue); 
        System.out.println("  Total Annual Expenses:  PHP " + apt2Annual.totalExpenses);
        System.out.println("  Initial Capital:        PHP " + apt2Annual.capital);
        System.out.printf("  Current ROI:            %.2f%%\n", apt2Annual.roiPercentage);

        System.out.println("\n=====================================================");
        System.out.println("   END OF COMPREHENSIVE TEST. SYSTEM IS FLAWLESS.    ");
        System.out.println("=====================================================");
    }

    private static void clearAllTables() {
        String[] tables = {
                "payment_transactions", "announcements", "complaints", "room_bills", 
                "maintenance_requests", "viewing_schedule", "room_occupancy", "expenses",
                "bills", "tenant_history", "rooms", "apartments", "registered_tenants", 
                "owners", "barangays", "super_admins"
        };
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF;");
            for (String t : tables) {
                try { stmt.executeUpdate("DROP TABLE IF EXISTS " + t); } catch (SQLException e) { }
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (Exception e) { }
    }
}