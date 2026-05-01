// DONOT MODIFYYYYY


package com.mycompany.apartmentssystem1;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {

        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            //ang register form inputs kay mapunta ari nga table
            String owners = "CREATE TABLE IF NOT EXISTS owners ("
                    + "owner_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "address TEXT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT"
                    + ")";
            stmt.execute(owners);

            // same sd sa apartments
            String apartments = "CREATE TABLE IF NOT EXISTS apartments ("
                    + "apartment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "owner_id INTEGER,"
                    + "apartment_name TEXT,"
                    + "rooms_available INTEGER,"
                    + "rent_per_room REAL,"
                    + "down_payment REAL,"
                    + "description TEXT,"
                    + "barangay TEXT,"
                    + "street TEXT,"
                    + "electricity TEXT,"
                    + "water TEXT,"
                    + "internet TEXT,"
                    + "contact_number TEXT,"
                    + "email TEXT,"
                    + "social_media TEXT"
                    + ")";
            stmt.execute(apartments);

            System.out.println("Tables created successfully!");

        } catch (Exception e) {
            System.out.println("Table Error: " + e.getMessage());
        }
    }
}