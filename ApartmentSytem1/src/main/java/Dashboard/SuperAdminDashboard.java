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

        // Load the dynamic database panels
        cardsContainer.add(createDashboardCard(), "Dashboard");
        cardsContainer.add(createOwnersCard(), "Owners");
        cardsContainer.add(createInquiriesCard(), "Inquiries");
        cardsContainer.add(createTransactionCard(), "Transaction");
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
        btnTransaction = createNavButton("Billing");
        btnNotification = createNavButton("Notification");

        navButtons = new JButton[]{btnDashboard, btnOwners, btnInquiries, btnTransaction, btnNotification};

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
        else if (source == btnTransaction) { activateButton(btnTransaction); cardLayout.show(cardsContainer, "Transaction"); }
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
                int apartmentId = Integer.parseInt(o[0]);
                list2.add(createListItem(o[1], o[2], o[3], createDetailsButton(apartmentId)));
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
                int apartmentId = Integer.parseInt(apt[0]);
                list.add(createListItem(apt[1], apt[2], apt[3], createDetailsButton(apartmentId))); 
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
                list.add(createListItem(apt[1], "Owner: " + apt[2], "Contact: " + apt[3], createInquiryActions(aptId, apt[1])));
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createTransactionCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_MAIN_BG);
        card.add(createHeader("Transaction"), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Apartment Owners Status"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> transactions = dao.getTransactionOverview();

        if (transactions.isEmpty()) {
            list.add(createEmptyLabel("No transaction data available."));
        } else {
            for (String[] t : transactions) {
                int aptId = Integer.parseInt(t[0]);
                String aptName = t[1];
                String ownerName = t[2];
                String contact = t[3];
                String rooms = t[4];
                String date = t[5];
                String status = t[6].equals("APPROVED") ? "PAID" : "UNPAID";
                String tin = t[7];
                String method = t[8];
                
                // Fetch live 2% fee from backend
                double[] financials = dao.getFinancialProjections(aptId);
                String formattedTotal = String.format("%,.2f", financials[1]);

                list.add(createTransactionListItem(aptId, aptName, ownerName, contact, rooms, date, formattedTotal, status, tin, method));
            }
        }

        JScrollPane scroll = new JScrollPane(list); makeScrollTransparent(scroll);
        mainContent.add(scroll, BorderLayout.CENTER);
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    //4. Transaction
    private JPanel createTransactionListItem(int aptId, String aptName, String ownerName, String contact, String rooms, String date, String total, String status, String tin, String method) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_LIST_ITEM);
        // Green outer border like your design
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 153, 76), 3), 
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // --- LEFT SIDE: TEXT DETAILS ---
        JPanel leftText = new JPanel(); leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS)); leftText.setOpaque(false);
        
        JLabel lblAptName = new JLabel(aptName); lblAptName.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblAptName.setForeground(Color.WHITE); leftText.add(lblAptName);
        JLabel lblOwner = new JLabel(ownerName); lblOwner.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblOwner.setForeground(Color.LIGHT_GRAY); leftText.add(lblOwner);
        JLabel lblContact = new JLabel(contact); lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblContact.setForeground(Color.LIGHT_GRAY); leftText.add(lblContact);
        
        leftText.add(Box.createVerticalStrut(15));
        JLabel lblRoomsDate = new JLabel(rooms + " Rooms Listed - " + date); lblRoomsDate.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblRoomsDate.setForeground(Color.WHITE); leftText.add(lblRoomsDate);
        JLabel lblTotal = new JLabel("Total Platform Fee: ₱ " + total); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTotal.setForeground(Color.WHITE); leftText.add(lblTotal);
        panel.add(leftText, BorderLayout.WEST);

        // --- RIGHT SIDE: BUTTONS & STATUS ---
        JPanel rightPanel = new JPanel(new BorderLayout()); rightPanel.setOpaque(false);
        
        // Proof Button (Top Right)
        JButton btnProof = createActionButton("PROOF OF TRANSACTION", new Color(0, 204, 102));
        btnProof.setPreferredSize(new Dimension(220, 35));
        btnProof.addActionListener(e -> showProofPopup(aptName, tin, method, date));
        
        JPanel pnlProof = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlProof.setOpaque(false); pnlProof.add(btnProof);
        rightPanel.add(pnlProof, BorderLayout.NORTH);

        // Status & Action Buttons (Bottom Right)
        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); bottomControls.setOpaque(false);
        
        JPanel pnlStatus = new JPanel(); pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.Y_AXIS)); pnlStatus.setOpaque(false);
        JLabel lblStatusText = new JLabel("STATUS"); lblStatusText.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblStatusText.setForeground(Color.LIGHT_GRAY); lblStatusText.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblStatusValue = new JLabel(status); lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 26)); lblStatusValue.setForeground(Color.WHITE); lblStatusValue.setAlignmentX(Component.RIGHT_ALIGNMENT);
        pnlStatus.add(lblStatusText); pnlStatus.add(lblStatusValue);
        bottomControls.add(pnlStatus);

        JPanel pnlActionBtns = new JPanel(); pnlActionBtns.setLayout(new BoxLayout(pnlActionBtns, BoxLayout.Y_AXIS)); pnlActionBtns.setOpaque(false);
        JButton btnPaid = createActionButton("PAID", new Color(0, 153, 204)); // Blue
        btnPaid.setPreferredSize(new Dimension(100, 30));
        btnPaid.addActionListener(e -> {
            if (dao.setApartmentPaymentStatus(aptId, true)) { refreshDashboard(); }
        });
        
        JButton btnUnpaid = createActionButton("UNPAID", new Color(204, 51, 51)); // Red
        btnUnpaid.setPreferredSize(new Dimension(100, 30));
        btnUnpaid.addActionListener(e -> {
            if (dao.setApartmentPaymentStatus(aptId, false)) { refreshDashboard(); }
        });

        pnlActionBtns.add(btnPaid); pnlActionBtns.add(Box.createVerticalStrut(5)); pnlActionBtns.add(btnUnpaid);
        bottomControls.add(pnlActionBtns);

        rightPanel.add(bottomControls, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); 
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    // 3. The Popup Dialog for Proof of Transaction
    private void showProofPopup(String aptName, String tin, String method, String date) {
        JDialog dialog = new JDialog(this, true); dialog.setUndecorated(true); dialog.getContentPane().setBackground(COLOR_LIST_ITEM);
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); panel.setOpaque(false); 
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_CONTAINER, 2), BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        JLabel lblTitle = new JLabel("Payment Verification"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTitle.setForeground(Color.WHITE); lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT); panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(20));

        panel.add(createPopupDetailRow("Apartment:", aptName));
        panel.add(createPopupDetailRow("TIN Number:", tin != null ? tin : "Not Provided"));
        panel.add(createPopupDetailRow("Payment Method:", method != null ? method : "Bank Transfer"));
        panel.add(createPopupDetailRow("Transaction Date:", date));
        
        // Mock Reference Number generator for visual completeness
        String ref = "REF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        panel.add(createPopupDetailRow("Reference No.:", ref));

        panel.add(Box.createVerticalStrut(30));
        JButton btnClose = createActionButton("CLOSE", new Color(150, 150, 150)); btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose);

        dialog.add(panel); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private JPanel createPopupDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false); row.setMaximumSize(new Dimension(400, 30));
        JLabel lbl = new JLabel(label); lbl.setForeground(Color.LIGHT_GRAY); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel val = new JLabel(value); val.setForeground(Color.WHITE); val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        row.add(lbl, BorderLayout.WEST); row.add(val, BorderLayout.EAST);
        return row;
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
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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

    private JPanel createInquiryActions(int apartmentId, String apartmentName) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnl.setOpaque(false);
        pnl.add(createDetailsButton(apartmentId));
        pnl.add(createApproveRejectButtons(apartmentId, apartmentName));
        return pnl;
    }

    private JButton createDetailsButton(int apartmentId) {
        JButton btn = new JButton("VIEW DETAILS");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(0, 120, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> showRegistrationDetails(apartmentId));
        return btn;
    }

    private void showRegistrationDetails(int apartmentId) {
        JTextArea details = new JTextArea(dao.getFullRegistrationDetails(apartmentId));
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(details);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        JOptionPane.showMessageDialog(this, scrollPane, "Registration Details", JOptionPane.INFORMATION_MESSAGE);
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
