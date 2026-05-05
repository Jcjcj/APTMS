package Dashboard;

import com.mycompany.apartmentsytem1.SuperAdminDAO;
import main.LandingPage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

public class SuperAdminDashboard extends JFrame implements ActionListener {

    // Theme Colors
    private final Color COLOR_SIDEBAR = new Color(0, 35, 20);
    private final Color COLOR_MAIN_BG = new Color(0, 51, 26);
    private final Color COLOR_CONTAINER = new Color(0, 102, 51);
    private final Color COLOR_LIST_ITEM = new Color(5, 20, 10);
    private final Color COLOR_BTN_ACTIVE = new Color(0, 102, 51);
    private final Color COLOR_TEXT = Color.WHITE;

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    
    private JButton btnDashboard, btnOwners, btnInquiries, btnBilling, btnNotification, btnLogout;
    private JButton[] navButtons;
    
    // --- DATABASE CONNECTION ---
    private SuperAdminDAO dao = new SuperAdminDAO(); 

    public SuperAdminDashboard() {
        this.setTitle("Super Admin Dashboard - Apartment Management System");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        this.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setBackground(COLOR_MAIN_BG);
        cardsContainer.setBorder(new EmptyBorder(30, 40, 40, 40));

        // Load the dynamic database panels
        cardsContainer.add(createDashboardCard(), "Dashboard");
        cardsContainer.add(createOwnersCard(), "Owners");
        cardsContainer.add(createInquiriesCard(), "Inquiries");
        cardsContainer.add(createBillingCard(), "Billing");
        cardsContainer.add(createNotificationCard(), "Notification");

        this.add(cardsContainer, BorderLayout.CENTER);
        activateButton(btnDashboard);
    }

    // =========================================================================
    // SIDEBAR CONSTRUCTION
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(COLOR_SIDEBAR);

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

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(COLOR_SIDEBAR);

        btnDashboard = createNavButton("Super Admin Dashboard");
        btnOwners = createNavButton("Apartment Owners");
        btnInquiries = createNavButton("Inquiries");
        btnBilling = createNavButton("Billing");
        btnNotification = createNavButton("Notification");

        navButtons = new JButton[]{btnDashboard, btnOwners, btnInquiries, btnBilling, btnNotification};

        for (JButton btn : navButtons) {
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(5));
        }

        // Logout button
        btnLogout = createNavButton("Log Out");
        btnLogout.setForeground(new Color(255, 100, 100)); // Red tint
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);
        
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == btnLogout) {
            this.dispose();
            new LandingPage().setVisible(true);
            return;
        }
        
        for (JButton btn : navButtons) { btn.setBackground(COLOR_SIDEBAR); }

        if (source == btnDashboard) { activateButton(btnDashboard); cardLayout.show(cardsContainer, "Dashboard"); } 
        else if (source == btnOwners) { activateButton(btnOwners); cardLayout.show(cardsContainer, "Owners"); } 
        else if (source == btnInquiries) { activateButton(btnInquiries); cardLayout.show(cardsContainer, "Inquiries"); } 
        else if (source == btnBilling) { activateButton(btnBilling); cardLayout.show(cardsContainer, "Billing"); } 
        else if (source == btnNotification) { activateButton(btnNotification); cardLayout.show(cardsContainer, "Notification"); }
    }

    private void activateButton(JButton btn) { btn.setBackground(COLOR_BTN_ACTIVE); }

    // =========================================================================
    // DYNAMIC DATABASE TABS
    // =========================================================================

    // 1. Dashboard (Overview)
    private JPanel createDashboardCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Super Admin Dashboard"), BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 30, 0));
        columns.setOpaque(false);

        // Left Column: Due Warnings
        JPanel col1 = new JPanel(new BorderLayout()); col1.setOpaque(false);
        col1.add(createSubHeader("On Due Date Warning"), BorderLayout.NORTH);
        JPanel list1 = createContainerBox();
        List<String[]> warnings = dao.getDueWarnings();
        
        if (warnings.isEmpty()) {
            list1.add(createEmptyLabel("No warnings active. All payments are up to date."));
        } else {
            for (String[] w : warnings) {
                // w[0] = aptName, w[1] = ownerName, w[2] = date
                list1.add(createListItem(w[0], w[1], "Due: " + w[2], createWarningLabel("⚠ OVERDUE")));
            }
        }
        JScrollPane scroll1 = new JScrollPane(list1); makeScrollTransparent(scroll1);
        col1.add(scroll1, BorderLayout.CENTER);
        columns.add(col1);

        // Right Column: Active Owners
        JPanel col2 = new JPanel(new BorderLayout()); col2.setOpaque(false);
        col2.add(createSubHeader("Active Owners"), BorderLayout.NORTH);
        JPanel list2 = createContainerBox();
        List<String[]> activeOwners = dao.getActiveOwners();
        
        if (activeOwners.isEmpty()) {
            list2.add(createEmptyLabel("No active owners. Approve registrations in the Inquiries tab."));
        } else {
            for (String[] o : activeOwners) {
                // o[0] = aptName, o[1] = ownerName, o[2] = contact
                list2.add(createListItem(o[0], o[1], o[2], null));
            }
        }
        JScrollPane scroll2 = new JScrollPane(list2); makeScrollTransparent(scroll2);
        col2.add(scroll2, BorderLayout.CENTER);
        columns.add(col2);

        card.add(columns, BorderLayout.CENTER);
        return card;
    }

    // 2. Active Owners (Approved from DB)
    private JPanel createOwnersCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Apartment Owners"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Active System Owners"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> activeOwners = dao.getActiveOwners();

        if (activeOwners.isEmpty()) {
            list.add(createEmptyLabel("No active apartments currently listed."));
        } else {
            for (String[] apt : activeOwners) {
                list.add(createListItem(apt[0], apt[1], apt[2], createPublishActionButtons(true))); 
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 3. Inquiries (Pending from DB)
    private JPanel createInquiriesCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Inquiries"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Pending Apartment Registrations"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> pendingRegistrations = dao.getPendingApartments();

        if (pendingRegistrations.isEmpty()) {
            list.add(createEmptyLabel("All caught up! No pending registrations in the queue."));
        } else {
            for (String[] apt : pendingRegistrations) {
                // apt[0] = id, apt[1] = aptName, apt[2] = ownerName, apt[3] = contact
                int aptId = Integer.parseInt(apt[0]);
                list.add(createListItem(apt[1], "Owner: " + apt[2], "Contact: " + apt[3], createApproveRejectButtons(aptId, apt[1])));
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 4. Billing (Overview from DB)
    private JPanel createBillingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Billing"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Owners Status"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> billingOverview = dao.getBillingOverview();

        if (billingOverview.isEmpty()) {
            list.add(createEmptyLabel("No billing data available."));
        } else {
            for (String[] b : billingOverview) {
                // b[0] = aptName, b[1] = ownerName, b[2] = contact
                list.add(createListItem(b[0], b[1], b[2], createStatusLabel("Monthly")));
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // 5. Notification
    private JPanel createNotificationCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("System Notifications"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Due Warnings & Alerts"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> warnings = dao.getDueWarnings();
        
        if (warnings.isEmpty()) {
            list.add(createEmptyLabel("No warnings active."));
        } else {
            for (String[] w : warnings) {
                list.add(createListItem(w[0], w[1], "Platform Fee Notice", createWarningLabel("⚠ Due " + w[2])));
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // UI HELPER METHODS
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

    private JLabel createEmptyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return lbl;
    }

    private void makeScrollTransparent(JScrollPane scroll) {
        scroll.setBorder(null); 
        scroll.setOpaque(false); 
        scroll.getViewport().setOpaque(false);
    }

    private JPanel createContainerBox() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_CONTAINER);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(container, BorderLayout.NORTH);
        return container; 
    }

    private JPanel createListItem(String line1, String line2, String line3, JComponent rightAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIST_ITEM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel leftText = new JPanel();
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.setOpaque(false);

        JLabel l1 = new JLabel(line1); l1.setFont(new Font("Segoe UI", Font.BOLD, 18)); l1.setForeground(Color.WHITE); leftText.add(l1);
        if (line2 != null) { leftText.add(Box.createVerticalStrut(5)); JLabel l2 = new JLabel(line2); l2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); l2.setForeground(Color.LIGHT_GRAY); leftText.add(l2); }
        if (line3 != null) { leftText.add(Box.createVerticalStrut(2)); JLabel l3 = new JLabel(line3); l3.setFont(new Font("Segoe UI", Font.PLAIN, 14)); l3.setForeground(Color.LIGHT_GRAY); leftText.add(l3); }

        panel.add(leftText, BorderLayout.CENTER);
        if (rightAction != null) { panel.add(rightAction, BorderLayout.EAST); }

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    // --- Dynamic Action Buttons ---

    private JPanel createApproveRejectButtons(int apartmentId, String apartmentName) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnl.setOpaque(false);

        JButton btnApprove = createCircleIconBtn("✓", new Color(0, 180, 80));
        btnApprove.addActionListener(e -> {
            if (dao.approveApartmentRegistration(apartmentId)) {
                JOptionPane.showMessageDialog(this, apartmentName + " has been Approved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Error approving apartment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60));
        btnReject.addActionListener(e -> {
            // Prompt the admin to provide a rejection reason (as per the Terms & Conditions!)
            String reason = JOptionPane.showInputDialog(this, "Enter reason for rejection for " + apartmentName + ":");
            if (reason != null && !reason.trim().isEmpty()) {
                if (dao.rejectApartmentRegistration(apartmentId, reason)) {
                    JOptionPane.showMessageDialog(this, apartmentName + " has been Rejected. Owner notified.", "Rejected", JOptionPane.WARNING_MESSAGE);
                    refreshDashboard();
                } else {
                    JOptionPane.showMessageDialog(this, "Error rejecting apartment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pnl.add(btnApprove);
        pnl.add(btnReject);
        return pnl;
    }

    private JPanel createPublishActionButtons(boolean activeGreen) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pnl.setOpaque(false);
        Color pubColor = activeGreen ? new Color(0, 204, 102) : new Color(80, 80, 80);
        pnl.add(createActionButton("PUBLISHED", pubColor));
        return pnl;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); return btn;
    }

    private JButton createCircleIconBtn(String text, Color bg) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 18)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(40, 40)); btn.setBorder(BorderFactory.createEmptyBorder()); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
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
    
    // Completely reloads the frame so new data appears instantly
    private void refreshDashboard() {
        this.dispose();
        new SuperAdminDashboard().setVisible(true);
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