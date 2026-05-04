package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfitDAO {

    // 1. REVENUE FROM BILLS (MONTHLY)
    public double getMonthlyRevenue(int apartmentId, String month) {
        double revenue = 0;
        String sql = "SELECT SUM(total) AS income FROM bills WHERE apartment_id = ? AND month = ? AND paid = 1";
        try (Connection conn = DBConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, apartmentId);
            pstmt.setString(2, month);
            try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) revenue = rs.getDouble("income"); }
        } catch (Exception e) { System.out.println("Monthly Rev Error: " + e.getMessage()); }
        return revenue;
    }

    // 2. REVENUE FROM BILLS (ANNUAL)
    public double getAnnualRevenue(int apartmentId, String year) {
        double revenue = 0;
        String sql = "SELECT SUM(total) AS income FROM bills WHERE apartment_id = ? AND month LIKE ? AND paid = 1";
        try (Connection conn = DBConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, apartmentId);
            pstmt.setString(2, "%" + year);
            try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) revenue = rs.getDouble("income"); }
        } catch (Exception e) { System.out.println("Annual Rev Error: " + e.getMessage()); }
        return revenue;
    }

    // 3. GET CAPITAL FROM APARTMENTS TABLE
    public double getCapitalTotal(int apartmentId) {
        double capital = 0;
        String sql = "SELECT capital FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, apartmentId);
            try (ResultSet rs = pstmt.executeQuery()) { if (rs.next()) capital = rs.getDouble("capital"); }
        } catch (Exception e) { System.out.println("Capital Error: " + e.getMessage()); }
        return capital;
    }
}