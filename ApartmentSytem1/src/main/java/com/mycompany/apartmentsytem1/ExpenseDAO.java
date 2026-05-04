package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExpenseDAO {

    // 1. ADD WHOLE BUILDING UTILITY EXPENSE
    public boolean addBuildingExpense(int apartmentId, String category, double amount, String date, String month, String description) {
        String sql = "INSERT INTO expenses (apartment_id, room_number, expense_category, amount, expense_date, month, description) VALUES (?, NULL, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, category);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            ps.setString(5, month);
            ps.setString(6, description);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { System.out.println("Building Expense Error: " + e.getMessage()); return false; }
    }

    // 2. ADD ROOM-SPECIFIC EXPENSE (Maintenance)
    public boolean addRoomExpense(int apartmentId, String roomNumber, String category, double amount, String date, String month, String description) {
        String sql = "INSERT INTO expenses (apartment_id, room_number, expense_category, amount, expense_date, month, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, category);
            ps.setDouble(4, amount);
            ps.setString(5, date);
            ps.setString(6, month);
            ps.setString(7, description);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { System.out.println("Room Expense Error: " + e.getMessage()); return false; }
    }

    // 3. READ BUILDING EXPENSES ONLY
    public double getBuildingExpensesOnly(int apartmentId, String month) {
        double total = 0;
        // CHANGED: "month = ?" is now "month LIKE ?"
        String sql = "SELECT SUM(amount) AS total FROM expenses WHERE apartment_id = ? AND month LIKE ? AND room_number IS NULL";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total = rs.getDouble("total"); }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    // 4. READ ROOM EXPENSES ONLY
    public double getRoomExpensesOnly(int apartmentId, String month) {
        double total = 0;
        // CHANGED: "month = ?" is now "month LIKE ?"
        String sql = "SELECT SUM(amount) AS total FROM expenses WHERE apartment_id = ? AND month LIKE ? AND room_number IS NOT NULL";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total = rs.getDouble("total"); }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }
}