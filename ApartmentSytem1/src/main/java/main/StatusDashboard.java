package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusDashboard extends JFrame {

    // Expects an array: [0]room_number, [1]apartment_name, [2]apartment_address,
    // [3]schedule_date, [4]viewing_time, [5]status, [6]tenant_name, [7]contact, [8]username
    public StatusDashboard(String[] viewingData, String username){
        boolean isReservation = viewingData.length > 4 && "RESERVE_NOW".equalsIgnoreCase(viewingData[4]);
        setTitle(isReservation ? "Reservation Status" : "Room Viewing Status");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 35, 20)); 

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 51));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(isReservation ? "Room Reservation" : "Room Viewing");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Dynamically set the user's name
        JLabel userLabel = new JLabel(viewingData[6]); 
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(Color.WHITE);
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel statusCard = new JPanel(new BorderLayout());
        statusCard.setBackground(new Color(0, 120, 60)); 
        statusCard.setPreferredSize(new Dimension(800, 300));
        statusCard.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Info on the left of the card
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Dynamically set Room and Apartment Name
        JLabel roomLbl = new JLabel("Room " + viewingData[0]);
        roomLbl.setFont(new Font("Segoe UI", Font.BOLD, 48));
        roomLbl.setForeground(Color.WHITE);
        
        JLabel aptLbl = new JLabel(viewingData[1]);
        aptLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        aptLbl.setForeground(Color.WHITE);

        // Dynamically set Date and Time
        JLabel dateLbl = new JLabel(isReservation ? "Reservation Request" : viewingData[3]);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        dateLbl.setForeground(Color.WHITE);

        JLabel timeLbl = new JLabel(isReservation ? "Pending owner approval" : viewingData[4]);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        timeLbl.setForeground(Color.WHITE);

        infoPanel.add(roomLbl);
        infoPanel.add(aptLbl);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        infoPanel.add(dateLbl);
        infoPanel.add(timeLbl);

        // Status Badge on the right
        String currentStatus = viewingData[5];
        JLabel statusBadge = new JLabel(currentStatus, SwingConstants.CENTER);
        statusBadge.setOpaque(true);
        
        // Change color based on status
        if (currentStatus.equals("PENDING")) {
            statusBadge.setBackground(new Color(255, 165, 0)); // Yellow/Orange
        } else if (currentStatus.equals("APPROVED")) {
            statusBadge.setBackground(new Color(0, 204, 102)); // Green
        } else if (currentStatus.equals("REJECTED")) {
            statusBadge.setBackground(new Color(220, 60, 60)); // Red
        } else {
            statusBadge.setBackground(Color.GRAY);
        }
        
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusBadge.setPreferredSize(new Dimension(200, 60));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(statusBadge);

        statusCard.add(infoPanel, BorderLayout.WEST);
        statusCard.add(badgeWrapper, BorderLayout.EAST);

        JButton btnRegister = new JButton("CONTINUE REGISTRATION");
        btnRegister.setBackground(new Color(0, 204, 102));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setVisible("APPROVED".equalsIgnoreCase(currentStatus));
        btnRegister.addActionListener(e -> {
            this.dispose();
            String prefillName = viewingData.length > 6 ? viewingData[6] : "";
            String prefillContact = viewingData.length > 7 ? viewingData[7] : "";
            String currentUsername = viewingData.length > 8 ? viewingData[8] : "";
            String prefillEmail = viewingData.length > 9 ? viewingData[9] : "";
            new SignUp(
                    "TENANT_PREFILL",
                    prefillName,
                    prefillContact,
                    prefillEmail,
                    currentUsername,
                    viewingData[1],
                    viewingData[0]
            ).setVisible(true);
        });

        // Logout Button to go back to landing page
        JButton btnBack = new JButton("LOG OUT");
        btnBack.setBackground(new Color(180, 50, 50));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            this.dispose();
            new LandingPage().setVisible(true);
        });

        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(btnRegister);
        bottomWrapper.add(btnBack);
        statusCard.add(bottomWrapper, BorderLayout.SOUTH);

        centerPanel.add(statusCard);
        add(centerPanel, BorderLayout.CENTER);
    }
}
