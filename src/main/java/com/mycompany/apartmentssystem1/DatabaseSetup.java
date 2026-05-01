package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // =============================================
            // OWNERS TABLE
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS owners ("
                    + "owner_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT)");

            // =============================================
            // APARTMENTS TABLE (UPDATED STRUCTURE)
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS apartments ("
                    + "apartment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_code TEXT UNIQUE,"
                    + "apartment_name TEXT,"
                    + "owner_id INTEGER,"
                    + "tin_no TEXT,"
                    + "floors INTEGER,"
                    + "rooms_per_floor INTEGER,"
                    + "total_rooms INTEGER,"
                    + "rooms_available INTEGER,"
                    + "rent_per_room REAL,"
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
                    + "profile_image TEXT)");

            // =============================================
            // BARANGAYS TABLE (FULL RESTORED LIST)
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS barangays ("
                    + "barangay_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT UNIQUE,"
                    + "nearby_1 TEXT,"
                    + "nearby_2 TEXT,"
                    + "nearby_3 TEXT)");

            // =============================================
            // FULL CEBU CITY BARANGAYS DATA
            // =============================================

            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Adlaon','Pit-os','Binaliw','Agus')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Agus','Lahug','Apas','Pit-os')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Apas','Lahug','Agus','Camputhaw')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Babag','Busan','Bonbon','Pung-ol')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Bacayan','Pit-os','Cabangcalan','Cambinocot')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Banilad','Talamban','Apas','Camputhaw')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Basak Pardo','Labangon','Poblacion Pardo','Bulacao')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Basak San Nicolas','Mambaling','Poblacion Pardo','Labangon')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Binaliw','Adlaon','Pit-os','Cambinocot')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Bonbon','Babag','Busan','Pung-ol')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Budla-an','Sirao','Busay','Agus')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Bulacao','Basak Pardo','Poblacion Pardo','Inayawan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Busay','Sirao','Lahug','Kalunasan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Cabangcalan','Cambinocot','Bacayan','Pit-os')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Cambinocot','Binaliw','Cabangcalan','Bacayan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Camputhaw','Lahug','Apas','Banilad')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Capitol Site','Lahug','Camputhaw','Kalunasan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Carreta','Sambag 1','Tejero','Luz')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Cogon Pardo','Poblacion Pardo','Basak Pardo','Bulacao')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Cogon Ramos','Sambag 2','Pari-an','Zapatera')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Day-as','Luz','Carreta','Sambag 1')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Duljo','Poblacion Pardo','Basak Pardo','Mambaling')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Ermita','Parian','Sto Nino','Colon')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Guadalupe','Punta Princesa','Capitol Site','Kalunasan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Hipodromo','Luz','Carreta','Sambag 1')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Inayawan','Bulacao','Basak Pardo','Poblacion Pardo')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Kalunasan','Guadalupe','Busay','Lahug')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Kamagayan','Pari-an','Sto Nino','Ermita')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Kasambagan','Banilad','Talamban','Apas')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Labangon','Basak Pardo','Mambaling','Quiot')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Lahug','Apas','Camputhaw','Banilad')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Luz','Carreta','Sambag 1','Hipodromo')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Mabolo','Banilad','Talamban','Kasambagan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Mambaling','Basak San Nicolas','Labangon','Suba')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Pari-an','Sto Nino','Ermita','Sambag 2')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Pit-os','Binaliw','Adlaon','Bacayan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Poblacion Pardo','Basak Pardo','Cogon Pardo','Mambaling')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Punta Princesa','Guadalupe','Capitol Site','Kalunasan')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Sambag 1','Carreta','Luz','Sambag 2')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Sambag 2','Sambag 1','Cogon Ramos','Zapatera')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'San Nicolas','Basak San Nicolas','Mambaling','Labangon')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Sirao','Busay','Budla-an','Pulangbato')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Sto Nino','Parian','Ermita','Colon')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Talamban','Banilad','Kasambagan','Apas')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Tejero','Carreta','Luz','Sambag 1')");
            stmt.execute("INSERT OR IGNORE INTO barangays VALUES (NULL,'Zapatera','Sambag 2','Cogon Ramos','Sambag 1')");

            // =============================================
            // ROOM OCCUPANCY
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS room_occupancy ("
                    + "occupancy_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "tenant_id INTEGER,"
                    + "move_in_date TEXT,"
                    + "move_out_date TEXT,"
                    + "status TEXT)");

            // =============================================
            // TENANT HISTORY
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS tenant_history ("
                    + "history_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "tenant_id INTEGER,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "apartment_id INTEGER,"
                    + "room_number TEXT,"
                    + "move_in_date TEXT,"
                    + "move_out_date TEXT)");

            // =============================================
            // TENANTS BASE (NO is_active)
            // =============================================
            stmt.execute("CREATE TABLE IF NOT EXISTS tenants_base ("
                    + "tenant_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "emergency_contact TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT,"
                    + "valid_id TEXT,"
                    + "moved_out_date TEXT)");

            // =============================================
            // VIEW (CURRENT TENANTS ONLY)
            // =============================================
            stmt.execute("DROP VIEW IF EXISTS tenants");

            stmt.execute("CREATE VIEW tenants AS "
                    + "SELECT t.tenant_id, t.name, t.contact_number, t.email, t.address, "
                    + "a.apartment_name, a.apartment_code, ro.room_number, ro.move_in_date "
                    + "FROM room_occupancy ro "
                    + "JOIN tenants_base t ON ro.tenant_id = t.tenant_id "
                    + "JOIN apartments a ON ro.apartment_id = a.apartment_id "
                    + "WHERE ro.status='Current'");

        } catch (Exception e) {
            System.out.println("DB Setup Error: " + e.getMessage());
        }
    }
}