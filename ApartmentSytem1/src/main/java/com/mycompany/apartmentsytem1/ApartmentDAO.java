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
                                 double capital,double taxRate, String paymentMethod, String description, String policy,
                                 String barangay, String street, 
                                 String electricityType, double elecRate, // Added elecRate
                                 String waterType, double waterRate,      // Added waterRate
                                 String internetType, double internetRate, // Added internetRate
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


        // Updated SQL to include the new rate columns and specific utility types
        String sql = "INSERT INTO apartments(" +
                "apartment_code, apartment_name, owner_id, tin_no, floors, total_rooms, rooms_available, " +
                "capital, tax_rate, payment_method, description, policy, barangay, street, " +
                "electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, " +
                "contact_number, email, social_media, emergency_number, profile_image, is_active) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)";


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
            ps.setString(10, paymentMethod);   
            ps.setString(11, description);
            ps.setString(12, policy);
            ps.setString(13, barangay);
            ps.setString(14, street);
            
            // Utility Inputs
            ps.setString(15, electricityType); 
            ps.setDouble(16, elecRate);
            ps.setString(17, waterType);       
            ps.setDouble(18, waterRate);
            ps.setString(19, internetType);    
            ps.setDouble(20, internetRate);

            ps.setString(21, contact);
            ps.setString(22, email);
            ps.setString(23, social);
            ps.setString(24, emergency);
            ps.setString(25, profileImage);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                generateCustomRooms(conn, newId, roomsPerFloorList, rentPricesPerFloor, downPaymentsPerFloor, securityDepositsPerFloor);
            }
            LOGGER.info(() -> "Registered Apartment: " + name);
            
            // Trigger the notification to all 6 Super Admins
            NotificationDAO notificationEngine = new NotificationDAO();
            notificationEngine.notifySuperAdminsNewRegistration(name);
            return true;
            
        } catch (Exception e) {
            LOGGER.severe(() -> "ApartmentDAO Error: " + e.getMessage());
            return false;
        }
    }


    // 2. Saves the right side of your UI form (Clicking "Add Room")
    // FIXED: Now properly captures the Utility types from the UI dropdowns
    public boolean addRoomFromUI(int apartmentId, String roomNumber, int roomFloor, String roomDetails, 
                                  double rent, double downPayment, double secDeposit, String roomImage,
                                  String electricityType, String waterType, String internetType) { // <-- Added parameters
                                  
        String insertRoom = "INSERT INTO rooms(apartment_id, room_number, room_floor, room_details, rent_amount, down_payment, security_deposit, room_image, electricity_type, water_type, internet_type) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
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
                psRoom.setString(9, electricityType); // <-- Saving correctly
                psRoom.setString(10, waterType);      // <-- Saving correctly
                psRoom.setString(11, internetType);   // <-- Saving correctly
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
        } catch (Exception e) { LOGGER.severe(() -> "Search Error: " + e.getMessage()); }
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
             LOGGER.severe(() -> "Nearby Fallback Error: " + e.getMessage()); 
        }
        return list;
    }

    /**
     * Retrieves the exact data needed for the Owner's Registration Status Dashboard.
     * Joins the apartments and owners tables to get the owner's real name.
     * @param ownerId
     * @return 
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
        } catch (Exception e) { LOGGER.severe(() -> "Dashboard Error: " + e.getMessage()); }
        return list;
    }

    // REPLACED THE EXCEPTIONS WITH WORKING LOGIC
    private String generateApartmentCode() {
        // Generates a simple random code like "APT-4928"
        return "APT-" + (int)(Math.random() * 9000 + 1000); 
    }

    private void generateCustomRooms(Connection conn, int newId, List<Integer> roomsPerFloorList,
                                      List<List<Double>> rentPricesPerFloor, List<List<Double>> downPaymentsPerFloor,
                                      List<List<Double>> securityDepositsPerFloor) {
        
        // Failsafe: If no room data is provided, exit safely
        if (roomsPerFloorList == null || roomsPerFloorList.isEmpty()) return;

        // Uses a single prepared statement to blast the entire array into the database at once
        String sql = "INSERT INTO rooms(apartment_id, room_number, room_floor, rent_amount, down_payment, security_deposit) VALUES(?,?,?,?,?,?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int roomCounter = 1;
            
            for (int floorIndex = 0; floorIndex < roomsPerFloorList.size(); floorIndex++) {
                int roomsOnThisFloor = roomsPerFloorList.get(floorIndex);
                int floorNumber = floorIndex + 1;

                for (int i = 0; i < roomsOnThisFloor; i++) {
                    // Generates names like "Room 1 - 1F", matching your UI
                    String roomNum = "Room " + roomCounter + " - " + floorNumber + "F";
                    
                    // Safely pull the financial data (defaulting to 0.0 if the lists are mismatched)
                    double rent = (rentPricesPerFloor != null && rentPricesPerFloor.size() > floorIndex && rentPricesPerFloor.get(floorIndex).size() > i) ? rentPricesPerFloor.get(floorIndex).get(i) : 0.0;
                    double down = (downPaymentsPerFloor != null && downPaymentsPerFloor.size() > floorIndex && downPaymentsPerFloor.get(floorIndex).size() > i) ? downPaymentsPerFloor.get(floorIndex).get(i) : 0.0;
                    double sec = (securityDepositsPerFloor != null && securityDepositsPerFloor.size() > floorIndex && securityDepositsPerFloor.get(floorIndex).size() > i) ? securityDepositsPerFloor.get(floorIndex).get(i) : 0.0;

                    ps.setInt(1, newId);
                    ps.setString(2, roomNum);
                    ps.setInt(3, floorNumber);
                    ps.setDouble(4, rent);
                    ps.setDouble(5, down);
                    ps.setDouble(6, sec);
                    
                    ps.addBatch(); // Queue it up!
                    roomCounter++;
                }
            }
            ps.executeBatch(); // Fire the whole queue to the database
            LOGGER.info("Batch Custom Rooms successfully generated for Apartment ID: " + newId);
            
        } catch (Exception e) {
            LOGGER.severe("Batch Room Generation Error: " + e.getMessage());
        }
    }
}
