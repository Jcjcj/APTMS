package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class AnalyticsDAO {
    private static final Logger LOGGER = Logger.getLogger(AnalyticsDAO.class.getName());

    public void printOccupancyStats(int apartmentId) {
        String sql = "SELECT apartment_name, total_rooms, rooms_available FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("apartment_name");
                int total = rs.getInt("total_rooms");
                int available = rs.getInt("rooms_available");
                int occupied = total - available;
                
                // Safety check for division by zero
                double occupancyRate = (total > 0) ? ((double) occupied / total) * 100 : 0;
                
                System.out.println("--- OCCUPANCY REPORT FOR: " + name + " ---");
                System.out.println("Total Rooms: " + total);
                System.out.println("Occupied: " + occupied);
                System.out.println("Available: " + available);
                System.out.printf("Occupancy Rate: %.2f%%\n", occupancyRate);
                
                // NEW: Logic to show price variety
                fetchPriceDetails(apartmentId);
            }
        } catch (Exception e) {
            LOGGER.severe("Occupancy Stats Error: " + e.getMessage());
        }
    }

    private void fetchPriceDetails(int apartmentId) {
        String sql = "SELECT MIN(rent_amount) as min_p, MAX(rent_amount) as max_p FROM rooms WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                System.out.printf("Rent Price Range: PHP %.2f - PHP %.2f\n", rs.getDouble("min_p"), rs.getDouble("max_p"));
            }
        } catch (Exception e) {
             LOGGER.severe("Price Detail Error: " + e.getMessage());
        }
    }

    public void printPopularBarangays() {
        String sql = "SELECT barangay, COUNT(apartment_id) as apt_count "
                   + "FROM apartments GROUP BY barangay ORDER BY apt_count DESC LIMIT 5";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("--- MOST POPULAR BARANGAYS ---");
            while (rs.next()) {
                System.out.println("Barangay: " + rs.getString("barangay") + 
                                   " | Active Apartments: " + rs.getInt("apt_count"));
            }
        } catch (Exception e) {
            LOGGER.severe("Popular Barangays Error: " + e.getMessage());
        }
    }
}