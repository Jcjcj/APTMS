package main;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

//import main.SignUp;

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
            //draw the triangle
        JButton arrowBtn = super.createArrowButton(); 
        
        //dark green
        arrowBtn.setBackground(new Color(0, 102, 51)); 
        
        //triangle White
        arrowBtn.setForeground(Color.WHITE); 
        
        //delete button border
        arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Added 10px right padding so it doesn't touch the edge
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
            System.out.println("Selected Barangay: " + selectedBarangay);
        });

        // Add to search section
        searchSection.add(title1);
        searchSection.add(title2);
        searchSection.add(subtitle);
        searchSection.add(dropdown);
        searchSection.add(Box.createRigidArea(new Dimension(0, 30)));

        // 2. Bottom Section (Apartment Image)
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setVerticalAlignment(JLabel.BOTTOM);
        
        // Load Background Image safely
        URL bgUrl = getClass().getResource("/main/Background.png");
        if (bgUrl != null) {
            ImageIcon bgIcon = new ImageIcon(bgUrl);
            //Scale image to fit the panel better
            Image scaledBg = bgIcon.getImage().getScaledInstance(800, 550, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledBg));
        } else {
            imgLabel.setText("[Image not found in resources]");
        }
        imgLabel.setLayout(new BorderLayout());
        
        JPanel textContainer = new JPanel();
        textContainer.setOpaque(false);
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 0));
        
        JLabel text = new JLabel("Owned an Apartment?");
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 28));      
        
        JLabel text1 = new JLabel("Register your Apartment Now!");
        text1.setForeground(Color.WHITE);
        text1.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        textContainer.add(text);
        textContainer.add(text1);
        imgLabel.add(textContainer, BorderLayout.SOUTH);
        
        searchSection.add(imgLabel);
        
        // Add sections to Left Panel
        leftPanel.add(searchSection, BorderLayout.NORTH);
        leftPanel.add(imgLabel, BorderLayout.CENTER);

        //RIGHT PANEL (Login Side)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0, 51, 26)); // Dark Green
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0); // Vertical padding between rows
        
        // Center the whole block, but allow fields to stretch horizontally
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false); // Make it transparent to show dark green
        
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 0, 5, 0);
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.anchor = GridBagConstraints.WEST; // LEFT-ALIGNS the labels
        formGbc.gridx = 0;

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        URL logoUrl = getClass().getResource("/main/logo.png");
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            Image scaledLogo = logoIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }

        // Using HTML; stack on multiple lines in a JLabel
        JLabel headerText = new JLabel("<html>Apartment<br>Management<br>System</html>");
        headerText.setFont(new Font("Segoe UI", Font.BOLD, 50));
        headerText.setForeground(Color.WHITE);

        headerPanel.add(logoLabel);
        headerPanel.add(headerText);

        formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 0, 50, 0); // Big 50px space below the entire header
        formContainer.add(headerPanel, formGbc);

        // Sign-in Title
        JLabel signTitle = new JLabel("Sign in to AMS");
        signTitle.setForeground(Color.WHITE);
        signTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formGbc.gridy = 1;
        formGbc.insets = new Insets(0, 0, 20, 0); // Extra space below title
        formContainer.add(signTitle, formGbc);
        
        // Username Label
        JLabel lblUser = new JLabel("Username");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 2;
        formGbc.insets = new Insets(5, 0, 2, 0); // Tighter space below label
        formContainer.add(lblUser, formGbc);

        // Username Field
        JTextField txtUser = new JTextField(25);
        txtUser.setPreferredSize(new Dimension(300, 40));
        formGbc.gridy = 3;
        formGbc.insets = new Insets(0, 0, 15, 0);
        txtUser.setBorder(null);
        formContainer.add(txtUser, formGbc);

        // Password Label
        JLabel lblPass = new JLabel("Password");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 4;
        formGbc.insets = new Insets(5, 0, 2, 0);
        formContainer.add(lblPass, formGbc);

        // Password Field
        JPasswordField txtPass = new JPasswordField(25);
        txtPass.setPreferredSize(new Dimension(300, 40));
        formGbc.gridy = 5;
        formGbc.insets = new Insets(0, 0, 25, 0);
        txtPass.setBorder(null);
        formContainer.add(txtPass, formGbc);

        // Sign In Button
        JButton btnSignIn = new JButton("SIGN IN");
        btnSignIn.setPreferredSize(new Dimension(300, 40));
        btnSignIn.setBackground(new Color(0, 204, 102)); // Action Green
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignIn.setFocusPainted(false);
        btnSignIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formGbc.gridy = 6;
        formContainer.add(btnSignIn, formGbc);

        //Sign-up
        JPanel signUpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        signUpPanel.setOpaque(false);

        JLabel text2 = new JLabel("No Account Yet?");
        text2.setForeground(Color.WHITE);
        text2.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnSignUp = new JButton("SIGN UP");
        btnSignUp.setContentAreaFilled(false);
        btnSignUp.setBorder(null);
        btnSignUp.setOpaque(false);
        btnSignUp.setForeground(Color.GREEN);
        btnSignUp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignUp.setFocusPainted(false);
        btnSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSignUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectionPage regForm = new SelectionPage();
                regForm.setVisible(true);
            }
        });

        //Underline effeck
        btnSignUp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                //Wrap the text in HTML underline tags when hovered
                btnSignUp.setText("<html><u>SIGN UP</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Change it back to normal plain text when the mouse leaves
                btnSignUp.setText("SIGN UP");
            }
        });

        // Add form container to the exact center of the right panel
        signUpPanel.add(text2);
        signUpPanel.add(btnSignUp);
        
        formGbc.gridy = 7;
        formGbc.insets = new Insets(30, 0, 0, 0); // Space above the sign-up section
        formContainer.add(signUpPanel, formGbc);

        rightPanel.add(formContainer, new GridBagConstraints());

        // Add both to main frame
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