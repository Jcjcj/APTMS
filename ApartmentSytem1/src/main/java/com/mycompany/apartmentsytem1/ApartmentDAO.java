package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class ApartmentDAO {

    private static final Logger LOGGER = Logger.getLogger(ApartmentDAO.class.getName());

    private String generateApartmentCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean addApartment(String apartmentCode, String name, String tin, int floors, 
                             List<Integer> roomsPerFloorList, 
                             List<List<Double>> rentPricesPerFloor,
                             List<List<Double>> downPaymentsPerFloor, 
                             List<List<Double>> securityDepositsPerFloor, 
                             double capital, String paymentMethod, String description, String policy,
                             String barangay, String street, String electricity, String water, String internet,
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
                "capital, payment_method, description, policy, barangay, street, electricity, water, internet, " +
                "contact_number, email, social_media, emergency_number, profile_image, is_active) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";

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
            ps.setString(9, paymentMethod);
            ps.setString(10, description);
            ps.setString(11, policy);
            ps.setString(12, barangay);
            ps.setString(13, street);
            ps.setString(14, electricity);
            ps.setString(15, water);
            ps.setString(16, internet);
            ps.setString(17, contact);
            ps.setString(18, email);
            ps.setString(19, social);
            ps.setString(20, emergency);
            ps.setString(21, profileImage);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                generateCustomRooms(conn, newId, roomsPerFloorList, rentPricesPerFloor, downPaymentsPerFloor, securityDepositsPerFloor);
            }
            LOGGER.info("Registered Apartment: " + name);
            return true;
            
        } catch (Exception e) {
            LOGGER.severe("ApartmentDAO Error: " + e.getMessage());
            return false;
        }
    }

    private void generateCustomRooms(Connection conn, int apartmentId, 
                                     List<Integer> roomsPerFloorList, 
                                     List<List<Double>> rentPricesPerFloor,
                                     List<List<Double>> downPaymentsPerFloor,
                                     List<List<Double>> securityDepositsPerFloor) {
        
        String roomSql = "INSERT INTO rooms(apartment_id, room_number, rent_amount, down_payment, security_deposit) VALUES(?, ?, ?, ?, ?)";
        
        try (PreparedStatement psRoom = conn.prepareStatement(roomSql)) {
            
            for (int f = 0; f < roomsPerFloorList.size(); f++) {
                int floorNum = f + 1;
                int roomsOnThisFloor = roomsPerFloorList.get(f);
                
                List<Double> rents = (f < rentPricesPerFloor.size()) ? rentPricesPerFloor.get(f) : new ArrayList<>();
                List<Double> downs = (f < downPaymentsPerFloor.size()) ? downPaymentsPerFloor.get(f) : new ArrayList<>();
                List<Double> deposits = (f < securityDepositsPerFloor.size()) ? securityDepositsPerFloor.get(f) : new ArrayList<>();

                for (int r = 1; r <= roomsOnThisFloor; r++) {
                    String roomNum = String.format("%d%02d", floorNum, r);
                    
                    double roomRent = (r <= rents.size()) ? Math.max(0, rents.get(r-1)) : 0.0;
                    double roomDown = (r <= downs.size()) ? Math.max(0, downs.get(r-1)) : 0.0;
                    double roomDep = (r <= deposits.size()) ? Math.max(0, deposits.get(r-1)) : 0.0;

                    psRoom.setInt(1, apartmentId);
                    psRoom.setString(2, roomNum);
                    psRoom.setDouble(3, roomRent);
                    psRoom.setDouble(4, roomDown);
                    psRoom.setDouble(5, roomDep);
                    psRoom.addBatch();
                }
            }
            psRoom.executeBatch();
            
        } catch (Exception e) {
            LOGGER.severe("Room Generation Error: " + e.getMessage());
        }
    }

    public boolean addSingleRoom(int apartmentId, String roomNumber, double rentAmount, double downPayment, double secDeposit, 
                                 String capacityText, String utilitiesText, String designText, 
                                 String elec, String water, String internet) {
        
        if (rentAmount < 0 || downPayment < 0 || secDeposit < 0 || roomNumber == null || roomNumber.isEmpty()) {
            return false;
        }

        String insertRoom = "INSERT INTO rooms(apartment_id, room_number, rent_amount, down_payment, security_deposit, capacity_text, utilities_text, design_text, electricity_type, water_type, internet_type) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        String updateCounts = "UPDATE apartments SET total_rooms = total_rooms + 1, rooms_available = rooms_available + 1 WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); 
            
            try (PreparedStatement psRoom = conn.prepareStatement(insertRoom);
                 PreparedStatement psApt = conn.prepareStatement(updateCounts)) {
                
                psRoom.setInt(1, apartmentId);
                psRoom.setString(2, roomNumber);
                psRoom.setDouble(3, rentAmount);
                psRoom.setDouble(4, downPayment);
                psRoom.setDouble(5, secDeposit);
                psRoom.setString(6, capacityText);
                psRoom.setString(7, utilitiesText);
                psRoom.setString(8, designText);
                psRoom.setString(9, elec);
                psRoom.setString(10, water);
                psRoom.setString(11, internet);
                psRoom.executeUpdate();
                
                psApt.setInt(1, apartmentId);
                psApt.executeUpdate();
                
                conn.commit();
                return true;
                
            } catch (Exception e) {
                conn.rollback();
                LOGGER.severe("Add Single Room Failed: " + e.getMessage());
                return false;
            }
        } catch (Exception e) { 
            return false; 
        }
    }

    public List<String> searchApartmentsWithAvailableRooms(String barangay) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT apartment_name, total_rooms, rooms_available FROM apartments WHERE barangay=? AND rooms_available>0 AND is_active=1";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, barangay);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("apartment_name") + " | Total: " + rs.getInt("total_rooms") + " | Available: " + rs.getInt("rooms_available"));
            }
            
        } catch (Exception e) { 
            LOGGER.severe("Search Error: " + e.getMessage()); 
        }
        return list;
    }

    public List<String> getAllBarangays() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM barangays ORDER BY name";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) { 
                list.add(rs.getString("name")); 
            }
            
        } catch (Exception e) { 
            LOGGER.severe("Get Barangays Error: " + e.getMessage()); 
        }
        return list;
    }
}