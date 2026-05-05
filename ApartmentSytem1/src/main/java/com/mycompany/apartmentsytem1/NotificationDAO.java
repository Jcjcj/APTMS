package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NotificationDAO {

    // 1. Dispatcher for UPCOMING bills (3 days before due date)
    public void generateUpcomingBillAlerts() {
        // Looks for bills that are unpaid and exactly 3 days away from today
        String sql = "SELECT b.total_amount, b.due_date, t.username " +
                     "FROM bills b " +
                     "JOIN tenants t ON b.room_number = t.room_number AND b.apartment_id = t.apartment_id " +
                     "WHERE b.paid = 0 AND b.due_date = date('now', '+3 days')";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                double amount = rs.getDouble("total_amount");
                String dueDate = rs.getString("due_date");

                String title = "Upcoming Bill Reminder";
                String message = "Friendly reminder! Your bill of PHP " + amount + " is due on " + dueDate + ". Please pay on time to avoid penalties.";
                
                sendNotification(username, title, message);
            }
        } catch (Exception e) {
            System.err.println("Error generating upcoming alerts: " + e.getMessage());
        }
    }

    // 2. Dispatcher for LATE bills (Triggered right after a penalty is applied)
    public void generateLateBillAlerts() {
        // 1. UPDATED SQL: We added a JOIN to the apartments table so we can fetch 
        // the 'owner_username' and the 'room_number' to use in the owner's message.
        String sql = "SELECT b.total_amount, b.due_date, b.penalty, t.username, b.room_number, a.owner_username " +
                     "FROM bills b " +
                     "JOIN tenants t ON b.room_number = t.room_number AND b.apartment_id = t.apartment_id " +
                     "JOIN apartments a ON b.apartment_id = a.apartment_id " +
                     "WHERE b.paid = 0 AND b.due_date < date('now')";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Fetch data for the Tenant
                String tenantUsername = rs.getString("username");
                double penalty = rs.getDouble("penalty");
                
                // Fetch data for the Owner
                String roomNumber = rs.getString("room_number");
                String ownerUsername = rs.getString("owner_username");
                
                // --- ALERTS ---

                // 2. Notify the Tenant (Your original code)
                String tenantTitle = "Late Payment Alert";
                String tenantMessage = "Your payment is past due. A penalty of PHP " + penalty + " has been applied to your account. Please settle your balance immediately.";
                sendNotification(tenantUsername, tenantTitle, tenantMessage);

                // 3. Notify the Owner (The NEW code)
                String ownerTitle = "Tenant Delayed Payment";
                String ownerMessage = "The tenant in Room " + roomNumber + " missed their due date. A penalty of PHP " + penalty + " was applied to their bill.";
                sendNotification(ownerUsername, ownerTitle, ownerMessage);
            }
        } catch (Exception e) {
            System.err.println("Error generating late alerts: " + e.getMessage());
        }
    }

    // Helper method to insert the actual message into the database
    private void sendNotification(String username, String title, String message) {
        String insertSql = "INSERT INTO notifications (target_username, title, message) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, username);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }
    }
    
    // --- INSTANT ALERTS (Triggered by actions) ---

    // 1. Notify Owner that a Tenant Paid
    public void notifyOwnerTenantPaid(String ownerUsername, String tenantName, String roomNumber, double amount) {
        String title = "Payment Received";
        String message = "Tenant " + tenantName + " in Room " + roomNumber + " has paid their bill of PHP " + amount + ".";
        sendNotification(ownerUsername, title, message);
    }

    // 2. Notify ALL Super Admins of a new registration
    public void notifySuperAdminsNewRegistration(String apartmentName) {
        String title = "New Registration Pending";
        String message = "A new apartment ('" + apartmentName + "') has registered and is waiting for your approval.";
        
        // Broadcast to your specific 6 Super Admins
        String[] superAdmins = {"hilaryjanz", "shynjhy", "myles", "cj", "jerome", "yeasha"};
        for (String admin : superAdmins) {
            sendNotification(admin, title, message);
        }
    }
    
    // 3. Notify Super Admins that an Owner paid their 2% subscription
    public void notifySuperAdminsOwnerPaid(String ownerUsername, double amount) {
        String title = "Platform Fee Received";
        String message = "Owner " + ownerUsername + " has paid their platform subscription fee of PHP " + amount + ".";
        
        String[] superAdmins = {"hilaryjanz", "shynjhy", "myles", "cj", "jerome", "yeasha"};
        for (String admin : superAdmins) {
            sendNotification(admin, title, message);
        }
    }

    // --- SCHEDULED ALERTS (Triggered by the daily timer) ---

    // 4. Notify Owner that their 2% Subscription is Due Soon
    public void generateOwnerSubscriptionAlerts() {
        // Looks for apartments where the next_billing_date is 3 days away
        String sql = "SELECT owner_username, next_billing_date FROM apartments WHERE next_billing_date = date('now', '+3 days')";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ownerUsername = rs.getString("owner_username");
                String dueDate = rs.getString("next_billing_date");

                String title = "Subscription Due Soon";
                String message = "Your 2% platform subscription fee is due on " + dueDate + ". Please settle it to maintain system access.";
                sendNotification(ownerUsername, title, message);
            }
        } catch (Exception e) {
            System.err.println("Error generating owner subscription alerts: " + e.getMessage());
        }
    }
}