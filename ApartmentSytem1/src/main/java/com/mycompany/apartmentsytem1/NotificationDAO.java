package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class NotificationDAO {

    // 1. Dispatcher for UPCOMING bills (3 days before due date)
    public void generateUpcomingBillAlerts() {
    // Unpaid bills with due_date exactly 3 days from today
    String sql = "SELECT b.total, b.due_date, t.username " +
                 "FROM bills b " +
                 "JOIN registered_tenants t ON b.tenant_id = t.tenant_id " +
                 "WHERE b.paid = 0 AND b.due_date = date('now', '+3 days')";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String username = rs.getString("username");
            double amount   = rs.getDouble("total");
            String dueDate  = rs.getString("due_date");

            String title   = "Upcoming Bill Reminder";
            String message = "Friendly reminder! Your bill of PHP " + amount +
                             " is due on " + dueDate +
                             ". Please pay on time to avoid penalties.";

            sendNotification(username, title, message);
        }
    } catch (Exception e) {
        System.err.println("Error generating upcoming alerts: " + e.getMessage());
    }
}


    // 2. Dispatcher for LATE bills (Triggered right after a penalty is applied)
    public void generateLateBillAlerts() {
    String sql =
        "SELECT b.penalty, b.due_date, t.username AS tenant_username, " +
        "       ro.room_number, o.username AS owner_username " +
        "FROM bills b " +
        "JOIN registered_tenants t ON b.tenant_id = t.tenant_id " +
        "LEFT JOIN room_occupancy ro " +
        "    ON ro.tenant_id = b.tenant_id " +
        "   AND ro.apartment_id = b.apartment_id " +
        "   AND ro.status = 'Current' " +
        "JOIN apartments a ON b.apartment_id = a.apartment_id " +
        "JOIN owners o ON a.owner_id = o.owner_id " +
        "WHERE b.paid = 0 AND b.due_date < date('now')";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String tenantUsername = rs.getString("tenant_username");
            double penalty        = rs.getDouble("penalty");
            String roomNumber     = rs.getString("room_number"); // may be null
            String ownerUsername  = rs.getString("owner_username");

            // Tenant alert
            String tenantTitle   = "Late Payment Alert";
            String tenantMessage = "Your payment is past due. A penalty of PHP " +
                                   penalty + " has been applied to your account. " +
                                   "Please settle your balance immediately.";
            sendNotification(tenantUsername, tenantTitle, tenantMessage);

            // Owner alert
            String ownerTitle = "Tenant Delayed Payment";
            String roomLabel  = (roomNumber != null ? "Room " + roomNumber : "a tenant");
            String ownerMessage = "The tenant in " + roomLabel +
                                  " missed their due date. A penalty of PHP " +
                                  penalty + " was applied to their bill.";
            sendNotification(ownerUsername, ownerTitle, ownerMessage);
        }
    } catch (Exception e) {
        System.err.println("Error generating late alerts: " + e.getMessage());
    }
}


    // Helper method to insert the actual message into the database
    private void sendNotification(String username, String title, String message) {
        String checkSql = "SELECT 1 FROM notifications WHERE target_username = ? AND title = ? AND message = ? LIMIT 1";
        String insertSql = "INSERT INTO notifications (target_username, title, message) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setString(1, username);
            psCheck.setString(2, title);
            psCheck.setString(3, message);

            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) return;
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, title);
                ps.setString(3, message);
                ps.executeUpdate();
            }
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
    String sql = "SELECT o.username AS owner_username, a.next_billing_date " +
                 "FROM apartments a " +
                 "JOIN owners o ON a.owner_id = o.owner_id " +
                 "WHERE a.next_billing_date = date('now', '+3 days')";

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String ownerUsername = rs.getString("owner_username");
            String dueDate       = rs.getString("next_billing_date");

            String title   = "Subscription Due Soon";
            String message = "Your 2% platform subscription fee is due on " +
                             dueDate +
                             ". Please settle it to maintain system access.";
            sendNotification(ownerUsername, title, message);
        }
    } catch (Exception e) {
        System.err.println("Error generating owner subscription alerts: " + e.getMessage());
    }
}

}
