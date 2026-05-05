package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // SUPER ADMINS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS super_admins ("
                    + "admin_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT)");
            stmt.execute("INSERT OR IGNORE INTO super_admins (username, password) VALUES ('superadmin', '" + PasswordUtil.hashPassword("admin123") + "')");

            // OWNERS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS owners ("
                    + "owner_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "emergency_number TEXT,"
                    + "valid_id TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT,"
                    + "is_active INTEGER DEFAULT 1)");

            // APARTMENTS TABLE (UPDATED FOR NEW UTILITY COLUMNS & PENALTIES)
            stmt.execute("CREATE TABLE IF NOT EXISTS apartments ("
                    + "apartment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_code TEXT UNIQUE,"
                    + "apartment_name TEXT,"
                    + "owner_id INTEGER,"
                    + "tin_no TEXT,"
                    + "floors INTEGER,"
                    + "total_rooms INTEGER,"
                    + "rooms_available INTEGER,"
                    + "capital REAL," 
                    + "tax_rate REAL DEFAULT 0.12," 
                    + "penalty_rate REAL DEFAULT 0.05," // <--- ADDED THIS FATAL MISSING LINE
                    + "payment_method TEXT,"
                    + "description TEXT,"
                    + "policy TEXT,"
                    + "barangay TEXT,"
                    + "street TEXT,"
                    + "electricity_type TEXT,"      
                    + "elec_rate REAL DEFAULT 0.0," 
                    + "water_type TEXT,"            
                    + "water_rate REAL DEFAULT 0.0,"
                    + "internet_type TEXT,"         
                    + "internet_rate REAL DEFAULT 0.0," 
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "social_media TEXT,"
                    + "emergency_number TEXT,"
                    + "profile_image TEXT,"
                    + "is_active INTEGER DEFAULT 1,"
                    + "approval_status TEXT DEFAULT 'PENDING'," 
                    + "rejection_reason TEXT," 
                    + "next_billing_date TEXT,"                 
                    + "FOREIGN KEY(owner_id) REFERENCES owners(owner_id) ON DELETE CASCADE)");

            // ROOMS TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms ("
                    + "room_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "status TEXT DEFAULT 'Available',"
                    + "rent_amount REAL DEFAULT 0.0," 
                    + "down_payment REAL DEFAULT 0.0,"     
                    + "security_deposit REAL DEFAULT 0.0," 
                    + "capacity_text TEXT DEFAULT 'Specify capacity here...',"   
                    + "utilities_text TEXT DEFAULT 'Specify utilities here...'," 
                    + "design_text TEXT DEFAULT 'Specify design here...',"       
                    + "electricity_type TEXT DEFAULT 'Meter',"
                    + "water_type TEXT DEFAULT 'Meter',"
                    + "internet_type TEXT DEFAULT 'Plan',"
                    + "image_url TEXT DEFAULT 'default_room.png',"
                    + "current_elec_reading REAL DEFAULT 0.0,"  // <-- FIXED: Added this
                    + "current_water_reading REAL DEFAULT 0.0," // <-- FIXED: Added this
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id) ON DELETE CASCADE)");

            // REGISTERED TENANTS TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS registered_tenants ("
                    + "tenant_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "emergency_contact TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT,"
                    + "target_apartment_id INTEGER,"
                    + "target_room_number TEXT,"
                    + "move_in_date TEXT,"
                    + "occupants INTEGER,"
                    + "approval_status TEXT DEFAULT 'PENDING',"
                    + "valid_id TEXT,"
                    + "is_active INTEGER DEFAULT 1,"
                    + "moved_out_date TEXT,"
                    + "FOREIGN KEY(target_apartment_id) REFERENCES apartments(apartment_id))");

            // ROOM OCCUPANCY TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS room_occupancy ("
                    + "occupancy_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "tenant_id INTEGER,"
                    + "move_in_date TEXT,"
                    + "move_out_date TEXT,"
                    + "status TEXT,"
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id),"
                    + "FOREIGN KEY(tenant_id) REFERENCES registered_tenants(tenant_id))");

            // MAINTENANCE REQUESTS TABLE (Combined and Fixed)
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance_requests ("
                    + "request_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "apartment_id INTEGER, room_number TEXT, tenant_id INTEGER, "
                    + "issue_description TEXT, priority_level TEXT, "
                    + "status TEXT DEFAULT 'PENDING', rejection_reason TEXT, "
                    + "date_reported TEXT, date_resolved TEXT, date_updated TEXT, "
                    + "FOREIGN KEY (apartment_id) REFERENCES apartments(apartment_id), "
                    + "FOREIGN KEY (tenant_id) REFERENCES registered_tenants(tenant_id))");

            // VIEWING SCHEDULE TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS viewing_schedule ("
                    + "schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "tenant_name TEXT,"
                    + "contact_number TEXT,"
                    + "schedule_date TEXT,"
                    + "viewing_time TEXT,"
                    + "status TEXT DEFAULT 'PENDING',"
                    + "rejection_reason TEXT," 
                    + "temp_username TEXT,"
                    + "temp_password TEXT,"
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id) ON DELETE CASCADE)");

            // BARANGAYS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS barangays ("
                    + "barangay_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE,"
                    + "nearby_1 TEXT,"
                    + "nearby_2 TEXT,"
                    + "nearby_3 TEXT)");
            
            // BILLS HISTORY TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS bills ("
                    + "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "tenant_id INTEGER, "
                    + "apartment_id INTEGER, "
                    + "month TEXT, "
                    + "rent REAL DEFAULT 0.0, "
                    + "electricity REAL DEFAULT 0.0, "
                    + "water REAL DEFAULT 0.0, "
                    + "internet REAL DEFAULT 0.0, "
                    + "tax REAL DEFAULT 0.0, "
                    + "penalty REAL DEFAULT 0.0, "
                    + "total REAL DEFAULT 0.0, "
                    + "due_date TEXT, "
                    + "paid INTEGER DEFAULT 0, "
                    + "amount_paid REAL DEFAULT 0.0, "
                    + "payment_date TEXT, "
                    + "payment_method TEXT, "
                    + "reference_number TEXT, "
                    + "penalty_applied_at TEXT, "
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id), "
                    + "FOREIGN KEY(tenant_id) REFERENCES registered_tenants(tenant_id))");
            
            // TENANT HISTORY TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS tenant_history ("
                    + "history_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "room_id INTEGER, "
                    + "tenant_id INTEGER, "
                    + "move_in_date TEXT, "
                    + "move_out_date TEXT, "
                    + "termination_reason TEXT, "
                    + "FOREIGN KEY (room_id) REFERENCES rooms(room_id), "
                    + "FOREIGN KEY (tenant_id) REFERENCES registered_tenants(tenant_id))");
            
            // COMPLAINTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS complaints ("
                    + "complaint_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "message TEXT,"
                    + "date_submitted TEXT)");

            // ROOM BILLS TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS room_bills ("
                    + "bill_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "rent_amount REAL DEFAULT 0.0,"
                    + "rent_due_date TEXT,"
                    + "electricity_amount REAL DEFAULT 0.0,"
                    + "electricity_due_date TEXT,"
                    + "water_amount REAL DEFAULT 0.0,"
                    + "water_due_date TEXT,"
                    + "internet_amount REAL DEFAULT 0.0,"
                    + "internet_due_date TEXT)");

             // ANNOUNCEMENTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS announcements ("
                    + "announcement_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "title TEXT,"
                    + "message TEXT,"
                    + "date_posted TEXT)");

            // PAYMENTS TABLE 
            stmt.execute("CREATE TABLE IF NOT EXISTS payment_transactions ("
                    + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "tenant_id INTEGER,"
                    + "room_number TEXT,"
                    + "payment_method TEXT,"
                    + "reference_no TEXT,"
                    + "date_paid TEXT,"
                    + "status TEXT DEFAULT 'PENDING')");

            // EXPENSES TABLE (THIS WAS MISSING!)
            stmt.execute("CREATE TABLE IF NOT EXISTS expenses ("
                    + "expense_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "apartment_id INTEGER, "
                    + "room_number TEXT, "
                    + "expense_category TEXT, "
                    + "amount REAL DEFAULT 0.0, "
                    + "expense_date TEXT, "
                    + "month TEXT, "
                    + "description TEXT, "
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id))");

            // Create a table for personal tenant and owner notifications
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications ("
                    + "notification_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                      + "target_username TEXT, " // The specific tenant or owner who gets the message
                      + "title TEXT, "
                      + "message TEXT, "
                      + "is_read INTEGER DEFAULT 0, " // 0 for unread, 1 for read
                      + "date_created DATE DEFAULT CURRENT_DATE"
                      + ")");
            
            // FULL BARANGAY SEEDING LIST
            String[] bQueries = {
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Adlaon','Pit-os','Binaliw','Agus')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Agus','Lahug','Apas','Pit-os')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Apas','Lahug','Agus','Camputhaw')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Babag','Busan','Bonbon','Pung-ol')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Bacayan','Pit-os','Cabangcalan','Cambinocot')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Banilad','Talamban','Apas','Camputhaw')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Basak Pardo','Labangon','Poblacion Pardo','Bulacao')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Basak San Nicolas','Mambaling','Poblacion Pardo','Labangon')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Binaliw','Adlaon','Pit-os','Cambinocot')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Bonbon','Babag','Busan','Pung-ol')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Budla-an','Sirao','Busay','Agus')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Bulacao','Basak Pardo','Poblacion Pardo','Inayawan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Busay','Sirao','Lahug','Kalunasan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Cabangcalan','Cambinocot','Bacayan','Pit-os')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Cambinocot','Binaliw','Cabangcalan','Bacayan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Camputhaw','Lahug','Apas','Banilad')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Capitol Site','Lahug','Camputhaw','Kalunasan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Carreta','Sambag 1','Tejero','Luz')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Cogon Pardo','Poblacion Pardo','Basak Pardo','Bulacao')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Cogon Ramos','Sambag 2','Pari-an','Zapatera')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Day-as','Luz','Carreta','Sambag 1')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Duljo','Poblacion Pardo','Basak Pardo','Mambaling')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Ermita','Parian','Sto Nino','Colon')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Guadalupe','Punta Princesa','Capitol Site','Kalunasan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Hipodromo','Luz','Carreta','Sambag 1')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Inayawan','Bulacao','Basak Pardo','Poblacion Pardo')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Kalunasan','Guadalupe','Busay','Lahug')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Kamagayan','Pari-an','Sto Nino','Ermita')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Kasambagan','Banilad','Talamban','Apas')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Labangon','Basak Pardo','Mambaling','Quiot')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Lahug','Apas','Camputhaw','Banilad')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Luz','Carreta','Sambag 1','Hipodromo')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Mabolo','Banilad','Talamban','Kasambagan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Mambaling','Basak San Nicolas','Labangon','Suba')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Pari-an','Sto Nino','Ermita','Sambag 2')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Pit-os','Binaliw','Adlaon','Bacayan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Poblacion Pardo','Basak Pardo','Cogon Pardo','Mambaling')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Punta Princesa','Guadalupe','Capitol Site','Kalunasan')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Sambag 1','Carreta','Luz','Sambag 2')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Sambag 2','Sambag 1','Cogon Ramos','Zapatera')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'San Nicolas','Basak San Nicolas','Mambaling','Labangon')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Sirao','Busay','Budla-an','Pulangbato')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Sto Nino','Parian','Ermita','Colon')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Talamban','Banilad','Kasambagan','Apas')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Tejero','Carreta','Luz','Sambag 1')",
                "INSERT OR IGNORE INTO barangays VALUES (NULL,'Zapatera','Sambag 2','Cogon Ramos','Sambag 1')"
            };
            
            for (String q : bQueries) {
                stmt.execute(q);
            }

            System.out.println("✓ Database structure updated (Text-based capacity only).");

        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
        }
    }
}