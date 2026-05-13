package Dashboard;

import com.mycompany.apartmentsytem1.SuperAdminDAO;
import com.mycompany.apartmentsytem1.FileStorageUtil;
import main.LandingPage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

        // --- BUG FIX: Parse the massive database string to extract clean values ---
        String cleanMethod = method != null ? method : "Bank Transfer";
        String cleanRef = "REF-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        if (cleanMethod.contains("(Ref:")) {
            try {
                // Extract the reference number trapped between "(Ref: " and ")"
                int refStart = cleanMethod.indexOf("(Ref: ") + 6;
                int refEnd = cleanMethod.indexOf(")", refStart);
                if (refStart > 5 && refEnd > refStart) {
                    cleanRef = cleanMethod.substring(refStart, refEnd);
                }
                // Keep only the text before "(Ref:" as the clean payment method
                cleanMethod = cleanMethod.substring(0, cleanMethod.indexOf("(Ref:")).trim();
            } catch (Exception e) {}
        }
        // -------------------------------------------------------------------------

        panel.add(createPopupDetailRow("Apartment:", aptName));
        panel.add(createPopupDetailRow("TIN Number:", tin != null ? tin : "Not Provided"));
        panel.add(createPopupDetailRow("Payment Method:", cleanMethod));
        panel.add(createPopupDetailRow("Transaction Date:", date));
        panel.add(createPopupDetailRow("Reference No.:", cleanRef));

        panel.add(Box.createVerticalStrut(30));
        JButton btnClose = createActionButton("CLOSE", new Color(150, 150, 150)); btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose);

        dialog.add(panel); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private JPanel createPopupDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout()); 
        row.setOpaque(false); 
        
        // --- BUG FIX: Increased width to 500 to prevent text overlapping ---
        row.setMaximumSize(new Dimension(500, 30)); 
        
        JLabel lbl = new JLabel(label); 
        lbl.setForeground(Color.LIGHT_GRAY); 
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // --- BUG FIX: Fixed label width ensures perfect column alignment ---
        lbl.setPreferredSize(new Dimension(140, 30)); 
        
        JLabel val = new JLabel(value); 
        val.setForeground(Color.WHITE); 
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        val.setHorizontalAlignment(SwingConstants.RIGHT); // Aligns values nicely to the right
        
        row.add(lbl, BorderLayout.WEST); 
        row.add(val, BorderLayout.CENTER);
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
        String[] details = dao.getOwnerRegistrationDetailsForReview(apartmentId);
        if (details == null) {
            showThemedMessage("Owner Registration Details", "Unable to load owner registration details.");
            return;
        }

        JDialog dialog = new JDialog(this, "Owner Registration Details", true);
        dialog.setUndecorated(true);
        dialog.getContentPane().setBackground(COLOR_LIST_ITEM);

        JPanel shell = new JPanel(new BorderLayout(0, 18));
        shell.setBackground(COLOR_LIST_ITEM);
        shell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CONTAINER, 2),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Owner Registration Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COLOR_TEXT);
        header.add(title, BorderLayout.WEST);

        JButton btnClose = createActionButton("CLOSE", new Color(150, 150, 150));
        btnClose.addActionListener(e -> dialog.dispose());
        header.add(btnClose, BorderLayout.EAST);
        shell.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 24, 0));
        content.setOpaque(false);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(COLOR_CONTAINER);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        detailsPanel.add(createProfileSectionLabel("Owner Details"));
        detailsPanel.add(createProfileDetailRow("Name", details[0]));
        detailsPanel.add(createProfileDetailRow("Username", details[5]));
        detailsPanel.add(createProfileDetailRow("Contact Number", details[1]));
        detailsPanel.add(createProfileDetailRow("Email", details[2]));
        detailsPanel.add(createProfileDetailRow("Address", details[3]));
        detailsPanel.add(createProfileDetailRow("Emergency Contact", details[4]));
        detailsPanel.add(createProfileDetailRow("GCash Number", details[6]));
        detailsPanel.add(createProfileDetailRow("GCash Name", details[7]));
        detailsPanel.add(createProfileDetailRow("Paymaya Number", details[8]));
        detailsPanel.add(createProfileDetailRow("Paymaya Name", details[9]));

        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(createProfileSectionLabel("Apartment Details"));
        detailsPanel.add(createProfileDetailRow("Apartment Name", details[11]));
        detailsPanel.add(createProfileDetailRow("Apartment Code", details[12]));
        detailsPanel.add(createProfileDetailRow("TIN Number", details[13]));
        detailsPanel.add(createProfileDetailRow("Barangay", details[23]));
        detailsPanel.add(createProfileDetailRow("Street", details[24]));
        detailsPanel.add(createProfileDetailRow("Floors", details[14]));
        detailsPanel.add(createProfileDetailRow("Total Rooms", details[15]));
        detailsPanel.add(createProfileDetailRow("Rooms Available", details[16]));
        detailsPanel.add(createProfileDetailRow("Capital", details[17]));
        detailsPanel.add(createProfileDetailRow("Tax Rate", details[18]));
        detailsPanel.add(createProfileDetailRow("Penalty Rate", details[19]));
        detailsPanel.add(createProfileDetailRow("Payment Method", details[20]));
        detailsPanel.add(createProfileDetailRow("Apartment Contact", details[31]));
        detailsPanel.add(createProfileDetailRow("Apartment Email", details[32]));
        detailsPanel.add(createProfileDetailRow("Apartment Emergency", details[33]));
        detailsPanel.add(createProfileDetailRow("Approval Status", details[34]));
        detailsPanel.add(createProfileDetailRow("Rejection Reason", details[35]));
        detailsPanel.add(createProfileDetailRow("Next Billing Date", details[36]));

        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(createProfileSectionLabel("Utilities"));
        detailsPanel.add(createProfileDetailRow("Electricity", safeText(details[25]) + " | Rate: " + safeText(details[26])));
        detailsPanel.add(createProfileDetailRow("Water", safeText(details[27]) + " | Rate: " + safeText(details[28])));
        detailsPanel.add(createProfileDetailRow("Internet", safeText(details[29]) + " | Rate: " + safeText(details[30])));

        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(createProfileSectionLabel("Description"));
        detailsPanel.add(createProfileTextBlock(details[21]));
        detailsPanel.add(Box.createVerticalStrut(8));
        detailsPanel.add(createProfileSectionLabel("Policy"));
        detailsPanel.add(createProfileTextBlock(details[22]));

        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.setBorder(null);
        detailsScroll.setOpaque(false);
        detailsScroll.getViewport().setOpaque(false);

        content.add(detailsScroll);
        content.add(createValidIdPreview(details[10]));
        shell.add(content, BorderLayout.CENTER);

        dialog.add(shell);
        dialog.setPreferredSize(new Dimension(980, 600));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel createProfileSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(COLOR_TEXT);
        label.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createProfileDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKey = new JLabel(label + ":");
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKey.setForeground(COLOR_TEXT);
        lblKey.setPreferredSize(new Dimension(155, 24));

        JLabel lblValue = new JLabel(safeText(value));
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblValue.setForeground(COLOR_TEXT);
        lblValue.setVerticalAlignment(SwingConstants.TOP);

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    private JTextArea createProfileTextBlock(String value) {
        JTextArea text = new JTextArea(safeText(value));
        text.setOpaque(false);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setForeground(COLOR_TEXT);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        text.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        return text;
    }

    private JPanel createValidIdPreview(String validIdPath) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_CONTAINER);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Owner Valid ID");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(COLOR_TEXT);
        panel.add(title, BorderLayout.NORTH);

        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(COLOR_LIST_ITEM);
        imageLabel.setForeground(Color.LIGHT_GRAY);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 140, 70), 1));

        File idFile = resolveUploadedFile(validIdPath);
        if (idFile != null) {
            ImageIcon icon = new ImageIcon(idFile.getAbsolutePath());
            if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
                imageLabel.setIcon(scaleIcon(icon, 360, 390));
                imageLabel.setText("");
                imageLabel.setToolTipText(idFile.getAbsolutePath());
            } else {
                imageLabel.setText("Unable to preview this ID image.");
            }
        } else {
            imageLabel.setText("No valid ID image found.");
        }

        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }

    private ImageIcon scaleIcon(ImageIcon icon, int maxWidth, int maxHeight) {
        double widthRatio = maxWidth / (double) icon.getIconWidth();
        double heightRatio = maxHeight / (double) icon.getIconHeight();
        double scale = Math.min(widthRatio, heightRatio);
        int width = Math.max(1, (int) Math.round(icon.getIconWidth() * scale));
        int height = Math.max(1, (int) Math.round(icon.getIconHeight() * scale));
        Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private File resolveUploadedFile(String storedPath) {
        if (storedPath == null || storedPath.trim().isEmpty() || "no_id.png".equalsIgnoreCase(storedPath.trim())) {
            return null;
        }

        File direct = new File(storedPath.trim());
        if (direct.exists() && direct.isFile()) return direct;

        File base = new File(System.getProperty("user.dir"));
        while (base != null) {
            File relativeFile = new File(base, storedPath.trim());
            if (relativeFile.exists() && relativeFile.isFile()) return relativeFile;

            File uploadFile = new File(new File(base, FileStorageUtil.getUploadPath()), storedPath.trim());
            if (uploadFile.exists() && uploadFile.isFile()) return uploadFile;
            base = base.getParentFile();
        }

        return null;
    }

    private void showThemedMessage(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(COLOR_LIST_ITEM);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CONTAINER, 2),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT);
        panel.add(lblTitle, BorderLayout.NORTH);

        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblMessage.setForeground(COLOR_TEXT);
        panel.add(lblMessage, BorderLayout.CENTER);

        JButton btnClose = createActionButton("CLOSE", new Color(150, 150, 150));
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnClose);
        panel.add(btnRow, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "N/A";
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
