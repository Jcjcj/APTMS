package com.mycompany.apartmentsytem1;

import java.util.Scanner;
import java.sql.*;

public class DataBaseSeeder {
    private static final Scanner scanner = new Scanner(System.in);

    public static void runFullSystemTest() {
        while (true) {
            System.out.println("\n===========================================");
            System.out.println("   MASTER TESTER: LOGIN & FEATURE LOOP     ");
            System.out.println("===========================================");
            System.out.println("1. SUPER ADMIN");
            System.out.println("2. OWNER");
            System.out.println("3. TENANT");
            System.out.println("4. PUBLIC SEARCH (No Login Required)");
            System.out.println("5. VIEW ALL TABLES (Debug View)");
            System.out.println("0. EXIT");
            System.out.print("\nSelect Role: ");

            String choice = scanner.nextLine();
            if (choice.equals("0")) break;

            switch (choice) {
                case "1": authWrapper("super_admins"); break;
                case "2": authWrapper("owners"); break;
                case "3": authWrapper("registered_tenants"); break;
                case "4": searchModule(); break;
                case "5": viewHistoryModule(); break;
                default: System.out.println("Invalid selection.");
            }
        }
    }

    // --- AUTHENTICATION GATE ---
    private static void authWrapper(String table) {
        System.out.println("\n--- " + table.toUpperCase() + " ACCESS ---");
        System.out.println("1. Login");
        System.out.println("2. Sign Up");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("2")) {
            handleSignUp(table);
        } else if (choice.equals("1")) {
            if (handleLogin(table)) {
                // Route to the correct menu based on the table
                if (table.equals("super_admins")) superAdminFeatures();
                else if (table.equals("owners")) ownerFeatures();
                else if (table.equals("registered_tenants")) tenantFeatures();
            } else {
                System.out.println("Login Failed! Incorrect credentials.");
            }
        }
    }

    private static boolean handleLogin(String table) {
        System.out.print("Username: "); String user = scanner.nextLine();
        System.out.print("Password: "); String pass = scanner.nextLine();

        String sql = "SELECT password FROM " + table + " WHERE username = ?";
        try (Connection c = DBConnection.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashed = rs.getString("password");
                return PasswordUtil.checkPassword(pass, hashed); // Verifying the hash
            }
        } catch (Exception e) { System.err.println("Login Error: " + e.getMessage()); }
        return false;
    }

    private static void handleSignUp(String table) {
        System.out.print("Enter New Username: "); String u = scanner.nextLine();
        System.out.print("Enter New Password: "); String p = PasswordUtil.hashPassword(scanner.nextLine());
        
        if (table.equals("owners")) {
            System.out.print("Full Name: "); String n = scanner.nextLine();
            execute("INSERT INTO owners (name, username, password, is_active) VALUES (?, ?, ?, 1)", n, u, p);
        } else if (table.equals("registered_tenants")) {
            System.out.print("Full Name: "); String n = scanner.nextLine();
            execute("INSERT INTO registered_tenants (name, username, password, is_active, approval_status) VALUES (?, ?, ?, 1, 'PENDING')", n, u, p);
        } else {
            execute("INSERT INTO super_admins (username, password) VALUES (?, ?)", u, p);
        }
        System.out.println("Sign Up Successful!");
    }

    // --- ROLE-SPECIFIC FEATURES (Accessed only after login) ---
   // --- ROLE-SPECIFIC FEATURES (Accessed only after login) ---
    private static void superAdminFeatures() {
        System.out.println("\n[LOGGED IN AS ADMIN]");
        
        // 1. Show the Pending List First
        String listSql = "SELECT apartment_id, apartment_name FROM apartments WHERE approval_status = 'PENDING'";
        boolean hasPending = false;

        try (Connection c = DBConnection.connect(); 
             Statement stmt = c.createStatement(); 
             ResultSet rs = stmt.executeQuery(listSql)) {
            
            System.out.println("\n--- PENDING APARTMENTS WAITING FOR APPROVAL ---");
            while (rs.next()) {
                hasPending = true;
                System.out.println("ID: " + rs.getInt("apartment_id") + " | Name: " + rs.getString("apartment_name"));
            }
        } catch (Exception e) { 
            System.err.println("Error fetching list: " + e.getMessage()); 
        }

        // 2. Logical Check: If nothing is pending, stop here.
        if (!hasPending) {
            System.out.println("There is no pending apartment waiting for approval.");
            return; // Go back to the admin menu
        }

        // 3. If there are pending items, ask for the ID
        try {
            System.out.print("\nEnter Apartment ID to Approve: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            // Execute the update
            execute("UPDATE apartments SET approval_status = 'APPROVED' WHERE apartment_id = ?", id);
            System.out.println("Apartment ID " + id + " successfully APPROVED!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric ID.");
        }
    }

    private static void ownerFeatures() {
        System.out.println("\n[LOGGED IN AS OWNER]");
        System.out.print("Apartment Name: "); String an = scanner.nextLine();
        execute("INSERT INTO apartments (apartment_name, owner_id, approval_status) VALUES (?, 1, 'PENDING')", an);
        System.out.println("Apartment Registered (Pending Admin Approval).");
    }

    private static void tenantFeatures() {
        System.out.println("\n[LOGGED IN AS TENANT]");
        System.out.print("GCash Ref for Payment: "); String ref = scanner.nextLine();
        execute("INSERT INTO payment_transactions (apartment_id, reference_no, status, date_paid) VALUES (1, ?, 'PAID', '2026-05-04')", ref);
        System.out.println("Payment Logged!");
    }

    // --- UTILITIES ---
    private static void searchModule() {
        System.out.print("\nSearch Apartment Name: ");
        String s = scanner.nextLine();
        query("SELECT apartment_name, approval_status FROM apartments WHERE apartment_name LIKE ?", "%"+s+"%");
    }

    private static void viewHistoryModule() {
        System.out.println("\n--- DATABASE SNAPSHOT ---");
        System.out.println("[ADMINS]"); query("SELECT admin_id, username FROM super_admins", null);
        System.out.println("[APARTMENTS]"); query("SELECT apartment_id, apartment_name, approval_status FROM apartments", null);
        System.out.println("[PAYMENTS]"); query("SELECT * FROM payment_transactions", null);
    }

    private static void execute(String sql, Object... p) {
        try (Connection c = DBConnection.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i=0; i<p.length; i++) ps.setObject(i+1, p[i]);
            ps.executeUpdate();
        } catch (Exception e) { System.err.println("SQL Error: " + e.getMessage()); }
    }

    private static void query(String sql, String p) {
        try (Connection c = DBConnection.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (p != null) ps.setString(1, p);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i=1; i<=md.getColumnCount(); i++) System.out.print(rs.getString(i) + " | ");
                System.out.println();
            }
        } catch (Exception e) { System.err.println("Query Error: " + e.getMessage()); }
    }
}