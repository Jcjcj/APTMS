package main;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import com.formdev.flatlaf.FlatLightLaf; 

public class LandingPage extends JFrame {

    public LandingPage() {
        setTitle("Apartment Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Makes it full screen
        setLayout(new GridLayout(1, 2)); // 50/50 Split

        // ==========================================
        // --- LEFT PANEL (Discovery Side) ---
        // ==========================================
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BorderLayout());

        // 1. Top Section (Titles and Search)
        JPanel searchSection = new JPanel();
        searchSection.setBackground(Color.WHITE);
        searchSection.setLayout(new BoxLayout(searchSection, BoxLayout.Y_AXIS));
        searchSection.setBorder(BorderFactory.createEmptyBorder(60, 60, 20, 60));

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

        // Oval Search Bar using FlatLaf
        JTextField searchBar = new JTextField();
        searchBar.setMaximumSize(new Dimension(400, 45));
        searchBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBar.putClientProperty("JComponent.roundRect", true);
        searchBar.putClientProperty("JTextField.placeholderText", "Where in Cebu City");
        
        // Load Search Icon safely
        URL searchIconUrl = getClass().getResource("/searchicon_1.png");
        if (searchIconUrl != null) {
            ImageIcon sIcon = new ImageIcon(searchIconUrl);
            Image scaledIcon = sIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            searchBar.putClientProperty("JTextField.leadingIcon", new ImageIcon(scaledIcon));
        }

        searchBar.setBackground(new Color(0, 102, 51));
        searchBar.setForeground(Color.WHITE);
        searchBar.setCaretColor(Color.WHITE);

        // Add to search section
        searchSection.add(title1);
        searchSection.add(title2);
        searchSection.add(subtitle);
        searchSection.add(Box.createRigidArea(new Dimension(0, 30)));
        searchSection.add(searchBar);

        // 2. Bottom Section (Apartment Image)
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setVerticalAlignment(JLabel.BOTTOM);
        
        // Load Background Image safely
        URL bgUrl = getClass().getResource("/Background.png");
        if (bgUrl != null) {
            ImageIcon bgIcon = new ImageIcon(bgUrl);
            // Optional: Scale image to fit the panel better
            Image scaledBg = bgIcon.getImage().getScaledInstance(700, 450, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledBg));
        } else {
            imgLabel.setText("[Image not found in resources]");
        }
        
        // Add sections to Left Panel
        leftPanel.add(searchSection, BorderLayout.NORTH);
        leftPanel.add(imgLabel, BorderLayout.CENTER);


        // ==========================================
        // --- RIGHT PANEL (Login Side) ---
        // ==========================================
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

        // Title
        JLabel signTitle = new JLabel("Sign in to AMS");
        signTitle.setForeground(Color.WHITE);
        signTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 0, 20, 0); // Extra space below title
        formContainer.add(signTitle, formGbc);
        
        // Username Label
        JLabel lblUser = new JLabel("Username");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 1;
        formGbc.insets = new Insets(5, 0, 2, 0); // Tighter space below label
        formContainer.add(lblUser, formGbc);

        // Username Field
        JTextField txtUser = new JTextField(25);
        txtUser.setPreferredSize(new Dimension(300, 40));
        txtUser.putClientProperty("JComponent.roundRect", true);
        txtUser.putClientProperty("JTextField.placeholderText", "Enter Username");
        formGbc.gridy = 2;
        formGbc.insets = new Insets(0, 0, 15, 0);
        formContainer.add(txtUser, formGbc);

        // Password Label
        JLabel lblPass = new JLabel("Password");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGbc.gridy = 3;
        formGbc.insets = new Insets(5, 0, 2, 0);
        formContainer.add(lblPass, formGbc);

        // Password Field
        JPasswordField txtPass = new JPasswordField(25);
        txtPass.setPreferredSize(new Dimension(300, 40));
        txtPass.putClientProperty("JComponent.roundRect", true);
        txtPass.putClientProperty("JTextField.placeholderText", "Enter Password");
        txtPass.putClientProperty("JTextField.showRevealButton", true); // Adds the 'eye' icon
        formGbc.gridy = 4;
        formGbc.insets = new Insets(0, 0, 25, 0);
        formContainer.add(txtPass, formGbc);

        // Sign In Button
        JButton btnSignIn = new JButton("SIGN IN");
        btnSignIn.setPreferredSize(new Dimension(300, 40));
        btnSignIn.putClientProperty("JButton.buttonType", "roundRect");
        btnSignIn.setBackground(new Color(0, 204, 102)); // Action Green
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignIn.setFocusPainted(false);
        formGbc.gridy = 5;
        formContainer.add(btnSignIn, formGbc);

        // Add form container to the exact center of the right panel
        rightPanel.add(formContainer, gbc);

        // Add both to main frame
        add(leftPanel);
        add(rightPanel);
    }

    public static void main(String[] args) {
        // Initialize FlatLaf before starting the UI
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        
        SwingUtilities.invokeLater(() -> new LandingPage().setVisible(true));
    }
}