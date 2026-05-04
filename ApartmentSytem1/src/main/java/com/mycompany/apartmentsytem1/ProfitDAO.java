package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfitDAO {

    // 1. Matches financeService.getMonthlyReport()
    public double getMonthlyRevenue(int apartmentId, String month) {
        double total = 0.0;
        // Sums actual money collected (amount_paid) from the bills table
        String sql = "SELECT SUM(amount_paid) AS total_revenue FROM bills WHERE apartment_id = ? AND month LIKE ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ps.setString(2, month + "%"); // Matches exactly (e.g., "2026-05%")
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                total = rs.getDouble("total_revenue");
            }
        } catch (Exception e) {
            System.out.println("Monthly Revenue Error: " + e.getMessage());
        }
        return total;
    }

    // 2. Matches financeService.getAnnualReport()
    public double getAnnualRevenue(int apartmentId, String year) {
        double total = 0.0;
        // Sums actual money collected for the whole year
        String sql = "SELECT SUM(amount_paid) AS total_revenue FROM bills WHERE apartment_id = ? AND month LIKE ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ps.setString(2, year + "%"); // Matches exactly (e.g., "2026%")
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                total = rs.getDouble("total_revenue");
            }
        } catch (Exception e) {
            System.out.println("Annual Revenue Error: " + e.getMessage());
        }
        return total;
    }

    // 3. Matches financeService.getAnnualReport() ROI Math
    public double getCapitalTotal(int apartmentId) {
        double capital = 0.0;
        // Pulls the owner's initial investment from the apartments table
        String sql = "SELECT capital FROM apartments WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                capital = rs.getDouble("capital");
            }
        } catch (Exception e) {
            System.out.println("Capital Retrieval Error: " + e.getMessage());
        }
        return capital;
    }
}