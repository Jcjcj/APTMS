package Dashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class SuperAdminDashboard extends JFrame implements ActionListener {

    // Theme Colors
    private final Color COLOR_SIDEBAR = new Color(0, 35, 20);      // Darkest green (Sidebar)
    private final Color COLOR_MAIN_BG = new Color(0, 51, 26);      // Main background behind cards
    private final Color COLOR_CONTAINER = new Color(0, 102, 51);   // Lighter green container box
    private final Color COLOR_LIST_ITEM = new Color(5, 20, 10);    // Very dark item blocks
    private final Color COLOR_BTN_ACTIVE = new Color(0, 102, 51);  // Highlighted menu item
    private final Color COLOR_TEXT = Color.WHITE;

    // Layout variables
    private CardLayout cardLayout;
    private JPanel cardsContainer;
    
    // Sidebar Buttons
    private JButton btnDashboard, btnOwners, btnInquiries, btnBilling, btnNotification;
    private JButton[] navButtons;

    public SuperAdminDashboard() {
        // 1. Setup Main Frame
        this.setTitle("Super Admin Dashboard - Apartment Management System");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        // 2. Create the Sidebar (Left)
        JPanel sidebar = createSidebar();
        this.add(sidebar, BorderLayout.WEST);

        // 3. Create the Main Content Area (Right) with CardLayout
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setBackground(COLOR_MAIN_BG);
        cardsContainer.setBorder(new EmptyBorder(30, 40, 40, 40));

        // Add the 5 different views to the CardLayout container
        cardsContainer.add(createDashboardCard(), "Dashboard");
        cardsContainer.add(createOwnersCard(), "Owners");
        cardsContainer.add(createInquiriesCard(), "Inquiries");
        cardsContainer.add(createBillingCard(), "Billing");
        cardsContainer.add(createNotificationCard(), "Notification");

        this.add(cardsContainer, BorderLayout.CENTER);

        // Start on the Dashboard Overview tab
        activateButton(btnDashboard);
    }

    // =========================================================================
    // SIDEBAR CONSTRUCTION
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(COLOR_SIDEBAR);

        // -- Logo Area --
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setBackground(COLOR_SIDEBAR);
        
        URL logoUrl = getClass().getResource("/logowhite.png");
        JLabel logoLabel = new JLabel();
        logoLabel.setForeground(COLOR_TEXT);
        if (logoUrl != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(logoUrl).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            logoLabel.setIcon(icon);
            logoLabel.setText("<html>Apartment<br>Management<br>System</html>");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        } else {
            logoLabel.setText("System Logo");
        }
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // -- Navigation Buttons --
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(COLOR_SIDEBAR);

        btnDashboard = createNavButton("Super Admin Dashboard");
        btnOwners = createNavButton("Apartment Owners");
        btnInquiries = createNavButton("Inquiries");
        btnBilling = createNavButton("Billing");
        btnNotification = createNavButton("Notification");

        navButtons = new JButton[]{btnDashboard, btnOwners, btnInquiries, btnBilling, btnNotification};

        navPanel.add(btnDashboard);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(btnOwners);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(btnInquiries);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(btnBilling);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(btnNotification);

        sidebar.add(navPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(COLOR_TEXT);
        btn.setBackground(COLOR_SIDEBAR);
        btn.setBorder(new EmptyBorder(15, 25, 15, 25));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        return btn;
    }

    // =========================================================================
    // ACTION LISTENER (Handles Tab Switching)
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        // Reset all buttons to dark background
        for (JButton btn : navButtons) {
            btn.setBackground(COLOR_SIDEBAR);
        }

        // Highlight the clicked button and switch the card
        if (source == btnDashboard) {
            activateButton(btnDashboard);
            cardLayout.show(cardsContainer, "Dashboard");
        } else if (source == btnOwners) {
            activateButton(btnOwners);
            cardLayout.show(cardsContainer, "Owners");
        } else if (source == btnInquiries) {
            activateButton(btnInquiries);
            cardLayout.show(cardsContainer, "Inquiries");
        } else if (source == btnBilling) {
            activateButton(btnBilling);
            cardLayout.show(cardsContainer, "Billing");
        } else if (source == btnNotification) {
            activateButton(btnNotification);
            cardLayout.show(cardsContainer, "Notification");
        }
    }

    private void activateButton(JButton btn) {
        btn.setBackground(COLOR_BTN_ACTIVE);
    }

    // =========================================================================
    // INDIVIDUAL TAB VIEWS (The Cards)
    // =========================================================================

    // 1. Super Admin Dashboard (Overview)
    private JPanel createDashboardCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Super Admin Dashboard"), BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 30, 0));
        columns.setOpaque(false);

        // Left Column
        JPanel col1 = new JPanel(new BorderLayout()); col1.setOpaque(false);
        col1.add(createSubHeader("On Due Date Warning"), BorderLayout.NORTH);
        JPanel list1 = createContainerBox();
        list1.add(createListItem("First Residence", "Juan Dela Cruz", "0987-235-6738", null));
        col1.add(list1, BorderLayout.CENTER);
        columns.add(col1);

        // Right Column
        JPanel col2 = new JPanel(new BorderLayout()); col2.setOpaque(false);
        col2.add(createSubHeader("Active Owners"), BorderLayout.NORTH);
        JPanel list2 = createContainerBox();
        list2.add(createListItem("YES! Apartment", "Caroline San Pedro", "0967-345-2399", null));
        col2.add(list2, BorderLayout.CENTER);
        columns.add(col2);

        card.add(columns, BorderLayout.CENTER);
        return card;
    }

    // 2. Apartment Owners
    private JPanel createOwnersCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Apartment Owners"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Owners"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        
        list.add(createListItem("YES! Apartment", "Caroline San Pedro", "10 Tenants", createPublishActionButtons()));
        list.add(createListItem("First Residence", "Juan Dela Cruz", "5 Tenants", createPublishActionButtons()));
        list.add(createListItem("ONE Apartment", "Olivia Rodrigo", "4 Tenants", createPublishActionButtons(true))); // Green Publish

        mainContent.add(list, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 3. Inquiries
    private JPanel createInquiriesCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Inquiries"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Listing"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        
        list.add(createListItem("Golden Peak Apartment", "Daniel Padilla", "0967-345-2399", createApproveRejectButtons()));
        list.add(createEmptySlot());
        list.add(createEmptySlot());
        list.add(createEmptySlot());

        mainContent.add(list, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 4. Billing
    private JPanel createBillingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Billing"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Owners Status"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        
        list.add(createListItem("YES! Apartment", "Caroline San Pedro", "0967-345-2399", createStatusLabel("Monthly")));
        list.add(createListItem("First Residence", "Juan Dela Cruz", "0987-235-6738", createStatusLabel("Monthly")));
        list.add(createEmptySlot());
        list.add(createEmptySlot());

        mainContent.add(list, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 5. Notification
    private JPanel createNotificationCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Notification"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Listing"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        
        list.add(createListItem("YES! Apartment", "Caroline San Pedro", "0967-345-2399", createStatusLabel("Due June 1, 2026")));
        list.add(createListItem("First Residence", "Juan Dela Cruz", "0987-235-6738", createWarningLabel("⚠ Due May 16, 2026")));
        list.add(createEmptySlot());
        list.add(createEmptySlot());

        mainContent.add(list, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // UI HELPER METHODS (For Layout & Design Consistency)
    // =========================================================================

    private JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblTitle.setForeground(COLOR_TEXT);

        JLabel lblAdmin = new JLabel("Super Admin");
        lblAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lblAdmin.setForeground(COLOR_TEXT);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblAdmin, BorderLayout.EAST);
        return header;
    }

    private JLabel createSubHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(COLOR_TEXT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return lbl;
    }

    private JPanel createContainerBox() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_CONTAINER);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Wrap in another panel that aligns to NORTH so it doesn't stretch items vertically
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(container, BorderLayout.NORTH);
        return container; // return the direct container so we can add to it
    }

    // Core method for making the dark list rows
    private JPanel createListItem(String line1, String line2, String line3, JComponent rightAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIST_ITEM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel l1 = new JLabel(line1);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l1.setForeground(Color.WHITE);
        leftText.add(l1);

        if (line2 != null) {
            leftText.add(Box.createVerticalStrut(5));
            JLabel l2 = new JLabel(line2);
            l2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l2.setForeground(Color.LIGHT_GRAY);
            leftText.add(l2);
        }

        if (line3 != null) {
            leftText.add(Box.createVerticalStrut(2));
            JLabel l3 = new JLabel(line3);
            l3.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l3.setForeground(Color.LIGHT_GRAY);
            leftText.add(l3);
        }

        panel.add(leftText, BorderLayout.CENTER);

        if (rightAction != null) {
            panel.add(rightAction, BorderLayout.EAST);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Bottom margin between items
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    // Creates the visual placeholder rectangles for empty list items
    private JPanel createEmptySlot() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_LIST_ITEM);
        panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    // --- Specific Action Buttons for the Right Side of Lists ---

    private JPanel createPublishActionButtons() {
        return createPublishActionButtons(false);
    }

    private JPanel createPublishActionButtons(boolean activeGreen) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnl.setOpaque(false);

        Color pubColor = activeGreen ? new Color(0, 204, 102) : new Color(80, 80, 80);
        JButton btnPublish = createActionButton("PUBLISH", pubColor);
        JButton btnView = createActionButton("VIEW ONLY", new Color(220, 60, 60));

        pnl.add(btnPublish);
        pnl.add(btnView);
        return pnl;
    }

    private JPanel createApproveRejectButtons() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnl.setOpaque(false);

        JButton btnApprove = createCircleIconBtn("✓", new Color(0, 180, 80));
        JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60));

        pnl.add(btnApprove);
        pnl.add(btnReject);
        return pnl;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createCircleIconBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createStatusLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JLabel createWarningLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(255, 204, 0)); // Warning Yellow
        return lbl;
    }

    // =========================================================================
    // MAIN LAUNCHER
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SuperAdminDashboard dashboard = new SuperAdminDashboard();
            dashboard.setVisible(true);
        });
    }
}