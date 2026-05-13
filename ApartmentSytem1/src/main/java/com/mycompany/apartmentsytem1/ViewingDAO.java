package com.mycompany.apartmentsytem1;

import java.sql.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ViewingDAO {
    
    private static final Logger LOGGER = Logger.getLogger(ViewingDAO.class.getName());
    private String lastBookingError = "Booking failed. Please try again.";
    private String lastReservationError = "Reservation failed. Please try again.";

    public String[] bookRoomViewing(int apartmentId, String roomNumber, String tenantName, 
                                     String contactNumber, String scheduleDate, String viewingTime) { 
                                     
        String baseUsername = tenantName.replaceAll("\\s+", "").toLowerCase();
        String tempUsername = baseUsername + (int)(Math.random() * 1000); 
        String tempRawPassword = String.format("%010d", (long)(Math.random() * 10000000000L));
        String hashedTempPassword = PasswordUtil.hashPassword(tempRawPassword);

        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, " +
                     "contact_number, schedule_date, viewing_time, status, temp_username, temp_password) " +
                     "VALUES(?,?,?,?,?,?, 'PENDING',?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, tenantName);
            ps.setString(4, contactNumber);
            ps.setString(5, scheduleDate);
            ps.setString(6, viewingTime); 
            ps.setString(7, tempUsername);
            ps.setString(8, hashedTempPassword);

            if (ps.executeUpdate() > 0) {
                return new String[] { tempUsername, tempRawPassword }; 
            }
        } catch (Exception e) {
            System.out.println("Viewing Booking Error: " + e.getMessage());
        }
        return null; 
    }

    public boolean bookRoomViewingWithAccount(int apartmentId, String roomNumber, String tenantName,
                                              String contactNumber, String email, String username,
                                              String rawPassword, String scheduleDate, String viewingTime) {
        lastBookingError = "Booking failed. Please try again.";

        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, contact_number, " +
                     "schedule_date, viewing_time, status, temp_username, temp_password, temp_email) " +
                     "VALUES(?,?,?,?,?,?, 'PENDING', ?, ?, ?)";

        try (Connection conn = DBConnection.connect()) {
            ensureReservationColumns(conn);

            if (isUsernameTaken(conn, username)) {
                lastBookingError = "Username is already in use. Please choose another username.";
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, apartmentId);
                ps.setString(2, roomNumber);
                ps.setString(3, tenantName);
                ps.setString(4, contactNumber);
                ps.setString(5, scheduleDate);
                ps.setString(6, viewingTime);
                ps.setString(7, username);
                ps.setString(8, PasswordUtil.hashPassword(rawPassword));
                ps.setString(9, email);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            lastBookingError = "Booking failed: " + e.getMessage();
            LOGGER.severe("Viewing Booking Error: " + e.getMessage());
            return false;
        }
    }

    public boolean bookRoomViewing(int apartmentId, String roomNumber, String tenantName,
                                   String contactNumber, String email, String username,
                                   String rawPassword, String scheduleDate, String viewingTime) {
        return bookRoomViewingWithAccount(
                apartmentId,
                roomNumber,
                tenantName,
                contactNumber,
                email,
                username,
                rawPassword,
                scheduleDate,
                viewingTime
        );
    }

    public boolean updateViewingStatus(int scheduleId, String status) {
        String sql = "UPDATE viewing_schedule SET status = ? WHERE schedule_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase());
            ps.setInt(2, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String[] bookRoomReservation(int apartmentId, String roomNumber, String tenantName, String contactNumber) {
        return bookRoomViewing(apartmentId, roomNumber, tenantName, contactNumber, "Reservation", "RESERVE_NOW");
    }

    public boolean bookRoomReservationWithPayment(int apartmentId, String roomNumber, String tenantName,
                                                  String contactNumber, String email, String username,
                                                  String rawPassword, String paymentReference, double downPaymentAmount) {
        lastReservationError = "Reservation failed. Please try again.";

        String sql = "INSERT INTO viewing_schedule(apartment_id, room_number, tenant_name, contact_number, " +
                     "schedule_date, viewing_time, status, temp_username, temp_password, temp_email, payment_reference, down_payment_amount) " +
                     "VALUES(?,?,?,?, 'Reservation', 'RESERVE_NOW', 'PENDING', ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.connect()) {
            ensureReservationColumns(conn);

            if (isUsernameTaken(conn, username)) {
                lastReservationError = "Username is already in use. Please choose another username.";
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, apartmentId);
                ps.setString(2, roomNumber);
                ps.setString(3, tenantName);
                ps.setString(4, contactNumber);
                ps.setString(5, username);
                ps.setString(6, PasswordUtil.hashPassword(rawPassword));
                ps.setString(7, email);
                ps.setString(8, paymentReference);
                ps.setDouble(9, downPaymentAmount);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            lastReservationError = "Reservation failed: " + e.getMessage();
            LOGGER.severe("Reservation Booking Error: " + e.getMessage());
            return false;
        }
    }

    public String getLastReservationError() {
        return lastReservationError;
    }

    public String getLastBookingError() {
        return lastBookingError;
    }

    private void ensureReservationColumns(Connection conn) throws SQLException {
        addColumnIfMissing(conn, "viewing_schedule", "temp_email", "TEXT");
        addColumnIfMissing(conn, "viewing_schedule", "payment_reference", "TEXT");
        addColumnIfMissing(conn, "viewing_schedule", "down_payment_amount", "REAL DEFAULT 0.0");
    }

    private void addColumnIfMissing(Connection conn, String tableName, String columnName, String definition) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) {
                throw e;
            }
        }
    }

    private boolean isUsernameTaken(Connection conn, String username) throws SQLException {
        String sql =
                "SELECT 1 FROM super_admins WHERE username = ? " +
                "UNION SELECT 1 FROM owners WHERE username = ? " +
                "UNION SELECT 1 FROM registered_tenants WHERE username = ? " +
                "UNION SELECT 1 FROM viewing_schedule WHERE temp_username = ? " +
                "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setString(3, username);
            ps.setString(4, username);
            return ps.executeQuery().next();
        }
    }

    public String[] getTemporaryUserDashboard(String tempUsername, String rawTempPassword) {
        String sql = "SELECT v.temp_password, v.temp_username, v.room_number, a.apartment_name, a.street, a.barangay, " +
                     "v.schedule_date, v.viewing_time, v.status, v.tenant_name, v.contact_number, v.temp_email " +
                     "FROM viewing_schedule v " +
                     "JOIN apartments a ON v.apartment_id = a.apartment_id " +
                     "WHERE v.temp_username = ?";

        try (Connection conn = DBConnection.connect()) {
            ensureReservationColumns(conn);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tempUsername);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("temp_password");
                
                if (PasswordUtil.checkPassword(rawTempPassword, storedHash)) {
                    String fullAddress = rs.getString("street") + ", " + rs.getString("barangay");
                    return new String[] {
                        rs.getString("room_number"),       
                        rs.getString("apartment_name"),    
                        fullAddress,                       
                        rs.getString("schedule_date"),     
                        rs.getString("viewing_time"),      
                        rs.getString("status"),            
                        rs.getString("tenant_name"),
                        rs.getString("contact_number"),
                        rs.getString("temp_username"),
                        rs.getString("temp_email")
                    };
                }
            }
            }
        } catch (Exception e) {
            LOGGER.severe("Temp User Login Error: " + e.getMessage());
        }
        return null; 
    }
    
    public boolean rejectViewing(int scheduleId, String reason) {
        String sql = "UPDATE viewing_schedule SET status = 'REJECTED', rejection_reason = ? WHERE schedule_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.severe("Reject Viewing Error: " + e.getMessage());
            return false;
        }
    }
    
    public List<String> getAvailableTimeSlots(int apartmentId, String roomNumber, String scheduleDate) {
        List<String> availableSlots = new ArrayList<>();
        for (String[] slot : TimeSlotHelper.getAllSlots()) {
            availableSlots.add(slot[0] + " - " + slot[1]);
        }
        String sql = "SELECT viewing_time FROM viewing_schedule " +
                     "WHERE apartment_id = ? AND room_number = ? AND schedule_date = ? AND status != 'REJECTED'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, scheduleDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                availableSlots.remove(rs.getString("viewing_time"));
            }
        } catch (Exception e) {
            System.out.println("Timeslot Error: " + e.getMessage());
        }
        return availableSlots; 
    }
    
    // ==========================================
    // THE FATAL BUG 2 FIX IS HERE
    // ==========================================
    public List<String> getBookedTimes(int apartmentId, String roomNumber, String date) {
        List<String> bookedTimes = new ArrayList<>();
        // FATAL SQL BUG FIXED: Table is viewing_schedule (not room_viewings). Column is schedule_date.
        String sql = "SELECT viewing_time FROM viewing_schedule WHERE apartment_id = ? AND room_number = ? AND schedule_date = ? AND status != 'REJECTED'";
        
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ps.setString(3, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookedTimes.add(rs.getString("viewing_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookedTimes;
    }
    
    public void cleanupTemporaryAccount(int apartmentId, String tenantName) {
        String sql = "DELETE FROM viewing_schedule WHERE apartment_id = ? AND tenant_name = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, tenantName);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Cleanup Error: " + e.getMessage());
        }
    }
}
