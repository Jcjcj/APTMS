package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class SignUp extends JFrame implements ActionListener {

    JButton submitButton, tenantSubmitButton, btnAddRoom;
    JPanel mainPanel;
    
    private File tempRoomImageFile; 
    private List<String> pendingRoomImages = new ArrayList<>(); 
    private List<String> pendingRoomNames = new ArrayList<>();
    private List<Double> pendingRents = new ArrayList<>();
    private List<Double> pendingDPs = new ArrayList<>();
    private List<Double> pendingSecs = new ArrayList<>();
    private List<String> pendingRoomDescriptions = new ArrayList<>();
    
    private File[] apartmentVisuals; 
    private File profilePicture;
    private File validIdFile;

    private DefaultListModel<String> roomListModel;
    private JList<String> roomList;
    
    // --- OWNER UI VARIABLES ---
    private JTextField txtOwnerName, txtOwnerAddress, txtOwnerContact, txtOwnerEmail, txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtAptName, txtTin, txtStreet, txtAptEmergency, txtCapital, txtFloors, txtPenaltyRate;
    private JComboBox<String> cmbBarangay;
    private JCheckBox cbCash, cbGCash, cbMaya;
    private JRadioButton eFixed, eMeter, wFixed, wMeter, iNone, iPost, iPre;
    private JTextField txtElecRate, txtWaterRate, txtNetRate;
    private JTextArea areaPolicy, areaDesc;
    private JTextField txtAptEmail, txtAptContact;
    private JTextField txtRoomNum, txtRoomFloor, txtRoomRent, txtRoomDP, txtRoomSecDep;
    private JTextArea areaRoom;
    
    // FIXED: Added variables to capture GCash and Paymaya details!
    private JTextField txtGcashName, txtGcashNo, txtMayaName, txtMayaNo;

    // --- TENANT UI VARIABLES ---
    private JTextField txtTenName, txtTenContact, txtTenEmail, txtTenAddress, txtTenEmergency;
    private JTextField txtTenAptName, txtTenMoveIn, txtTenOccupants, txtTenRoomNum, txtTenUser;
    private JPasswordField txtTenPass;
    private JCheckBox cbTenantPolicyAgreement;
    private String tenantRegistrationMode = "";
    private String pendingTenantName = "";
    private String pendingTenantContact = "";
    private String pendingTenantEmail = "";
    private String pendingTenantUsername = "";
    private String pendingTenantApartment = "";
    private String pendingTenantRoom = "";

    private static final String[] barangayList = {
        "Adlaon", "Agsungot", "Apas", "Bacayan", "Babag", "Banilad", "Basak Pardo", "Basak San Nicolas",
        "Binaliw", "Bonbon", "Budla-an", "Buhisan", "Bulacao", "Buot-Taup Pardo", "Busay", "Calamba", "Cambinocot",
        "Camputhaw", "Capitol Site", "Carreta", "Central", "Cogon Ramos", "Cogon Pardo", "Day-as", "Duljo", "Ermita",
        "Guadalupe", "Guba", "Hippodromo", "Inayawan", "Kalubihan", "Kalunasan", "Kamagayan", "Kasambagan",
        "Kinasang-an Pardo", "Labangon", "Lahug", "Lorega", "Lusaran", "Luz", "Mabini", "Mabolo", "Malubog",
        "Mambaling", "Pahina Central", "Pahina San Nicolas", "Pamutan", "Pardo", "Pari-an", "Paril", "Pasil", "Pit-os",
        "Pulangbato", "Pung-ol-Sibugay", "Punta Princesa", "Quiot Pardo", "Sambag I", "Sambag II", "San Antonio",
        "San Jose", "San Nicolas Central", "San Roque", "Santa Cruz", "Sapangdaku", "Sawang Calero", "Sinsin", "Sirao",
        "Suba Poblacion", "Sudlon I", "Sudlon II", "Tagbao", "Talamban", "Taptap", "Tejero", "Tinago", "Tisa", "To-ong Pardo",
        "T. Padilla", "Zapatera"
    };

    private final String termsText = "Terms and Conditions: Platform Service Agreement\n\n" +
            "1. The Platform Service Fee\n" +
            "The 2% Rule: The Owner agrees to pay a 2% Platform Service Fee based on the total Gross Rent Pool of their apartment.\n" +
            "Basis of Calculation: The 2% fee is applied strictly to the Base Rent of each room.\n" +
            "Exclusions: The Superadmin shall not take any percentage from utility collections (Water, Electricity), Internet fees, or Late Penalties collected from tenants.\n" +
            "Automated Calculation: All fees are calculated at the database level to ensure mathematical accuracy and transparency.\n\n" +
            "2. Registration and Approval\n" +
            "Vetting Process: The Owner must submit accurate apartment details, including valid Tax Identification Numbers (TIN) and initial capital investment, for Superadmin review.\n" +
            "Right of Refusal: The Superadmin reserves the right to Approve or Reject any apartment registration.\n" +
            "Rejection Transparency: If a registration is denied, the Superadmin must provide a specific Rejection Reason to the Owner.\n\n" +
            "3. Financial Responsibilities\n" +
            "Revenue Reporting: The system provides the Owner with Gross Profit and Net Income analytics based on backend logic.\n" +
            "Tax Compliance: The Owner is responsible for their own income tax payments unless otherwise specified.\n" +
            "System Maintenance: The 2% platform service fee covers the cost of backend security and system updates.\n\n" +
            "4. Data Integrity and Usage\n" +
            "Accuracy: The Owner is responsible for the accuracy of room prices and occupancy data entered into the system.\n" +
            "Security: Access to the Superadmin Dashboard is strictly limited to authorized personnel.\n" +
            "Transaction Logs: All payments must include a Reference Number for audit purposes.\n\n" +
            "5. Appeal of Rejection\n" +
            "Appeal Window: If an apartment registration is rejected, the Owner has exactly 7 calendar days to appeal the decision.\n" +
            "Process: The Owner must address the specific Rejection Reason provided by the Superadmin and resubmit the necessary documents or information.\n" +
            "Final Decision: After the 7-day window expires, the rejection becomes final, and the Owner must restart the entire registration process from the beginning.\n\n" +
            "Agreement Acknowledgment:\n" +
            "By clicking \"Register Apartment,\" the Owner confirms they have read these terms and agree to the 2% Platform Service Fee deduction from their rental revenue pool. This agreement is strictly governed by the backend architecture of the management system.";
    
    public SignUp(String userType) {
        this.setTitle(userType + " Registration");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(0, 70, 51));
    
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setBackground(new Color(0, 102, 51));
        headerPanel.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 120));
    
        URL logoUrl = getClass().getResource("/logowhite.png");
        JLabel logoLabel = new JLabel();
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            Image scaledLogo = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
    
        JLabel headerText = new JLabel("Apartment Management System");
        headerText.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerText.setForeground(Color.WHITE);
    
        headerPanel.add(logoLabel);
        headerPanel.add(headerText);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
    
        JPanel formPanel = userType.equals("OWNER") ? ownerRegistration() : tenantRegistration();

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(0, 70, 51));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        this.add(mainPanel);
    }

    public SignUp(String mode, String name, String contact, String email, String username, String apartmentName, String roomNumber) {
        this("TENANT");
        this.tenantRegistrationMode = mode != null ? mode : "";
        this.pendingTenantName = safePrefill(name);
        this.pendingTenantContact = safePrefill(contact);
        this.pendingTenantEmail = safePrefill(email);
        this.pendingTenantUsername = safePrefill(username);
        this.pendingTenantApartment = safePrefill(apartmentName);
        this.pendingTenantRoom = safePrefill(roomNumber);
        applyTenantPrefill();
    }

    private String safePrefill(String value) {
        return value != null ? value : "";
    }

    private double parsePercentRate(String value, double fallback) {
        try {
            String rawText = value != null ? value.trim().replace(",", "") : "";
            boolean hasPercentSign = rawText.contains("%");
            String text = rawText.replace("%", "");
            if (text.isEmpty()) return fallback;

            double parsed = Double.parseDouble(text);
            if (parsed < 0) return fallback;

            return hasPercentSign || parsed > 1.0 ? parsed / 100.0 : parsed;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void applyTenantPrefill() {
        if (txtTenName == null) return;

        txtTenName.setText(pendingTenantName);
        txtTenContact.setText(pendingTenantContact);
        txtTenEmail.setText(pendingTenantEmail);
        txtTenUser.setText(pendingTenantUsername);
        txtTenAptName.setText(pendingTenantApartment);
        txtTenRoomNum.setText(pendingTenantRoom);

        boolean finalizeMode = "TENANT_FINALIZE".equalsIgnoreCase(tenantRegistrationMode);
        boolean prefillMode = "TENANT_PREFILL".equalsIgnoreCase(tenantRegistrationMode);
        if (finalizeMode || prefillMode) {
            setTitle("Finalize Tenant Registration");
            tenantSubmitButton.setText("FINALIZE");
        }
        if (finalizeMode) {
            txtTenName.setEditable(false);
            txtTenContact.setEditable(false);
            txtTenEmail.setEditable(false);
            txtTenUser.setEditable(false);
            txtTenAptName.setEditable(false);
            txtTenRoomNum.setEditable(false);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // =========================================================
        // OWNER DATABASE INTEGRATION
        // =========================================================
        if (e.getSource() == submitButton) {
            try {
                // 1. Gather Owner Details & Validate
                String ownerName = txtOwnerName.getText().trim();
                String ownerContact = txtOwnerContact.getText().trim();
                String ownerEmail = txtOwnerEmail.getText().trim();
                String ownerAddress = txtOwnerAddress.getText().trim();
                String ownerEmergency = txtAptEmergency.getText().trim(); 
                
                String username = txtUsername.getText().trim();
                String password = new String(txtPassword.getPassword());

                if (ownerName.isEmpty() || username.isEmpty() || password.length() < 6) {
                    JOptionPane.showMessageDialog(this, "Please fill in all required fields. Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Fetch the values from the form to pre-fill the popup
                String prefillTin = txtTin.getText().trim();
                StringBuilder prefillMethod = new StringBuilder();
                if(cbCash.isSelected()) prefillMethod.append("Cash ");
                if(cbGCash.isSelected()) prefillMethod.append("GCash ");
                if(cbMaya.isSelected()) prefillMethod.append("Maya");

                // --- SHOW THE PAYMENT MODAL ---
                String[] paymentData = showRegistrationPaymentPopup(prefillTin, prefillMethod.toString().trim());
                if (paymentData == null) return; 
                
                String finalTin = paymentData[0];
                String finalMethod = paymentData[1] + " (Ref: " + paymentData[3] + ") - " + paymentData[2];

                // --- CONTINUE WITH DATABASE INSERTION ---
                String validIdPath = (validIdFile != null) ? com.mycompany.apartmentsytem1.FileStorageUtil.saveImage(validIdFile) : "no_id.png";

                String aptName = txtAptName.getText().trim();
                int floors = txtFloors.getText().trim().isEmpty() ? 1 : Integer.parseInt(txtFloors.getText().trim().replace(",", ""));
                double capital = txtCapital.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtCapital.getText().trim().replace(",", ""));
                
                if (capital <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Please enter a valid Capital amount greater than 0. This is required for ROI calculations.",
                        "Invalid Capital",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String description = areaDesc.getText().trim();
                String policy = areaPolicy.getText().trim();
                String barangay = cmbBarangay.getSelectedItem().toString();
                String street = txtStreet.getText().trim();
                
                String elecType = eFixed.isSelected() ? "Fixed" : "Meter";
                double elecRate = txtElecRate.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtElecRate.getText().trim().replace(",", ""));
                String waterType = wFixed.isSelected() ? "Fixed" : "Meter";
                double waterRate = txtWaterRate.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtWaterRate.getText().trim().replace(",", ""));
                String netType = iNone.isSelected() ? "None" : (iPost.isSelected() ? "Postpaid" : "Prepaid");
                double netRate = txtNetRate.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtNetRate.getText().trim().replace(",", ""));

                String aptContact = txtAptContact.getText().trim();
                String aptEmail = txtAptEmail.getText().trim();
                String aptEmergency = txtAptEmergency.getText().trim();
                
                String profileImgPath = (apartmentVisuals != null && apartmentVisuals.length > 0) ? com.mycompany.apartmentsytem1.FileStorageUtil.saveImage(apartmentVisuals[0]) : "default_apt.png";

                double penaltyRateVal = parsePercentRate(txtPenaltyRate.getText(), 0.05);

                // FIXED: Extract the actual GCash/Paymaya values from our new variables
                String gNo = (txtGcashNo != null) ? txtGcashNo.getText().trim() : "";
                String gName = (txtGcashName != null) ? txtGcashName.getText().trim() : "";
                String mNo = (txtMayaNo != null) ? txtMayaNo.getText().trim() : "";
                String mName = (txtMayaName != null) ? txtMayaName.getText().trim() : "";

                List<Integer> roomsPerFloor = new ArrayList<>();
                List<List<Double>> rentPrices = new ArrayList<>();
                List<List<Double>> downPayments = new ArrayList<>();
                List<List<Double>> secDeposits = new ArrayList<>();
                List<List<String>> roomImagesPerFloor = new ArrayList<>();
                roomImagesPerFloor.add(pendingRoomImages); 

                roomsPerFloor.add(roomListModel.size()); 
                
                List<List<String>> roomNamesArray = new ArrayList<>();
                roomNamesArray.add(pendingRoomNames);
                
                List<List<String>> roomDescriptionsPerFloor = new ArrayList<>();
                roomDescriptionsPerFloor.add(pendingRoomDescriptions);
                
                rentPrices.add(pendingRents);
                downPayments.add(pendingDPs);
                secDeposits.add(pendingSecs);

                com.mycompany.apartmentsytem1.OwnerDAO ownerDAO = new com.mycompany.apartmentsytem1.OwnerDAO();
                
                // FIXED: Passing the GCash and Paymaya details securely to the DAO
                int newOwnerId = ownerDAO.registerOwner(ownerName, ownerContact, ownerEmail, ownerAddress, ownerEmergency, validIdPath, gNo, gName, mNo, mName, username, password);

                if (newOwnerId != -1) {
                    com.mycompany.apartmentsytem1.ApartmentDAO aptDAO = new com.mycompany.apartmentsytem1.ApartmentDAO();
                
                    boolean isAptRegistered = aptDAO.addApartment(
                    null, aptName, finalTin, floors, 
                    roomsPerFloor, roomNamesArray, rentPrices, downPayments, secDeposits,
                    roomImagesPerFloor, roomDescriptionsPerFloor,     
                    capital, 0.12, penaltyRateVal, finalMethod, description, policy, 
                    barangay, street, 
                    elecType, elecRate, waterType, waterRate, netType, netRate, 
                    aptContact, aptEmail, aptEmergency, profileImgPath, 
                    newOwnerId
                    );

                    if (isAptRegistered) {
                        JOptionPane.showMessageDialog(this, "Registration Successful! Please wait for Super Admin approval.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        new LandingPage().setVisible(true); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Apartment Registration failed. Please check NetBeans console.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Owner Registration failed. Username might be taken.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unexpected Error: " + ex.getMessage());
            }

       } else if (e.getSource() == tenantSubmitButton) {
            
            // =========================================================
            // TENANT DATABASE INTEGRATION
            // =========================================================
            String tName = txtTenName.getText().trim();
            String tContact = txtTenContact.getText().trim();
            String tEmail = txtTenEmail.getText().trim();
            String tAddress = txtTenAddress.getText().trim();
            String tEmergency = txtTenEmergency.getText().trim();
            String tUser = txtTenUser.getText().trim();
            String tPass = new String(txtTenPass.getPassword());
            
            String tAptName = txtTenAptName.getText().trim();
            String tRoomNum = txtTenRoomNum.getText().trim();
            String tMoveIn = txtTenMoveIn.getText().trim();
            String tOccupants = txtTenOccupants.getText().trim();

            if (cbTenantPolicyAgreement == null || !cbTenantPolicyAgreement.isSelected()) {
                JOptionPane.showMessageDialog(this,
                        "Please review and agree to the apartment policy before signing up.",
                        "Apartment Policy Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String tId = (validIdFile != null) ? com.mycompany.apartmentsytem1.FileStorageUtil.saveImage(validIdFile) : "no_id.png";

            com.mycompany.apartmentsytem1.TenantDAO tenantDAO = new com.mycompany.apartmentsytem1.TenantDAO();
            boolean success = tenantDAO.registerTenant(tName, tContact, tEmail, tAddress, tEmergency, tUser, tPass, tId, tAptName, tRoomNum, tMoveIn, tOccupants);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Tenant Registration Successful! Pending Owner Approval.", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                new LandingPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Registration Failed. Please check if the Apartment Name is spelled correctly.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } else if (e.getSource() == btnAddRoom) {
            String rNum = txtRoomNum.getText().trim();
            String rFloor = txtRoomFloor.getText().trim();
            if(!rNum.isEmpty() && !rFloor.isEmpty()) {
                roomListModel.addElement("Room " + rNum + " - " + rFloor + "F");
                
                String savedRoomImagePath = (tempRoomImageFile != null) 
                        ? com.mycompany.apartmentsytem1.FileStorageUtil.saveImage(tempRoomImageFile) 
                        : "default_room.png";
                pendingRoomImages.add(savedRoomImagePath);
                tempRoomImageFile = null; 
                
                String desc = areaRoom.getText().trim();
                if (desc.isEmpty()) desc = "Standard Room";
                pendingRoomDescriptions.add(desc);

                pendingRoomNames.add("Room " + rNum);
                pendingRents.add(txtRoomRent.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtRoomRent.getText().trim().replace(",", "")));
                pendingDPs.add(txtRoomDP.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtRoomDP.getText().trim().replace(",", "")));
                pendingSecs.add(txtRoomSecDep.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtRoomSecDep.getText().trim().replace(",", "")));
                
                txtRoomNum.setText("");
                txtRoomFloor.setText("");
                txtRoomRent.setText("");
                txtRoomDP.setText("");
                txtRoomSecDep.setText("");
                areaRoom.setText("");            

            } else {
                JOptionPane.showMessageDialog(this, "Please enter both Room Number and Floor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // REGISTRATION PAYMENT POPUP
    // =========================================================================
    private String[] showRegistrationPaymentPopup(String prefillTin, String prefillMethod) {
        JDialog dialog = new JDialog(this, true); 
        dialog.setUndecorated(true); 
        dialog.getContentPane().setBackground(new Color(5, 20, 10)); 
        
        JPanel panel = new JPanel(new BorderLayout(20, 20)); 
        panel.setOpaque(false); 
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 51), 2), 
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        JLabel lblTitle = new JLabel("APARTMENT REGISTRATION PENDING", SwingConstants.CENTER); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
        lblTitle.setForeground(Color.WHITE); 
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel innerGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        innerGrid.setOpaque(false);

        // Left Side: Dummy Methods
        JPanel pnlMethods = new JPanel(); pnlMethods.setLayout(new BoxLayout(pnlMethods, BoxLayout.Y_AXIS)); pnlMethods.setOpaque(false);
        JLabel lblPayMethods = new JLabel("Payment Methods"); lblPayMethods.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblPayMethods.setForeground(Color.WHITE); pnlMethods.add(lblPayMethods);
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createPopupLabel("GCash", 16));
        pnlMethods.add(createPopupLabel("0978 563 1928", 16));
        pnlMethods.add(createPopupLabel("(Sara Duterte)", 16));
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createPopupLabel("Paymaya", 16));
        pnlMethods.add(createPopupLabel("0978 563 1928", 16));
        pnlMethods.add(createPopupLabel("(Leni Robredo)", 16));
        pnlMethods.add(Box.createVerticalStrut(30));
        
        JLabel lblNotice = new JLabel("<html>LOG IN to your account to view<br>the status of your registration.</html>");
        lblNotice.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblNotice.setForeground(Color.LIGHT_GRAY);
        pnlMethods.add(lblNotice);
        innerGrid.add(pnlMethods);

        // Right Side: Inputs
        JPanel pnlInputs = new JPanel(); pnlInputs.setLayout(new BoxLayout(pnlInputs, BoxLayout.Y_AXIS)); pnlInputs.setOpaque(false);
        
        JTextField popTin = createDarkPopupField("TIN Number");
        if (!prefillTin.isEmpty()) { popTin.setText(prefillTin); popTin.setForeground(Color.WHITE); }
        
        JComboBox<String> popMethod = createDarkPopupComboBox(new String[]{"GCash", "Paymaya"});
        if (prefillMethod.toLowerCase().contains("paymaya") || prefillMethod.toLowerCase().contains("maya")) {
            popMethod.setSelectedItem("Paymaya");
        } else {
            popMethod.setSelectedItem("GCash");
        }
        
        JTextField popDate = createDarkPopupField("Date (YYYY-MM-DD)");
        JTextField popRef = createDarkPopupField("Reference No.");
        
        pnlInputs.add(popTin); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(popMethod); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(popDate); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(popRef); pnlInputs.add(Box.createVerticalStrut(20));
        
        JButton btnSubmitPop = new JButton("SUBMIT");
        btnSubmitPop.setBackground(new Color(0, 204, 102)); btnSubmitPop.setForeground(Color.WHITE); btnSubmitPop.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmitPop.setFocusPainted(false); btnSubmitPop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmitPop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        String[] result = new String[4]; 
        
        btnSubmitPop.addActionListener(e -> {
            result[0] = popTin.getText().equals("TIN Number") ? "" : popTin.getText();
            result[1] = popMethod.getSelectedItem() != null ? popMethod.getSelectedItem().toString() : "";
            result[2] = popDate.getText().equals("Date (YYYY-MM-DD)") ? "" : popDate.getText();
            result[3] = popRef.getText().equals("Reference No.") ? "" : popRef.getText();
            dialog.dispose();
        });
        
        JButton btnCancelPop = new JButton("CANCEL");
        btnCancelPop.setBackground(new Color(150, 50, 50)); btnCancelPop.setForeground(Color.WHITE); btnCancelPop.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelPop.setFocusPainted(false); btnCancelPop.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelPop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancelPop.addActionListener(e -> dialog.dispose());

        pnlInputs.add(btnSubmitPop);
        pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(btnCancelPop);
        
        innerGrid.add(pnlInputs);
        panel.add(innerGrid, BorderLayout.CENTER);
        dialog.add(panel); 
        dialog.pack(); 
        dialog.setLocationRelativeTo(this); 
        dialog.setVisible(true); 
        
        if (result[0] == null) return null; 
        return result;
    }

    private JComboBox<String> createDarkPopupComboBox(String[] options) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setBackground(new Color(0, 35, 20));
        combo.setForeground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 35, 20)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setForeground(Color.WHITE);
                label.setBackground(isSelected ? new Color(0, 102, 51) : new Color(0, 35, 20));
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                return label;
            }
        });
        return combo;
    }
    
    private JTextField createDarkPopupField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setBackground(new Color(0, 35, 20)); 
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 35, 20)), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txt.setText(placeholder);
        txt.setHorizontalAlignment(JTextField.CENTER);
        
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txt.getText().isEmpty()) { txt.setText(placeholder); }
            }
        });
        return txt;
    }
    
    private JLabel createPopupLabel(String text, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, size));
        return l;
    }

    // =========================================================================
    // UI PANELS
    // =========================================================================
    public JPanel ownerRegistration() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        JLabel title = new JLabel("Apartment Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        container.add(title, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(1, 4, 30, 0));
        gridPanel.setOpaque(false);
        gridPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // --- COLUMN 1: Personal Information ---
        JPanel col1 = new JPanel(); col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS)); col1.setOpaque(false);
        col1.setAlignmentY(Component.TOP_ALIGNMENT);
        
        col1.add(createSectionHeader("Personal Information"));
        col1.add(createInputBlock("Name", txtOwnerName = new JTextField()));
        col1.add(createInputBlock("Address", txtOwnerAddress = new JTextField()));
        col1.add(createInputBlock("Contact Number", txtOwnerContact = new JTextField()));
        col1.add(createInputBlock("Email", txtOwnerEmail = new JTextField()));
        col1.add(createInputBlock("Account Username", txtUsername = new JTextField()));
        col1.add(createInputBlock("Account Password", txtPassword = new JPasswordField()));
        col1.add(Box.createVerticalGlue()); 
        gridPanel.add(col1);

        // --- COLUMN 2: Apartment Details ---
        JPanel col2 = new JPanel(); col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS)); col2.setOpaque(false);
        col2.setAlignmentY(Component.TOP_ALIGNMENT);
        
        col2.add(createSectionHeader("Apartment Details"));
        
        col2.add(createPairedInputBlock("Apartment Name", txtAptName = new JTextField(), "TIN No.", txtTin = new JTextField()));
        
        cmbBarangay = new JComboBox<>(barangayList);
        cmbBarangay.setBackground(Color.WHITE);
        cmbBarangay.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrowBtn = super.createArrowButton(); 
                arrowBtn.setBackground(new Color(0, 102, 51));
                arrowBtn.setForeground(Color.WHITE); 
                arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
                return arrowBtn;
            }
        });
        col2.add(createInputBlock("Barangay", cmbBarangay));
        col2.add(createInputBlock("Street", txtStreet = new JTextField()));

        JPanel pnlPay = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); pnlPay.setOpaque(false);
        cbCash = new JCheckBox("Cash"); cbCash.setOpaque(false); cbCash.setForeground(Color.WHITE);
        cbGCash = new JCheckBox("GCash"); cbGCash.setOpaque(false); cbGCash.setForeground(Color.WHITE);
        cbMaya = new JCheckBox("Maya"); cbMaya.setOpaque(false); cbMaya.setForeground(Color.WHITE);
        pnlPay.add(cbCash); pnlPay.add(cbGCash); pnlPay.add(cbMaya);
        col2.add(createInputBlock("Payment Method", pnlPay));

        JPanel dynamicPaymentContainer = new JPanel();
        dynamicPaymentContainer.setLayout(new BoxLayout(dynamicPaymentContainer, BoxLayout.Y_AXIS));
        dynamicPaymentContainer.setOpaque(false);

        // FIXED: Added variable names to the GCash/Paymaya fields so we can extract them later!
        JPanel gcashPanel = new JPanel(new GridLayout(2, 2, 5, 5)); gcashPanel.setOpaque(false);
        gcashPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "GCash Details", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));
        gcashPanel.add(createWhiteLabel("Full Name:")); gcashPanel.add(txtGcashName = new JTextField());
        gcashPanel.add(createWhiteLabel("GCash No.:")); gcashPanel.add(txtGcashNo = new JTextField());
        gcashPanel.setVisible(false); 
        gcashPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        dynamicPaymentContainer.add(gcashPanel);
        
        JPanel mayaPanel = new JPanel(new GridLayout(2, 2, 5, 5)); mayaPanel.setOpaque(false);
        mayaPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Maya Details", 0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));
        mayaPanel.add(createWhiteLabel("Full Name:")); mayaPanel.add(txtMayaName = new JTextField());
        mayaPanel.add(createWhiteLabel("Maya No.:")); mayaPanel.add(txtMayaNo = new JTextField());
        mayaPanel.setVisible(false); 
        mayaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        dynamicPaymentContainer.add(mayaPanel);

        col2.add(dynamicPaymentContainer); 

        cbGCash.addActionListener(e -> { gcashPanel.setVisible(cbGCash.isSelected()); col2.revalidate(); col2.repaint(); });
        cbMaya.addActionListener(e -> { mayaPanel.setVisible(cbMaya.isSelected()); col2.revalidate(); col2.repaint(); });

        col2.add(createPairedInputBlock("Emergency Number", txtAptEmergency = new JTextField(), "Apartment Capital", txtCapital = new JTextField()));

        JButton btnID = new JButton("+"); btnID.setBackground(Color.WHITE); btnID.setFont(new Font("Arial", Font.BOLD, 20)); btnID.setFocusable(false);
        JButton btnPhoto = new JButton("+"); btnPhoto.setBackground(Color.WHITE); btnPhoto.setFont(new Font("Arial", Font.BOLD, 20)); btnPhoto.setFocusable(false);
        
        btnID.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                validIdFile = chooser.getSelectedFile();
                btnID.setText("ID Uploaded");
                btnID.setBackground(new Color(200, 255, 200));
            }
        });

        btnPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                apartmentVisuals = new File[]{chooser.getSelectedFile()};
                btnPhoto.setText("Photo Uploaded");
                btnPhoto.setBackground(new Color(200, 255, 200));
            }
        });

        col2.add(createTripleInputBlock("Attach Valid ID", btnID, "Establishment Photo", btnPhoto, "Penalty Rate", txtPenaltyRate = new JTextField()));

        JPanel pElec = new JPanel(new GridLayout(2,1)); pElec.setOpaque(false);
        eFixed = new JRadioButton("Fixed"); eFixed.setOpaque(false); eFixed.setForeground(Color.WHITE);
        eMeter = new JRadioButton("Meter"); eMeter.setOpaque(false); eMeter.setForeground(Color.WHITE);
        ButtonGroup bgE = new ButtonGroup(); bgE.add(eFixed); bgE.add(eMeter);
        pElec.add(eFixed); pElec.add(eMeter);

        JPanel pWat = new JPanel(new GridLayout(2,1)); pWat.setOpaque(false);
        wFixed = new JRadioButton("Fixed"); wFixed.setOpaque(false); wFixed.setForeground(Color.WHITE);
        wMeter = new JRadioButton("Meter"); wMeter.setOpaque(false); wMeter.setForeground(Color.WHITE);
        ButtonGroup bgW = new ButtonGroup(); bgW.add(wFixed); bgW.add(wMeter);
        pWat.add(wFixed); pWat.add(wMeter);

        JPanel pInt = new JPanel(new GridLayout(3,1)); pInt.setOpaque(false);
        iNone = new JRadioButton("None"); iNone.setOpaque(false); iNone.setForeground(Color.WHITE);
        iPost = new JRadioButton("Postpaid"); iPost.setOpaque(false); iPost.setForeground(Color.WHITE);
        iPre = new JRadioButton("Prepaid"); iPre.setOpaque(false); iPre.setForeground(Color.WHITE);
        ButtonGroup bgI = new ButtonGroup(); bgI.add(iNone); bgI.add(iPost); bgI.add(iPre);
        pInt.add(iNone); pInt.add(iPost); pInt.add(iPre);
        iNone.setSelected(true);

        col2.add(createTripleInputBlock("Electricity", pElec, "Water", pWat, "Internet", pInt));

        JPanel utilDetailsPanel = new JPanel(new GridLayout(1, 3, 10, 0)); utilDetailsPanel.setOpaque(false);
        utilDetailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel elecDetailBox = new JPanel(new BorderLayout(0,2)); elecDetailBox.setOpaque(false);
        JLabel lblElecDetail = createWhiteLabel("Fixed Amount (₱):");
        elecDetailBox.add(lblElecDetail, BorderLayout.NORTH); elecDetailBox.add(txtElecRate = new JTextField(), BorderLayout.CENTER);
        elecDetailBox.setVisible(false); 

        JPanel watDetailBox = new JPanel(new BorderLayout(0,2)); watDetailBox.setOpaque(false);
        JLabel lblWatDetail = createWhiteLabel("Fixed Amount (₱):"); 
        watDetailBox.add(lblWatDetail, BorderLayout.NORTH); watDetailBox.add(txtWaterRate = new JTextField(), BorderLayout.CENTER);
        watDetailBox.setVisible(false); 

        JPanel intDetailBox = new JPanel(new BorderLayout(0,2)); intDetailBox.setOpaque(false);
        JLabel lblIntDetail = createWhiteLabel("Amount (₱):"); 
        intDetailBox.add(lblIntDetail, BorderLayout.NORTH); intDetailBox.add(txtNetRate = new JTextField(), BorderLayout.CENTER);
        intDetailBox.setVisible(false); 

        utilDetailsPanel.add(elecDetailBox);
        utilDetailsPanel.add(watDetailBox);
        utilDetailsPanel.add(intDetailBox); 
        col2.add(utilDetailsPanel);

        ActionListener elecAction = e -> {
            elecDetailBox.setVisible(true);
            lblElecDetail.setText(eFixed.isSelected() ? "Fixed Amount (₱):" : "Rate per kWh (₱):");
            col2.revalidate(); col2.repaint();
        };
        eFixed.addActionListener(elecAction);
        eMeter.addActionListener(elecAction);

        ActionListener watAction = e -> {
            watDetailBox.setVisible(true);
            lblWatDetail.setText(wFixed.isSelected() ? "Fixed Amount (₱):" : "Rate per m³ (₱):");
            col2.revalidate(); col2.repaint();
        };
        wFixed.addActionListener(watAction);
        wMeter.addActionListener(watAction);

        ActionListener intAction = e -> {
            if (iNone.isSelected()) {
                intDetailBox.setVisible(false);
            } else {
                intDetailBox.setVisible(true);
                lblIntDetail.setText(iPost.isSelected() ? "Monthly Amt (₱):" : "Top-up Amt (₱):");
            }
            col2.revalidate(); col2.repaint();
        };
        iNone.addActionListener(intAction);
        iPost.addActionListener(intAction);
        iPre.addActionListener(intAction);

        areaPolicy = new JTextArea(3, 20); areaPolicy.setLineWrap(true); areaPolicy.setWrapStyleWord(true);
        col2.add(createInputBlock("Apartment Policy", new JScrollPane(areaPolicy)));

        areaDesc = new JTextArea(3, 20); areaDesc.setLineWrap(true); areaDesc.setWrapStyleWord(true);
        col2.add(createInputBlock("Apartment Description", new JScrollPane(areaDesc)));

        JPanel pnlContact = new JPanel();
        pnlContact.setLayout(new BoxLayout(pnlContact, BoxLayout.Y_AXIS));
        pnlContact.setOpaque(false);
        
        pnlContact.add(createInputBlock("Email", txtAptEmail = new JTextField()));
        pnlContact.add(createInputBlock("Phone no.", txtAptContact = new JTextField()));
        
        JPanel pnlContactWrapper = new JPanel(new BorderLayout());
        pnlContactWrapper.setOpaque(false);
        JLabel lblContactHeader = new JLabel("Contact Details");
        lblContactHeader.setForeground(Color.WHITE);
        lblContactHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblContactHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        pnlContactWrapper.add(lblContactHeader, BorderLayout.NORTH);
        pnlContactWrapper.add(pnlContact, BorderLayout.CENTER);
        
        col2.add(pnlContactWrapper);

        col2.add(Box.createVerticalGlue()); 
        gridPanel.add(col2);

        // --- COLUMN 3: Room Details ---
        JPanel col3 = new JPanel(); col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS)); col3.setOpaque(false);
        col3.setAlignmentY(Component.TOP_ALIGNMENT);

        col3.add(createSectionHeader("Room Details"));

        col3.add(createInputBlock("Number of Floors", txtFloors = new JTextField()));
        
        txtRoomNum = new JTextField(); 
        txtRoomFloor = new JTextField();
        col3.add(createPairedInputBlock("Room Number", txtRoomNum, "Room Floor", txtRoomFloor));

        areaRoom = new JTextArea(3, 20);
        areaRoom.setLineWrap(true);
        areaRoom.setWrapStyleWord(true);
        col3.add(createInputBlock("Room Details", new JScrollPane(areaRoom)));

        col3.add(createInputBlock("Room Rent", txtRoomRent = new JTextField()));
        
        col3.add(createPairedInputBlock("Down Payment", txtRoomDP = new JTextField(), "Security Deposit", txtRoomSecDep = new JTextField()));

        JPanel pnlRoomAction = new JPanel(new GridLayout(1, 2, 10, 0)); pnlRoomAction.setOpaque(false);
        JButton btnRoomImg = new JButton("+"); btnRoomImg.setBackground(Color.WHITE); btnRoomImg.setFont(new Font("Arial", Font.BOLD, 20)); btnRoomImg.setFocusable(false);
        
        btnRoomImg.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tempRoomImageFile = chooser.getSelectedFile();
                btnRoomImg.setText("Uploaded");
                btnRoomImg.setBackground(new Color(200, 255, 200));
            }
        });
        
        pnlRoomAction.add(createInputBlock("Room Image", btnRoomImg));
        
        btnAddRoom = new JButton("ADD ROOM");
        btnAddRoom.setBackground(new Color(0, 153, 76));
        btnAddRoom.setForeground(Color.WHITE);
        btnAddRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddRoom.setFocusPainted(false);
        btnAddRoom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddRoom.addActionListener(this);
        
        JPanel pnlBtnAdd = new JPanel(new BorderLayout()); pnlBtnAdd.setOpaque(false);
        pnlBtnAdd.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0)); 
        pnlBtnAdd.add(btnAddRoom, BorderLayout.CENTER);
        pnlRoomAction.add(pnlBtnAdd);

        col3.add(pnlRoomAction);
        col3.add(Box.createVerticalGlue());
        gridPanel.add(col3);

        // --- COLUMN 4: Room Added & Actions ---
        JPanel col4 = new JPanel(); col4.setLayout(new BoxLayout(col4, BoxLayout.Y_AXIS)); col4.setOpaque(false);
        col4.setAlignmentY(Component.TOP_ALIGNMENT);
        
        col4.add(createSectionHeader("Room Added"));
        
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        roomList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 5, 0, new Color(0, 70, 51)), 
                    BorderFactory.createEmptyBorder(10, 10, 10, 10) 
                ));
                label.setBackground(Color.WHITE); 
                return label;
            }
        });
        roomList.setBackground(new Color(0, 70, 51));

        JScrollPane listScroll = new JScrollPane(roomList);
        listScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        listScroll.setPreferredSize(new Dimension(200, 400));
        listScroll.setBorder(null);
        col4.add(listScroll);

        col4.add(Box.createVerticalGlue()); 

        // --- TERMS AND CONDITIONS SECTION ---
        JPanel pnlTerms = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlTerms.setOpaque(false);
        pnlTerms.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JCheckBox cbTerms = new JCheckBox();
        cbTerms.setOpaque(false);
        cbTerms.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnTerms = new JButton("<html><u>TERMS AND CONDITIONS</u></html>");
        btnTerms.setForeground(Color.WHITE);
        btnTerms.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTerms.setContentAreaFilled(false);
        btnTerms.setBorderPainted(false);
        btnTerms.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnTerms.addActionListener(e -> {
            JTextArea textArea = new JTextArea(termsText);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            textArea.setMargin(new Insets(10, 10, 10, 10));

            JScrollPane scroll = new JScrollPane(textArea);
            scroll.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scroll, "Platform Service Agreement", JOptionPane.INFORMATION_MESSAGE);
            
            cbTerms.setSelected(true);
        });

        pnlTerms.add(cbTerms);
        pnlTerms.add(btnTerms);
        
        col4.add(pnlTerms);
        col4.add(Box.createVerticalStrut(15));

        submitButton = new JButton("SIGN UP");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setBackground(new Color(0, 204, 102)); 
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(120, 40));
        submitButton.setMaximumSize(new Dimension(120, 40));
        submitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.setEnabled(false); 
        submitButton.addActionListener(this); 

        cbTerms.addItemListener(e -> submitButton.setEnabled(cbTerms.isSelected()));
        
        col4.add(submitButton);
        gridPanel.add(col4);
        
        container.add(gridPanel, BorderLayout.CENTER);
        return container;
    }

    // ======================================================================================
    // HELPER METHODS FOR UI CONSTRUCTION
    // ======================================================================================

    private JLabel createSectionHeader(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
    
    private JLabel createWhiteLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JPanel createInputBlock(String labelText, JComponent inputComp) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, BorderLayout.NORTH);

        Dimension fixedSize = new Dimension(Integer.MAX_VALUE, 35);
        if (inputComp instanceof JButton) {
            if (((JButton)inputComp).getText().equals("+")) {
                fixedSize = new Dimension(Integer.MAX_VALUE, 45); 
            } else {
                 fixedSize = new Dimension(Integer.MAX_VALUE, 45); 
            }
        } else if (inputComp instanceof JScrollPane) {
            fixedSize = new Dimension(Integer.MAX_VALUE, 80); 
        } else if (inputComp instanceof JPanel) {
            fixedSize = new Dimension(Integer.MAX_VALUE, inputComp.getPreferredSize().height); 
        }
        
        inputComp.setPreferredSize(new Dimension(inputComp.getPreferredSize().width, fixedSize.height));
        inputComp.setMaximumSize(fixedSize); 

        inputComp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        if(inputComp instanceof JTextField || inputComp instanceof JPasswordField) {
            ((JComponent) inputComp).setOpaque(true);
            inputComp.setBackground(Color.WHITE);
        }

        panel.add(inputComp, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

        return panel;
    }

    private JPanel createPairedInputBlock(String l1, JComponent c1, String l2, JComponent c2) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setOpaque(false);
        panel.add(createInputBlock(l1, c1));
        panel.add(createInputBlock(l2, c2));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JPanel createTripleInputBlock(String l1, JComponent c1, String l2, JComponent c2, String l3, JComponent c3) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        panel.add(createInputBlock(l1, c1));
        panel.add(createInputBlock(l2, c2));
        panel.add(createInputBlock(l3, c3));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private void showTenantApartmentPolicyDialog() {
        String apartmentName = txtTenAptName != null ? txtTenAptName.getText().trim() : "";
        if (apartmentName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the apartment name first so the correct policy can be loaded.",
                    "Apartment Name Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String policy = getApartmentPolicy(apartmentName);
        if (policy == null) {
            JOptionPane.showMessageDialog(this,
                    "Apartment not found. Please check the apartment name before agreeing to the policy.",
                    "Apartment Not Found",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (policy.isBlank()) {
            policy = "This apartment owner has not provided additional written policies.";
        }

        JTextArea textArea = new JTextArea(policy);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(520, 360));
        JOptionPane.showMessageDialog(this, scroll, "Apartment Policy - " + apartmentName, JOptionPane.INFORMATION_MESSAGE);

        if (cbTenantPolicyAgreement != null) {
            cbTenantPolicyAgreement.setEnabled(true);
        }
    }

    private String getApartmentPolicy(String apartmentName) {
        String sql = "SELECT policy FROM apartments WHERE apartment_name = ? AND is_active = 1 LIMIT 1";

        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apartmentName);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("policy");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading apartment policy: " + ex.getMessage(),
                    "Policy Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }

    public JPanel tenantRegistration() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);
        GridBagConstraints mainGbc = new GridBagConstraints();

        mainGbc.gridx = 0; mainGbc.gridy = 0; mainGbc.insets = new Insets(20, 0, 30, 0);
        mainGbc.anchor = GridBagConstraints.CENTER;
        JLabel title = new JLabel("Tenant Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Color.WHITE);
        container.add(title, mainGbc);

        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);

        GridBagConstraints colGbc = new GridBagConstraints();
        colGbc.fill = GridBagConstraints.HORIZONTAL; colGbc.anchor = GridBagConstraints.NORTH;
        colGbc.weightx = 1.0; colGbc.weighty = 1.0; colGbc.insets = new Insets(0, 20, 0, 20); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0; gbc.gridx = 0;

        JPanel leftCol = new JPanel(new GridBagLayout()); leftCol.setOpaque(false);
        gbc.gridy = 0;
        JLabel personalInfo = new JLabel("Personal Information");
        personalInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        personalInfo.setForeground(Color.WHITE);
        gbc.insets = new Insets(0, 0, 15, 0);
        leftCol.add(personalInfo, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        leftCol.add(createWhiteLabel("Name"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenName = new JTextField(); txtTenName.setPreferredSize(new Dimension(280, 40));
        leftCol.add(txtTenName, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        leftCol.add(createWhiteLabel("Contact Number"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenContact = new JTextField(); txtTenContact.setPreferredSize(new Dimension(280, 40));
        leftCol.add(txtTenContact, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        leftCol.add(createWhiteLabel("Email"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenEmail = new JTextField(); txtTenEmail.setPreferredSize(new Dimension(280, 40));
        leftCol.add(txtTenEmail, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        leftCol.add(createWhiteLabel("Address"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenAddress = new JTextField(); txtTenAddress.setPreferredSize(new Dimension(280, 40));
        leftCol.add(txtTenAddress, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        leftCol.add(createWhiteLabel("Emergency Contact"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenEmergency = new JTextField(); txtTenEmergency.setPreferredSize(new Dimension(280, 40));
        leftCol.add(txtTenEmergency, gbc);

        colGbc.gridx = 0; bodyPanel.add(leftCol, colGbc);

        JPanel centerCol = new JPanel(new GridBagLayout()); centerCol.setOpaque(false);
        gbc.gridy = 0;
        JLabel aptDetailsLabel = new JLabel("Apartment Details");
        aptDetailsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        aptDetailsLabel.setForeground(Color.WHITE);
        gbc.insets = new Insets(0, 0, 15, 0);
        centerCol.add(aptDetailsLabel, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Apartment Name"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenAptName = new JTextField(); txtTenAptName.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenAptName, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Move-in Date (YYYY/MM/DD)"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenMoveIn = new JTextField(); txtTenMoveIn.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenMoveIn, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Number of Occupants"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenOccupants = new JTextField(); txtTenOccupants.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenOccupants, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Room Number"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenRoomNum = new JTextField(); txtTenRoomNum.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenRoomNum, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Account Username"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenUser = new JTextField(); txtTenUser.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenUser, gbc);

        gbc.gridy++; gbc.insets = new Insets(10, 0, 2, 0);
        centerCol.add(createWhiteLabel("Password"), gbc);
        gbc.gridy++; gbc.insets = new Insets(0, 0, 5, 0);
        txtTenPass = new JPasswordField(); txtTenPass.setPreferredSize(new Dimension(280, 40));
        centerCol.add(txtTenPass, gbc);

        colGbc.gridx = 1; bodyPanel.add(centerCol, colGbc);

        JPanel rightCol = new JPanel(new GridBagLayout()); rightCol.setOpaque(false);
        gbc.gridy = 0;
        JLabel lblUpload = new JLabel("Upload Valid ID");
        lblUpload.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblUpload.setForeground(Color.WHITE);
        gbc.insets = new Insets(45, 0, 5, 0); 
        rightCol.add(lblUpload, gbc);

        gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0);
        JButton btnUploadId = new JButton("Click to Upload");
        btnUploadId.setPreferredSize(new Dimension(200, 200)); 
        btnUploadId.setBackground(new Color(240, 240, 240)); 
        btnUploadId.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnUploadId.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                validIdFile = fileChooser.getSelectedFile();
                btnUploadId.setText("<html><center>ID Uploaded:<br><font color='#006633'>" + validIdFile.getName() + "</font></center></html>");
            }
        });
        rightCol.add(btnUploadId, gbc);

        gbc.gridy++; gbc.insets = new Insets(20, 0, 5, 0);
        JButton btnViewPolicy = new JButton("<html><center>View Apartment<br>Policy</center></html>");
        btnViewPolicy.setPreferredSize(new Dimension(200, 45));
        btnViewPolicy.setBackground(new Color(0, 153, 76));
        btnViewPolicy.setForeground(Color.WHITE);
        btnViewPolicy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnViewPolicy.setFocusPainted(false);
        btnViewPolicy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewPolicy.addActionListener(e -> showTenantApartmentPolicyDialog());
        rightCol.add(btnViewPolicy, gbc);

        gbc.gridy++; gbc.insets = new Insets(8, 0, 0, 0);
        cbTenantPolicyAgreement = new JCheckBox("<html>I have read and agree<br>to the apartment policy.</html>");
        cbTenantPolicyAgreement.setOpaque(false);
        cbTenantPolicyAgreement.setForeground(Color.WHITE);
        cbTenantPolicyAgreement.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTenantPolicyAgreement.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cbTenantPolicyAgreement.setEnabled(false);
        rightCol.add(cbTenantPolicyAgreement, gbc);

        colGbc.gridx = 2; bodyPanel.add(rightCol, colGbc);

        mainGbc.gridy = 1; mainGbc.fill = GridBagConstraints.HORIZONTAL; mainGbc.weightx = 1.0;
        container.add(bodyPanel, mainGbc);

        mainGbc.gridy = 2; mainGbc.fill = GridBagConstraints.NONE; mainGbc.anchor = GridBagConstraints.EAST; 
        mainGbc.insets = new Insets(40, 0, 50, 20); 

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0)); buttonPanel.setOpaque(false);
        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 16)); btnCancel.setBackground(new Color(180, 50, 50)); 
        btnCancel.setForeground(Color.WHITE); btnCancel.setPreferredSize(new Dimension(120, 45));
        btnCancel.addActionListener(e -> this.dispose()); 

        tenantSubmitButton = new JButton("SIGN UP");
        tenantSubmitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tenantSubmitButton.setBackground(new Color(0, 204, 102)); tenantSubmitButton.setForeground(Color.WHITE);
        tenantSubmitButton.setPreferredSize(new Dimension(150, 45));
        tenantSubmitButton.addActionListener(this);
        
        buttonPanel.add(btnCancel); buttonPanel.add(tenantSubmitButton);
        container.add(buttonPanel, mainGbc);

        return container;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingPage().setVisible(true));
    }
}
