package com.mycompany.apartmentsytem1;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Connection; 
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PenaltyManager {
    
    private BillingDAO billingDAO;

    public PenaltyManager(BillingDAO billingDAO) {
        this.billingDAO = billingDAO;
        System.out.println("Applying penalties via PenaltyManager...");
    }
    
    // =========================================================
    // APPLY PENALTY
    //
    // Penalty granularity is set per apartment by the owner:
    //   "DAILY"   — penalty accrues every day overdue
    //   "WEEKLY"  — penalty accrues every week overdue
    //   "MONTHLY" — penalty accrues every month overdue (default)

    // Formula: penalty = base × penalty_rate × units_late
    //   base  = rent + electricity + water + internet + tax
    //   units = days / weeks / months since due date, per granularity
    // 
    // Safe to call repeatedly — guard: penalty=0 ensures it only
    // runs once per bill. To recalculate monthly, track
    // penalty_applied_at and compare against today instead.
    // =========================================================
    public void applyPenalty(Connection conn, int billId) {
        String sql = "SELECT b.rent, b.electricity, b.water, b.internet, b.tax, "
                   + "b.due_date, a.penalty_rate, a.penalty_granularity "
                   + "FROM bills b "
                   + "JOIN apartments a ON b.apartment_id = a.apartment_id "
                   + "WHERE b.bill_id=? AND b.paid=0 AND b.penalty=0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;  // already penalised or paid

            LocalDate due = LocalDate.parse(rs.getString("due_date"));
            LocalDate now = LocalDate.now();
            if (!now.isAfter(due)) return;  // not overdue yet

            String granularity = rs.getString("penalty_granularity");
            double units       = computePenaltyUnits(due, now, granularity);
            if (units <= 0) return;

            double penaltyRate = rs.getDouble("penalty_rate");
            double base = rs.getDouble("rent")
                        + rs.getDouble("electricity")
                        + rs.getDouble("water")
                        + rs.getDouble("internet")
                        + rs.getDouble("tax");

            double penalty  = base * penaltyRate * units;
            double newTotal = base + penalty;

            String update = "UPDATE bills SET penalty=?, total=?, penalty_applied_at=? "
                          + "WHERE bill_id=?";
            try (PreparedStatement up = conn.prepareStatement(update)) {
                up.setDouble(1, penalty);
                up.setDouble(2, newTotal);
                up.setString(3, now.toString());
                up.setInt(4,    billId);
                up.executeUpdate();
            }
            System.out.printf(
                "Penalty applied → Bill #%d | Granularity: %s | Units late: %.1f | "
                + "Rate: %.4f | Penalty: %.2f | New Total: %.2f%n",
                billId, granularity, units, penaltyRate, penalty, newTotal);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // COMPUTE PENALTY UNITS
    // Helper: returns how many days/weeks/months a bill is overdue
    // based on the apartment's penalty_granularity setting.
    private double computePenaltyUnits(LocalDate due, LocalDate now, String granularity) {
        if (granularity == null) granularity = "MONTHLY";
        switch (granularity.toUpperCase()) {
            case "DAILY":
                return ChronoUnit.DAYS.between(due, now);
            case "WEEKLY":
                return ChronoUnit.WEEKS.between(due, now);
            case "MONTHLY":
            default:
                return ChronoUnit.MONTHS.between(due, now);
        }
    }
    
    // AUTO APPLY PENALTIES — DAILY SCHEDULER
    // frontend: triggered by PenaltyScheduler, not by user action
    // Only processes bills that are overdue and not yet penalised.
    public void applyPenaltiesForOverdueBills() {
        String sql = "SELECT bill_id, due_date FROM bills WHERE paid=0 AND penalty=0";
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

    // =========================================================
    // NOTIFY TENANT
    // Currently prints to console.
    // To add email/SMS: replace System.out block with JavaMail/Twilio.
    // =========================================================
    public void notifyTenant(int billId) {
        // MODIFIED: Corrected 'tenants' table name to 'registered_tenants'
        String sql = "SELECT t.name, t.email, b.total, b.due_date, b.penalty "
                   + "FROM bills b JOIN registered_tenants t ON b.tenant_id = t.tenant_id "
                   + "WHERE b.bill_id=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.printf(
                    "OVERDUE NOTICE → %s (%s) | Bill #%d | Due: %s | "
                    + "Total: %.2f (penalty: %.2f)%n",
                    rs.getString("name"), rs.getString("email"),
                    billId,
                    rs.getString("due_date"),
                    rs.getDouble("total"),
                    rs.getDouble("penalty"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}