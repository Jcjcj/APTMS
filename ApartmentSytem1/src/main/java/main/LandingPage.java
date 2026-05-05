package main;

// --- NEW: Backend & Dashboard Imports ---
import com.mycompany.apartmentsytem1.LoginDAO;
import com.mycompany.apartmentsytem1.OwnerDAO;
import com.mycompany.apartmentsytem1.SuperAdminDAO;
import Dashboard.OwnerDashboard;
import Dashboard.SuperAdminDashboard;
import Dashboard.TenantDashboard;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class LandingPage extends JFrame {

    public LandingPage() {
        setTitle("Apartment Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Makes it full screen
        setLayout(new GridLayout(1, 2)); // 50/50 Split

        // --- LEFT PANEL (Discovery Side) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BorderLayout());

        // 1. Top Section (Titles and Search)
        JPanel searchSection = new JPanel();
        searchSection.setBackground(Color.WHITE);
        searchSection.setLayout(new BoxLayout(searchSection, BoxLayout.Y_AXIS));
        searchSection.setBorder(BorderFactory.createEmptyBorder(60, 40, 20, 60));

        JLabel title1 = new JLabel("Finding");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 55));
        title1.setForeground(new Color(0, 102, 51));
        title1.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel title2 = new JLabel("Apartment");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 55));
        title2.setForeground(new Color(0, 102, 51));
        title2.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitle = new JLabel("in Cebu City?");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        subtitle.setForeground(new Color(0, 153, 76));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        
        JComboBox<String> dropdown = new JComboBox<>(barangayList);   
        dropdown.setUI(new BasicComboBoxUI() {
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

        dropdown.setMaximumSize(new Dimension(350, 60));
        dropdown.setPreferredSize(new Dimension(350, 60));
        dropdown.setForeground(Color.WHITE);
        dropdown.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        dropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        dropdown.setFocusable(false);
        dropdown.setBackground(new Color(0, 102, 51));
        dropdown.setBorder(BorderFactory.createEmptyBorder(0, 15, 0 , 0));
        dropdown.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dropdown.addActionListener(e -> {
            String selectedBarangay = (String) dropdown.getSelectedItem();
            this.setVisible(false);                  
            SearchWindow resultsWindow = new SearchWindow(selectedBarangay);
            resultsWindow.setExtendedState(JFrame.MAXIMIZED_BOTH); 
            resultsWindow.setVisible(true);
            
            resultsWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    LandingPage.this.setVisible(true); 
                }
            });
        });

        searchSection.add(title1);
        searchSection.add(title2);
        searchSection.add(subtitle);
        searchSection.add(dropdown);
        searchSection.add(Box.createRigidArea(new Dimension(0, 30)));

        // 2. Bottom Section (Apartment Image)
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setVerticalAlignment(JLabel.BOTTOM);
        
        // Inside LandingPage constructor
        URL bgUrl = getClass().getResource("/Background.png");
        if (bgUrl != null) {
            ImageIcon bgIcon = new ImageIcon(bgUrl);
            // Matches the scaled instance in your provided source[cite: 25, 26]
            Image scaledBg = bgIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledBg));
        } else {
            imgLabel.setText("MISSING: /Background.png");
            imgLabel.setForeground(Color.RED);
        }
        imgLabel.setLayout(new BorderLayout());
        
        JPanel textContainer = new JPanel();
        textContainer.setOpaque(false);
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        
        JLabel text = new JLabel("Owned an Apartment?");
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel btnRegisterApt = new JLabel("Register your Apartment Now!");
        btnRegisterApt.setForeground(Color.WHITE);
        btnRegisterApt.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnRegisterApt.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegisterApt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btnRegisterApt.setForeground(new Color(0, 204, 102)); 
                btnRegisterApt.setText("<html><u>Register your Apartment Now!</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                btnRegisterApt.setForeground(Color.WHITE);
                btnRegisterApt.setText("Register your Apartment Now!");
            }
            @Override
            public void mouseClicked(MouseEvent evt) {
                new SignUp("OWNER").setVisible(true); 
            }
        });
        
        textContainer.add(text);
        textContainer.add(btnRegisterApt);
        imgLabel.add(textContainer, BorderLayout.SOUTH);
        
        searchSection.add(imgLabel);
        leftPanel.add(searchSection, BorderLayout.NORTH);
        leftPanel.add(imgLabel, BorderLayout.CENTER);


        // --- RIGHT PANEL (Login Side) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0, 51, 26)); 
        rightPanel.setLayout(new GridBagLayout());
        
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);          
        
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 0, 5, 0);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.anchor = GridBagConstraints.WEST; 
        formGbc.gridx = 0;

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setOpaque(false);
        JLabel logoLabel = new JLabel();
       // Inside LandingPage constructor
        URL logoUrl = getClass().getResource("/logo.png");
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            // Scale specifically for the login container
            Image scaledLogo = logoIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        } else {
            logoLabel.setText("MISSING: /logo.png");
            logoLabel.setForeground(Color.RED);
        }

        JLabel headerText = new JLabel("<html>Apartment<br>Management<br>System</html>");
        headerText.setFont(new Font("Segoe UI", Font.BOLD, 50));
        headerText.setForeground(Color.WHITE);
        headerPanel.add(logoLabel);
        headerPanel.add(headerText);

        formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 0, 50, 0); 
        formContainer.add(headerPanel, formGbc);

        JLabel signTitle = new JLabel("Sign in to AMS");
        signTitle.setForeground(Color.WHITE);
        signTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formGbc.gridy = 1;
        formGbc.insets = new Insets(0, 0, 20, 0); 
        formContainer.add(signTitle, formGbc);
        
        JLabel lblUser = new JLabel("Username");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 2;
        formGbc.insets = new Insets(5, 0, 2, 0); 
        formContainer.add(lblUser, formGbc);

        JTextField txtUser = new JTextField(25);
        txtUser.setPreferredSize(new Dimension(300, 40));
        formGbc.gridy = 3;
        formGbc.insets = new Insets(0, 0, 15, 0);
        txtUser.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        formContainer.add(txtUser, formGbc);

        JLabel lblPass = new JLabel("Password");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 4;
        formGbc.insets = new Insets(5, 0, 2, 0);
        formContainer.add(lblPass, formGbc);

        JPasswordField txtPass = new JPasswordField(25);
        txtPass.setPreferredSize(new Dimension(300, 40));
        formGbc.gridy = 5;
        formGbc.insets = new Insets(0, 0, 25, 0);
        txtPass.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        formContainer.add(txtPass, formGbc);

        // =========================================================
        // --- NEW: DATABASE-READY CASCADING LOGIN LOGIC ---
        // =========================================================
        JButton btnSignIn = new JButton("SIGN IN");
        btnSignIn.setPreferredSize(new Dimension(300, 40));
        btnSignIn.setBackground(new Color(0, 204, 102)); 
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignIn.setFocusPainted(false);
        btnSignIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnSignIn.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // CHECK 1: Super Admin
            SuperAdminDAO adminDAO = new SuperAdminDAO();
            if (adminDAO.login(user, pass)) {
                this.dispose();
                new SuperAdminDashboard().setVisible(true); 
                return;
            }

            // CHECK 2: Owner
            OwnerDAO ownerDAO = new OwnerDAO();
            int ownerId = ownerDAO.authenticateOwner(user, pass);
            if (ownerId != -1) {
                this.dispose();
                new Dashboard.OwnerDashboard(ownerId).setVisible(true); 
                return;
            }

            // CHECK 3: Tenant
            LoginDAO loginDAO = new LoginDAO();
            String tenantStatus = loginDAO.loginTenant(user, pass);
            
            if (tenantStatus.startsWith("SUCCESS:")) {
                int tenantId = Integer.parseInt(tenantStatus.split(":")[1]);
                this.dispose();
                new Dashboard.TenantDashboard(tenantId).setVisible(true); 
                return;
            } else if (!"Username not found.".equals(tenantStatus) && !"Invalid password.".equals(tenantStatus)) {
                JOptionPane.showMessageDialog(this, tenantStatus, "Registration Status", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // =========================================================
            // CHECK 4: ROOM VIEWING TEMPORARY ACCOUNT
            // =========================================================
            com.mycompany.apartmentsytem1.ViewingDAO viewingDAO = new com.mycompany.apartmentsytem1.ViewingDAO();
            String[] viewingData = viewingDAO.getTemporaryUserDashboard(user, pass);
            
            if (viewingData != null) {
                this.dispose();
                new StatusDashboard(viewingData).setVisible(true); // Routes to the dynamic Status Dashboard!
                return;
            }

            // If ALL checks fail:
            JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        });
        
        formGbc.gridy = 6;
        formContainer.add(btnSignIn, formGbc);

        rightPanel.add(formContainer, new GridBagConstraints());

        add(leftPanel);
        add(rightPanel);
        this.setResizable(false);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingPage().setVisible(true));
    }
}