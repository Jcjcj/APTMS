package com.mycompany.apartmentsytem1;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PenaltyManager {

    private BillingDAO billingDAO; // kept in case you use it elsewhere

    public PenaltyManager(BillingDAO billingDAO) {
        this.billingDAO = billingDAO;
        System.out.println("Applying penalties via PenaltyManager...");
    }

    // =========================================================
    // APPLY PENALTY
    //
    // Formula (fixed monthly):
    //   base    = rent + electricity + water + internet + tax
    //   months  = full months since due date (minimum 1 if overdue)
    //   penalty = base × penalty_rate × months
    //
    // Safe to call repeatedly because we only touch bills
    // where paid = 0 AND penalty = 0.
    // =========================================================
    public void applyPenalty(Connection conn, int billId) {
        String sql =
            "SELECT b.rent, b.electricity, b.water, b.internet, b.tax, " +
            "       b.due_date, a.penalty_rate " +
            "FROM bills b " +
            "JOIN apartments a ON b.apartment_id = a.apartment_id " +
            "WHERE b.bill_id = ? AND b.paid = 0 AND b.penalty = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;  // already penalised or paid / not found

            LocalDate due = LocalDate.parse(rs.getString("due_date"));
            LocalDate now = LocalDate.now();
            if (!now.isAfter(due)) return;  // not overdue yet

            double penaltyRate = rs.getDouble("penalty_rate");
            double base = rs.getDouble("rent")
                         + rs.getDouble("electricity")
                         + rs.getDouble("water")
                         + rs.getDouble("internet")
                         + rs.getDouble("tax");

            // 1 unit per full month overdue; minimum 1 if overdue at all
            long monthsLate = ChronoUnit.MONTHS.between(due, now);
            if (monthsLate <= 0) monthsLate = 1;

            double penalty  = base * penaltyRate * monthsLate;
            double newTotal = base + penalty;

            String update =
                "UPDATE bills SET penalty = ?, total = ?, penalty_applied_at = ? " +
                "WHERE bill_id = ?";
            try (PreparedStatement up = conn.prepareStatement(update)) {
                up.setDouble(1, penalty);
                up.setDouble(2, newTotal);
                up.setString(3, now.toString());
                up.setInt(4, billId);
                up.executeUpdate();
            }

            System.out.printf(
                "Penalty applied → Bill #%d | Months late: %d | " +
                "Rate: %.4f | Penalty: %.2f | New Total: %.2f%n",
                billId, monthsLate, penaltyRate, penalty, newTotal
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // AUTO APPLY PENALTIES — DAILY SCHEDULER
    // Only processes bills that are overdue and not yet penalised.
    public void applyPenaltiesForOverdueBills() {
        String sql = "SELECT bill_id, due_date FROM bills WHERE paid = 0 AND penalty = 0";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                LocalDate due = LocalDate.parse(rs.getString("due_date"));
                if (LocalDate.now().isAfter(due)) {
                    int billId = rs.getInt("bill_id");
                    applyPenalty(conn, billId);
                    notifyTenant(billId);
                    count++;
                }
            }
            System.out.println("Penalty run complete. Bills processed: " + count);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NOTIFY TENANT (console only)
    public void notifyTenant(int billId) {
        String sql =
            "SELECT t.name, t.email, b.total, b.due_date, b.penalty " +
            "FROM bills b " +
            "JOIN registered_tenants t ON b.tenant_id = t.tenant_id " +
            "WHERE b.bill_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.printf(
                    "OVERDUE NOTICE → %s (%s) | Bill #%d | Due: %s | " +
                    "Total: %.2f (penalty: %.2f)%n",
                    rs.getString("name"),
                    rs.getString("email"),
                    billId,
                    rs.getString("due_date"),
                    rs.getDouble("total"),
                    rs.getDouble("penalty")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
