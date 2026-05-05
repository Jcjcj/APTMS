package main;

import com.mycompany.apartmentsytem1.DBConnection;
import com.mycompany.apartmentsytem1.PasswordUtil;
import Dashboard.TenantDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TenantRegistrationWindow extends JFrame {

    private int tenantId;
    private JTextField txtName; 
    private int currentAptId;
    private final Color COLOR_BG = new Color(0, 51, 26);
    private final Color COLOR_HEADER = new Color(0, 102, 51);
    private final Color COLOR_BTN = new Color(0, 204, 102);

    public TenantRegistrationWindow(int tenantId) {
        
        this.tenantId = tenantId;

        setTitle("Tenant Registration - Complete Your Profile");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(COLOR_HEADER);
        JLabel titleLabel = new JLabel("Apartment Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTAINER ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel lblMainTitle = new JLabel("Tenant Registration");
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblMainTitle.setForeground(Color.WHITE);
        mainPanel.add(lblMainTitle, BorderLayout.NORTH);

        // --- FORM GRID (2 Columns) ---
        JPanel formGrid = new JPanel(new GridLayout(1, 2, 80, 0));
        formGrid.setOpaque(false);
        formGrid.setBorder(new EmptyBorder(30, 0, 0, 0));

        // LEFT COLUMN (Personal Info)
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        
        JLabel lblPersonalInfo = new JLabel("Personal Information");
        lblPersonalInfo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPersonalInfo.setForeground(Color.WHITE);
        leftCol.add(lblPersonalInfo);
        leftCol.add(Box.createVerticalStrut(20));

        txtName = createFieldRow(leftCol, "Name");
        txtName.setEditable(false); // Locked, owner assigned this
        JTextField txtAddress = createFieldRow(leftCol, "Address");
        JTextField txtContact = createFieldRow(leftCol, "Contact Number");
        JTextField txtEmail = createFieldRow(leftCol, "Email");
        JTextField txtEmergency = createFieldRow(leftCol, "Emergency Contact");

        // RIGHT COLUMN (Apartment Details)
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        JLabel lblAptDetails = new JLabel("Apartment Details");
        lblAptDetails.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAptDetails.setForeground(Color.WHITE);
        rightCol.add(lblAptDetails);
        rightCol.add(Box.createVerticalStrut(20));

        JTextField txtAptName = createFieldRow(rightCol, "Apartment Name");
        txtAptName.setEditable(false);
        JTextField txtRoom = createFieldRow(rightCol, "Room Number");
        txtRoom.setEditable(false);
        
        // Split Row for Date and Occupants
        JPanel splitRow = new JPanel(new GridLayout(1, 2, 20, 0));
        splitRow.setOpaque(false);
        JPanel datePanel = new JPanel(); datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS)); datePanel.setOpaque(false);
        JTextField txtMoveIn = createFieldRow(datePanel, "Move-in Date");
        txtMoveIn.setEditable(false);
        
        JPanel occPanel = new JPanel(); occPanel.setLayout(new BoxLayout(occPanel, BoxLayout.Y_AXIS)); occPanel.setOpaque(false);
        JTextField txtOccupants = createFieldRow(occPanel, "Number of Occupants");
        
        splitRow.add(datePanel);
        splitRow.add(occPanel);
        rightCol.add(splitRow);
        rightCol.add(Box.createVerticalStrut(15));

        JTextField txtUser = createFieldRow(rightCol, "Account Username");
        txtUser.setEditable(false); // Locked
        
        JLabel lblPass = createLabel("Password");
        rightCol.add(lblPass);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPass.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        rightCol.add(txtPass);

        formGrid.add(leftCol);
        formGrid.add(rightCol);
        mainPanel.add(formGrid, BorderLayout.CENTER);

        // --- SUBMIT BUTTON ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        JButton btnSignUp = new JButton("SIGN UP");
        btnSignUp.setBackground(COLOR_BTN);
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSignUp.setPreferredSize(new Dimension(150, 45));
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSignUp.addActionListener(e -> {
            String address = txtAddress.getText().trim();
            String contact = txtContact.getText().trim();
            String email = txtEmail.getText().trim();
            String emergency = txtEmergency.getText().trim();
            String occupants = txtOccupants.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (address.isEmpty() || contact.isEmpty() || email.isEmpty() || emergency.isEmpty() || occupants.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all empty fields to complete your registration.");
                return;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }

            saveTenantProfile(address, contact, email, emergency, occupants, pass);
        });

        bottomPanel.add(btnSignUp);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Load the Initial Data!
        loadInitialData(txtName, txtAptName, txtRoom, txtMoveIn, txtUser);
    }

    private JTextField createFieldRow(JPanel parent, String labelText) {
        parent.add(createLabel(labelText));
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        parent.add(txt);
        parent.add(Box.createVerticalStrut(15));
        return txt;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    // Pulls the data the Owner already set up
    private void loadInitialData(JTextField txtName, JTextField txtAptName, JTextField txtRoom, JTextField txtMoveIn, JTextField txtUser) {
        // ADDED: t.target_apartment_id to the query
        String sql = "SELECT t.name, t.target_room_number, t.move_in_date, t.username, a.apartment_name, t.target_apartment_id " +
                     "FROM registered_tenants t JOIN apartments a ON t.target_apartment_id = a.apartment_id " +
                     "WHERE t.tenant_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("name"));
                txtRoom.setText(rs.getString("target_room_number"));
                txtMoveIn.setText(rs.getString("move_in_date"));
                txtUser.setText(rs.getString("username"));
                txtAptName.setText(rs.getString("apartment_name"));
                
                // STORE THE ID:
                this.currentAptId = rs.getInt("target_apartment_id");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveTenantProfile(String address, String contact, String email, String emergency, String occupants, String pass) {
        String hashedPass = PasswordUtil.hashPassword(pass);
        String sql = "UPDATE registered_tenants SET address = ?, contact_number = ?, email = ?, emergency_contact = ?, occupants = ?, password = ? WHERE tenant_id = ?";
        
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address);
            ps.setString(2, contact);
            ps.setString(3, email);
            ps.setString(4, emergency);
            ps.setInt(5, Integer.parseInt(occupants));
            ps.setString(6, hashedPass);
            ps.setInt(7, tenantId);
            
            if (ps.executeUpdate() > 0) {
                // --- NEW: CLEANUP LOGIC ---
                // We fetch the name and aptId to find the temp record
                com.mycompany.apartmentsytem1.ViewingDAO vDao = new com.mycompany.apartmentsytem1.ViewingDAO(); 
                // 'txtName' and 'apt' are available in your window's scope
                vDao.cleanupTemporaryAccount(currentAptId, txtName.getText());

                JOptionPane.showMessageDialog(this, "Profile Completed! Welcome to your dashboard.");
                this.dispose();
                new Dashboard.TenantDashboard(tenantId).setVisible(true); 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}