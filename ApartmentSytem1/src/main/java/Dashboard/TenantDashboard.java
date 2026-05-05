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

    // --- Core Theme Colors ---
    private final Color COLOR_SIDEBAR = new Color(0, 25, 15);      
    private final Color COLOR_MAIN_BG = new Color(0, 35, 20);      
    private final Color COLOR_CONTAINER = new Color(0, 102, 51);   
    private final Color COLOR_LIST_ITEM = new Color(0, 25, 15);    
    private final Color COLOR_BTN_ACTION = new Color(0, 204, 102); 
    private final Color COLOR_TEXT = Color.WHITE;

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    private JButton btnDash, btnMaintenance, btnNotification, btnInquiry, btnHistory, btnLogout;
    private JButton[] navButtons;

    // --- DATABASE VARIABLES ---
    private TenantDashboardDAO dao = new TenantDashboardDAO();
    private int currentTenantId; 
    private int currentApartmentId = -1; 
    private String currentRoom = "N/A"; 
    private String tenantName = "Tenant";

    public TenantDashboard(int tenantId) {
        this.currentTenantId = tenantId;
        
        // ---------------------------------------------------------
        // 1. AUTO-FETCH TENANT'S ROOM AND APARTMENT INFO
        // ---------------------------------------------------------
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT t.name, r.apartment_id, r.room_number FROM registered_tenants t " +
                 "LEFT JOIN room_occupancy r ON t.tenant_id = r.tenant_id AND r.status = 'Current' " +
                 "WHERE t.tenant_id = ?")) {
            ps.setInt(1, tenantId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                this.tenantName = rs.getString("name");
                if (rs.getObject("apartment_id") != null) {
                    this.currentApartmentId = rs.getInt("apartment_id");
                    this.currentRoom = rs.getString("room_number");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setTitle("Tenant Dashboard - " + tenantName + " (Room " + currentRoom + ")");
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
        cardsContainer.add(createDashboardCard(), "Dashboard");
        cardsContainer.add(createMaintenanceCard(), "Maintenance");
        cardsContainer.add(createNotificationCard(), "Notification");
        cardsContainer.add(createInquiryCard(), "Inquiry");
        cardsContainer.add(createHistoryCard(), "History");

        this.add(cardsContainer, BorderLayout.CENTER);
    }

    // =========================================================================
    // PANEL 1: DASHBOARD & BILLS (Wires to getMyBills & submitPayment)
    // =========================================================================
    private JPanel createDashboardCard() {
        JPanel card = createBaseCard("My Bills & Payments");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); 
        grid.setOpaque(false);

        // Fetch Real Data from DAO
        Object[] billData = dao.getMyBills(currentApartmentId, currentRoom);
        
        JPanel leftCol = new JPanel(); 
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS)); 
        leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Current Charges"));
        
        JPanel rentBox = createContainerBox();
        rentBox.add(createLabel("Rent Amount", 20, SwingConstants.LEFT, Font.BOLD));
        rentBox.add(createLabel("Due: " + billData[1], 14, SwingConstants.LEFT, Font.PLAIN)); 
        rentBox.add(createLabel("₱ " + String.format("%,.2f", (Double)billData[0]), 45, SwingConstants.LEFT, Font.BOLD)); 
        leftCol.add(rentBox);
        leftCol.add(Box.createVerticalStrut(20));

        JPanel utilBox = createContainerBox();
        utilBox.add(createLabel("Utilities Breakdown", 18, SwingConstants.LEFT, Font.BOLD));
        utilBox.add(Box.createVerticalStrut(10));
        utilBox.add(createMiniUtility("Electricity", (String)billData[3], (Double)billData[2]));
        utilBox.add(createMiniUtility("Water", (String)billData[5], (Double)billData[4]));
        utilBox.add(createMiniUtility("Internet", (String)billData[7], (Double)billData[6]));
        leftCol.add(utilBox);

        // Right Column: Payment Action
        JPanel rightCol = new JPanel(new BorderLayout()); 
        rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Make a Payment"), BorderLayout.NORTH);
        
        JPanel payBox = createContainerBox();
        payBox.add(createLabel("Submit your payment reference to the owner.", 14, SwingConstants.LEFT, Font.PLAIN));
        payBox.add(Box.createVerticalStrut(20));
        
        String[] methods = {"GCash", "Maya", "Cash Drop-off"};
        JComboBox<String> cmbMethod = new JComboBox<>(methods);
        cmbMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        payBox.add(cmbMethod);
        payBox.add(Box.createVerticalStrut(10));
        
        JTextField txtRef = new JTextField("Reference Number (If GCash/Maya)");
        txtRef.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        payBox.add(txtRef);
        payBox.add(Box.createVerticalStrut(20));

        JButton btnPay = createActionButton("SUBMIT PAYMENT", COLOR_BTN_ACTION);
        btnPay.addActionListener(e -> {
            String ref = txtRef.getText().trim();
            if(dao.submitPayment(currentApartmentId, currentTenantId, currentRoom, cmbMethod.getSelectedItem().toString(), LocalDate.now().toString(), ref)) {
                JOptionPane.showMessageDialog(this, "Payment submitted for Owner verification!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtRef.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        payBox.add(btnPay);
        
        rightCol.add(payBox, BorderLayout.CENTER);

        grid.add(leftCol);
        grid.add(rightCol);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 2: MAINTENANCE (Wires to submitMaintenanceRequest & getMyMaintenanceRequests)
    // =========================================================================
    private JPanel createMaintenanceCard() {
        JPanel card = createBaseCard("Maintenance");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);

        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Report an Issue"), BorderLayout.NORTH);
        
        JPanel pnlInput = createContainerBox();
        JTextArea area = new JTextArea(); 
        area.setBackground(COLOR_LIST_ITEM); 
        area.setForeground(Color.WHITE);
        area.setLineWrap(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlInput.add(new JScrollPane(area));
        pnlInput.add(Box.createVerticalStrut(15));
        
        JButton btnSubmit = createActionButton("SUBMIT REQUEST", COLOR_BTN_ACTION);
        btnSubmit.addActionListener(e -> {
            String issue = area.getText().trim();
            if(!issue.isEmpty()){
                if(dao.submitMaintenanceRequest(currentApartmentId, currentRoom, issue)) {
                    JOptionPane.showMessageDialog(this, "Maintenance Request Sent!");
                    area.setText("");
                    refreshDashboard(); // Refresh to show new request
                }
            }
        });
        pnlInput.add(btnSubmit);
        leftCol.add(pnlInput, BorderLayout.CENTER);

        // Right side: Request History
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("My Recent Requests"), BorderLayout.NORTH);
        
        JPanel pnlReq = createContainerBox();
        List<String> requests = dao.getMyMaintenanceRequests(currentApartmentId, currentRoom);
        if(requests.isEmpty()) pnlReq.add(createLabel("No maintenance requests found.", 14, SwingConstants.LEFT, Font.ITALIC));
        for(String req : requests) pnlReq.add(createSimpleListBlock(req));
        
        JScrollPane scroll = new JScrollPane(pnlReq);
        scroll.setBorder(null);
        rightCol.add(scroll, BorderLayout.CENTER);
        
        grid.add(leftCol); grid.add(rightCol);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 3: NOTIFICATIONS (Wires to getAnnouncements)
    // =========================================================================
    private JPanel createNotificationCard() {
        JPanel card = createBaseCard("Announcements");
        JPanel container = createContainerBox();
        
        List<String> announcements = dao.getAnnouncements(currentApartmentId);
        if(announcements.isEmpty()) {
            container.add(createLabel("No new announcements from the owner.", 16, SwingConstants.CENTER, Font.PLAIN));
        } else {
            for(String ann : announcements) {
                container.add(createSimpleListBlock(ann));
            }
        }
        
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 4: INQUIRY/COMPLAINTS (Wires to submitComplaint)
    // =========================================================================
    private JPanel createInquiryCard() {
        JPanel card = createBaseCard("Inquiry & Feedback");
        JPanel pnlInq = new JPanel(new BorderLayout(0, 15)); pnlInq.setOpaque(false);
        pnlInq.add(createLabel("Send a message, suggestion, or complaint directly to the Owner.", 16, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        
        JPanel container = createContainerBox();
        JTextArea area = new JTextArea(); 
        area.setBackground(COLOR_LIST_ITEM); 
        area.setForeground(Color.WHITE);
        area.setLineWrap(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        container.add(new JScrollPane(area));
        container.add(Box.createVerticalStrut(15));
        
        JButton btnSubmit = createActionButton("SEND TO OWNER", COLOR_BTN_ACTION);
        btnSubmit.addActionListener(e -> {
            String msg = area.getText().trim();
            if(!msg.isEmpty()) {
                if(dao.submitComplaint(currentApartmentId, currentRoom, msg)) {
                    JOptionPane.showMessageDialog(this, "Message sent securely to the Owner.");
                    area.setText("");
                }
            }
        });
        container.add(btnSubmit);
        pnlInq.add(container, BorderLayout.CENTER);
        card.add(pnlInq, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // PANEL 5: HISTORY (Wires to getMyBillHistory & getMyComplaintsHistory)
    // =========================================================================
    private JPanel createHistoryCard() {
        JPanel card = createBaseCard("My History");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);

        // Payment History
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Payment History"), BorderLayout.NORTH);
        JPanel pnlPayments = createContainerBox();
        List<String> payments = dao.getMyBillHistory(currentTenantId);
        if(payments.isEmpty()) pnlPayments.add(createLabel("No payment history.", 14, SwingConstants.LEFT, Font.ITALIC));
        for(String p : payments) pnlPayments.add(createSimpleListBlock(p));
        leftCol.add(new JScrollPane(pnlPayments), BorderLayout.CENTER);

        // Message History
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Sent Messages"), BorderLayout.NORTH);
        JPanel pnlMsgs = createContainerBox();
        List<String> msgs = dao.getMyComplaintsHistory(currentApartmentId, currentRoom);
        if(msgs.isEmpty()) pnlMsgs.add(createLabel("No message history.", 14, SwingConstants.LEFT, Font.ITALIC));
        for(String m : msgs) pnlMsgs.add(createSimpleListBlock(m));
        rightCol.add(new JScrollPane(pnlMsgs), BorderLayout.CENTER);

        grid.add(leftCol); grid.add(rightCol);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // ======================================================================================
    // HELPER METHODS FOR UI CONSTRUCTION
    // ======================================================================================

    private void refreshDashboard() {
        // Simple trick to refresh the UI by rebuilding it
        new TenantDashboard(currentTenantId).setVisible(true);
        this.dispose();
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 20)); card.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        header.add(createLabel(title, 36, SwingConstants.LEFT, Font.BOLD), BorderLayout.WEST);
        
        JPanel userInfo = new JPanel(new GridLayout(2,1)); userInfo.setOpaque(false);
        userInfo.add(createLabel(tenantName, 18, SwingConstants.RIGHT, Font.BOLD));
        userInfo.add(createLabel("Room: " + currentRoom, 14, SwingConstants.RIGHT, Font.PLAIN));
        header.add(userInfo, BorderLayout.EAST);
        
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JLabel createSubHeader(String text) {
        JLabel lbl = createLabel(text, 22, SwingConstants.LEFT, Font.BOLD);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
        return lbl;
    }

    private JPanel createContainerBox() {
        JPanel c = new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(COLOR_CONTAINER); 
        c.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        return c;
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
        p.add(createLabel(name + " (Due: " + date + ")", 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        p.add(createLabel("₱ " + String.format("%,.2f", amt), 24, SwingConstants.LEFT, Font.BOLD), BorderLayout.CENTER); 
        return p;
    }

    private JPanel createSimpleListBlock(String text) {
        JPanel p = new JPanel(new BorderLayout()); 
        p.setBackground(COLOR_LIST_ITEM); 
        p.setBorder(new EmptyBorder(15,15,15,15));
        
        JTextArea lbl = new JTextArea(text);
        lbl.setWrapStyleWord(true);
        lbl.setLineWrap(true);
        lbl.setOpaque(false);
        lbl.setEditable(false);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl, BorderLayout.CENTER);
        
        JPanel wrapper = new JPanel(new BorderLayout()); 
        wrapper.setOpaque(false); 
        wrapper.setBorder(new EmptyBorder(0,0,10,0)); 
        wrapper.add(p); 
        return wrapper;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()); 
        sidebar.setPreferredSize(new Dimension(250, 0)); 
        sidebar.setBackground(COLOR_SIDEBAR);
        
        JPanel navPanel = new JPanel(); 
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS)); 
        navPanel.setBackground(COLOR_SIDEBAR);
        navPanel.add(Box.createVerticalStrut(30)); // Top padding
        
        btnDash = createNavButton("Dashboard"); 
        btnMaintenance = createNavButton("Maintenance");
        btnNotification = createNavButton("Announcements"); 
        btnInquiry = createNavButton("Inquiry"); 
        btnHistory = createNavButton("History");
        
        navButtons = new JButton[]{btnDash, btnMaintenance, btnNotification, btnInquiry, btnHistory};
        for (JButton btn : navButtons) { 
            navPanel.add(btn); 
            navPanel.add(Box.createVerticalStrut(5)); 
        }
        
        btnLogout = createNavButton("Log Out");
        btnLogout.setForeground(new Color(255, 100, 100)); // Red text for logout
        
        sidebar.add(navPanel, BorderLayout.CENTER); 
        sidebar.add(btnLogout, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text); 
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
        
        // Reset all buttons to default color
        for (JButton btn : navButtons) {
            btn.setBackground(COLOR_SIDEBAR);
        }
        
        if (source == btnLogout) {
            this.dispose();
            new main.LandingPage().setVisible(true);
            return;
        }

        // Highlight selected and switch card
        if (source instanceof JButton) {
            ((JButton) source).setBackground(COLOR_CONTAINER);
        }

        if (source == btnDash) cardLayout.show(cardsContainer, "Dashboard");
        else if (source == btnMaintenance) cardLayout.show(cardsContainer, "Maintenance");
        else if (source == btnNotification) cardLayout.show(cardsContainer, "Notification");
        else if (source == btnInquiry) cardLayout.show(cardsContainer, "Inquiry");
        else if (source == btnHistory) cardLayout.show(cardsContainer, "History");
    }

    public static void main(String[] args) {
        // Passing '1' as a test Tenant ID so you can run this file directly during development
        SwingUtilities.invokeLater(() -> new TenantDashboard(1).setVisible(true));
    }
}