package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;

class OwnerRegistration extends JFrame implements ActionListener {
    JButton submitButton;
    JPanel mainPanel;
    
    public OwnerRegistration() {
        // Setup the maximized window
        this.setTitle("Owner Registration");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create main panel with null layout for absolute positioning
        mainPanel = new JPanel(null);
        mainPanel.setBackground(new Color(0, 70, 51));
        
        // Header Layering
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, 150);

        // Create back header panel 
        JPanel headerPanelBack = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanelBack.setOpaque(true);
        headerPanelBack.setBackground(new Color(0, 102, 51));
        headerPanelBack.setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, 150);
        layeredPane.add(headerPanelBack, JLayeredPane.DEFAULT_LAYER);

        // Create front header panel
        JPanel headerPanelFront = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanelFront.setOpaque(false);
        headerPanelFront.setBounds(0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, 150);
        layeredPane.add(headerPanelFront, JLayeredPane.PALETTE_LAYER);

        // Load logo
        URL logoUrl = getClass().getResource("/main/logowhite.png");
        JLabel logoLabel = new JLabel();
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            Image scaledLogo = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }

        // Header text
        JLabel headerText = new JLabel("<html>Apartment<br>Management<br>System</html>");
        headerText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerText.setForeground(Color.WHITE);

        headerPanelFront.add(logoLabel);
        headerPanelFront.add(headerText);
        
        // Add header to mainPanel
        mainPanel.add(layeredPane);

        // Create and add registration form
        JPanel formPanel = formRegistration();
        //Set bounds for the form panel
        formPanel.setBounds(0, 150, 
                           Toolkit.getDefaultToolkit().getScreenSize().width, 
                           Toolkit.getDefaultToolkit().getScreenSize().height - 100);
        mainPanel.add(formPanel);

        this.add(mainPanel);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            JOptionPane.showMessageDialog(this, "Registration submitted!");
            // Add your registration logic here
        }
    }

   public JPanel formRegistration() {
    JPanel container = new JPanel(new GridBagLayout());
    container.setOpaque(false);
    
    GridBagConstraints gbc = new GridBagConstraints();
    
    // Title - spans all 3 columns
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.insets = new Insets(0, 30, 0, 0);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1.0;
    
    JLabel title = new JLabel("Apartment Registration");
    title.setFont(new Font("Segoe UI", Font.BOLD, 40));
    title.setForeground(Color.WHITE);
    container.add(title, gbc);
    
    // Reset
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    
    // ==================== LEFT COLUMN (Personal Information) ====================
    gbc.gridx = 0;
    
    // Personal Information Title
    gbc.gridy = 1;
    gbc.insets = new Insets(20, 30, 10, 0);
    gbc.anchor = GridBagConstraints.WEST;
    JLabel personalInfo = new JLabel("Personal Information");
    personalInfo.setFont(new Font("Segoe UI", Font.BOLD, 15));
    personalInfo.setForeground(Color.WHITE);
    container.add(personalInfo, gbc);
    
    // Name
    gbc.gridy = 2;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel nameLabel = new JLabel("Name:");
    nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    nameLabel.setForeground(Color.WHITE);
    container.add(nameLabel, gbc);
    
    gbc.gridy = 3;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField nameField = new JTextField(25);
    nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    nameField.setBackground(Color.WHITE);
    nameField.setPreferredSize(new Dimension(280, 35));
    nameField.setForeground(Color.BLACK);
    container.add(nameField, gbc);
    
    // Contact Number
    gbc.gridy = 4;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel contactLabel = new JLabel("Contact Number:");
    contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    contactLabel.setForeground(Color.WHITE);
    container.add(contactLabel, gbc);
    
    gbc.gridy = 5;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField contactField = new JTextField(25);
    contactField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    contactField.setPreferredSize(new Dimension(280, 35));
    contactField.setBackground(Color.WHITE);
    contactField.setForeground(Color.BLACK);
    container.add(contactField, gbc);
    
    // Email
    gbc.gridy = 6;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel emailLabel = new JLabel("Email:");
    emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    emailLabel.setForeground(Color.WHITE);
    container.add(emailLabel, gbc);
    
    gbc.gridy = 7;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField emailField = new JTextField(25);
    emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    emailField.setPreferredSize(new Dimension(280, 35));
    emailField.setBackground(Color.WHITE);
    emailField.setForeground(Color.BLACK);
    container.add(emailField, gbc);
    
    // Address
    gbc.gridy = 8;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel addressLabel = new JLabel("Address:");
    addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    addressLabel.setForeground(Color.WHITE);
    container.add(addressLabel, gbc);
    
    gbc.gridy = 9;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField addressField = new JTextField(25);
    addressField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    addressField.setPreferredSize(new Dimension(280, 35));
    addressField.setBackground(Color.WHITE);
    addressField.setForeground(Color.BLACK);
    container.add(addressField, gbc);
    
    // Account Username
    gbc.gridy = 10;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel usernameLabel = new JLabel("Account Username:");
    usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    usernameLabel.setForeground(Color.WHITE);
    container.add(usernameLabel, gbc);
    
    gbc.gridy = 11;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField usernameField = new JTextField(25);
    usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    usernameField.setPreferredSize(new Dimension(280, 35));
    usernameField.setBackground(Color.WHITE);
    usernameField.setForeground(Color.BLACK);
    container.add(usernameField, gbc);
    
    // Password
    gbc.gridy = 12;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    passwordLabel.setForeground(Color.WHITE);
    container.add(passwordLabel, gbc);
    
    gbc.gridy = 13;
    gbc.insets = new Insets(2, 30, 20, 0);
    JPasswordField passwordField = new JPasswordField(25);
    passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    passwordField.setPreferredSize(new Dimension(280, 35));
    passwordField.setBackground(Color.WHITE);
    passwordField.setForeground(Color.BLACK);
    container.add(passwordField, gbc);
    
    // ==================== CENTER COLUMN (Apartment Details) ====================
    gbc.gridx = 1;
    
    // Apartment Details Title
    gbc.gridy = 1;
    gbc.insets = new Insets(20, 30, 10, 0);
    JLabel aptDetailsLabel = new JLabel("Apartment Details");
    aptDetailsLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
    aptDetailsLabel.setForeground(Color.WHITE);
    container.add(aptDetailsLabel, gbc);
    
    // Apartment Name
    gbc.gridy = 2;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel aptNameLabel = new JLabel("Apartment Name:");
    aptNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    aptNameLabel.setForeground(Color.WHITE);
    container.add(aptNameLabel, gbc);
    
    gbc.gridy = 3;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField aptNameField = new JTextField(25);
    aptNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    aptNameField.setPreferredSize(new Dimension(250, 35));
    aptNameField.setBackground(Color.WHITE);
    aptNameField.setForeground(Color.BLACK);
    container.add(aptNameField, gbc);
    
    // TIN Number (same row as Apartment Name - in center column)
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.insets = new Insets(10, 200, 0, 0);  // Position to the right of Apartment Name
    JLabel tinLabel = new JLabel("TIN No.:");
    tinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    tinLabel.setForeground(Color.WHITE);
    container.add(tinLabel, gbc);
    
    gbc.gridy = 3;
    gbc.insets = new Insets(2, 200, 10, 0);
    JTextField tinField = new JTextField(12);
    tinField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    tinField.setPreferredSize(new Dimension(150, 35));
    tinField.setBackground(Color.WHITE);
    tinField.setForeground(Color.BLACK);
    container.add(tinField, gbc);
    
    // Reset insets for remaining fields
    gbc.insets = new Insets(10, 30, 0, 0);
    
    // Rooms Available
    gbc.gridy = 4;
    JLabel roomCountLabel = new JLabel("Rooms Available:");
    roomCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    roomCountLabel.setForeground(Color.WHITE);
    container.add(roomCountLabel, gbc);
    
    gbc.gridy = 5;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField roomCountField = new JTextField(25);
    roomCountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    roomCountField.setPreferredSize(new Dimension(250, 35));
    roomCountField.setBackground(Color.WHITE);
    roomCountField.setForeground(Color.BLACK);
    container.add(roomCountField, gbc);
    
    // Rent per Room
    gbc.gridy = 6;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel rentLabel = new JLabel("Rent per Room:");
    rentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    rentLabel.setForeground(Color.WHITE);
    container.add(rentLabel, gbc);
    
    gbc.gridy = 7;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField rentField = new JTextField(25);
    rentField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    rentField.setPreferredSize(new Dimension(250, 35));
    rentField.setBackground(Color.WHITE);
    rentField.setForeground(Color.BLACK);
    container.add(rentField, gbc);
    
    // Down Payment (same row as Rent)
    gbc.gridy = 6;
    gbc.insets = new Insets(10, 200, 0, 0);
    JLabel downPaymentLabel = new JLabel("Down Payment:");
    downPaymentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    downPaymentLabel.setForeground(Color.WHITE);
    container.add(downPaymentLabel, gbc);
    
    gbc.gridy = 7;
    gbc.insets = new Insets(2, 200, 10, 0);
    JTextField downPaymentField = new JTextField(12);
    downPaymentField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    downPaymentField.setPreferredSize(new Dimension(150, 35));
    downPaymentField.setBackground(Color.WHITE);
    downPaymentField.setForeground(Color.BLACK);
    container.add(downPaymentField, gbc);
    
    // Reset insets
    gbc.insets = new Insets(10, 30, 0, 0);
    
    // Payment Method
    gbc.gridy = 8;
    JLabel paymentMethodLabel = new JLabel("Payment Method:");
    paymentMethodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    paymentMethodLabel.setForeground(Color.WHITE);
    container.add(paymentMethodLabel, gbc);
    
    gbc.gridy = 9;
    gbc.insets = new Insets(2, 30, 10, 0);
    JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    paymentPanel.setOpaque(false);
    
    JCheckBox cashCheck = new JCheckBox("Cash");
    JCheckBox bankTransferCheck = new JCheckBox("Bank Transfer");
    JCheckBox gcashCheck = new JCheckBox("GCash");
    JCheckBox mayaCheck = new JCheckBox("Maya");
    
    JCheckBox[] checks = {cashCheck, bankTransferCheck, gcashCheck, mayaCheck};
    for (JCheckBox chk : checks) {
        chk.setForeground(Color.WHITE);
        chk.setOpaque(false);
        paymentPanel.add(chk);
    }
    container.add(paymentPanel, gbc);
    
    // Apartment/Room Description
    gbc.gridy = 10;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel descriptionLabel = new JLabel("Apartment/Room Description:");
    descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    descriptionLabel.setForeground(Color.WHITE);
    container.add(descriptionLabel, gbc);
    
    gbc.gridy = 11;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextArea descriptionArea = new JTextArea(3, 25);
    descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBackground(Color.WHITE);
    descriptionArea.setForeground(Color.BLACK);
    JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
    descriptionScroll.setPreferredSize(new Dimension(400, 70));
    container.add(descriptionScroll, gbc);
    
    // Policy Statement
    gbc.gridy = 12;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel policyLabel = new JLabel("Policy Statement:");
    policyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    policyLabel.setForeground(Color.WHITE);
    container.add(policyLabel, gbc);
    
    gbc.gridy = 13;
    gbc.insets = new Insets(2, 30, 20, 0);
    JTextArea policyArea = new JTextArea(3, 25);
    policyArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    policyArea.setLineWrap(true);
    policyArea.setWrapStyleWord(true);
    policyArea.setBackground(Color.WHITE);
    policyArea.setForeground(Color.BLACK);
    JScrollPane policyScroll = new JScrollPane(policyArea);
    policyScroll.setPreferredSize(new Dimension(400, 70));
    container.add(policyScroll, gbc);
    
    // ==================== RIGHT COLUMN (Apartment Address) ====================
    gbc.gridx = 2;
    gbc.insets = new Insets(10, 30, 0, 0);
    
    // Apartment Address Title
    gbc.gridy = 1;
    gbc.insets = new Insets(20, 30, 10, 0);
    JLabel aptAddressLabel = new JLabel("Apartment Address");
    aptAddressLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
    aptAddressLabel.setForeground(Color.WHITE);
    container.add(aptAddressLabel, gbc);
    
    // Barangay (First component in right column)
    gbc.gridy = 2;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel barangayLabel = new JLabel("Barangay:");
    barangayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    barangayLabel.setForeground(Color.WHITE);
    container.add(barangayLabel, gbc);
    
    gbc.gridy = 3;
    gbc.insets = new Insets(2, 30, 10, 0);
    JComboBox<String> barangayDropdown = new JComboBox<>(barangayList);
    barangayDropdown.setUI(new BasicComboBoxUI() {
        @Override
        protected JButton createArrowButton() {
            JButton arrowBtn = super.createArrowButton(); 
            arrowBtn.setBackground(new Color(0, 102, 51)); 
            arrowBtn.setForeground(Color.WHITE); 
            arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            arrowBtn.setFocusPainted(false);
            arrowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return arrowBtn;
        }
    });
    barangayDropdown.setPreferredSize(new Dimension(250, 35));
    barangayDropdown.setForeground(Color.BLACK);
    barangayDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    barangayDropdown.setBackground(Color.WHITE);
    container.add(barangayDropdown, gbc);
    
    // Street
    gbc.gridy = 4;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel streetLabel = new JLabel("Street:");
    streetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    streetLabel.setForeground(Color.WHITE);
    container.add(streetLabel, gbc);
    
    gbc.gridy = 5;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField streetField = new JTextField(25);
    streetField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    streetField.setPreferredSize(new Dimension(280, 35));
    streetField.setBackground(Color.WHITE);
    streetField.setForeground(Color.BLACK);
    container.add(streetField, gbc);
    
    // Electricity
    gbc.gridy = 6;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel electricityLabel = new JLabel("Electricity:");
    electricityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    electricityLabel.setForeground(Color.WHITE);
    container.add(electricityLabel, gbc);
    
    gbc.gridy = 7;
    gbc.insets = new Insets(2, 30, 10, 0);
    JPanel electricityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    electricityPanel.setOpaque(false);
    JRadioButton electricFixed = new JRadioButton("Fixed");
    JRadioButton electricMeter = new JRadioButton("Meter");
    ButtonGroup electricGroup = new ButtonGroup();
    electricGroup.add(electricFixed);
    electricGroup.add(electricMeter);
    electricFixed.setSelected(true);
    electricFixed.setForeground(Color.WHITE);
    electricMeter.setForeground(Color.WHITE);
    electricityPanel.add(electricFixed);
    electricityPanel.add(electricMeter);
    container.add(electricityPanel, gbc);
    
    // Water
    gbc.gridy = 8;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel waterLabel = new JLabel("Water:");
    waterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    waterLabel.setForeground(Color.WHITE);
    container.add(waterLabel, gbc);
    
    gbc.gridy = 9;
    gbc.insets = new Insets(2, 30, 10, 0);
    JPanel waterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    waterPanel.setOpaque(false);
    JRadioButton waterFixed = new JRadioButton("Fixed");
    JRadioButton waterMeter = new JRadioButton("Meter");
    ButtonGroup waterGroup = new ButtonGroup();
    waterGroup.add(waterFixed);
    waterGroup.add(waterMeter);
    waterFixed.setSelected(true);
    waterFixed.setForeground(Color.WHITE);
    waterMeter.setForeground(Color.WHITE);
    waterPanel.add(waterFixed);
    waterPanel.add(waterMeter);
    container.add(waterPanel, gbc);
    
    // Contact Details
    gbc.gridy = 10;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel contactDetailsLabel = new JLabel("Contact Details:");
    contactDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    contactDetailsLabel.setForeground(Color.WHITE);
    container.add(contactDetailsLabel, gbc);
    
    gbc.gridy = 11;
    gbc.insets = new Insets(2, 30, 10, 0);
    JTextField contactDetailsField = new JTextField(25);
    contactDetailsField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    contactDetailsField.setPreferredSize(new Dimension(280, 35));
    contactDetailsField.setBackground(Color.WHITE);
    contactDetailsField.setForeground(Color.BLACK);
    contactDetailsField.setToolTipText("Contact No. / Email Ad / SocMeds");
    container.add(contactDetailsField, gbc);
    
    // Upload Apartment Visuals
    gbc.gridy = 12;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel uploadVisualsLabel = new JLabel("Upload Apartment Visuals:");
    uploadVisualsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    uploadVisualsLabel.setForeground(Color.WHITE);
    container.add(uploadVisualsLabel, gbc);
    
    gbc.gridy = 13;
    gbc.insets = new Insets(2, 30, 10, 0);
    JButton uploadVisualsBtn = new JButton("Choose Files");
    uploadVisualsBtn.setBackground(new Color(0, 102, 51));
    uploadVisualsBtn.setForeground(Color.WHITE);
    uploadVisualsBtn.setFocusPainted(false);
    uploadVisualsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    container.add(uploadVisualsBtn, gbc);
    
    // Upload Profile Picture
    gbc.gridy = 14;
    gbc.insets = new Insets(10, 30, 0, 0);
    JLabel uploadProfileLabel = new JLabel("Upload Profile Picture:");
    uploadProfileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    uploadProfileLabel.setForeground(Color.WHITE);
    container.add(uploadProfileLabel, gbc);
    
    gbc.gridy = 15;
    gbc.insets = new Insets(2, 30, 20, 0);
    JButton uploadProfileBtn = new JButton("Choose Image");
    uploadProfileBtn.setBackground(new Color(0, 102, 51));
    uploadProfileBtn.setForeground(Color.WHITE);
    uploadProfileBtn.setFocusPainted(false);
    uploadProfileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    container.add(uploadProfileBtn, gbc);
    
    // ==================== SIGN UP BUTTON (spans all 3 columns) ====================
    gbc.gridx = 0;
    gbc.gridy = 16;
    gbc.gridwidth = 3;
    gbc.insets = new Insets(30, 30, 50, 30);
    gbc.anchor = GridBagConstraints.CENTER;
    
    submitButton = new JButton("SIGN UP");
    submitButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
    submitButton.setBackground(new Color(0, 153, 76));
    submitButton.setForeground(Color.WHITE);
    submitButton.setFocusPainted(false);
    submitButton.setPreferredSize(new Dimension(200, 50));
    submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    submitButton.addActionListener(this);
    container.add(submitButton, gbc);
    
    // Add spacer at bottom
    gbc.gridy = 17;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.VERTICAL;
    gbc.insets = new Insets(0, 0, 0, 0);
    JPanel spacer = new JPanel();
    spacer.setOpaque(false);
    container.add(spacer, gbc);
    
    return container;
}

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
}