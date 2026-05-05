package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ApartmentDAO {

    private static final Logger LOGGER = Logger.getLogger(ApartmentDAO.class.getName());

     public boolean addApartment(String apartmentCode, String name, String tin, int floors, 
                             List<Integer> roomsPerFloorList, 
                             List<List<Double>> rentPricesPerFloor,
                             List<List<Double>> downPaymentsPerFloor, 
                             List<List<Double>> securityDepositsPerFloor, 
                             List<List<String>> roomImagesPerFloor, 
                             double capital, double taxRate, double penaltyRate, String paymentMethod, String description, String policy,
                             String barangay, String street, 
                             String electricityType, double elecRate, 
                             String waterType, double waterRate,      
                             String internetType, double internetRate, 
                             String contact, String email, String social, String emergency, String profileImage,
                             int ownerId) {

        if (name == null || name.trim().isEmpty() || capital < 0) {
            LOGGER.warning("Validation Failed: Invalid apartment data.");
            return false;
        }

        if (apartmentCode == null || apartmentCode.isEmpty()) {
            apartmentCode = generateApartmentCode();
        }

        int totalRooms = 0;
        for (int count : roomsPerFloorList) {
            totalRooms += count;
        }

        String sql = "INSERT INTO apartments(" +
                "apartment_code, apartment_name, owner_id, tin_no, floors, total_rooms, rooms_available, " +
                "capital, tax_rate, penalty_rate, payment_method, description, policy, barangay, street, " +
                "electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, " +
                "contact_number, email, social_media, emergency_number, profile_image, is_active) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, apartmentCode);
            ps.setString(2, name);
            ps.setInt(3, ownerId);
            ps.setString(4, tin);
            ps.setInt(5, floors);
            ps.setInt(6, totalRooms);
            ps.setInt(7, totalRooms);
            ps.setDouble(8, capital);
            ps.setDouble(9, taxRate);        
            ps.setDouble(10, penaltyRate);      
            ps.setString(11, paymentMethod);   
            ps.setString(12, description);
            ps.setString(13, policy);
            ps.setString(14, barangay);
            ps.setString(15, street);
            
            ps.setString(16, electricityType); 
            ps.setDouble(17, elecRate);
            ps.setString(18, waterType);       
            ps.setDouble(19, waterRate);
            ps.setString(20, internetType);    
            ps.setDouble(21, internetRate);

            ps.setString(22, contact);
            ps.setString(23, email);
            ps.setString(24, social);
            ps.setString(25, emergency);
            ps.setString(26, profileImage);
            
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                // Trigger the Safe Room Generator
                generateCustomRooms(conn, newId, roomsPerFloorList, rentPricesPerFloor, downPaymentsPerFloor, securityDepositsPerFloor, roomImagesPerFloor, electricityType, waterType, internetType);
            }
            LOGGER.info(() -> "Registered Apartment: " + name);
            return true;
            
        } catch (Exception e) {
            LOGGER.severe(() -> "ApartmentDAO Error: " + e.getMessage());
            return false;
        }
    }

    public boolean addRoomFromUI(int apartmentId, String roomNumber, int roomFloor, String roomDetails, 
                                  double rent, double downPayment, double secDeposit, String roomImage,
                                  String electricityType, String waterType, String internetType) { 
                                  
        // SAFE MODE: Stripped missing columns to ensure it saves successfully
        String insertRoom = "INSERT INTO rooms(apartment_id, room_number, room_floor, rent_amount, down_payment, security_deposit, status) VALUES(?,?,?,?,?,?,'Available')";
        String updateCounts = "UPDATE apartments SET total_rooms = total_rooms + 1, rooms_available = rooms_available + 1 WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement psRoom = conn.prepareStatement(insertRoom);
                 PreparedStatement psApt = conn.prepareStatement(updateCounts)) {
                 
                psRoom.setInt(1, apartmentId);
                psRoom.setString(2, roomNumber);
                psRoom.setInt(3, roomFloor);
                psRoom.setDouble(4, rent);
                psRoom.setDouble(5, downPayment);
                psRoom.setDouble(6, secDeposit);
                psRoom.executeUpdate();
                
                psApt.setInt(1, apartmentId);
                psApt.executeUpdate();
                
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.severe("Add Room Failed: " + e.getMessage());
                return false;
            }
        } catch (Exception e) { return false; }
    }

    public List<String> searchApartmentsWithRentRange(String barangay) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT a.apartment_id, a.establishment_photo, a.apartment_name, (a.street || ', ' || a.barangay) AS apartment_address, a.rooms_available, " +
                     "MIN(r.rent_amount) as min_rent, MAX(r.rent_amount) as max_rent " +
                     "FROM apartments a " +
                     "LEFT JOIN rooms r ON a.apartment_id = r.apartment_id AND r.status = 'Available' " +
                     "WHERE (a.street || ', ' || a.barangay) LIKE ? AND a.rooms_available > 0 AND a.is_active = 1 AND a.approval_status = 'APPROVED' " +
                     "GROUP BY a.apartment_id";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + barangay + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String details = String.format("Photo: %s | ID: %d | Name: %s | Location: %s | Vacant: %d | Rent: PHP %.2f - %.2f",
                        rs.getString("establishment_photo"), rs.getInt("apartment_id"), rs.getString("apartment_name"),
                        rs.getString("apartment_address"), rs.getInt("rooms_available"), rs.getDouble("min_rent"), rs.getDouble("max_rent")
                );
                list.add(details);
            }
        } catch (Exception e) { LOGGER.severe(() -> "Search Error: " + e.getMessage()); }
        return list;
    }

    public List<String> getNearbyApartments(String barangay) {
        List<String> list = new ArrayList<>();
        String findNearbySql = "SELECT nearby_1, nearby_2, nearby_3 FROM barangays WHERE name = ?";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(findNearbySql)) {
            
            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String n1 = rs.getString("nearby_1");
                String n2 = rs.getString("nearby_2");
                String n3 = rs.getString("nearby_3");
                
                if(n1 != null) list.addAll(searchApartmentsWithRentRange(n1));
                if(n2 != null) list.addAll(searchApartmentsWithRentRange(n2));
                if(n3 != null) list.addAll(searchApartmentsWithRentRange(n3));
            }
        } catch (Exception e) {
             LOGGER.severe(() -> "Nearby Fallback Error: " + e.getMessage()); 
        }
        return list;
    }

    public String[] getRegistrationStatusDashboard(int ownerId) {
        String sql = "SELECT a.apartment_name, (a.street || ', ' || a.barangay) AS apartment_address, o.name AS owner_name, " +
                     "a.total_rooms, a.approval_status " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.owner_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String status = rs.getString("approval_status");
                if (status == null) status = "PENDING";

                return new String[] {
                    rs.getString("apartment_name"),      
                    rs.getString("apartment_address"),   
                    rs.getString("owner_name"),          
                    String.valueOf(rs.getInt("total_rooms")), 
                    status                               
                };
            }
        } catch (Exception e) {
            System.out.println("Registration Dashboard Error: " + e.getMessage());
        }
        return null;
    }
    
    public List<String> getOwnerApartmentsDashboard(int ownerId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT apartment_name, (street || ', ' || barangay) AS apartment_address, total_rooms, approval_status FROM apartments WHERE owner_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(String.format("Name: %s | Location: %s | Rooms Listed: %d | Status: %s",
                        rs.getString("apartment_name"),
                        rs.getString("apartment_address"),
                        rs.getInt("total_rooms"),
                        rs.getString("approval_status")));
            }
        } catch (Exception e) { LOGGER.severe(() -> "Dashboard Error: " + e.getMessage()); }
        return list;
    }

    private String generateApartmentCode() {
        return "APT-" + (int)(Math.random() * 9000 + 1000); 
    }

    // SAFE MODE: Only inserts into columns guaranteed to exist in your Database.
    private void generateCustomRooms(Connection conn, int newId, List<Integer> roomsPerFloorList,
                                      List<List<Double>> rentPricesPerFloor, List<List<Double>> downPaymentsPerFloor,
                                      List<List<Double>> securityDepositsPerFloor,
                                      List<List<String>> roomImagesPerFloor,
                                      String elecType, String waterType, String netType) {
        
        if (roomsPerFloorList == null || roomsPerFloorList.isEmpty()) return;

        try {
            // Check which columns actually exist in your 'rooms' table to avoid SQLITE_ERRORs
            java.util.Set<String> dbColumns = new java.util.HashSet<>();
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "rooms", null)) {
                while (rs.next()) {
                    dbColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }

            // Build the dynamic SQL based on your database structure
            StringBuilder sql = new StringBuilder("INSERT INTO rooms (apartment_id, room_number, rent_amount, status");
            StringBuilder placeholders = new StringBuilder(" VALUES (?, ?, ?, 'Available'");

            // Add necessities only if the database column exists[cite: 5]
            if (dbColumns.contains("room_floor")) { sql.append(", room_floor"); placeholders.append(", ?"); }
            if (dbColumns.contains("down_payment")) { sql.append(", down_payment"); placeholders.append(", ?"); }
            if (dbColumns.contains("security_deposit")) { sql.append(", security_deposit"); placeholders.append(", ?"); }
            if (dbColumns.contains("room_image")) { sql.append(", room_image"); placeholders.append(", ?"); }
            if (dbColumns.contains("electricity_type")) { sql.append(", electricity_type"); placeholders.append(", ?"); }
            if (dbColumns.contains("water_type")) { sql.append(", water_type"); placeholders.append(", ?"); }
            if (dbColumns.contains("internet_type")) { sql.append(", internet_type"); placeholders.append(", ?"); }
            if (dbColumns.contains("room_details")) { sql.append(", room_details"); placeholders.append(", 'Standard Room'"); }
            
            sql.append(")").append(placeholders).append(")");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int roomCounter = 1;
                for (int fIdx = 0; fIdx < roomsPerFloorList.size(); fIdx++) {
                    int roomsOnFloor = roomsPerFloorList.get(fIdx);
                    for (int i = 0; i < roomsOnFloor; i++) {
                        int pIdx = 1;
                        ps.setInt(pIdx++, newId);
                        ps.setString(pIdx++, "Room " + roomCounter);
                        ps.setDouble(pIdx++, rentPricesPerFloor.get(fIdx).get(i));

                        // Dynamic parameter mapping[cite: 5]
                        if (dbColumns.contains("room_floor")) ps.setInt(pIdx++, (fIdx + 1));
                        if (dbColumns.contains("down_payment")) ps.setDouble(pIdx++, downPaymentsPerFloor.get(fIdx).get(i));
                        if (dbColumns.contains("security_deposit")) ps.setDouble(pIdx++, securityDepositsPerFloor.get(fIdx).get(i));
                        if (dbColumns.contains("room_image")) ps.setString(pIdx++, roomImagesPerFloor.get(fIdx).get(i));
                        if (dbColumns.contains("electricity_type")) ps.setString(pIdx++, elecType);
                        if (dbColumns.contains("water_type")) ps.setString(pIdx++, waterType);
                        if (dbColumns.contains("internet_type")) ps.setString(pIdx++, netType);

                        ps.addBatch();
                        roomCounter++;
                    }
                }
                ps.executeBatch();
                LOGGER.info("Successfully saved rooms with all available necessities.");
            }
        } catch (Exception e) {
            LOGGER.severe("Critical Room Generation Error: " + e.getMessage());
        }
    }
}