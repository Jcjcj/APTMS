package main;

import com.mycompany.apartmentsytem1.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoomSelectionWindow extends JFrame {

    private SearchWindow.ApartmentData apt;

    public RoomSelectionWindow(SearchWindow.ApartmentData apt) {
        this.apt = apt;
        
        setTitle("Select a Room - " + apt.name);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 25, 10));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(0, 102, 51));
        JLabel titleLabel = new JLabel("Apartment Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainSplit = new JPanel(new BorderLayout());
        mainSplit.setOpaque(false);

        mainSplit.add(buildLeftPanel(), BorderLayout.WEST);

        JPanel rightPanel = buildRoomGrid();
        JScrollPane scrollPane = new JScrollPane(rightPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        mainSplit.add(scrollPane, BorderLayout.CENTER);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(new Color(0, 51, 26)); 
        left.setPreferredSize(new Dimension(400, 0));
        left.setBorder(new EmptyBorder(40, 40, 40, 20));

        String desc = "Loading...";
        String contact = "Loading...";
        String email = "Loading...";
        String utilities = "";

        // Get details to display on the left side
        String sql = "SELECT description, contact_number, email, electricity_type, water_type, internet_type FROM apartments WHERE apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apt.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                desc = rs.getString("description");
                contact = rs.getString("contact_number");
                email = rs.getString("email");
                utilities = "Water (" + rs.getString("water_type") + ")<br>Electricity (" + rs.getString("electricity_type") + ")<br>Internet (" + rs.getString("internet_type") + ")";
            }
        } catch (Exception e) { e.printStackTrace(); }

        JLabel lblName = new JLabel("<html><div style='width:250px;'>" + apt.name + "</div></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblName.setForeground(Color.WHITE);

        JLabel lblAddress = new JLabel(apt.barangay + ", " + apt.street);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblAddress.setForeground(Color.LIGHT_GRAY);
        
        JLabel lblVacant = new JLabel(apt.vacantRooms + " Vacant Room/s Available");
        lblVacant.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lblVacant.setForeground(Color.WHITE);

        JLabel lblDesc = new JLabel("<html><b>Establishment Description</b><br><br>" + desc + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(Color.WHITE);
        
        JLabel lblUtil = new JLabel("<html><b>Utilities</b><br><br>" + utilities + "</html>");
        lblUtil.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUtil.setForeground(Color.WHITE);

        left.add(lblName);
        left.add(lblAddress);
        left.add(Box.createRigidArea(new Dimension(0, 30)));
        left.add(lblVacant);
        left.add(Box.createRigidArea(new Dimension(0, 30)));
        left.add(lblDesc);
        left.add(Box.createRigidArea(new Dimension(0, 20)));
        left.add(lblUtil);

        return left;
    }

    private JPanel buildRoomGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 20, 20)); 
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // 1. UPDATED SQL: Now grabs the image_url from the rooms table
        String sql = "SELECT room_number, rent_amount, image_url FROM rooms WHERE apartment_id = ? AND status = 'Available' ORDER BY room_number ASC";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apt.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String roomNum = rs.getString("room_number");
                double rent = rs.getDouble("rent_amount");
                String imgUrl = rs.getString("image_url"); // 2. Extract the image filename
                
                // 3. Pass the imgUrl into the card creator
                gridPanel.add(createRoomCard(roomNum, rent, imgUrl)); 
            }
        } catch (Exception e) { e.printStackTrace(); }

        return gridPanel;
    }

    // 4. UPDATED PARAMETERS: Added 'String imageUrl' so the card knows what file to look for
    private JPanel createRoomCard(String roomNumber, double rentAmount, String imageUrl) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0, 35, 15)); 
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 51), 2)); 
        card.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        card.setPreferredSize(new Dimension(300, 250));

        JLabel imgPlaceholder = new JLabel();
        imgPlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        imgPlaceholder.setOpaque(true);
        imgPlaceholder.setBackground(new Color(220, 240, 255)); 
        
        // --- REAL IMAGE LOADING LOGIC ---
        try {
            java.io.File file = new java.io.File("uploads/" + imageUrl); 
            if (file.exists() && imageUrl != null && !imageUrl.trim().isEmpty() && !imageUrl.equals("default_room.png")) {
                ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());
                // Scale smaller to fit inside the selection card
                Image scaledImg = originalIcon.getImage().getScaledInstance(300, 180, Image.SCALE_SMOOTH);
                imgPlaceholder.setIcon(new ImageIcon(scaledImg));
            } else {
                imgPlaceholder.setText("No Image Available");
                imgPlaceholder.setForeground(Color.GRAY);
            }
        } catch (Exception ex) {
            imgPlaceholder.setText("Error Loading Image");
        }
        // ----------------------------------------------
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblRoomName = new JLabel(roomNumber);
        lblRoomName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblRoomName.setForeground(Color.WHITE);

        JLabel lblRent = new JLabel("₱ " + String.format("%,.2f", rentAmount));
        lblRent.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRent.setForeground(Color.WHITE);

        infoPanel.add(lblRoomName, BorderLayout.WEST);
        infoPanel.add(lblRent, BorderLayout.EAST);
        card.add(imgPlaceholder, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        // TRANSITION TO THE FINAL BOOKING FORM
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); 
                apt.rent = rentAmount; // Pass the exact rent of this room
                new RoomDetailsWindow(apt, roomNumber).setVisible(true); 
            }
        });

        return card;
    }
}