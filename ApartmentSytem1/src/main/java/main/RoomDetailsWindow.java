package main;

import com.mycompany.apartmentsytem1.DBConnection;
import com.mycompany.apartmentsytem1.ViewingDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RoomDetailsWindow extends JFrame {

    public RoomDetailsWindow(int apartmentId, String roomNumber) {
        
        // --- SECURE DATABASE PIPELINE ---
        String aptName = "Loading...";
        String capacity = "Loading...", utilities = "Loading...", contact = "Loading...";
        double rent = 0.0, dp = 0.0, sd = 0.0;
        String imgUrl = "";

        String sql = "SELECT r.rent_amount, r.down_payment, r.security_deposit, r.capacity_text, r.image_url, " +
                     "a.apartment_name, a.electricity_type, a.water_type, a.internet_type, a.contact_number, a.email " +
                     "FROM rooms r JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "WHERE r.apartment_id = ? AND r.room_number = ?";
                     
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apartmentId);
            ps.setString(2, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                aptName = rs.getString("apartment_name");
                rent = rs.getDouble("rent_amount");
                dp = rs.getDouble("down_payment");
                sd = rs.getDouble("security_deposit");
                capacity = rs.getString("capacity_text");
                imgUrl = rs.getString("image_url");
                contact = rs.getString("contact_number") + " | " + rs.getString("email");
                
                utilities = "Water (" + rs.getString("water_type") + ") | " +
                            "Elec (" + rs.getString("electricity_type") + ") | " +
                            "Net (" + rs.getString("internet_type") + ")";
            }
        } catch (Exception e) { e.printStackTrace(); }

        setTitle("Room Viewing - " + aptName);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 51, 26));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(0, 102, 51));
        JButton btnBack = new JButton("← Back");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE); 
        btnBack.setContentAreaFilled(false); btnBack.setBorderPainted(false); btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> dispose());
        headerPanel.add(btnBack);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setOpaque(false);

        // ========================================================
        // LEFT PANEL: Details & Booking Form
        // ========================================================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(0, 35, 15));
        leftPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("Establishment Details");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblDesc = new JLabel("<html><b>Description:</b> <br>" + capacity + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDesc.setForeground(Color.LIGHT_GRAY);
        
        JLabel lblUtil = new JLabel("<html><b>Utilities:</b> <br>" + utilities + "</html>");
        lblUtil.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblUtil.setForeground(Color.LIGHT_GRAY);

        JLabel lblDP = new JLabel("Down Payment: ₱ " + String.format("%,.2f", dp));
        lblDP.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDP.setForeground(Color.WHITE);

        JLabel lblSD = new JLabel("Security Deposit: ₱ " + String.format("%,.2f", sd));
        lblSD.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSD.setForeground(Color.WHITE);
        
        JLabel lblContact = new JLabel("Owner Contact: " + contact);
        lblContact.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblContact.setForeground(Color.ORANGE);

        leftPanel.add(lblTitle); leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(lblDesc); leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(lblUtil); leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(lblDP); leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(lblSD); leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(lblContact); leftPanel.add(Box.createVerticalStrut(40));

        // Booking Form
        JLabel lblFormTitle = new JLabel("Book This Room");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblFormTitle.setForeground(new Color(0, 204, 102));

        JTextField txtName = createInputField("Full Name");
        JTextField txtContact = createInputField("Contact Number");
        JTextField txtDate = createInputField("Move-in Date (YYYY-MM-DD)");
        
        JComboBox<String> timeCombo = new JComboBox<>(new String[]{"08:00 - 09:00", "09:00 - 10:00", "13:00 - 14:00", "15:00 - 16:00"});
        timeCombo.setMaximumSize(new Dimension(400, 40));

        JButton btnBook = new JButton("CONFIRM BOOKING");
        btnBook.setBackground(new Color(0, 204, 102));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBook.setMaximumSize(new Dimension(400, 45));
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBook.addActionListener(e -> {
            ViewingDAO dao = new ViewingDAO();
            String[] creds = dao.bookRoomViewing(apartmentId, roomNumber, txtName.getText(), txtContact.getText(), txtDate.getText(), timeCombo.getSelectedItem().toString());
            if (creds != null) {
                JOptionPane.showMessageDialog(this, "Success! Give these to the tenant to log in:\nUser: " + creds[0] + "\nPass: " + creds[1]);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book viewing.");
            }
        });

        leftPanel.add(lblFormTitle); leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(txtName); leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(txtContact); leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(txtDate); leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(timeCombo); leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(btnBook);

        // ========================================================
        // RIGHT PANEL: Image & Price
        // ========================================================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(220, 240, 255));
        
        try {
            java.io.File file = new java.io.File("uploads/" + imgUrl);
            if (file.exists() && imgUrl != null && !imgUrl.trim().isEmpty()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(600, 450, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(scaled));
            } else {
                imgLabel.setText("No Image Available");
                imgLabel.setForeground(Color.GRAY);
                imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            }
        } catch (Exception ex) { imgLabel.setText("Image Error"); }

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel lblRoom = new JLabel(roomNumber);
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblRoom.setForeground(Color.WHITE);
        lblRoom.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel("₱ " + String.format("%,.2f", rent) + " / mos");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPrice.setForeground(new Color(0, 204, 102));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(lblRoom);
        titlePanel.add(lblPrice);

        rightPanel.add(imgLabel, BorderLayout.CENTER);
        rightPanel.add(titlePanel, BorderLayout.SOUTH);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);
        add(mainContainer, BorderLayout.CENTER);
    }

    private JTextField createInputField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setMaximumSize(new Dimension(400, 40));
        txt.setBackground(new Color(0, 51, 26));
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 102, 51)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) { if (txt.getText().equals(placeholder)) txt.setText(""); }
            public void focusLost(java.awt.event.FocusEvent evt) { if (txt.getText().isEmpty()) txt.setText(placeholder); }
        });
        return txt;
    }
}