package com.mycompany.apartmentssystem1;

public class ApartmentsSystem1 {

    public static void main(String[] args) {

        // DO NOT TOUCH THIS

        DBConnection.connect();          // connect database
        DatabaseSetup.createTables();   // create all tables

        System.out.println("System Ready!");

        // TEST AREA (OPTIONAL)
        // REMOVE THIS WHEN FRONTEND IS CONNECTED

        /*
        OwnerDAO owner = new OwnerDAO();
        owner.registerOwner("Test", "09123", "test@email.com", "Cebu", "testuser", "1234");

        TenantDAO tenant = new TenantDAO();
        tenant.registerTenant("Juan", "09123", "juan@email.com", "Cebu", "09999", "juan", "1234", "id.png");

        ApartmentDAO apt = new ApartmentDAO();
        apt.addApartment("Test Apartment", "123456", 5, 3000, 5000,
                "Cash", "Nice place", "No pets",
                "Lahug", "Street 1",
                "fixed", "meter", "fixed",
                "09123", "apt@email.com", "fb.com/test",
                "09999", "image.png");

        ViewingDAO view = new ViewingDAO();
        view.scheduleViewing(1, "Juan", "09123", "2026-05-01", "10:00 AM");
        */

    }
}