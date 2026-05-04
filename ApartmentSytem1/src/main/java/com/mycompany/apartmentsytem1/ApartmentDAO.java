package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ApartmentDAO {

    private static final Logger LOGGER = Logger.getLogger(ApartmentDAO.class.getName());

    // Added the 'boolean agreedToTerms' parameter to enforce the rule.
    public int registerApartmentBuilding(String name, String tin, String address, String paymentMethod, 
                                         String emergency, double capital, String photoUrl, String policy, 
                                         String contactDetails, int floors, String electricity, 
                                         String water, String internet, double penaltyRate, 
                                         int ownerId, boolean agreedToTerms) {

        // f they didn't check the box in the UI, reject it immediately.
        if (!agreedToTerms) {
            LOGGER.warning("Registration rejected: Owner did not agree to the Terms and Conditions.");
            return -1; // Stops the registration process!
        }

        String sql = "INSERT INTO apartments(apartment_name, tin_no, apartment_address, payment_method, " +
                     "emergency_number, capital, establishment_photo, policy, contact_details, floors, " +
                     "electricity, water, internet, penalty_rate, owner_id, approval_status) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'PENDING')";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, tin);
            ps.setString(3, address);
            ps.setString(4, paymentMethod);
            ps.setString(5, emergency);
            ps.setDouble(6, capital);
            ps.setString(7, photoUrl);
            ps.setString(8, policy);
            ps.setString(9, contactDetails);
            ps.setInt(10, floors);
            ps.setString(11, electricity);
            ps.setString(12, water);
            ps.setString(13, internet);
            ps.setDouble(14, penaltyRate);
            ps.setInt(15, ownerId);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                LOGGER.info("Successfully registered Apartment: " + name + " ID: " + newId);
                return newId; // Returns the new Apartment ID so you can save the rooms!
            }
        } catch (Exception e) {
            LOGGER.severe("Apartment Registration Error: " + e.getMessage());
        }
        return -1; 
    }

    // 2. Saves the right side of your UI form (Clicking "Add Room")
    public boolean addRoomFromUI(int apartmentId, String roomNumber, int roomFloor, String roomDetails, 
                                 double rent, double downPayment, double secDeposit, String roomImage) {

        String insertRoom = "INSERT INTO rooms(apartment_id, room_number, room_floor, room_details, rent_amount, down_payment, security_deposit, room_image) VALUES(?,?,?,?,?,?,?,?)";
        String updateCounts = "UPDATE apartments SET total_rooms = total_rooms + 1, rooms_available = rooms_available + 1 WHERE apartment_id = ?";
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false); 
            
            try (PreparedStatement psRoom = conn.prepareStatement(insertRoom);
                 PreparedStatement psApt = conn.prepareStatement(updateCounts)) {
                
                psRoom.setInt(1, apartmentId);
                psRoom.setString(2, roomNumber);
                psRoom.setInt(3, roomFloor);
                psRoom.setString(4, roomDetails);
                psRoom.setDouble(5, rent);
                psRoom.setDouble(6, downPayment);
                psRoom.setDouble(7, secDeposit);
                psRoom.setString(8, roomImage);
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
        } catch (Exception e) { 
            return false; 
        }
    }

    // 3. Search logic for UI Page 2 (Public Search)
    public List<String> searchApartmentsWithRentRange(String barangay) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT a.apartment_id, a.establishment_photo, a.apartment_name, a.apartment_address, a.rooms_available, " +
                     "MIN(r.rent_amount) as min_rent, MAX(r.rent_amount) as max_rent " +
                     "FROM apartments a " +
                     "LEFT JOIN rooms r ON a.apartment_id = r.apartment_id AND r.status = 'Available' " +
                     "WHERE a.apartment_address LIKE ? AND a.rooms_available > 0 AND a.is_active = 1 AND a.approval_status = 'APPROVED' " +
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
        } catch (Exception e) { LOGGER.severe("Search Error: " + e.getMessage()); }
        return list;
    }

    // 4. Fallback search for nearby areas
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
             LOGGER.severe("Nearby Fallback Error: " + e.getMessage()); 
        }
        return list;
    }

    /**
     * Retrieves the exact data needed for the Owner's Registration Status Dashboard.
     * Joins the apartments and owners tables to get the owner's real name.
     */
    public String[] getRegistrationStatusDashboard(int ownerId) {
        String sql = "SELECT a.apartment_name, a.apartment_address, o.name AS owner_name, " +
                     "a.total_rooms, a.approval_status " +
                     "FROM apartments a " +
                     "JOIN owners o ON a.owner_id = o.owner_id " +
                     "WHERE a.owner_id = ?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[] {
                    rs.getString("apartment_name"),      // Index 0: e.g., "YES! Apartment"
                    rs.getString("apartment_address"),   // Index 1: e.g., "Sambag 1, Urgello St. Cebu"
                    rs.getString("owner_name"),          // Index 2: e.g., "Caroline San Pedro"
                    String.valueOf(rs.getInt("total_rooms")), // Index 3: e.g., "5"
                    rs.getString("approval_status")      // Index 4: e.g., "PENDING"
                };
            }
        } catch (Exception e) {
            System.out.println("Registration Dashboard Error: " + e.getMessage());
        }
        return null;
    }
    // 5. Data for the Owner Dashboard
    public List<String> getOwnerApartmentsDashboard(int ownerId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT apartment_name, apartment_address, total_rooms, approval_status FROM apartments WHERE owner_id = ?";
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
        } catch (Exception e) { LOGGER.severe("Dashboard Error: " + e.getMessage()); }
        return list;
    }
}
