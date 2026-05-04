package com.mycompany.apartmentsytem1;

import java.sql.*;
import java.time.LocalDate;

public class DataBaseSeeder {

    // NO CONSOLE MENUS. JUST PURE DATA INJECTION.
    public static void seedMassiveData() {
        System.out.println("\n[INJECTING THOUSANDS OF ROWS TO ALL TABLES... PLEASE WAIT]");
        
        String[] firsts = {"Juan", "Maria", "Pedro", "Ana", "Luis", "Carmen", "Jose", "Rosa", "Carlos", "Elena"};
        String[] lasts = {"Cruz", "Santos", "Reyes", "Bautista", "Ocampo", "Garcia", "Mendoza", "Torres", "Vilanueva", "Lim"};
        String[] brgys = {"Lahug", "Mabolo", "Apas", "Talamban", "Guadalupe", "Capitol Site"};
        String[] aptSuffix = {"Residences", "Lofts", "Dorms", "Apartments", "Suites", "Flats"};
        String[] issues = {"Leaking pipe under the sink.", "Aircon is not cooling.", "No internet connection.", "Flickering lights in hallway.", "Clogged shower drain."};
        
        String pass = PasswordUtil.hashPassword("pass123");
        String today = LocalDate.now().toString();

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            conn.setAutoCommit(false); 

            // 1. SUPER ADMIN (Login: admin / pass123)
            stmt.addBatch("INSERT OR IGNORE INTO super_admins (username, password) VALUES ('admin', '" + pass + "')");

            // 2. OWNERS (10 Owners - Login: owner1 to owner10 / pass123)
            for(int i=1; i<=10; i++) {
                String name = firsts[i%10] + " " + lasts[i%10];
                stmt.addBatch("INSERT INTO owners (name, contact_number, email, address, emergency_number, valid_id, username, password, is_active) " +
                              "VALUES ('" + name + "', '091700000" + i + "', 'owner"+i+"@test.com', 'Cebu City', '091800000" + i + "', 'ID-00" + i + "', 'owner"+i+"', '" + pass + "', 1)");
            }

            // 3. APARTMENTS (20 Apartments - 2 per owner)
            for(int i=1; i<=20; i++) {
                int ownerId = ((i-1) % 10) + 1; 
                String aptName = lasts[i%10] + " " + aptSuffix[i%6] + " " + i;
                String brgy = brgys[i%6];
                stmt.addBatch("INSERT INTO apartments (apartment_name, owner_id, floors, total_rooms, rooms_available, capital, payment_method, barangay, electricity, water, internet, is_active, approval_status) " +
                              "VALUES ('" + aptName + "', " + ownerId + ", 3, 10, 10, 500000, 'GCash', '" + brgy + "', 'Meter', 'Meter', 'Plan', 1, 'APPROVED')");
            }

            // 4. ROOMS (200 Rooms - 10 per Apartment)
            for(int aptId=1; aptId<=20; aptId++) {
                for(int r=1; r<=10; r++) {
                    String roomNum = aptId + "0" + r; 
                    double rent = 4000 + (Math.round(Math.random() * 6000)); 
                    stmt.addBatch("INSERT INTO rooms (apartment_id, room_number, status, rent_amount, capacity_text, utilities_text, design_text) " +
                                  "VALUES (" + aptId + ", '" + roomNum + "', 'Available', " + rent + ", '2 Persons', 'Free WiFi', 'Standard Studio')");
                }
            }

            // 5. TENANTS (100 Tenants - Login: tenant1 to tenant100 / pass123)
            for(int i=1; i<=100; i++) {
                String name = firsts[(i+3)%10] + " " + lasts[(i+2)%10] + " " + i;
                int targetApt = ((i-1) % 20) + 1;
                int targetRoom = ((i-1) % 10) + 1;
                String roomNum = targetApt + "0" + targetRoom;
                stmt.addBatch("INSERT INTO registered_tenants (name, contact_number, email, emergency_contact, username, password, target_apartment_id, target_room_number, move_in_date, occupants, approval_status, is_active) " +
                              "VALUES ('" + name + "', '09990000" + i + "', 'tenant"+i+"@test.com', '09191234567', 'tenant"+i+"', '" + pass + "', " + targetApt + ", '" + roomNum + "', '" + today + "', 2, 'APPROVED', 1)");
            }

            // 6. OCCUPANCY & UPDATE APARTMENT CAPACITY
            for(int i=1; i<=100; i++) {
                int aptId = ((i-1) % 20) + 1;
                int roomIdx = ((i-1) % 10) + 1;
                String roomNum = aptId + "0" + roomIdx;
                stmt.addBatch("INSERT INTO room_occupancy (apartment_id, room_number, tenant_id, move_in_date, status) VALUES (" + aptId + ", '" + roomNum + "', " + i + ", '" + today + "', 'Current')");
                stmt.addBatch("UPDATE rooms SET status = 'Occupied' WHERE apartment_id = " + aptId + " AND room_number = '" + roomNum + "'");
                stmt.addBatch("UPDATE apartments SET rooms_available = rooms_available - 1 WHERE apartment_id = " + aptId);
            }

            // 7. MASSIVE DATA: BILLS, EXPENSES, PAYMENTS, MAINTENANCE, COMPLAINTS
            for(int i=1; i<=100; i++) {
                int aptId = ((i-1) % 20) + 1;
                int roomIdx = ((i-1) % 10) + 1;
                String roomNum = aptId + "0" + roomIdx;
                
                // Room Bills
                stmt.addBatch("INSERT INTO room_bills (apartment_id, room_number, rent_amount, electricity_amount, water_amount, internet_amount) " +
                              "VALUES (" + aptId + ", '" + roomNum + "', 5500, 1500, 300, 500)");
                
                // Financial Bills Table
                stmt.addBatch("INSERT INTO bills (tenant_id, apartment_id, month, rent, electricity, water, internet, tax, penalty, total, due_date, paid) " +
                              "VALUES (" + i + ", " + aptId + ", '2026-04', 5500, 1500, 300, 500, 0, 0, 7800, '2026-04-15', 1)");
                
                // Payment History
                stmt.addBatch("INSERT INTO payment_transactions (apartment_id, tenant_id, room_number, payment_method, reference_no, date_paid, status) " +
                              "VALUES (" + aptId + ", " + i + ", '" + roomNum + "', 'GCash', 'REF-2026-" + i + "99', '2026-04-10', 'PAID')");
                
                // Maintenance Requests
                String issue = issues[i % 5];
                stmt.addBatch("INSERT INTO maintenance_requests (apartment_id, room_number, tenant_id, issue_description, priority_level, status, date_reported) " +
                              "VALUES (" + aptId + ", '" + roomNum + "', " + i + ", '" + issue + "', 'MEDIUM', 'PENDING', '" + today + "')");
                
                // Complaints
                stmt.addBatch("INSERT INTO complaints (apartment_id, room_number, message, date_submitted) " +
                              "VALUES (" + aptId + ", '" + roomNum + "', 'Loud noises from the hallway late at night.', '" + today + "')");
                
                // Building & Room Expenses
                stmt.addBatch("INSERT INTO expenses (apartment_id, room_number, expense_category, amount, expense_date, month, description) " +
                              "VALUES (" + aptId + ", NULL, 'Maintenance', 2000, '" + today + "', '2026-05', 'Routine cleaning')");
                stmt.addBatch("INSERT INTO expenses (apartment_id, room_number, expense_category, amount, expense_date, month, description) " +
                              "VALUES (" + aptId + ", '" + roomNum + "', 'Repair', 500, '" + today + "', '2026-05', 'Fixed lock')");
            }

            stmt.executeBatch();
            conn.commit();
            
            System.out.println("SUCCESS! AUTOMATIC DATA INJECTION COMPLETE.");

        } catch (Exception e) {
            System.err.println("Massive Seeding Error: " + e.getMessage());
        }
    }
}