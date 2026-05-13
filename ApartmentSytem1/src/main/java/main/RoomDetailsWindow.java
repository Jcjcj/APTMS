package main;

import com.mycompany.apartmentsytem1.DBConnection;
import com.mycompany.apartmentsytem1.RoomDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RoomDetailsWindow extends JFrame {

    private JButton createBackButton(String text) {
        JButton btnBack = new JButton(text);
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.LIGHT_GRAY); 
        btnBack.setContentAreaFilled(false); 
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btnBack;
    }

    public RoomDetailsWindow(SearchWindow.ApartmentData apt, String selectedRoomNumber) {
        setTitle("Room Viewing - " + apt.name);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 51, 26)); // Dark Green Background

        // --- HEADER WITH BACK BUTTON ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 51));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 20));

        JButton btnBack = createBackButton("← Back to Rooms");
        btnBack.addActionListener(e -> {
            this.dispose();
            new RoomSelectionWindow(apt).setVisible(true); // Routes safely back to the rooms grid!
        });

        JLabel titleLabel = new JLabel("Apartment Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // Dummy label for right side to keep the title perfectly centered
        JLabel dummyRight = new JLabel("                  "); 

        headerPanel.add(btnBack, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(dummyRight, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

       // --- SECURE & EXACT DATA FETCHING ---
        double rentVal = 0.0, dpVal = 0.0, secVal = 0.0;
        String descStr = "No description provided.";
        String elecStr = "Standard", waterStr = "Standard", internetStr = "None";
        String imageUrl = "No Image Uploaded";
        String contactNum = "N/A", email = "N/A";

        // FIXED: Using 'r.design_text' to match your actual database schema
        String sql = "SELECT r.rent_amount, r.down_payment, r.security_deposit, r.image_url, r.design_text, " +
                     "a.electricity_type, a.water_type, a.internet_type, a.contact_number, a.email " +
                     "FROM rooms r JOIN apartments a ON r.apartment_id = a.apartment_id " +
                     "WHERE r.apartment_id = ? AND r.room_number = ?";

        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, apt.id);
            ps.setString(2, selectedRoomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rentVal = rs.getDouble("rent_amount");
                dpVal = rs.getDouble("down_payment");
                secVal = rs.getDouble("security_deposit");
                
                String img = rs.getString("image_url");
                if (img != null) imageUrl = img;
                
                descStr = rs.getString("design_text");
                if (descStr == null || descStr.trim().isEmpty()) descStr = "Standard Room";
                
                elecStr = rs.getString("electricity_type");
                waterStr = rs.getString("water_type");
                internetStr = rs.getString("internet_type");
                contactNum = rs.getString("contact_number");
                email = rs.getString("email");
            }
        } catch (Exception e) { 
            System.out.println("Database Error: " + e.getMessage()); 
        }

        String rentStr = String.format("%,.2f", rentVal);
        String dpStr = String.format("%,.2f", dpVal);
        String secStr = String.format("%,.2f", secVal);
        final double finalDpVal = dpVal;
        final String finalDpStr = dpStr;

        // --- MAIN 2-PART SPLIT ---
        JPanel mainContainer = new JPanel(new GridLayout(1, 2));
        mainContainer.setOpaque(false);

        // ================= LEFT HALF =================
        JPanel infoAndFormPanel = new JPanel(new GridLayout(1, 2, 40, 0)); 
        infoAndFormPanel.setOpaque(false);
        infoAndFormPanel.setBorder(new EmptyBorder(40, 40, 40, 20));

        // ----------------- COLUMN 1: ROOM INFO -----------------
        JPanel farLeftPanel = new JPanel();
        farLeftPanel.setLayout(new BoxLayout(farLeftPanel, BoxLayout.Y_AXIS));
        farLeftPanel.setOpaque(false);

        JLabel roomName = new JLabel(selectedRoomNumber); 
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

        JPanel descUtilPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        descUtilPanel.setOpaque(false);
        descUtilPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String descHtml = "<html><b>Room Description</b><br><br>" 
                        + descStr + "</html>";
        
        JLabel descLabel = new JLabel(descHtml);
        descLabel.setForeground(Color.WHITE);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setVerticalAlignment(SwingConstants.TOP);

        // DYNAMIC UTILITIES
        String utilHtml = "<html><b>Utilities</b><br><br>"
                        + "Water (" + waterStr + ")<br>"
                        + "Electricity (" + elecStr + ")<br>"
                        + "Internet (" + internetStr + ")</html>";
        JLabel utilLabel = new JLabel(utilHtml);
        utilLabel.setForeground(Color.WHITE);
        utilLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        utilLabel.setVerticalAlignment(SwingConstants.TOP);

        descUtilPanel.add(descLabel);
        descUtilPanel.add(utilLabel);
        
        farLeftPanel.add(descUtilPanel);
        farLeftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // DYNAMIC CONTACT DETAILS
        String contactHtml = "<html><b>Contact Details</b><br>"
                           + "Tel: " + contactNum + "<br>"
                           + "Email: " + email + "</html>";
        JLabel contactLabel = new JLabel(contactHtml);
        contactLabel.setForeground(Color.WHITE);
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        farLeftPanel.add(contactLabel);
        farLeftPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // DYNAMIC PRICING
        String priceHtml = "<html><b style='font-size:24px'>₱ " + rentStr + "</b><br>"
                         + "<i style='font-size:10px'>₱ " + dpStr + " Down Payment<br>"
                         + "₱ " + secStr + " Security Deposit</i></html>";
        JLabel priceLabel = new JLabel(priceHtml);
        priceLabel.setForeground(Color.WHITE);
        farLeftPanel.add(priceLabel);

       // ----------------- COLUMN 2: BOOKING FORM -----------------
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setOpaque(false);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 70))); 

        String[] allTimes = {"7:00 AM - 8:00 AM", "8:00 AM - 9:00 AM", "9:00 AM - 10:00 AM", 
                             "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM", "1:00 PM - 2:00 PM", 
                             "2:00 PM - 3:00 PM", "3:00 PM - 4:00 PM"};
                             
        JComboBox<String> timeBox = new JComboBox<>(allTimes);
        timeBox.setMaximumSize(new Dimension(300, 35));
        timeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSchedule = new JButton("Schedule Room Viewing");
        btnSchedule.setBackground(new Color(0, 204, 102));
        btnSchedule.setForeground(Color.WHITE);
        btnSchedule.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSchedule.setMaximumSize(new Dimension(300, 45));
        btnSchedule.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSchedule.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnReserve = new JButton("Reserve Now (Pay Down Payment)");
        btnReserve.setBackground(new Color(0, 153, 204));
        btnReserve.setForeground(Color.WHITE);
        btnReserve.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReserve.setMaximumSize(new Dimension(300, 45));
        btnReserve.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnReserve.setCursor(new Cursor(Cursor.HAND_CURSOR));

        List<String> dateList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        for (int i = 1; i <= 30; i++) {
            dateList.add(today.plusDays(i).format(formatter));
        }

        JComboBox<String> dateBox = new JComboBox<>(dateList.toArray(new String[0]));
        dateBox.setMaximumSize(new Dimension(300, 35));
        dateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dateBox.addActionListener(e -> {
            String selectedDate = dateBox.getSelectedItem().toString();
            com.mycompany.apartmentsytem1.ViewingDAO vDao = new com.mycompany.apartmentsytem1.ViewingDAO();
            List<String> taken = vDao.getBookedTimes(apt.id, selectedRoomNumber, selectedDate);

            timeBox.removeAllItems();
            for (String t : allTimes) {
                if (!taken.contains(t)) {
                    timeBox.addItem(t);
                }
            }

            if (timeBox.getItemCount() == 0) {
                timeBox.addItem("FULLY BOOKED (Select another date)");
                btnSchedule.setEnabled(false); 
            } else {
                btnSchedule.setEnabled(true);
            }
        });

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

        btnSchedule.addActionListener(e -> {
             String name = txtName.getText().trim();
             String contact = txtContact.getText().trim();

             if (dateBox.getSelectedItem() == null || timeBox.getSelectedItem() == null) {
                 JOptionPane.showMessageDialog(this, "Please select a valid date and time.");
                 return;
             }

             String date = dateBox.getSelectedItem().toString();
             String time = timeBox.getSelectedItem().toString();

             if(name.isEmpty() || contact.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Please enter your name and contact number first.");
                 return;
             }

             if (time.startsWith("FULLY BOOKED")) {
                 JOptionPane.showMessageDialog(this, "Please select an available viewing time.");
                 return;
             }

             showViewingBookingPopup(name, contact, date, time, apt.id, selectedRoomNumber);
         });

        btnReserve.addActionListener(e -> {
             String name = txtName.getText().trim();
             String contact = txtContact.getText().trim();

             if(name.isEmpty() || contact.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Please enter your name and contact number.");
                 return;
             }

             showDirectBookingPopup(name, contact, finalDpVal, finalDpStr, apt.id, selectedRoomNumber);
          });

        middlePanel.add(dateBox);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        middlePanel.add(timeBox);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JLabel timeRule = new JLabel("Room Viewing is between 7:00 AM - 4:00 PM", SwingConstants.LEFT);
        timeRule.setForeground(Color.WHITE);
        timeRule.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeRule.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        middlePanel.add(timeRule);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        middlePanel.add(lblName); 
        middlePanel.add(txtName);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        middlePanel.add(lblContact); 
        middlePanel.add(txtContact);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        middlePanel.add(btnSchedule);
        middlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        middlePanel.add(btnReserve);

        infoAndFormPanel.add(farLeftPanel);
        infoAndFormPanel.add(middlePanel);

        // ================= RIGHT HALF (Image) =================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(40, 0, 40, 40));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0, 102, 51));

        // --- NEW REAL IMAGE LOADING LOGIC ---
        try {
            java.io.File file = new java.io.File("uploads/" + imageUrl);
            
            // Check if the file actually exists in your uploads folder
            if (file.exists() && !imageUrl.equals("No Image Uploaded") && !imageUrl.trim().isEmpty() && !imageUrl.equals("default_room.png")) {
                ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());
                // Scale the image smoothly so it looks great on the right side of the screen
                Image scaledImg = originalIcon.getImage().getScaledInstance(550, 450, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                // Safe fallback if the owner never uploaded a photo
                imageLabel.setText("No Image Available");
                imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                imageLabel.setForeground(Color.WHITE);
            }
        } catch (Exception ex) {
            imageLabel.setText("Image Load Error");
        }
        // ------------------------------------

        rightPanel.add(imageLabel, BorderLayout.CENTER);

        mainContainer.add(infoAndFormPanel);
        mainContainer.add(rightPanel);
        add(mainContainer, BorderLayout.CENTER);
    }

    private void showDirectBookingPopup(String name, String contact, double dpAmount, String dpStr, int aptId, String roomNum) {
        String gcashNo = "N/A", gcashName = "N/A", mayaNo = "N/A", mayaName = "N/A";
        String sql = "SELECT o.gcash_no, o.gcash_name, o.paymaya_no, o.paymaya_name " +
                     "FROM owners o JOIN apartments a ON o.owner_id = a.owner_id WHERE a.apartment_id = ?";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, aptId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gcashNo = rs.getString("gcash_no");
                gcashName = rs.getString("gcash_name");
                mayaNo = rs.getString("paymaya_no");
                mayaName = rs.getString("paymaya_name");
            }
        } catch (Exception e) {
            System.out.println("Payment Details Error: " + e.getMessage());
        }

        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.getContentPane().setBackground(new Color(5, 20, 10));

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 153, 204), 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel lblTitle = new JLabel("<html><center>MANDATORY DOWN PAYMENT<br><span style='font-size:14px; color:yellow'>PHP " + dpStr + "</span></center></html>", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        JPanel pnlMethods = new JPanel();
        pnlMethods.setLayout(new BoxLayout(pnlMethods, BoxLayout.Y_AXIS));
        pnlMethods.setOpaque(false);
        JLabel lblPayMethods = new JLabel("Send Payment To:");
        lblPayMethods.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPayMethods.setForeground(Color.WHITE);
        pnlMethods.add(lblPayMethods);
        pnlMethods.add(Box.createVerticalStrut(15));

        JLabel lblGcash = new JLabel("<html><b>GCash</b><br>" + safePopupText(gcashNo) + "<br>(" + safePopupText(gcashName) + ")</html>");
        lblGcash.setForeground(Color.WHITE);
        lblGcash.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlMethods.add(lblGcash);
        pnlMethods.add(Box.createVerticalStrut(15));

        JLabel lblMaya = new JLabel("<html><b>Maya</b><br>" + safePopupText(mayaNo) + "<br>(" + safePopupText(mayaName) + ")</html>");
        lblMaya.setForeground(Color.WHITE);
        lblMaya.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlMethods.add(lblMaya);
        centerPanel.add(pnlMethods);

        JPanel pnlInputs = new JPanel();
        pnlInputs.setLayout(new BoxLayout(pnlInputs, BoxLayout.Y_AXIS));
        pnlInputs.setOpaque(false);

        JTextField txtEmail = createDarkPopupField("Email Address");
        JTextField txtUser = createDarkPopupField("Create Username");
        JPasswordField txtPass = new JPasswordField("Password");
        setupPasswordField(txtPass);
        JTextField txtRef = createDarkPopupField("Payment Reference No.");

        pnlInputs.add(txtEmail);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtUser);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtPass);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtRef);
        pnlInputs.add(Box.createVerticalStrut(20));

        JButton btnSubmit = new JButton("SUBMIT RESERVATION");
        btnSubmit.setBackground(new Color(0, 153, 204));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            String ref = txtRef.getText().trim();

            if (email.isEmpty() || user.isEmpty() || pass.isEmpty() || ref.isEmpty()
                    || email.equals("Email Address") || user.equals("Create Username")
                    || pass.equals("Password") || ref.equals("Payment Reference No.")) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                return;
            }

            com.mycompany.apartmentsytem1.ViewingDAO dao = new com.mycompany.apartmentsytem1.ViewingDAO();
            boolean success = dao.bookRoomReservationWithPayment(aptId, roomNum, name, contact, email, user, pass, ref, dpAmount);

            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Reservation submitted! Please wait for the owner to approve it.\n\n" +
                        "Use your chosen username and password to check the status.\n" +
                        "After approval, continue to tenant registration.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                this.dispose();
                new LandingPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, dao.getLastReservationError(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBackground(new Color(150, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel pnlBtns = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnSubmit);
        pnlBtns.add(btnCancel);
        pnlInputs.add(pnlBtns);

        centerPanel.add(pnlInputs);
        panel.add(centerPanel, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTextField createDarkPopupField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setBackground(new Color(0, 35, 20));
        txt.setForeground(Color.LIGHT_GRAY);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 35, 20)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText("");
                    txt.setForeground(Color.WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txt.getText().isEmpty()) {
                    txt.setText(placeholder);
                    txt.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
        return txt;
    }

    private void setupPasswordField(JPasswordField txt) {
        txt.setBackground(new Color(0, 35, 20));
        txt.setForeground(Color.LIGHT_GRAY);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 35, 20)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setEchoChar((char) 0);
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (new String(txt.getPassword()).equals("Password")) {
                    txt.setText("");
                    txt.setEchoChar('*');
                    txt.setForeground(Color.WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (new String(txt.getPassword()).isEmpty()) {
                    txt.setText("Password");
                    txt.setEchoChar((char) 0);
                    txt.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
    }

    private void showViewingBookingPopup(String name, String contact, String date, String time, int aptId, String roomNum) {
        JDialog dialog = new JDialog(this, true);
        dialog.setTitle("Create Account for Viewing");
        dialog.setUndecorated(true);
        dialog.getContentPane().setBackground(new Color(5, 20, 10));

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 204, 102), 2),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel lblTitle = new JLabel(
                "<html><center>CREATE TENANT ACCOUNT<br><span style='font-size:12px; color:lightgray'>To secure your viewing schedule</span></center></html>",
                SwingConstants.CENTER
        );
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInputs = new JPanel();
        pnlInputs.setLayout(new BoxLayout(pnlInputs, BoxLayout.Y_AXIS));
        pnlInputs.setOpaque(false);

        JTextField txtEmail = createDarkPopupField("Email Address");
        JTextField txtUser = createDarkPopupField("Create Username");
        JPasswordField txtPass = new JPasswordField("Password");
        setupPasswordField(txtPass);

        pnlInputs.add(txtEmail);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtUser);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtPass);
        pnlInputs.add(Box.createVerticalStrut(20));

        JButton btnSubmit = new JButton("BOOK VIEWING");
        btnSubmit.setBackground(new Color(0, 204, 102));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> {
            String viewingEmail = txtEmail.getText().trim();
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            if (viewingEmail.isEmpty() || username.isEmpty() || password.isEmpty()
                    || viewingEmail.equals("Email Address") || username.equals("Create Username")
                    || password.equals("Password")) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
                return;
            }

            com.mycompany.apartmentsytem1.ViewingDAO dao = new com.mycompany.apartmentsytem1.ViewingDAO();
            boolean success = dao.bookRoomViewing(aptId, roomNum, name, contact, viewingEmail, username, password, date, time);

            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Viewing Scheduled!\n\nYou can log in anytime to check your schedule status.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                this.dispose();
                new LandingPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, dao.getLastBookingError(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBackground(new Color(150, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel pnlBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(btnSubmit);
        pnlBtns.add(btnCancel);
        pnlInputs.add(pnlBtns);

        panel.add(pnlInputs, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String safePopupText(String value) {
        return value != null && !value.trim().isEmpty() ? value : "N/A";
    }
}
