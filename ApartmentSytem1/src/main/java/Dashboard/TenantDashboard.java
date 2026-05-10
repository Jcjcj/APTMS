package Dashboard;

import com.mycompany.apartmentsytem1.TenantDashboardDAO;
import com.mycompany.apartmentsytem1.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class TenantDashboard extends JFrame implements ActionListener {

    // --- Core Theme Colors (Updated to match new design) ---
    private final Color COLOR_SIDEBAR = new Color(0, 25, 10);      
    private final Color COLOR_MAIN_BG = new Color(0, 35, 15);      
    private final Color COLOR_CONTAINER = new Color(0, 102, 51);   
    private final Color COLOR_DARK_BOX = new Color(0, 20, 10);    
    private final Color COLOR_BTN_ACTION = new Color(0, 204, 102); 
    private final Color COLOR_TEXT = Color.WHITE;

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private JButton btnSummary, btnExpenses, btnMaintenance, btnNotification, btnInquiry, btnHistory, btnLogout;
    private JButton[] navButtons;
    private boolean isViewOnly = false;

    // --- DATABASE VARIABLES ---
    private TenantDashboardDAO dao = new TenantDashboardDAO();
    private int currentTenantId; 
    private int currentApartmentId = -1; 
    private String currentRoom = "N/A"; 
    private String tenantName = "Catriona Gray"; // Default fallback
    private String currentApartmentName = "Apartment";

    public TenantDashboard(int tenantId) {
        this.currentTenantId = tenantId;
        
        // --- 1. AUTO-FETCH TENANT INFO ---
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT t.name, r.apartment_id, r.room_number, a.apartment_name FROM registered_tenants t " +
                 "LEFT JOIN room_occupancy r ON t.tenant_id = r.tenant_id AND r.status = 'Current' " +
                 "LEFT JOIN apartments a ON r.apartment_id = a.apartment_id " +
                 "WHERE t.tenant_id = ?")) {
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.tenantName = rs.getString("name");
                if (rs.getObject("apartment_id") != null) {
                    this.currentApartmentId = rs.getInt("apartment_id");
                    this.currentRoom = rs.getString("room_number");
                    this.currentApartmentName = rs.getString("apartment_name");
                }
            }
            
            if (this.currentApartmentId != -1) {
                try (PreparedStatement psLock = conn.prepareStatement("SELECT is_active FROM apartments WHERE apartment_id = ?")) {
                    psLock.setInt(1, this.currentApartmentId);
                    ResultSet rsLock = psLock.executeQuery();
                    if (rsLock.next()) {
                        this.isViewOnly = (rsLock.getInt("is_active") == 0);
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) { e.printStackTrace(); }

        this.setTitle("Tenant Dashboard - " + tenantName);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        this.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setBackground(COLOR_MAIN_BG);
        cardsContainer.setBorder(new EmptyBorder(30, 40, 40, 40));

        // Add Panels
        cardsContainer.add(createSummaryCard(), "Tenant Dashboard");
        cardsContainer.add(createExpensesCard(), "Expenses");
        cardsContainer.add(createMaintenanceCard(), "Maintenance");
        cardsContainer.add(createNotificationCard(), "Notification");
        cardsContainer.add(createInquiryCard(), "Inquiry");
        cardsContainer.add(createHistoryCard(), "History");

        this.add(cardsContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // PANEL 1: TENANT DASHBOARD (Summary - Image 08043b)
    // =========================================================================
    private JPanel createSummaryCard() {
        JPanel card = createBaseCard("Tenant Dashboard");
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0)); 
        grid.setOpaque(false);

        Object[] billData = dao.getMyBills(currentApartmentId, currentRoom);
        
        // Left Column (Rent & Utilities)
        JPanel leftCol = new JPanel(); 
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS)); 
        leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Payment Due"));
        
        JPanel rentBox = createContainerBox();
        rentBox.add(createLabel("Rent", 18, SwingConstants.LEFT, Font.BOLD));
        rentBox.add(createLabel((String)billData[1], 14, SwingConstants.LEFT, Font.PLAIN)); 
        rentBox.add(createLabel("₱ " + String.format("%,.2f", (Double)billData[0]), 42, SwingConstants.LEFT, Font.BOLD)); 
        leftCol.add(rentBox);
        leftCol.add(Box.createVerticalStrut(15));

        JPanel utilBox = createContainerBox();
        utilBox.add(createLabel("Utilities", 18, SwingConstants.LEFT, Font.BOLD));
        utilBox.add(Box.createVerticalStrut(10));
        utilBox.add(createMiniUtility("Electricity", (String)billData[3], (Double)billData[2]));
        utilBox.add(createMiniUtility("Water", (String)billData[5], (Double)billData[4]));
        utilBox.add(createMiniUtility("Internet", (String)billData[7], (Double)billData[6]));
        leftCol.add(utilBox);

        // Right Column (Notifications)
        JPanel rightCol = new JPanel(new BorderLayout()); 
        rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Notifications"), BorderLayout.NORTH);
        
        JPanel notifBox = createContainerBox();
        List<String> announcements = dao.getAnnouncements(currentApartmentId);
        if(announcements.isEmpty()) {
            notifBox.add(createDarkBox("No new announcements."));
        } else {
            // Show only top 3 on summary screen
            for(int i=0; i<Math.min(3, announcements.size()); i++) {
                notifBox.add(createDarkBox(announcements.get(i)));
            }
        }
        rightCol.add(notifBox, BorderLayout.CENTER);

        grid.add(leftCol);
        grid.add(rightCol);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 2: EXPENSES (Payment Form - Image 080402)
    // =========================================================================
    private JPanel createExpensesCard() {
        JPanel card = createBaseCard("Expenses");
        JPanel mainGrid = new JPanel(new GridLayout(2, 1, 20, 20)); 
        mainGrid.setOpaque(false);

        Object[] billData = dao.getMyBills(currentApartmentId, currentRoom);

        // --- TOP ROW ---
        JPanel topRow = new JPanel(new GridLayout(1, 2, 20, 0)); topRow.setOpaque(false);
        
        // Top Left: Rent
        JPanel pnlRentWrapper = new JPanel(new BorderLayout()); pnlRentWrapper.setOpaque(false);
        pnlRentWrapper.add(createSubHeader("Payment Due"), BorderLayout.NORTH);
        JPanel rentBox = createContainerBox();
        rentBox.add(createLabel("Rent", 18, SwingConstants.LEFT, Font.BOLD));
        rentBox.add(createLabel((String)billData[1], 14, SwingConstants.LEFT, Font.PLAIN)); 
        rentBox.add(createLabel("₱ " + String.format("%,.2f", (Double)billData[0]), 42, SwingConstants.LEFT, Font.BOLD)); 
        pnlRentWrapper.add(rentBox, BorderLayout.CENTER);
        topRow.add(pnlRentWrapper);

        // Top Right: Utilities
        JPanel pnlUtilWrapper = new JPanel(new BorderLayout()); pnlUtilWrapper.setOpaque(false);
        pnlUtilWrapper.add(createSubHeader("Utilities"), BorderLayout.NORTH);
        JPanel utilBox = createContainerBox();
        utilBox.add(createMiniUtility("Electricity", (String)billData[3], (Double)billData[2]));
        utilBox.add(createMiniUtility("Water", (String)billData[5], (Double)billData[4]));
        utilBox.add(createMiniUtility("Internet", (String)billData[7], (Double)billData[6]));
        pnlUtilWrapper.add(utilBox, BorderLayout.CENTER);
        topRow.add(pnlUtilWrapper);

        // --- BOTTOM ROW ---
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0)); bottomRow.setOpaque(false);
        
        // Bottom Left: Payment Methods
        JPanel pnlMethodsWrapper = new JPanel(new BorderLayout()); pnlMethodsWrapper.setOpaque(false);
        pnlMethodsWrapper.add(createSubHeader("Payment Methods"), BorderLayout.NORTH);
        JPanel methodsBox = createContainerBox();
        // REMOVED THE DUMMY TEXT AND REPLACED WITH DYNAMIC DATABASE CALL
        String paymentDetailsHtml = dao.getOwnerPaymentDetails(currentApartmentId);
        methodsBox.add(createLabel(paymentDetailsHtml, 14, SwingConstants.LEFT, Font.PLAIN));
        pnlMethodsWrapper.add(methodsBox, BorderLayout.CENTER);
        bottomRow.add(pnlMethodsWrapper);

        // Bottom Right: Transaction Form
        JPanel pnlTransWrapper = new JPanel(new BorderLayout()); pnlTransWrapper.setOpaque(false);
        pnlTransWrapper.add(createSubHeader("Transaction"), BorderLayout.NORTH);
        JPanel transBox = createContainerBox();
        
        JTextField txtRoom = createDarkInput("Room Number");
        txtRoom.setText(currentRoom); txtRoom.setEditable(false);
        
        JComboBox<String> cmbMethod = new JComboBox<>(new String[]{"GCash", "Paymaya", "Cash Drop-off"});
        // --- BUG FIX: Force the Dropdown to respect Dark Mode ---
        cmbMethod.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        // --------------------------------------------------------
        cmbMethod.setBackground(COLOR_DARK_BOX); cmbMethod.setForeground(Color.WHITE);
        cmbMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JTextField txtDate = createDarkInput("Date");
        txtDate.setText(LocalDate.now().toString()); txtDate.setEditable(false);
        
        JTextField txtRef = createDarkInput("Reference No.");
        
        JButton btnSubmit = createActionButton("SUBMIT", COLOR_BTN_ACTION);
        if (isViewOnly) btnSubmit.setEnabled(false); // LOCKOUT
        btnSubmit.addActionListener(e -> {
            String ref = txtRef.getText().trim();
            if(dao.submitPayment(currentApartmentId, currentTenantId, currentRoom, cmbMethod.getSelectedItem().toString(), LocalDate.now().toString(), ref)) {
                JOptionPane.showMessageDialog(this, "Payment submitted for Owner verification!");
                txtRef.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        transBox.add(txtRoom); transBox.add(Box.createVerticalStrut(10));
        transBox.add(cmbMethod); transBox.add(Box.createVerticalStrut(10));
        transBox.add(txtDate); transBox.add(Box.createVerticalStrut(10));
        transBox.add(txtRef); transBox.add(Box.createVerticalStrut(15));
        transBox.add(btnSubmit);
        
        pnlTransWrapper.add(transBox, BorderLayout.CENTER);
        bottomRow.add(pnlTransWrapper);

        mainGrid.add(topRow);
        mainGrid.add(bottomRow);
        card.add(mainGrid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 3: MAINTENANCE (Image 0803e1)
    // =========================================================================
    private JPanel createMaintenanceCard() {
        JPanel card = createBaseCard("Maintenance");
        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0)); grid.setOpaque(false);

        // Left Col
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Maintenance"), BorderLayout.NORTH);
        JPanel pnlInput = createContainerBox();
        JTextArea area = new JTextArea(); 
        area.setBackground(COLOR_DARK_BOX); area.setForeground(Color.WHITE); area.setLineWrap(true);
        pnlInput.add(new JScrollPane(area));
        pnlInput.add(Box.createVerticalStrut(15));
        
        JButton btnSubmit = createActionButton("SUBMIT", COLOR_BTN_ACTION);
        btnSubmit.setAlignmentX(Component.RIGHT_ALIGNMENT);
        if (isViewOnly) btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> {
            if(!area.getText().trim().isEmpty() && dao.submitMaintenanceRequest(currentApartmentId, currentRoom, area.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Maintenance Request Sent!");
                refreshDashboard(); 
            }
        });
        pnlInput.add(btnSubmit);
        leftCol.add(pnlInput, BorderLayout.CENTER);

        // Right Col
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Maintenance Request"), BorderLayout.NORTH);
        JPanel pnlReq = createContainerBox();
        List<String> requests = dao.getMyMaintenanceRequests(currentApartmentId, currentRoom);
        if(requests.isEmpty()) pnlReq.add(createDarkBox("No maintenance requests found."));
        for(String req : requests) pnlReq.add(createDarkBox(req));
        
        rightCol.add(new JScrollPane(pnlReq), BorderLayout.CENTER);
        
        grid.add(leftCol); grid.add(rightCol);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 4: NOTIFICATION (Image 0803a7)
    // =========================================================================
    private JPanel createNotificationCard() {
        JPanel card = createBaseCard("Notification");
        JPanel container = createContainerBox();
        
        List<String> announcements = dao.getAnnouncements(currentApartmentId);
        if(announcements.isEmpty()) {
            container.add(createDarkBox("No new announcements from the owner."));
        } else {
            for(String ann : announcements) container.add(createDarkBox(ann));
        }
        
        card.add(new JScrollPane(container), BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 5: INQUIRY (Image 080097)
    // =========================================================================
    private JPanel createInquiryCard() {
        JPanel card = createBaseCard("Inquiry");
        JPanel pnlInq = new JPanel(new BorderLayout(0, 15)); pnlInq.setOpaque(false);
        pnlInq.add(createSubHeader("Complaints / Suggestions"), BorderLayout.NORTH);
        
        JPanel container = createContainerBox();
        JTextArea area = new JTextArea(); 
        area.setBackground(COLOR_DARK_BOX); area.setForeground(Color.WHITE); area.setLineWrap(true);
        container.add(new JScrollPane(area));
        container.add(Box.createVerticalStrut(15));
        
        JButton btnSubmit = createActionButton("SUBMIT", COLOR_BTN_ACTION);
        btnSubmit.setAlignmentX(Component.RIGHT_ALIGNMENT);
        if (isViewOnly) btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> {
            if(!area.getText().trim().isEmpty() && dao.submitComplaint(currentApartmentId, currentRoom, area.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Message sent to Owner.");
                area.setText("");
            }
        });
        container.add(btnSubmit);
        pnlInq.add(container, BorderLayout.CENTER);
        card.add(pnlInq, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 6: HISTORY (Image 080062)
    // =========================================================================
    private JPanel createHistoryCard() {
        JPanel card = createBaseCard("History");
        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 0)); mainGrid.setOpaque(false);

        // Left Col (Bills)
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Bill History"), BorderLayout.NORTH);
        JPanel pnlBills = createContainerBox();
        List<String> payments = dao.getMyBillHistory(currentTenantId);
        if(payments.isEmpty()) pnlBills.add(createDarkBox("No payment history."));
        for(String p : payments) pnlBills.add(createDarkBox(p));
        leftCol.add(new JScrollPane(pnlBills), BorderLayout.CENTER);

        // Right Col (Split: Notifications & Complaints)
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 20)); rightCol.setOpaque(false);
        
        // Top Right
        JPanel pnlNotifWrap = new JPanel(new BorderLayout()); pnlNotifWrap.setOpaque(false);
        pnlNotifWrap.add(createSubHeader("Notification History"), BorderLayout.NORTH);
        JPanel pnlNotif = createContainerBox();
        List<String> anns = dao.getAnnouncements(currentApartmentId);
        if(anns.isEmpty()) pnlNotif.add(createDarkBox("No history."));
        for(String a : anns) pnlNotif.add(createDarkBox(a));
        pnlNotifWrap.add(new JScrollPane(pnlNotif), BorderLayout.CENTER);
        
        // Bottom Right
        JPanel pnlCompWrap = new JPanel(new BorderLayout()); pnlCompWrap.setOpaque(false);
        pnlCompWrap.add(createSubHeader("Complaints/Suggestions History"), BorderLayout.NORTH);
        JPanel pnlComp = createContainerBox();
        List<String> msgs = dao.getMyComplaintsHistory(currentApartmentId, currentRoom);
        if(msgs.isEmpty()) pnlComp.add(createDarkBox("No history."));
        for(String m : msgs) pnlComp.add(createDarkBox(m));
        pnlCompWrap.add(new JScrollPane(pnlComp), BorderLayout.CENTER);

        rightCol.add(pnlNotifWrap);
        rightCol.add(pnlCompWrap);

        mainGrid.add(leftCol);
        mainGrid.add(rightCol);
        card.add(mainGrid, BorderLayout.CENTER);
        return card;
    }

    // ======================================================================================
    // HELPER METHODS FOR UI CONSTRUCTION
    // ======================================================================================

    private void refreshDashboard() {
        new TenantDashboard(currentTenantId).setVisible(true);
        this.dispose();
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 20)); card.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        
        header.add(createLabel(title, 36, SwingConstants.LEFT, Font.BOLD), BorderLayout.WEST);
        
        // Tenant Name matches the "Catriona Gray" design in top right
        JPanel rightInfo = new JPanel(new BorderLayout()); rightInfo.setOpaque(false);
        if (isViewOnly) {
            JLabel warning = createLabel("⚠ APARTMENT SUSPENDED ", 14, SwingConstants.RIGHT, Font.BOLD);
            warning.setForeground(new Color(255, 102, 102));
            rightInfo.add(warning, BorderLayout.CENTER);
        }
        rightInfo.add(createLabel(tenantName, 16, SwingConstants.RIGHT, Font.PLAIN), BorderLayout.EAST);
        
        header.add(rightInfo, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JLabel createSubHeader(String text) {
        JLabel lbl = createLabel(text, 18, SwingConstants.LEFT, Font.BOLD);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
        return lbl;
    }

    private JPanel createContainerBox() {
        JPanel c = new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(COLOR_CONTAINER); 
        c.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        return c;
    }
    
    private JPanel createDarkBox(String text) {
        JPanel p = new JPanel(new BorderLayout()); 
        p.setBackground(COLOR_DARK_BOX); 
        p.setBorder(new EmptyBorder(15,15,15,15));
        
        JTextArea lbl = new JTextArea(text);
        lbl.setWrapStyleWord(true); lbl.setLineWrap(true);
        lbl.setOpaque(false); lbl.setEditable(false);
        lbl.setForeground(Color.WHITE); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl, BorderLayout.CENTER);
        
        JPanel wrapper = new JPanel(new BorderLayout()); 
        wrapper.setOpaque(false); wrapper.setBorder(new EmptyBorder(0,0,10,0)); 
        wrapper.add(p); 
        return wrapper;
    }
    
    private JTextField createDarkInput(String placeholder) {
        JTextField t = new JTextField(placeholder);
        t.setBackground(COLOR_DARK_BOX); t.setForeground(Color.WHITE);
        t.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        t.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) { if (t.getText().equals(placeholder)) t.setText(""); }
            public void focusLost(java.awt.event.FocusEvent evt) { if (t.getText().isEmpty()) t.setText(placeholder); }
        });
        return t;
    }

    private JLabel createLabel(String text, int size, int align, int style) {
        JLabel l = new JLabel(text); 
        l.setForeground(COLOR_TEXT); 
        l.setFont(new Font("Segoe UI", style, size));
        l.setHorizontalAlignment(align); 
        return l;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text); 
        
        // --- THE BUG FIX ---
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setBorderPainted(false);
        // -------------------
        
        b.setBackground(bg); 
        b.setForeground(Color.WHITE); 
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        return b;
    }

    private JPanel createMiniUtility(String name, String date, double amt) {
        JPanel p = new JPanel(new BorderLayout()); 
        p.setOpaque(false); 
        p.setBorder(new EmptyBorder(5,0,10,0));
        
        // --- BUG FIX: Used <html> and <br> tags so the date drops to the second line! ---
        p.add(createLabel("<html>" + name + "<br>" + date + "</html>", 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        
        String displayAmt = (amt == 0.0 && date.equals("N/A")) ? "₱ 0" : "₱ " + String.format("%,.2f", amt);
        p.add(createLabel(displayAmt, 24, SwingConstants.LEFT, Font.BOLD), BorderLayout.CENTER); 
        return p;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()); 
        sidebar.setPreferredSize(new Dimension(250, 0)); 
        sidebar.setBackground(COLOR_SIDEBAR);
        
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setBackground(COLOR_SIDEBAR);
        java.net.URL logoUrl = getClass().getResource("/logowhite.png");
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
        
        // --- Updated Nav Names to match Design ---
        btnSummary = createNavButton("Tenant Dashboard"); 
        btnExpenses = createNavButton("Expenses");
        btnMaintenance = createNavButton("Maintainance"); // Matched spelling on design
        btnNotification = createNavButton("Notification"); 
        btnInquiry = createNavButton("Inquiry"); 
        btnHistory = createNavButton("History");
        
        navButtons = new JButton[]{btnSummary, btnExpenses, btnMaintenance, btnNotification, btnInquiry, btnHistory};
        for (JButton btn : navButtons) { 
            navPanel.add(btn); 
            navPanel.add(Box.createVerticalStrut(5)); 
        }
        
        btnLogout = createNavButton("Log Out");
        btnLogout.setForeground(new Color(255, 100, 100)); 
        
        sidebar.add(navPanel, BorderLayout.CENTER); 
        sidebar.add(btnLogout, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text); 
        
        // --- THE BUG FIX: Forces the button to be flat and accept background colors ---
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI()); 
        btn.setBorderPainted(false); 
        // ----------------------------------------------------------------------------
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
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
        for (JButton btn : navButtons) btn.setBackground(COLOR_SIDEBAR);
        
        if (source == btnLogout) {
            this.dispose();
            new main.LandingPage().setVisible(true);
            return;
        }

        if (source instanceof JButton) ((JButton) source).setBackground(COLOR_CONTAINER);

        if (source == btnSummary) cardLayout.show(cardsContainer, "Tenant Dashboard");
        else if (source == btnExpenses) cardLayout.show(cardsContainer, "Expenses");
        else if (source == btnMaintenance) cardLayout.show(cardsContainer, "Maintenance");
        else if (source == btnNotification) cardLayout.show(cardsContainer, "Notification");
        else if (source == btnInquiry) cardLayout.show(cardsContainer, "Inquiry");
        else if (source == btnHistory) cardLayout.show(cardsContainer, "History");
    }
    
    // ======================================================================================
    // MAIN METHOD FOR INDEPENDENT TESTING
    // ======================================================================================
    public static void main(String[] args) {
        try {
            // This sets the UI to look like a modern Windows/Mac app instead of the old Java default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // We pass '1' as a dummy tenantId just to force the window to open
            new TenantDashboard(1).setVisible(true);
        });
    }
}