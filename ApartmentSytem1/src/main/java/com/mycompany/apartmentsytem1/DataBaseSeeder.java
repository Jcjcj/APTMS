package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;

public class DataBaseSeeder {
    public static void seedMassiveData() {
        System.out.println("Wiping property data (KEEPING ALL ACCOUNTS SAFE) and injecting realistic, varied sample data...");
        
        // This will only be used if the database is completely empty. 
        // Your existing account passwords will NOT be overwritten.
        String defaultPassword = PasswordUtil.hashPassword("password123"); 

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false);

            // 1. WIPE DATA (Safely keeping owners, registered_tenants, and super_admins)
            String[] tables = {
                "notifications", "payment_transactions", "tenant_history", 
                "announcements", "complaints", "room_bills", "maintenance_requests", 
                "viewing_schedule", "room_occupancy", "expenses", "bills", 
                "rooms", "apartments"
            }; 

            stmt.execute("PRAGMA foreign_keys = OFF;");
            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t);
            }
            stmt.execute("PRAGMA foreign_keys = ON;");
            
            // 2. BARANGAYS (Using IGNORE to prevent duplicate errors)
            stmt.addBatch("INSERT OR IGNORE INTO barangays (name, nearby_1, nearby_2) VALUES ('Apas', 'Lahug', 'Banilad')");
            stmt.addBatch("INSERT OR IGNORE INTO barangays (name, nearby_1, nearby_2) VALUES ('Talamban', 'Banilad', 'Pit-os')");
            stmt.addBatch("INSERT OR IGNORE INTO barangays (name, nearby_1, nearby_2) VALUES ('Lahug', 'Apas', 'Capitol Site')");

            // 3. OWNERS (INSERT OR IGNORE keeps your accounts safe)
            stmt.addBatch("INSERT OR IGNORE INTO owners (owner_id, name, contact_number, email, address, username, password, is_active) " +
                          "VALUES (1, 'Alice Owner', '0911-111-1111', 'alice@email.com', 'Apas, Cebu', 'alice123', '" + defaultPassword + "', 1)");
            stmt.addBatch("INSERT OR IGNORE INTO owners (owner_id, name, contact_number, email, address, username, password, is_active) " +
                          "VALUES (2, 'Bob Builder', '0922-222-2222', 'bob@email.com', 'Lahug, Cebu', 'bob123', '" + defaultPassword + "', 1)");
            stmt.addBatch("INSERT OR IGNORE INTO owners (owner_id, name, contact_number, email, address, username, password, is_active) " +
                          "VALUES (3, 'Charlie Pending', '0933-333-3333', 'charlie@email.com', 'Talamban', 'charlie123', '" + defaultPassword + "', 1)");

            // 4. APARTMENTS
            stmt.addBatch("INSERT INTO apartments (apartment_id, apartment_code, apartment_name, owner_id, floors, total_rooms, rooms_available, capital, payment_method, barangay, street, electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, is_active, approval_status) " +
                          "VALUES (1, 'APT-1001', 'Golden Peak Residences', 1, 3, 10, 8, 2500000.0, 'GCash, Maya, Bank', 'Lahug', 'Gorordo Ave', 'Meter', 15.5, 'Fixed', 250.0, 'Plan', 600.0, 1, 'APPROVED')");
            
            stmt.addBatch("INSERT INTO apartments (apartment_id, apartment_code, apartment_name, owner_id, floors, total_rooms, rooms_available, capital, payment_method, barangay, street, electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, is_active, approval_status) " +
                          "VALUES (2, 'APT-1002', 'Oasis Apartments', 2, 2, 5, 2, 1200000.0, 'Cash', 'Apas', 'San Miguel St', 'Meter', 14.0, 'Meter', 40.0, 'None', 0.0, 1, 'APPROVED')");

            stmt.addBatch("INSERT INTO apartments (apartment_id, apartment_code, apartment_name, owner_id, floors, total_rooms, rooms_available, capital, payment_method, barangay, street, electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, is_active, approval_status) " +
                          "VALUES (3, 'APT-1003', 'Future Horizons', 3, 4, 20, 20, 5000000.0, 'Bank Transfer', 'Talamban', 'Highway', 'Fixed', 1000.0, 'Fixed', 300.0, 'Plan', 800.0, 1, 'PENDING')");

            stmt.addBatch("INSERT INTO apartments (apartment_id, apartment_code, apartment_name, owner_id, floors, total_rooms, rooms_available, capital, payment_method, barangay, street, electricity_type, elec_rate, water_type, water_rate, internet_type, internet_rate, is_active, approval_status, rejection_reason) " +
                          "VALUES (4, 'APT-1004', 'Sketchy Lofts', 1, 1, 2, 2, 100000.0, 'Cash', 'Apas', 'Dark Alley', 'Fixed', 500.0, 'Fixed', 150.0, 'None', 0.0, 1, 'DENIED', 'Missing Business Permit and DTI Registration.')");

            // 5. ROOMS 
            stmt.addBatch("INSERT INTO rooms (room_id, apartment_id, room_number, status, rent_amount, down_payment, security_deposit) VALUES (1, 1, '101', 'Occupied', 7500.0, 7500.0, 7500.0)");
            stmt.addBatch("INSERT INTO rooms (room_id, apartment_id, room_number, status, rent_amount, down_payment, security_deposit) VALUES (2, 1, '102', 'Occupied', 8200.0, 8200.0, 8200.0)");
            stmt.addBatch("INSERT INTO rooms (room_id, apartment_id, room_number, status, rent_amount, down_payment, security_deposit) VALUES (3, 1, '103', 'Available', 9500.0, 9500.0, 9500.0)");
            stmt.addBatch("INSERT INTO rooms (room_id, apartment_id, room_number, status, rent_amount, down_payment, security_deposit) VALUES (4, 2, 'A1', 'Occupied', 6000.0, 6000.0, 6000.0)");

            // 6. TENANTS
            stmt.addBatch("INSERT OR IGNORE INTO registered_tenants (tenant_id, name, contact_number, email, username, password, target_apartment_id, target_room_number, move_in_date, approval_status, is_active) " +
                          "VALUES (1, 'Tom Holland', '0912-345-6789', 'tom@email.com', 'tom123', '" + defaultPassword + "', 1, '101', '2025-01-15', 'APPROVED', 1)");
            stmt.addBatch("INSERT OR IGNORE INTO registered_tenants (tenant_id, name, contact_number, email, username, password, target_apartment_id, target_room_number, move_in_date, approval_status, is_active) " +
                          "VALUES (2, 'Zendaya Coleman', '0998-765-4321', 'zen@email.com', 'zen123', '" + defaultPassword + "', 1, '102', '2025-03-01', 'APPROVED', 1)");
            stmt.addBatch("INSERT OR IGNORE INTO registered_tenants (tenant_id, name, contact_number, email, username, password, target_apartment_id, target_room_number, move_in_date, approval_status, is_active) " +
                          "VALUES (3, 'Pending Peter', '0944-555-6666', 'peter@email.com', 'peter123', '" + defaultPassword + "', 2, 'A1', '2026-06-01', 'PENDING', 1)");

            // 7. ROOM OCCUPANCY
            stmt.addBatch("INSERT INTO room_occupancy (apartment_id, room_number, tenant_id, move_in_date, status) VALUES (1, '101', 1, '2025-01-15', 'Current')");
            stmt.addBatch("INSERT INTO room_occupancy (apartment_id, room_number, tenant_id, move_in_date, status) VALUES (1, '102', 2, '2025-03-01', 'Current')");

            // 8. MAINTENANCE REQUESTS
            stmt.addBatch("INSERT INTO maintenance_requests (apartment_id, room_number, tenant_id, issue_description, priority_level, status, date_reported, date_resolved) " +
                          "VALUES (1, '101', 1, 'AC unit is not cooling properly.', 'High', 'PENDING', '2026-05-02', NULL)");
            stmt.addBatch("INSERT INTO maintenance_requests (apartment_id, room_number, tenant_id, issue_description, priority_level, status, date_reported, date_resolved) " +
                          "VALUES (1, '102', 2, 'Loose door handle.', 'Low', 'RESOLVED', '2026-04-20', '2026-04-22')");

            // 9. COMPLAINTS & SUGGESTIONS
            stmt.addBatch("INSERT INTO complaints (apartment_id, room_number, message, date_submitted) VALUES (1, '101', 'Suggestion: Could we add a designated parking spot for motorcycles?', '2026-05-01')");
            stmt.addBatch("INSERT INTO complaints (apartment_id, room_number, message, date_submitted) VALUES (1, '102', 'Complaint: The neighbor''s dog is barking constantly at night.', '2026-05-04')");

            // 10. EXPENSES 
            stmt.addBatch("INSERT INTO expenses (apartment_id, expense_category, amount, expense_date, month, description) VALUES (1, 'Utilities', 4500.0, '2026-04-28', '2026-04', 'Common Area Electricity & Water')");
            stmt.addBatch("INSERT INTO expenses (apartment_id, expense_category, amount, expense_date, month, description) VALUES (1, 'Maintenance', 2000.0, '2026-04-22', '2026-04', 'Fixed door handle in 102')");
            stmt.addBatch("INSERT INTO expenses (apartment_id, expense_category, amount, expense_date, month, description) VALUES (1, 'Taxes/Permits', 15000.0, '2026-01-10', '2026-01', 'Annual Business Permit Renewal')");

            // 11. BILLS & ROOM BILLS 
            stmt.addBatch("INSERT INTO room_bills (apartment_id, room_number, rent_amount, electricity_amount, water_amount, internet_amount) VALUES (1, '101', 7500.0, 1200.0, 250.0, 600.0)");
            stmt.addBatch("INSERT INTO bills (tenant_id, apartment_id, month, rent, electricity, water, internet, total, due_date, paid) VALUES (1, 1, '2026-05', 7500.0, 1200.0, 250.0, 600.0, 9550.0, '2026-05-05', 0)"); 
            stmt.addBatch("INSERT INTO bills (tenant_id, apartment_id, month, rent, electricity, water, internet, total, due_date, paid, amount_paid, payment_date) VALUES (2, 1, '2026-04', 8200.0, 950.0, 250.0, 600.0, 10000.0, '2026-04-05', 1, 10000.0, '2026-04-02')"); 

            // =========================================================================================
            // 12. COMPREHENSIVE NOTIFICATIONS (Mapped to specific user roles)
            // =========================================================================================

            // --- SUPER ADMIN NOTIFICATIONS ('superadmin') ---
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('superadmin', 'Pending Approval', 'A new apartment (Future Horizons) has registered and is waiting for approval.', 0, '2026-05-04')");
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('superadmin', 'Subscription Paid', 'Alice Owner has successfully paid their platform subscription fee.', 0, '2026-05-03')");

            // --- OWNER NOTIFICATIONS ('alice123') ---
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('alice123', 'Payment Received', 'The tenant in Room 101 has paid their bill.', 0, '2026-05-02')");
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('alice123', 'Penalty Applied', 'The tenant in Room 102 missed their due date. A penalty of PHP 500.00 was applied to their bill.', 0, '2026-05-06')");
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('alice123', 'Subscription Expiring', 'Your 2% platform subscription is expiring soon. Please renew to avoid service interruption.', 0, '2026-05-01')");

            // --- TENANT NOTIFICATIONS ('tom123' & 'zen123') ---
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('tom123', 'Upcoming Bill Reminder', 'Your upcoming bill is due in 3 days. Please ensure timely payment.', 0, '2026-05-02')");
            stmt.addBatch("INSERT INTO notifications (target_username, title, message, is_read, date_created) " +
                          "VALUES ('zen123', 'Payment Past Due', 'Your payment is past due. A penalty of PHP 500.00 has been applied to your account.', 0, '2026-05-06')");


            stmt.executeBatch();
            conn.commit();
            System.out.println("Massive Sample Data successfully injected! (Your specific accounts are untouched)");

        } catch (Exception e) {
            System.err.println("Seeding Error: " + e.getMessage());
        }
    }
}