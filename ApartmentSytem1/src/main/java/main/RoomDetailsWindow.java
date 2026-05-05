package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomDetailsWindow extends JFrame {

    public RoomDetailsWindow(SearchWindow.ApartmentData apt) {
        setTitle("Room Viewing - " + apt.name);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 51, 26)); // Dark Green Background

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(0, 102, 51));
        JLabel titleLabel = new JLabel("Apartment Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN 2-PART SPLIT (Left side for Info+Form, Right side for Image) ---
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setOpaque(false);

        // ================= LEFT HALF (Split into Info and Form) =================
        JPanel infoAndFormPanel = new JPanel(new GridLayout(1, 2, 40, 0)); // 2 columns inside the left half
        infoAndFormPanel.setOpaque(false);
        infoAndFormPanel.setBorder(new EmptyBorder(40, 40, 40, 20));

        // ----------------- COLUMN 1: FAR LEFT (Room Info & Descriptions) -----------------
        JPanel farLeftPanel = new JPanel();
        farLeftPanel.setLayout(new BoxLayout(farLeftPanel, BoxLayout.Y_AXIS));
        farLeftPanel.setOpaque(false);

        JLabel roomName = new JLabel("Room 8"); // Hardcoded for MVP
        roomName.setFont(new Font("Segoe UI", Font.BOLD, 48));
        roomName.setForeground(Color.WHITE);
        
        JLabel aptName = new JLabel(apt.name);
        aptName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        aptName.setForeground(Color.WHITE);

        JLabel address = new JLabel(apt.barangay + ", " + apt.street);
        address.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        address.setForeground(Color.LIGHT_GRAY);

        farLeftPanel.add(roomName);
        farLeftPanel.add(aptName);
        farLeftPanel.add(address);
        farLeftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Description & Utilities (Side-by-side using GridLayout)
        JPanel descUtilPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        descUtilPanel.setOpaque(false);
        descUtilPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html><b>Room Description</b><br><br>With Inside Bathroom<br>1 Bed Included<br>Appliances<br>Smart Lock<br>Kitchen with Exhaust</html>");
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setVerticalAlignment(SwingConstants.TOP);

        JLabel utilLabel = new JLabel("<html><b>Utilities</b><br><br>Water (Meter)<br>Electricity (Submeter)<br>Internet (Optional)</html>");
        utilLabel.setForeground(Color.WHITE);
        utilLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        utilLabel.setVerticalAlignment(SwingConstants.TOP);

        descUtilPanel.add(descLabel);
        descUtilPanel.add(utilLabel);
        
        farLeftPanel.add(descUtilPanel);
        farLeftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel contactLabel = new JLabel("<html><b>Contact Details</b><br>Tel: 0932-567-3219<br>Email: yesapartment@yahoo.com</html>");
        contactLabel.setForeground(Color.WHITE);
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        farLeftPanel.add(contactLabel);
        farLeftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel priceLabel = new JLabel("<html><b style='font-size:24px'>₱ " + String.format("%,.2f", apt.rent) + "</b><br><i style='font-size:9px'>₱ 4,000.00 Down Payment<br>₱ 7,000.00 Security Deposit</i></html>");
        priceLabel.setForeground(Color.WHITE);
        farLeftPanel.add(priceLabel);

        // ----------------- COLUMN 2: MIDDLE (Form) -----------------
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setOpaque(false);

        // --- ALIGNMENT FIX: Push the form down to match the Description text ---
        // This adds an invisible 140-pixel block at the top to simulate aligning with the middle.
        middlePanel.add(Box.createRigidArea(new Dimension(0, 140))); 

        // Dynamic 1-Month Date Generator
        List<String> dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        for (int i = 1; i <= 30; i++) {
            dateList.add(today.plusDays(i).format(formatter));
        }

        JComboBox<String> dateBox = new JComboBox<>(dateList.toArray(new String[0]));
        dateBox.setMaximumSize(new Dimension(300, 35));
        dateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        middlePanel.add(dateBox);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- RESTORED: Time Dropdown ---
        String[] times = {"7:00 AM - 8:00 AM", "8:00 AM - 9:00 AM", "9:00 AM - 10:00 AM", 
                          "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM", "1:00 PM - 2:00 PM", "2:00 PM - 3:00 PM", "3:00 PM - 4:00 PM"};
        JComboBox<String> timeBox = new JComboBox<>(times);
        timeBox.setMaximumSize(new Dimension(300, 35));
        timeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        middlePanel.add(timeBox);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel timeRule = new JLabel("Room Viewing is between 7:00 AM - 4:00 PM", SwingConstants.LEFT);
        timeRule.setForeground(Color.WHITE);
        timeRule.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeRule.setAlignmentX(Component.LEFT_ALIGNMENT);
        middlePanel.add(timeRule);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Form Fields
        JLabel lblName = new JLabel("Name");
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT); 
        JTextField txtName = new JTextField();
        txtName.setMaximumSize(new Dimension(300, 35));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblContact = new JLabel("Contact Number");
        lblContact.setForeground(Color.WHITE);
        lblContact.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField txtContact = new JTextField();
        txtContact.setMaximumSize(new Dimension(300, 35));
        txtContact.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSchedule = new JButton("Schedule Room Viewing");
        btnSchedule.setBackground(new Color(0, 204, 102));
        btnSchedule.setForeground(Color.WHITE);
        btnSchedule.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSchedule.setMaximumSize(new Dimension(300, 45));
        btnSchedule.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSchedule.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSchedule.addActionListener(e -> {
             String name = txtName.getText().trim();
             String contact = txtContact.getText().trim();
             String date = dateBox.getSelectedItem().toString();
             String time = timeBox.getSelectedItem().toString();

             if(name.isEmpty() || contact.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Please enter your name and contact number.");
                 return;
             }

             // 1. Find the actual Apartment ID from the database using the apartment name
             int aptId = -1;
             try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
                  java.sql.PreparedStatement ps = conn.prepareStatement("SELECT apartment_id FROM apartments WHERE apartment_name = ?")) {
                 ps.setString(1, apt.name);
                 java.sql.ResultSet rs = ps.executeQuery();
                 if (rs.next()) aptId = rs.getInt("apartment_id");
             } catch (Exception ex) {
                 ex.printStackTrace();
             }

             if (aptId == -1) {
                 JOptionPane.showMessageDialog(this, "Error: Apartment not found in database. Make sure it is registered.");
                 return;
             }

             // 2. Actually save the booking and get the securely generated credentials!
             com.mycompany.apartmentsytem1.ViewingDAO viewingDao = new com.mycompany.apartmentsytem1.ViewingDAO();
             // Passing "8" since "Room 8" is currently the displayed room in this window
             String[] credentials = viewingDao.bookRoomViewing(aptId, "8", name, contact, date, time);

             if (credentials != null) {
                 String message = "ROOM VIEWING BOOKED!\n\n"
                                + "Room Viewing booked, use the Temporary LOG IN Credentials\n"
                                + "to view the status of your booking.\n\n"
                                + "Username: " + credentials[0] + "\n"
                                + "Password: " + credentials[1];
                 JOptionPane.showMessageDialog(this, message, "Success!", JOptionPane.INFORMATION_MESSAGE);
                 this.dispose(); 
             } else {
                 JOptionPane.showMessageDialog(this, "Failed to book viewing. Database error.", "Error", JOptionPane.ERROR_MESSAGE);
             }
         });

        middlePanel.add(lblName); 
        middlePanel.add(txtName);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        middlePanel.add(lblContact); 
        middlePanel.add(txtContact);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        middlePanel.add(btnSchedule);

        infoAndFormPanel.add(farLeftPanel);
        infoAndFormPanel.add(middlePanel);

        // ================= RIGHT HALF (Image) =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(40, 0, 40, 40));

        JLabel imageLabel = new JLabel("No Image Uploaded", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        imageLabel.setForeground(Color.WHITE);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0, 102, 51));

        rightPanel.add(imageLabel, BorderLayout.CENTER);

        mainContainer.add(infoAndFormPanel);
        mainContainer.add(rightPanel);
        add(mainContainer, BorderLayout.CENTER);
    }
}