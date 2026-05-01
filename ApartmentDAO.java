package com.mycompany.apartmentssystem1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO {

    // ADD APARTMENT
    public void addApartment(String name,
                             String tin,
                             int rooms,
                             double rent,
                             double down,
                             String paymentMethod,
                             String description,
                             String policy,
                             String barangay,
                             String street,
                             String electricity,
                             String water,
                             String internet,
                             String contact,
                             String email,
                             String social,
                             String emergency,
                             String profileImage) {

        // OWNER APARTMENT FORM)
        //
        // txtApartmentName
        // txtTIN
        // txtRooms
        // txtRent
        // txtDown
        // txtPaymentMethod
        // txtDescription
        // txtPolicy
        // txtBarangay
        // txtStreet
        // radioElectricity (fixed/meter)
        // radioWater (fixed/meter)
        // radioInternet (fixed/meter)
        // txtContact
        // txtEmail
        // txtSocial
        // txtEmergency
        // uploadProfileImage
        //
        // BUTTON:
        // btnAddApartment → CALL THIS METHOD

        String sql = "INSERT INTO apartments(apartment_name, tin_no, rooms_available, rent_per_room, down_payment, payment_method, description, policy, barangay, street, electricity_type, water_type, internet_type, contact_number, email, social_media, emergency_number, profile_image) "
                   + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, tin);
            ps.setInt(3, rooms);
            ps.setDouble(4, rent);
            ps.setDouble(5, down);
            ps.setString(6, paymentMethod);
            ps.setString(7, description);
            ps.setString(8, policy);
            ps.setString(9, barangay);
            ps.setString(10, street);
            ps.setString(11, electricity);
            ps.setString(12, water);
            ps.setString(13, internet);
            ps.setString(14, contact);
            ps.setString(15, email);
            ps.setString(16, social);
            ps.setString(17, emergency);
            ps.setString(18, profileImage);

            ps.executeUpdate();
            System.out.println("Apartment added!");

        } catch (Exception e) {
            System.out.println("Apartment Error: " + e.getMessage());
        }
    }

    // 🟢 SEARCH BY BARANGAY
    public List<String> getApartmentsByBarangay(String barangay) {

        //  SEARCH LOCATION(TENANT SIDE)
        //
        // dropdownBarangay
        // btnSearch → CALL THIS METHOD
        //
        // OUTPUT:
        // show list with:
        // apartment name + image + available rooms

        List<String> list = new ArrayList<>();

        String sql = "SELECT * FROM apartments WHERE barangay=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barangay);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(
                    rs.getString("apartment_name") + " - Rooms: " +
                    rs.getInt("rooms_available")
                );
            }

        } catch (Exception e) {
            System.out.println("Search Error: " + e.getMessage());
        }

        return list;
    }
}