package Dashboard;

import com.mycompany.apartmentsytem1.SuperAdminDAO;
import main.LandingPage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDate;
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
    
    private JButton btnDashboard, btnOwners, btnInquiries, btnTransaction, btnNotification, btnLogout;
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

        cardsContainer.add(createDashboardCard(), "Dashboard");
        cardsContainer.add(createOwnersCard(), "Owners");
        cardsContainer.add(createInquiriesCard(), "Inquiries");
        cardsContainer.add(createBillingCard(), "Billing");
        cardsContainer.add(createNotificationCard(), "Notification");

        this.add(cardsContainer, BorderLayout.CENTER);

        activateButton(btnDashboard);
        cardLayout.show(cardsContainer, "Dashboard");
    }

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
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(COLOR_SIDEBAR);
        
        btnDashboard = createNavButton("Super Admin Dashboard");
        btnOwners = createNavButton("Apartment Owners");
        btnInquiries = createNavButton("Inquiries");
        btnTransaction = createNavButton("Billing");
        btnNotification = createNavButton("Notification");

        navButtons = new JButton[]{btnDashboard, btnOwners, btnInquiries, btnTransaction, btnNotification};

        for (JButton btn : navButtons) {
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(2)); 
        }

        btnLogout = createNavButton("Log Out");
        btnLogout.setForeground(new Color(255, 100, 100)); 
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
        btn.setMaximumSize(new Dimension(250, 45));
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

        for (JButton btn : navButtons) {
            btn.setBackground(COLOR_SIDEBAR); 
        }

        if (source == btnDashboard) { activateButton(btnDashboard); cardLayout.show(cardsContainer, "Dashboard"); }
        else if (source == btnOwners) { activateButton(btnOwners); cardLayout.show(cardsContainer, "Owners"); }
        else if (source == btnInquiries) { activateButton(btnInquiries); cardLayout.show(cardsContainer, "Inquiries"); }
        // BUG 1 FIXED: Changed the destination route from "Transaction" to "Billing" so the tab actually opens!
        else if (source == btnTransaction) { activateButton(btnTransaction); cardLayout.show(cardsContainer, "Billing"); }
        else if (source == btnNotification) { activateButton(btnNotification); cardLayout.show(cardsContainer, "Notification"); }
    }

    private void activateButton(JButton btn) {
        btn.setBackground(COLOR_BTN_ACTIVE);
    }

    // =========================================================================
    // CARD: SUPER ADMIN DASHBOARD
    // =========================================================================
    private JPanel createDashboardCard() {
        JPanel card = createBaseCard("Super Admin Dashboard");
        
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
        grid.setOpaque(false);

        // --- LEFT COLUMN: DUE DATE WARNINGS ---
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("On Due Date Warning"), BorderLayout.NORTH);
        
        JPanel duePanel = createContainerBox();
        List<String[]> warnings = dao.getDueWarnings();
        
        if (warnings.isEmpty()) {
            duePanel.add(createLabel("No warnings active. All payments are up to date.", 14, SwingConstants.LEFT, Font.ITALIC));
        } else {
            for (String[] w : warnings) {
                JPanel item = new JPanel(new BorderLayout()); item.setOpaque(false);
                item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 153, 76)), 
                    BorderFactory.createEmptyBorder(10, 0, 10, 0)
                ));
                
                item.add(createLabel(w[0], 18, SwingConstants.LEFT, Font.BOLD), BorderLayout.NORTH);
                item.add(createLabel(w[1], 14, SwingConstants.LEFT), BorderLayout.CENTER);
                
                // BUG 2 FIXED: Dynamic logic to check if it's "DUE SOON" or actually "OVERDUE"
                LocalDate today = LocalDate.now();
                LocalDate dueDate = LocalDate.parse(w[2]);
                String warnText = today.isAfter(dueDate) ? "⚠ OVERDUE" : "⚠ DUE SOON";
                
                item.add(createWarningLabel(warnText), BorderLayout.EAST);
                duePanel.add(item);
            }
        }
        
        leftCol.add(new JScrollPane(duePanel), BorderLayout.CENTER);
        grid.add(leftCol);

        // --- RIGHT COLUMN: ACTIVE OWNERS ---
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Active Owners"), BorderLayout.NORTH);
        
        JPanel ownersPanel = createContainerBox();
        List<String[]> activeOwners = dao.getActiveOwners();
        
        if (activeOwners.isEmpty()) {
            ownersPanel.add(createLabel("No active owners yet.", 14, SwingConstants.LEFT, Font.ITALIC));
        } else {
            for (String[] o : activeOwners) {
                JPanel item = new JPanel(new BorderLayout()); item.setBackground(COLOR_LIST_ITEM);
                item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                
                JPanel leftText = new JPanel(); leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS)); leftText.setOpaque(false);
                leftText.add(createLabel(o[0], 18, SwingConstants.LEFT, Font.BOLD));
                leftText.add(Box.createVerticalStrut(5));
                leftText.add(createLabel(o[1], 14, SwingConstants.LEFT));
                leftText.add(createLabel(o[2], 14, SwingConstants.LEFT));
                
                item.add(leftText, BorderLayout.CENTER);
                
                JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
                wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                wrapper.add(item, BorderLayout.CENTER);
                ownersPanel.add(wrapper);
            }
        }
        
        rightCol.add(new JScrollPane(ownersPanel), BorderLayout.CENTER);
        grid.add(rightCol);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // CARD: APARTMENT OWNERS
    // =========================================================================
    private JPanel createOwnersCard() {
        JPanel card = createBaseCard("Apartment Owners");
        
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Registered Apartments"), BorderLayout.NORTH);
        
        JPanel listPanel = createContainerBox();
        List<String[]> activeOwners = dao.getActiveOwners();
        
        if (activeOwners.isEmpty()) {
            listPanel.add(createLabel("No apartments registered in the system.", 14, SwingConstants.CENTER, Font.ITALIC));
        } else {
            for (String[] o : activeOwners) {
                String aptName = o[0]; String ownerName = o[1]; String contact = o[2];
                
                JPanel item = new JPanel(new BorderLayout()); item.setBackground(COLOR_LIST_ITEM);
                item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                
                JPanel leftText = new JPanel(); leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS)); leftText.setOpaque(false);
                leftText.add(createLabel(aptName, 22, SwingConstants.LEFT, Font.BOLD));
                leftText.add(Box.createVerticalStrut(5));
                leftText.add(createLabel("Owner: " + ownerName, 16, SwingConstants.LEFT));
                leftText.add(createLabel("Contact: " + contact, 14, SwingConstants.LEFT));
                
                item.add(leftText, BorderLayout.CENTER);
                
                JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlRight.setOpaque(false);
                JButton btnDeactivate = createActionButton("DEACTIVATE", new Color(220, 60, 60));
                pnlRight.add(btnDeactivate);
                
                item.add(pnlRight, BorderLayout.EAST);
                
                JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
                wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
                wrapper.add(item, BorderLayout.CENTER);
                listPanel.add(wrapper);
            }
        }
        
        mainContent.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // CARD: INQUIRIES
    // =========================================================================
    private JPanel createInquiriesCard() {
        JPanel card = createBaseCard("Inquiries");
        
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Pending Apartment Registrations"), BorderLayout.NORTH);
        
        JPanel listPanel = createContainerBox();
        List<String[]> pendingApts = dao.getPendingApartments();
        
        if (pendingApts.isEmpty()) {
            listPanel.add(createLabel("No pending registrations.", 14, SwingConstants.CENTER, Font.ITALIC));
        } else {
            for (String[] apt : pendingApts) {
                String aptId = apt[0]; String aptName = apt[1]; String owner = apt[2]; String contact = apt[3];
                
                JPanel item = new JPanel(new BorderLayout()); item.setBackground(COLOR_LIST_ITEM);
                item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                
                JPanel leftText = new JPanel(); leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS)); leftText.setOpaque(false);
                leftText.add(createLabel(aptName, 22, SwingConstants.LEFT, Font.BOLD));
                leftText.add(Box.createVerticalStrut(5));
                leftText.add(createLabel("Owner: " + owner, 16, SwingConstants.LEFT));
                leftText.add(createLabel("Contact: " + contact, 14, SwingConstants.LEFT));
                
                item.add(leftText, BorderLayout.CENTER);
                
                JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlRight.setOpaque(false);
                
                JButton btnApprove = createCircleIconBtn("✓", new Color(0, 204, 102));
                btnApprove.addActionListener(e -> {
                    if (dao.approveApartmentRegistration(Integer.parseInt(aptId))) {
                        JOptionPane.showMessageDialog(this, "Apartment Approved!");
                        refreshDashboard();
                    }
                });
                
                JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60));
                btnReject.addActionListener(e -> {
                    String reason = JOptionPane.showInputDialog(this, "Enter rejection reason:");
                    if (reason != null && !reason.trim().isEmpty()) {
                        if (dao.rejectApartmentRegistration(Integer.parseInt(aptId), reason)) {
                            refreshDashboard();
                        }
                    }
                });
                
                pnlRight.add(btnApprove); pnlRight.add(btnReject);
                item.add(pnlRight, BorderLayout.EAST);
                
                JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
                wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
                wrapper.add(item, BorderLayout.CENTER);
                listPanel.add(wrapper);
            }
        }
        
        mainContent.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // CARD: BILLING
    // =========================================================================
    private JPanel createBillingCard() {
        JPanel card = createBaseCard("Billing & Transactions");
        
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Subscriptions"), BorderLayout.NORTH);
        
        JPanel listPanel = createContainerBox();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        
        // Headers
        JPanel headerRow = new JPanel(new GridBagLayout()); headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        gbc.gridx=0; headerRow.add(createLabel("APARTMENT", 16, SwingConstants.LEFT, Font.BOLD), gbc);
        gbc.gridx=1; headerRow.add(createLabel("ROOMS", 16, SwingConstants.CENTER, Font.BOLD), gbc);
        gbc.gridx=2; headerRow.add(createLabel("NEXT BILLING", 16, SwingConstants.CENTER, Font.BOLD), gbc);
        gbc.gridx=3; headerRow.add(createLabel("STATUS", 16, SwingConstants.CENTER, Font.BOLD), gbc);
        gbc.gridx=4; headerRow.add(createLabel("PAYMENT INFO", 16, SwingConstants.CENTER, Font.BOLD), gbc);
        gbc.gridx=5; headerRow.add(createLabel("ACTION", 16, SwingConstants.RIGHT, Font.BOLD), gbc);
        listPanel.add(headerRow);
        
        List<String[]> transactions = dao.getTransactionOverview();
        
        if (transactions.isEmpty()) {
            listPanel.add(createLabel("No transactions found.", 14, SwingConstants.CENTER, Font.ITALIC));
        } else {
            for (String[] t : transactions) {
                String tId = t[0]; String tApt = t[1]; String tOwner = t[2]; String tContact = t[3];
                String tRooms = t[4]; String tDue = t[5]; boolean isPaid = t[6].equals("APPROVED");
                String tTin = t[7]; String tMethod = t[8];
                
                JPanel item = new JPanel(new GridBagLayout()); item.setBackground(COLOR_LIST_ITEM);
                item.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
                
                // Col 0: Apt Info
                gbc.gridx=0; gbc.gridy=0; 
                JPanel pnlApt = new JPanel(); pnlApt.setLayout(new BoxLayout(pnlApt, BoxLayout.Y_AXIS)); pnlApt.setOpaque(false);
                pnlApt.add(createLabel(tApt, 18, SwingConstants.LEFT, Font.BOLD));
                pnlApt.add(createLabel(tOwner + " - " + tContact, 12, SwingConstants.LEFT, Font.PLAIN));
                item.add(pnlApt, gbc);
                
                // Col 1: Rooms
                gbc.gridx=1; item.add(createLabel(tRooms, 16, SwingConstants.CENTER), gbc);
                
                // Col 2: Next Due Date
                gbc.gridx=2; item.add(createLabel(tDue, 16, SwingConstants.CENTER), gbc);
                
                // Col 3: Status
                gbc.gridx=3; 
                JLabel lblStatus = createLabel(isPaid ? "PAID" : "UNPAID", 16, SwingConstants.CENTER, Font.BOLD);
                lblStatus.setForeground(isPaid ? new Color(0, 204, 102) : new Color(255, 102, 102));
                item.add(lblStatus, gbc);
                
                // Col 4: Payment Info & 2% FEE DISPLAY
                gbc.gridx=4; 
                if (!isPaid) {
                    item.add(createLabel("FEE: ₱" + String.format("%,.2f", Double.parseDouble(t[9])), 16, SwingConstants.CENTER, Font.BOLD), gbc);
                } else {
                    item.add(createLabel(tTin, 14, SwingConstants.CENTER), gbc);
                    gbc.gridy = 1; item.add(createLabel(tMethod, 12, SwingConstants.CENTER), gbc);
                }
                
                // Col 5: Action Button
                gbc.gridx=5; gbc.gridy=0; gbc.anchor = GridBagConstraints.EAST;
                JButton btnToggle = createActionButton(isPaid ? "MARK UNPAID" : "VERIFY & APPROVE", isPaid ? new Color(220, 60, 60) : COLOR_BTN_ACTIVE);
                btnToggle.addActionListener(e -> {
                    if (dao.setApartmentPaymentStatus(Integer.parseInt(tId), !isPaid)) {
                        JOptionPane.showMessageDialog(this, "Payment status updated.");
                        refreshDashboard();
                    }
                });
                item.add(btnToggle, gbc);
                
                JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false);
                wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                wrapper.add(item, BorderLayout.CENTER);
                listPanel.add(wrapper);
            }
        }
        
        mainContent.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // CARD: NOTIFICATIONS
    // =========================================================================
    private JPanel createNotificationCard() {
        JPanel card = createBaseCard("System Notifications");
        
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Due Warnings & Alerts"), BorderLayout.NORTH);
        
        JPanel listPanel = createContainerBox();
        List<String[]> warnings = dao.getDueWarnings();
        
        if (warnings.isEmpty()) {
            listPanel.add(createLabel("No warnings active.", 14, SwingConstants.LEFT, Font.ITALIC));
        } else {
            for (String[] w : warnings) {
                JPanel item = new JPanel(new BorderLayout()); item.setOpaque(false);
                item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 153, 76)), 
                    BorderFactory.createEmptyBorder(10, 0, 10, 0)
                ));
                
                item.add(createLabel("Platform Fee Notice", 18, SwingConstants.LEFT, Font.BOLD), BorderLayout.NORTH);
                item.add(createLabel(w[0] + " (" + w[1] + ")", 14, SwingConstants.LEFT), BorderLayout.CENTER);
                
                // BUG 2 FIXED: Dynamic Due Soon vs Overdue label
                LocalDate today = LocalDate.now();
                LocalDate dueDate = LocalDate.parse(w[2]);
                String warnText = today.isAfter(dueDate) ? "⚠ Overdue since " + w[2] : "⚠ Due " + w[2];
                
                item.add(createWarningLabel(warnText), BorderLayout.EAST);
                listPanel.add(item);
            }
        }
        
        mainContent.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 30)); card.setBackground(COLOR_MAIN_BG); card.setOpaque(false); 
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(createLabel(title, 42, SwingConstants.LEFT, Font.BOLD), BorderLayout.WEST);
        header.add(createLabel("Super Admin", 20, SwingConstants.RIGHT, Font.PLAIN), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JLabel createSubHeader(String text) {
        JLabel lbl = createLabel(text, 24, SwingConstants.LEFT, Font.BOLD); lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT); return lbl;
    }

    private JPanel createContainerBox() {
        JPanel container = new JPanel(); container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS)); container.setBackground(COLOR_CONTAINER); container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); container.setAlignmentX(Component.LEFT_ALIGNMENT); return container; 
    }

    private JLabel createLabel(String text, int size, int alignment) { return createLabel(text, size, alignment, Font.PLAIN); }
    private JLabel createLabel(String text, int size, int alignment, int fontStyle) {
        JLabel label = new JLabel(text); label.setForeground(COLOR_TEXT); label.setFont(new Font("Segoe UI", fontStyle, size)); label.setHorizontalAlignment(alignment); return label;
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