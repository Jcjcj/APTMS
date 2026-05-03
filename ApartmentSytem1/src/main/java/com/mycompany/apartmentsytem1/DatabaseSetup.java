package com.mycompany.apartmentsytem1;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // OWNERS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS owners ("
                    + "owner_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT)");

            // APARTMENTS TABLE (MODIFIED: Removed rooms_per_floor and rent_per_room)
            stmt.execute("CREATE TABLE IF NOT EXISTS apartments ("
                    + "apartment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_code TEXT UNIQUE,"
                    + "apartment_name TEXT,"
                    + "owner_id INTEGER,"
                    + "tin_no TEXT,"
                    + "floors INTEGER,"
                    + "total_rooms INTEGER,"
                    + "rooms_available INTEGER,"
                    + "down_payment REAL,"
                    + "payment_method TEXT,"
                    + "description TEXT,"
                    + "policy TEXT,"
                    + "barangay TEXT,"
                    + "street TEXT,"
                    + "electricity TEXT,"
                    + "water TEXT,"
                    + "internet TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "social_media TEXT,"
                    + "emergency_number TEXT,"
                    + "profile_image TEXT,"
                    + "FOREIGN KEY(owner_id) REFERENCES owners(owner_id) ON DELETE CASCADE)");

            // ROOMS TABLE (MODIFIED: Added rent_amount)
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms ("
                    + "room_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "status TEXT DEFAULT 'Available',"
                    + "rent_amount REAL DEFAULT 0.0," // Each room now has its own price
                    + "description TEXT DEFAULT 'Standard Room description. Update via owner panel.',"
                    + "image_url TEXT DEFAULT 'default_room.png',"
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
                    + "valid_id TEXT,"
                    + "approval_status TEXT DEFAULT 'PENDING',"
                    + "moved_out_date TEXT)");

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

            // VIEWING SCHEDULE TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS viewing_schedule ("
                    + "schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "tenant_name TEXT,"
                    + "contact_number TEXT,"
                    + "schedule_date TEXT,"
                    + "start_time TEXT,"
                    + "end_time TEXT,"
                    + "status TEXT DEFAULT 'SCHEDULED',"
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id))");
                    
            // MAINTENANCE REQUESTS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance_requests ("
                    + "request_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "tenant_id INTEGER,"
                    + "issue_description TEXT,"
                    + "priority_level TEXT," 
                    + "status TEXT DEFAULT 'OPEN'," 
                    + "date_reported TEXT,"
                    + "date_resolved TEXT,"
                    + "FOREIGN KEY(apartment_id) REFERENCES apartments(apartment_id),"
                    + "FOREIGN KEY(tenant_id) REFERENCES registered_tenants(tenant_id))");

            // BARANGAYS TABLE
            stmt.execute("CREATE TABLE IF NOT EXISTS barangays ("
                    + "barangay_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE,"
                    + "nearby_1 TEXT,"
                    + "nearby_2 TEXT,"
                    + "nearby_3 TEXT)");

            // SEED DATA
            String[] barangayQueries = {
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

            for (String query : barangayQueries) {
                stmt.execute(query);
            }

            System.out.println("✓ Database tables created/updated successfully");

        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
        }
    }
}