package Dashboard;

import com.mycompany.apartmentsytem1.OwnerDashboardDAO;
import com.mycompany.apartmentsytem1.FinanceService;
import com.mycompany.apartmentsytem1.OwnerDAO;
import com.mycompany.apartmentsytem1.ViewingDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

public class OwnerDashboard extends JFrame implements ActionListener {

    private int currentOwnerId;
    private int currentApartmentId; 
    private String currentApartmentName = "My Apartment"; 
    private boolean isViewOnly = false; // Lockout Flag
    
    private OwnerDashboardDAO ownerDao = new OwnerDashboardDAO();
    
    // --- Core Theme Colors ---
    private final Color COLOR_SIDEBAR = new Color(0, 25, 15);      
    private final Color COLOR_MAIN_BG = new Color(0, 35, 20);      
    private final Color COLOR_CONTAINER = new Color(0, 102, 51);   
    private final Color COLOR_LIST_ITEM = new Color(0, 25, 15);    
    private final Color COLOR_BTN_ACTIVE = new Color(0, 102, 51);  
    private final Color COLOR_BTN_ACTION = new Color(0, 204, 102); 
    private final Color COLOR_TEXT = Color.WHITE;

    private CardLayout cardLayout;
    private JPanel cardsContainer;
    
    private JButton btnAptDash, btnProfit, btnExpenses, btnRooms, btnToDo, btnHistory, btnInquiries, btnTenants, btnAccount, btnLogout;
    private JButton[] navButtons;
    
    // --- TRACKING VARIABLES FOR ROOMS TABLE ---
    private List<String> roomNumbersList = new java.util.ArrayList<>();
    private List<JTextField> rentFields = new java.util.ArrayList<>();
    private List<JTextField> elecFields = new java.util.ArrayList<>();
    private List<JTextField> waterFields = new java.util.ArrayList<>();
    private List<JTextField> netFields = new java.util.ArrayList<>();

    public OwnerDashboard(int ownerId) {
        this.currentOwnerId = ownerId; 
        this.currentApartmentId = ownerDao.getApartmentIdByOwner(ownerId); 
        
        // Fetch the actual Apartment Name
        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT apartment_name FROM apartments WHERE apartment_id = ?")) {
            ps.setInt(1, currentApartmentId);
            java.sql.ResultSet rs = ps.executeQuery();
            if(rs.next()) this.currentApartmentName = rs.getString("apartment_name");
        } catch (Exception ex) {}

        // ENFORCE LOCKOUT IF UNPAID
        this.isViewOnly = !ownerDao.isApartmentActive(currentApartmentId);

        this.setTitle("Apartment Dashboard - Owner ID: " + ownerId + " | Apt ID: " + currentApartmentId);
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

        cardsContainer.add(createAptDashboardCard(), "AptDash");
        cardsContainer.add(createProfitCard(), "Profit");
        cardsContainer.add(createExpensesCard(), "Expenses");
        cardsContainer.add(createRoomsCard(), "Rooms");
        cardsContainer.add(createToDoCard(), "ToDo");
        cardsContainer.add(createHistoryCard(), "History");
        cardsContainer.add(createInquiriesCard(), "Inquiries");
        cardsContainer.add(createTenantsCard(), "Tenants");
        cardsContainer.add(createAccountCard(), "Account");

        this.add(cardsContainer, BorderLayout.CENTER);

        activateButton(btnAptDash);
        cardLayout.show(cardsContainer, "AptDash");
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
        
        btnAptDash = createNavButton("Apartment Dashboard");
        btnProfit = createNavButton("Profit");
        btnExpenses = createNavButton("Expenses");
        btnRooms = createNavButton("Rooms");
        btnToDo = createNavButton("To Do's");
        btnHistory = createNavButton("History");
        btnInquiries = createNavButton("Inquiries");
        btnTenants = createNavButton("Tenants");
        btnAccount = createNavButton("Account");

        navButtons = new JButton[]{btnAptDash, btnProfit, btnExpenses, btnRooms, btnToDo, btnHistory, btnInquiries, btnTenants, btnAccount};

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
            new main.LandingPage().setVisible(true);
            return;
        }

        for (JButton btn : navButtons) btn.setBackground(COLOR_SIDEBAR); 

        if (source == btnAptDash) { activateButton(btnAptDash); cardLayout.show(cardsContainer, "AptDash"); }
        else if (source == btnProfit) { activateButton(btnProfit); cardLayout.show(cardsContainer, "Profit"); }
        else if (source == btnExpenses) { activateButton(btnExpenses); cardLayout.show(cardsContainer, "Expenses"); }
        else if (source == btnRooms) { activateButton(btnRooms); cardLayout.show(cardsContainer, "Rooms"); }
        else if (source == btnToDo) { activateButton(btnToDo); cardLayout.show(cardsContainer, "ToDo"); }
        else if (source == btnHistory) { activateButton(btnHistory); cardLayout.show(cardsContainer, "History"); }
        else if (source == btnInquiries) { activateButton(btnInquiries); cardLayout.show(cardsContainer, "Inquiries"); }
        else if (source == btnTenants) { activateButton(btnTenants); cardLayout.show(cardsContainer, "Tenants"); }
        else if (source == btnAccount) { activateButton(btnAccount); cardLayout.show(cardsContainer, "Account"); }
    }

    private void activateButton(JButton btn) { btn.setBackground(COLOR_BTN_ACTIVE); }

    // =========================================================================
    // NEW EXPENSES PANEL (Platform Fee & Payment Form)
    // =========================================================================

    private JPanel createExpensesCard() {
        JPanel card = createBaseCard("Expenses");
        
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
        grid.setOpaque(false);

        // --- LEFT COLUMN (Room Utilities Summation) ---
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Room Utilities Summation"), BorderLayout.NORTH);
        
        JPanel utilitiesBox = createContainerBox(); 
        utilitiesBox.setLayout(new GridLayout(3, 1));
        
        double[] exps = ownerDao.getExpensesSummation(currentApartmentId);
        utilitiesBox.add(createUtilityBlock("Electricity", "₱ " + String.format("%,.2f", exps[0]))); 
        utilitiesBox.add(createUtilityBlock("Water", "₱ " + String.format("%,.2f", exps[1]))); 
        utilitiesBox.add(createUtilityBlock("Internet", "₱ " + String.format("%,.2f", exps[2])));
        
        leftCol.add(utilitiesBox, BorderLayout.CENTER);
        grid.add(leftCol);

        // --- RIGHT COLUMN (CardLayout for Table vs Form) ---
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Platform Service Fee"), BorderLayout.NORTH);
        
        CardLayout rightCardLayout = new CardLayout();
        JPanel rightContentPanel = new JPanel(rightCardLayout);
        rightContentPanel.setOpaque(false);

        // Add both views to the right side
        rightContentPanel.add(createFeeTablePanel(rightCardLayout, rightContentPanel), "TABLE");
        rightContentPanel.add(createPaymentFormPanel(rightCardLayout, rightContentPanel), "FORM");

        rightCol.add(rightContentPanel, BorderLayout.CENTER);
        grid.add(rightCol);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // Right Side: View 1 (The Fee Table)
    private JPanel createFeeTablePanel(CardLayout layout, JPanel parent) {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        
        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
        tableContainer.setOpaque(false);

        // Header Row
        JPanel headerRow = new JPanel(new GridLayout(1, 3, 5, 0)); headerRow.setOpaque(false);
        headerRow.add(createFeeCell("ROOMS", true));
        headerRow.add(createFeeCell("RENTS", true));
        headerRow.add(createFeeCell("SERVICE FEE", true));
        tableContainer.add(headerRow);
        tableContainer.add(Box.createVerticalStrut(5));

        // Data Rows
        double totalFee = 0.0;
        List<String[]> activeRooms = ownerDao.getActiveRoomsForServiceFee(currentApartmentId);
        
        if (activeRooms.isEmpty()) {
            JLabel empty = createLabel("No active rooms.", 16, SwingConstants.CENTER);
            empty.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
            tableContainer.add(empty);
        } else {
            for (String[] room : activeRooms) {
                JPanel row = new JPanel(new GridLayout(1, 3, 5, 0)); row.setOpaque(false);
                row.add(createFeeCell("ROOM " + room[0].replace("Room ", ""), false));
                row.add(createFeeCell(String.format("%,.2f", Double.parseDouble(room[1])), false));
                row.add(createFeeCell(String.format("%,.2f", Double.parseDouble(room[2])), false));
                tableContainer.add(row);
                tableContainer.add(Box.createVerticalStrut(5));
                totalFee += Double.parseDouble(room[2]);
            }
        }

        // Total Row
        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); totalRow.setOpaque(false);
        totalRow.add(createLabel("TOTAL", 18, SwingConstants.RIGHT, Font.BOLD));
        totalRow.add(Box.createHorizontalStrut(10));
        JPanel totalCell = createFeeCell(String.format("%,.2f", totalFee), false);
        totalCell.setPreferredSize(new Dimension(150, 40));
        totalRow.add(totalCell);
        tableContainer.add(Box.createVerticalStrut(15));
        tableContainer.add(totalRow);

        panel.add(tableContainer, BorderLayout.NORTH);

        // Pay Button
        JButton btnPay = createActionButton("PAY", COLOR_BTN_ACTION);
        btnPay.setPreferredSize(new Dimension(150, 40));
        btnPay.addActionListener(e -> layout.show(parent, "FORM")); // Swap to Form View
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlBottom.setOpaque(false);
        pnlBottom.add(btnPay);
        panel.add(pnlBottom, BorderLayout.SOUTH);

        return panel;
    }

    // Helper for Table Cells
    private JPanel createFeeCell(String text, boolean isHeader) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isHeader ? new Color(0, 153, 76) : COLOR_CONTAINER);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cell.add(createLabel(text, 16, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER);
        return cell;
    }

    // Right Side: View 2 (The Payment Form)
    private JPanel createPaymentFormPanel(CardLayout layout, JPanel parent) {
        JPanel panel = createContainerBox();
        panel.setLayout(new BorderLayout());

        JPanel innerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        innerGrid.setOpaque(false);

        // Left Side: Dummy Methods
        JPanel pnlMethods = new JPanel(); pnlMethods.setLayout(new BoxLayout(pnlMethods, BoxLayout.Y_AXIS)); pnlMethods.setOpaque(false);
        pnlMethods.add(createLabel("Payment Methods", 18, SwingConstants.LEFT, Font.BOLD));
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createLabel("GCash", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("0978 563 1928", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("(Sara Duterte)", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createLabel("Paymaya", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("0978 563 1928", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("(Leni Robredo)", 16, SwingConstants.LEFT, Font.PLAIN));
        innerGrid.add(pnlMethods);

        // Right Side: Inputs
        JPanel pnlInputs = new JPanel(); pnlInputs.setLayout(new BoxLayout(pnlInputs, BoxLayout.Y_AXIS)); pnlInputs.setOpaque(false);
        
        JTextField txtTin = createDarkTextField("TIN Number");
        JTextField txtMethod = createDarkTextField("Payment Method Used");
        JTextField txtDate = createDarkTextField("Date (YYYY-MM-DD)");
        JTextField txtRef = createDarkTextField("Reference No.");
        
        pnlInputs.add(txtTin); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtMethod); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtDate); pnlInputs.add(Box.createVerticalStrut(10));
        pnlInputs.add(txtRef);
        innerGrid.add(pnlInputs);

        panel.add(innerGrid, BorderLayout.CENTER);

        // Submit Button
        JButton btnSubmit = createActionButton("SUBMIT", COLOR_BTN_ACTION);
        btnSubmit.addActionListener(e -> {
            if (ownerDao.submitPlatformFeePayment(currentApartmentId, txtTin.getText(), txtMethod.getText(), txtDate.getText(), txtRef.getText())) {
                JOptionPane.showMessageDialog(this, "Payment Submitted! Pending Super Admin Verification.", "Success", JOptionPane.INFORMATION_MESSAGE);
                layout.show(parent, "TABLE"); // Send back to table view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlBottom.setOpaque(false);
        pnlBottom.add(btnSubmit);
        panel.add(pnlBottom, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField createDarkTextField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setBackground(new Color(0, 51, 26)); 
        txt.setForeground(Color.WHITE);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 51, 26)), 
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txt.setText(placeholder);
        
        // Simple placeholder clear
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txt.getText().isEmpty()) { txt.setText(placeholder); }
            }
        });
        return txt;
    }

    private JPanel createUtilityBlock(String title, String amount) {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setOpaque(false);
        pnl.add(createLabel(title, 24, SwingConstants.LEFT), BorderLayout.NORTH);
        pnl.add(createLabel(amount, 50, SwingConstants.LEFT, Font.BOLD), BorderLayout.CENTER);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); return pnl;
    }

    // =========================================================================
    // REMAINING CARDS & HELPERS (Untouched except for Lockout Enforcement)
    // =========================================================================
    
    private JPanel createAptDashboardCard() {
        JPanel card = createBaseCard("Apartment Dashboard");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);
        
        JPanel leftCol = new JPanel(); leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS)); leftCol.setOpaque(false);
        
        leftCol.add(createSubHeader("To Do's"));
        JPanel pnlToDo = createContainerBox();
        List<String[]> activeReqs = ownerDao.getPendingMaintenance(currentApartmentId);
        if(activeReqs.isEmpty()) pnlToDo.add(createLabel("No pending to-dos.", 14, SwingConstants.LEFT));
        for(String[] req : activeReqs) pnlToDo.add(createListItem("Room " + req[1], req[2], null, null, 80)); 
        leftCol.add(new JScrollPane(pnlToDo));
        leftCol.add(Box.createVerticalStrut(20));
        
        leftCol.add(createSubHeader("Expenses"));
        JPanel pnlExpenses = createContainerBox(); pnlExpenses.setLayout(new BorderLayout());
        double[] exps = ownerDao.getExpensesSummation(currentApartmentId);
        double totalExp = exps[0] + exps[1] + exps[2];
        pnlExpenses.add(createLabel("Combined Utilities", 16, SwingConstants.LEFT), BorderLayout.NORTH);
        pnlExpenses.add(createLabel("₱ " + String.format("%,.2f", totalExp), 60, SwingConstants.LEFT, Font.BOLD), BorderLayout.CENTER);
        leftCol.add(pnlExpenses);
        
        JPanel rightCol = new JPanel(); rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS)); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Complaints & Suggestions"));
        JPanel pnlComplaints = createContainerBox();
        List<String[]> comps = ownerDao.getRecentComplaints(currentApartmentId);
        if(comps.isEmpty()) pnlComplaints.add(createLabel("No complaints.", 14, SwingConstants.LEFT));
        for(String[] c : comps) pnlComplaints.add(createListItem("Room " + c[0], c[1], c[2], null, 100));
        rightCol.add(new JScrollPane(pnlComplaints));
        rightCol.add(Box.createVerticalStrut(20));
        
        rightCol.add(createSubHeader("Rooms"));
        JPanel pnlRooms = createContainerBox(); pnlRooms.setLayout(new GridLayout(2, 3));
        int[] occStats = ownerDao.getRoomOccupancyStats(currentApartmentId);
        pnlRooms.add(createLabel("Occupied", 16, SwingConstants.CENTER)); pnlRooms.add(createLabel("Vacant", 16, SwingConstants.CENTER)); pnlRooms.add(createLabel("Total", 16, SwingConstants.CENTER));
        pnlRooms.add(createLabel(String.valueOf(occStats[0]), 70, SwingConstants.CENTER, Font.BOLD)); pnlRooms.add(createLabel(String.valueOf(occStats[1]), 70, SwingConstants.CENTER, Font.BOLD)); pnlRooms.add(createLabel(String.valueOf(occStats[2]), 70, SwingConstants.CENTER, Font.BOLD));
        rightCol.add(pnlRooms);

        grid.add(leftCol); grid.add(rightCol); card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProfitCard() {
        JPanel card = createBaseCard("Profit");
        FinanceService finance = new FinanceService();
        String currMonth = LocalDate.now().toString().substring(0, 7);
        String currYear = LocalDate.now().toString().substring(0, 4);
        
        FinanceService.MonthlyReport monthly = finance.getMonthlyReport(currentApartmentId, currMonth);
        FinanceService.AnnualReport annual = finance.getAnnualReport(currentApartmentId, currYear);

        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);
        JPanel leftCol = new JPanel(new BorderLayout()); leftCol.setOpaque(false);
        JPanel capitalBox = createContainerBox(); 
        capitalBox.add(createSubHeader("Capital ROI")); 
        capitalBox.add(createLabel("Initial: ₱ " + String.format("%,.2f", annual.capital), 20, SwingConstants.LEFT));
        capitalBox.add(createLabel("ROI: " + String.format("%.2f%%", annual.roiPercentage), 40, SwingConstants.LEFT, Font.BOLD));
        leftCol.add(capitalBox, BorderLayout.NORTH); 
        
        JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 30)); rightCol.setOpaque(false);
        JPanel monthlyBox = createContainerBox(); 
        monthlyBox.add(createSubHeader("Monthly Net Profit")); 
        monthlyBox.add(createLabel("₱ " + String.format("%,.2f", monthly.netProfit), 50, SwingConstants.LEFT, Font.BOLD));
        rightCol.add(monthlyBox);
        
        JPanel annuallyBox = createContainerBox(); 
        annuallyBox.add(createSubHeader("Annual Net Profit")); 
        double annualNet = annual.grossProfit - (annual.grossProfit > 0 ? annual.grossProfit * 0.12 : 0);
        annuallyBox.add(createLabel("₱ " + String.format("%,.2f", annualNet), 50, SwingConstants.LEFT, Font.BOLD));
        rightCol.add(annuallyBox);
        
        grid.add(leftCol); grid.add(rightCol); card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRoomsCard() {
        JPanel card = createBaseCard("Rooms");
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Room Rents and Utilities"), BorderLayout.NORTH);
        
        JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setOpaque(false);
        JPanel headerRow = new JPanel(new GridLayout(1, 5, 2, 0)); headerRow.setOpaque(false);
        String[] headers = {"Room No.", "Rent", "Electricity", "Water", "Internet"};
        
        for(String h : headers) {
            JPanel cell = new JPanel(new BorderLayout()); cell.setBackground(COLOR_LIST_ITEM); cell.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            cell.add(createLabel(h, 20, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER); headerRow.add(cell);
        }
        tableContainer.add(headerRow, BorderLayout.NORTH);
        
        roomNumbersList.clear(); rentFields.clear(); elecFields.clear(); waterFields.clear(); netFields.clear();

        JPanel bodyRows = new JPanel();
        bodyRows.setLayout(new BoxLayout(bodyRows, BoxLayout.Y_AXIS));
        bodyRows.setOpaque(false); 
        bodyRows.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        List<String[]> roomsData = ownerDao.getOwnerRooms(currentApartmentId); 

        if (roomsData.isEmpty()) {
            bodyRows.add(createLabel("No rooms found. Please add rooms in the setup.", 16, SwingConstants.CENTER));
        } else {
            for (String[] rowData : roomsData) {
                boolean hasInternet = rowData[5].equals("true"); 
                bodyRows.add(createEditableTableRow(rowData[0], rowData[1], rowData[2], rowData[3], rowData[4], hasInternet));
            }
        }

        JScrollPane scrollPane = new JScrollPane(bodyRows);
        scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(null);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        JButton btnUpdate = new JButton("UPDATE"); 
        btnUpdate.setBackground(COLOR_BTN_ACTION); btnUpdate.setForeground(Color.WHITE); btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        btnUpdate.setFocusPainted(false); btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnUpdate.setPreferredSize(new Dimension(0, 50));
        
        // VIEW ONLY LOCKOUT
        if (isViewOnly) btnUpdate.setEnabled(false);

        btnUpdate.addActionListener(e -> {
            boolean success = true;
            for (int i = 0; i < roomNumbersList.size(); i++) {
                String rNum = roomNumbersList.get(i);
                double rRent = Double.parseDouble(rentFields.get(i).getText().isEmpty() ? "0" : rentFields.get(i).getText());
                double rElec = Double.parseDouble(elecFields.get(i).getText().isEmpty() ? "0" : elecFields.get(i).getText());
                double rWat = Double.parseDouble(waterFields.get(i).getText().isEmpty() ? "0" : waterFields.get(i).getText());
                double rNet = Double.parseDouble(netFields.get(i).getText().isEmpty() ? "0" : netFields.get(i).getText());
                
                if (!ownerDao.updateRoomUtilities(currentApartmentId, rNum, rRent, rElec, rWat, rNet)) {
                    success = false;
                }
            }
            if (success) {
                JOptionPane.showMessageDialog(this, "Room rates and utilities updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Errors occurred while updating rooms.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout()); bottomPanel.setOpaque(false); bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); bottomPanel.add(btnUpdate, BorderLayout.CENTER);
        tableContainer.add(bottomPanel, BorderLayout.SOUTH);
        
        mainContent.add(tableContainer, BorderLayout.CENTER); card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEditableTableRow(String roomNo, String rent, String elec, String water, String internet, boolean hasInternet) {
        JPanel row = new JPanel(new GridLayout(1, 5, 2, 0)); 
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

        JPanel cell1 = new JPanel(new BorderLayout()); cell1.setBackground(COLOR_CONTAINER); cell1.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        cell1.add(createLabel(roomNo, 18, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER); 
        row.add(cell1);
        roomNumbersList.add(roomNo); 

        JTextField txtRent = createTableInputField(rent, true);
        JTextField txtElec = createTableInputField(elec, true);
        JTextField txtWater = createTableInputField(water, true);
        JTextField txtNet = createTableInputField(internet, hasInternet);

        rentFields.add(txtRent); elecFields.add(txtElec); waterFields.add(txtWater); netFields.add(txtNet);

        row.add(wrapInCell(txtRent)); row.add(wrapInCell(txtElec)); row.add(wrapInCell(txtWater)); row.add(wrapInCell(txtNet));
        return row;
    }

    private JTextField createTableInputField(String value, boolean isEnabled) {
        JTextField txt = new JTextField(value);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setBackground(COLOR_CONTAINER); txt.setForeground(Color.WHITE); txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE)); txt.setOpaque(false);
        if (!isEnabled || isViewOnly) { // LOCKOUT ENFORCEMENT
            txt.setText(isEnabled ? value : "N/A"); txt.setEnabled(false); txt.setForeground(Color.GRAY); txt.setBorder(null);
        }
        return txt;
    }

    private JPanel wrapInCell(JTextField field) {
        JPanel cell = new JPanel(new BorderLayout()); cell.setBackground(COLOR_CONTAINER); cell.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); cell.add(field, BorderLayout.CENTER); return cell;
    }

    private JPanel createToDoCard() {
        JPanel card = createBaseCard("To Do's");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);

        // -- LEFT COLUMN --
        JPanel leftCol = new JPanel(); leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS)); leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Active Maintenance Requests"));
        
        JPanel pnlMaint = createContainerBox();
        List<String[]> activeRequests = ownerDao.getPendingMaintenance(currentApartmentId);
        if(activeRequests.isEmpty()) {
            pnlMaint.add(createLabel("All caught up! No active maintenance requests.", 14, SwingConstants.CENTER));
        } else {
            for(String[] req : activeRequests) {
                String reqId = req[0]; String roomNum = req[1]; String issue = req[2];
                JButton btnDone = createActionButton("MARK DONE", COLOR_BTN_ACTION);
                if (isViewOnly) btnDone.setEnabled(false); // LOCKOUT ENFORCEMENT
                btnDone.addActionListener(e -> {
                    if(ownerDao.markMaintenanceDone(Integer.parseInt(reqId))) {
                        JOptionPane.showMessageDialog(this, "Marked as resolved!");
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });
                pnlMaint.add(createListItem("Room " + roomNum, issue, null, btnDone, 80));
            }
        }
        
        JScrollPane scrollMaint = new JScrollPane(pnlMaint); scrollMaint.setBorder(null); scrollMaint.setOpaque(false); scrollMaint.getViewport().setOpaque(false);
        leftCol.add(scrollMaint); leftCol.add(Box.createVerticalStrut(20)); 

        leftCol.add(createSubHeader("Send Announcement"));
        JPanel pnlAnnounce = createContainerBox(); pnlAnnounce.setLayout(new BorderLayout(0, 10));
        JTextArea txtAnnounce = new JTextArea(); 
        txtAnnounce.setBackground(COLOR_LIST_ITEM); txtAnnounce.setForeground(Color.WHITE); txtAnnounce.setCaretColor(Color.WHITE); 
        txtAnnounce.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); txtAnnounce.setPreferredSize(new Dimension(0, 150)); txtAnnounce.setLineWrap(true); txtAnnounce.setWrapStyleWord(true);
        if (isViewOnly) txtAnnounce.setEnabled(false); // LOCKOUT ENFORCEMENT
        pnlAnnounce.add(txtAnnounce, BorderLayout.CENTER);
        
        JPanel pnlSend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlSend.setOpaque(false);
        JComboBox<String> comboSend = new JComboBox<>(new String[]{"TO ALL TENANTS"}); comboSend.setBackground(COLOR_LIST_ITEM); comboSend.setForeground(Color.WHITE);
        
        JButton btnSend = createActionButton("SEND", COLOR_BTN_ACTION); 
        if (isViewOnly) btnSend.setEnabled(false); // LOCKOUT ENFORCEMENT
        btnSend.addActionListener(e -> {
            if(!txtAnnounce.getText().trim().isEmpty()) {
                ownerDao.sendAnnouncement(currentApartmentId, txtAnnounce.getText().trim());
                JOptionPane.showMessageDialog(this, "Announcement broadcasted!");
                txtAnnounce.setText("");
            }
        });
        
        pnlSend.add(comboSend); pnlSend.add(btnSend); pnlAnnounce.add(pnlSend, BorderLayout.SOUTH); 
        leftCol.add(pnlAnnounce);

        // -- RIGHT COLUMN --
        JPanel rightCol = new JPanel(); rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS)); rightCol.setOpaque(false); 
        rightCol.add(createSubHeader("Complaints & Suggestions"));
        JPanel pnlComplaints = createContainerBox(); 
        List<String[]> complaints = ownerDao.getRecentComplaints(currentApartmentId);
        if(complaints.isEmpty()){
            pnlComplaints.add(createLabel("No complaints received.", 14, SwingConstants.CENTER));
        } else {
            for(String[] comp : complaints) { pnlComplaints.add(createListItem("Room " + comp[0], comp[1], comp[2], null, 100)); }
        }
        
        JScrollPane scrollComp = new JScrollPane(pnlComplaints); scrollComp.setBorder(null); scrollComp.setOpaque(false); scrollComp.getViewport().setOpaque(false);
        rightCol.add(scrollComp);
        
        grid.add(leftCol); grid.add(rightCol); card.add(grid, BorderLayout.CENTER); 
        return card;
    }

    private JPanel createHistoryCard() {
        JPanel card = createBaseCard("History");
        JPanel grid = new JPanel(new GridLayout(1, 3, 30, 0)); grid.setOpaque(false);
        
        JPanel col1 = new JPanel(); col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS)); col1.setOpaque(false);
        col1.add(createSubHeader("Bills History")); JPanel pnlBills = createContainerBox(); 
        List<String> bills = ownerDao.getBillsHistory(currentApartmentId);
        for(String b : bills) pnlBills.add(createListItem(b, null, null, null, 80)); 
        col1.add(new JScrollPane(pnlBills));
        
        JPanel col2 = new JPanel(); col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS)); col2.setOpaque(false);
        col2.add(createSubHeader("Maintenance History")); JPanel pnlMaint = createContainerBox(); 
        List<String> maint = ownerDao.getMaintenanceHistory(currentApartmentId);
        for(String m : maint) pnlMaint.add(createListItem(m, null, null, null, 80)); 
        col2.add(new JScrollPane(pnlMaint));
        
        JPanel col3 = new JPanel(); col3.setLayout(new BoxLayout(col3, BoxLayout.Y_AXIS)); col3.setOpaque(false);
        col3.add(createSubHeader("Notification History")); JPanel pnlNotif = createContainerBox(); 
        List<String> notifs = ownerDao.getNotificationHistory(currentApartmentId);
        for(String n : notifs) pnlNotif.add(createListItem(n, null, null, null, 80)); 
        col3.add(new JScrollPane(pnlNotif));
        
        grid.add(col1); grid.add(col2); grid.add(col3); card.add(grid, BorderLayout.CENTER); return card;
    }

    private JPanel createInquiriesCard() {
        JPanel card = createBaseCard("Inquiries");
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0)); grid.setOpaque(false);

        // -- LEFT COLUMN (Room Viewings) --
        JPanel leftCol = new JPanel(); leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS)); leftCol.setOpaque(false);
        leftCol.setAlignmentY(Component.TOP_ALIGNMENT); leftCol.add(createSubHeader("Room Viewings"));
        
        JPanel pnlViewings = createContainerBox();
        List<String[]> pendingViewings = ownerDao.getPendingRoomViewings(currentApartmentId);
        ViewingDAO viewingDao = new ViewingDAO();

        if (pendingViewings.isEmpty()) {
            pnlViewings.add(createLabel("No pending viewings.", 14, SwingConstants.CENTER));
        } else {
            for (String[] v : pendingViewings) {
                String vId = v[0]; String vName = v[1]; String vRoom = v[2]; String vDate = v[3];

                JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actionBtns.setOpaque(false);
                JButton btnAccept = createCircleIconBtn("✓", new Color(0, 204, 102), 40);
                if (isViewOnly) btnAccept.setEnabled(false); // LOCKOUT ENFORCEMENT
                btnAccept.addActionListener(e -> {
                    if (viewingDao.updateViewingStatus(Integer.parseInt(vId), "APPROVED")) {
                        JOptionPane.showMessageDialog(this, "Viewing Approved!");
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60), 40);
                if (isViewOnly) btnReject.setEnabled(false); // LOCKOUT ENFORCEMENT
                btnReject.addActionListener(e -> {
                    if (viewingDao.rejectViewing(Integer.parseInt(vId), "Schedule conflict")) {
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                actionBtns.add(btnAccept); actionBtns.add(btnReject);
                pnlViewings.add(createListItem(vName, "Requested " + vRoom, "Date: " + vDate, actionBtns, 85));
            }
        }
        
        JScrollPane scrollLeft = new JScrollPane(pnlViewings); scrollLeft.setBorder(null); scrollLeft.setOpaque(false); scrollLeft.getViewport().setOpaque(false);
        leftCol.add(scrollLeft);

        // -- RIGHT COLUMN (Pending Permanent Tenants) --
        JPanel rightCol = new JPanel(); rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS)); rightCol.setOpaque(false);
        rightCol.setAlignmentY(Component.TOP_ALIGNMENT); rightCol.add(createSubHeader("Pending Tenant Registrations"));
        
        JPanel pnlTenants = createContainerBox();
        List<String[]> pendingTenants = ownerDao.getPendingTenants(currentApartmentId);
        OwnerDAO baseOwnerDao = new OwnerDAO();

        if (pendingTenants.isEmpty()) {
            pnlTenants.add(createLabel("No pending tenant registrations.", 14, SwingConstants.CENTER));
        } else {
            for (String[] t : pendingTenants) {
                String tId = t[0]; String tName = t[1]; String tRoom = t[2]; String tDate = t[3];

                JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actionBtns.setOpaque(false);
                JButton btnAccept = createCircleIconBtn("✓", new Color(0, 204, 102), 40);
                if (isViewOnly) btnAccept.setEnabled(false); // LOCKOUT ENFORCEMENT
                btnAccept.addActionListener(e -> {
                    if (baseOwnerDao.updateTenantStatus(Integer.parseInt(tId), "APPROVED")) {
                        JOptionPane.showMessageDialog(this, tName + " is now an official tenant!");
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60), 40);
                if (isViewOnly) btnReject.setEnabled(false); // LOCKOUT ENFORCEMENT
                btnReject.addActionListener(e -> {
                    if (baseOwnerDao.updateTenantStatus(Integer.parseInt(tId), "REJECTED")) {
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                actionBtns.add(btnAccept); actionBtns.add(btnReject);
                pnlTenants.add(createListItem(tName, "Room: " + tRoom, "Requested: " + tDate, actionBtns, 85));
            }
        }
        
        JScrollPane scrollRight = new JScrollPane(pnlTenants); scrollRight.setBorder(null); scrollRight.setOpaque(false); scrollRight.getViewport().setOpaque(false);
        rightCol.add(scrollRight);

        grid.add(leftCol); grid.add(rightCol); card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTenantsCard() {
        JPanel card = createBaseCard("Tenants");
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Active Tenants"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> activeTenants = ownerDao.getActiveTenantDetails(currentApartmentId);
        
        if (activeTenants.isEmpty()) {
            list.add(createLabel("No active tenants found.", 16, SwingConstants.CENTER));
        } else {
            for(String[] t : activeTenants) {
                int tId = Integer.parseInt(t[0]); String name = t[1]; String room = t[2]; String contact = t[3]; String email = t[4]; String date = t[5];
                list.add(createListItem(name, "Room " + room, "Joined: " + date, createTenantActions(tId, name, room, contact, email), 80));
            }
        }

        mainContent.add(new JScrollPane(list), BorderLayout.CENTER); 
        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTenantActions(int tId, String name, String room, String contact, String email) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pnl.setOpaque(false);
        
        JButton btnEdit = new JButton("✎"); 
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 28)); btnEdit.setForeground(Color.WHITE); btnEdit.setContentAreaFilled(false); btnEdit.setBorderPainted(false); btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        if (!isViewOnly) btnEdit.addActionListener(e -> showDarkPopup("edit", tId, name, room, contact, email)); // LOCKOUT ENFORCEMENT
        
        JButton btnTrash = new JButton("🗑"); 
        btnTrash.setFont(new Font("Segoe UI", Font.PLAIN, 28)); btnTrash.setForeground(new Color(220, 60, 60)); btnTrash.setContentAreaFilled(false); btnTrash.setBorderPainted(false); btnTrash.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        if (!isViewOnly) btnTrash.addActionListener(e -> showDarkPopup("delete", tId, name, room, null, null)); // LOCKOUT ENFORCEMENT
        
        pnl.add(btnEdit); pnl.add(btnTrash); return pnl;
    }

    private void showDarkPopup(String type, int tId, String name, String room, String currentContact, String currentEmail) {
        JDialog dialog = new JDialog(this, true); dialog.setUndecorated(true); dialog.getContentPane().setBackground(COLOR_LIST_ITEM);
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); panel.setOpaque(false); panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_CONTAINER, 2), BorderFactory.createEmptyBorder(40, 50, 40, 50)));

        if (type.equals("edit")) {
            JLabel lblName = createLabel("EDIT " + name.toUpperCase(), 24, SwingConstants.CENTER, Font.BOLD); lblName.setAlignmentX(Component.CENTER_ALIGNMENT); 
            panel.add(lblName); panel.add(Box.createVerticalStrut(30));
            
            JTextField txtName = new JTextField(name); txtName.setPreferredSize(new Dimension(300, 40)); panel.add(txtName); panel.add(Box.createVerticalStrut(10));
            JTextField txtContact = new JTextField(currentContact); txtContact.setPreferredSize(new Dimension(300, 40)); panel.add(txtContact); panel.add(Box.createVerticalStrut(10));
            JTextField txtEmail = new JTextField(currentEmail); txtEmail.setPreferredSize(new Dimension(300, 40)); panel.add(txtEmail); panel.add(Box.createVerticalStrut(20));
            
            JButton btnApply = createActionButton("APPLY CHANGES", COLOR_BTN_ACTION); btnApply.setAlignmentX(Component.CENTER_ALIGNMENT); 
            btnApply.addActionListener(e -> {
                if (ownerDao.updateTenantContact(tId, txtName.getText(), txtContact.getText(), txtEmail.getText())) {
                    JOptionPane.showMessageDialog(this, "Tenant details updated successfully!");
                    dialog.dispose();
                    this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true); // Hard Refresh
                }
            }); 
            panel.add(btnApply);
            
            JButton btnCancel = createActionButton("CANCEL", new Color(150, 150, 150)); btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCancel.addActionListener(e -> dialog.dispose());
            panel.add(Box.createVerticalStrut(10)); panel.add(btnCancel);
            
        } else if (type.equals("delete")) {
            JLabel lblTrash = createLabel("🗑", 50, SwingConstants.CENTER); lblTrash.setForeground(new Color(220, 60, 60)); lblTrash.setAlignmentX(Component.CENTER_ALIGNMENT); 
            JLabel lblName = createLabel("EVICT " + name, 24, SwingConstants.CENTER, Font.BOLD); lblName.setAlignmentX(Component.CENTER_ALIGNMENT); 
            JLabel lblRoom = createLabel("Room " + room, 18, SwingConstants.CENTER, Font.PLAIN); lblRoom.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblTrash); panel.add(Box.createVerticalStrut(10)); panel.add(lblName); panel.add(lblRoom); panel.add(Box.createVerticalStrut(20));
            
            JButton btnDel = createActionButton("CONFIRM EVICTION", new Color(220, 60, 60)); btnDel.setAlignmentX(Component.CENTER_ALIGNMENT); 
            btnDel.addActionListener(e -> {
                if (ownerDao.evictTenant(tId, currentApartmentId)) {
                    JOptionPane.showMessageDialog(this, name + " has been officially evicted.");
                    dialog.dispose();
                    this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true); // Hard Refresh
                }
            }); 
            panel.add(btnDel);
            
            JButton btnCancel = createActionButton("CANCEL", new Color(150, 150, 150)); btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCancel.addActionListener(e -> dialog.dispose());
            panel.add(Box.createVerticalStrut(10)); panel.add(btnCancel);
        }
        dialog.add(panel); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private JPanel createAccountCard() {
        JPanel card = createBaseCard("Account");
        
        // Main Grid for the two columns
        JPanel grid = new JPanel(new GridLayout(1, 2, 40, 0)); 
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- LEFT BOX (Change Owner Password) ---
        JPanel leftBox = new JPanel(); 
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setBackground(COLOR_LIST_ITEM); // Dark background
        leftBox.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        
        JLabel lblChange = createLabel("CHANGE YOUR PASSWORD", 24, SwingConstants.CENTER, Font.BOLD); 
        lblChange.setAlignmentX(Component.CENTER_ALIGNMENT); 
        leftBox.add(lblChange); 
        leftBox.add(Box.createVerticalStrut(40));
        
        JPanel passWrapper = new JPanel(new BorderLayout()); passWrapper.setOpaque(false); 
        passWrapper.add(createLabel("New Password", 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        JPasswordField txtPass = new JPasswordField(); 
        txtPass.setBackground(COLOR_MAIN_BG); txtPass.setForeground(Color.WHITE); 
        txtPass.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); txtPass.setPreferredSize(new Dimension(0, 45));
        passWrapper.add(txtPass, BorderLayout.CENTER); 
        leftBox.add(passWrapper); 
        leftBox.add(Box.createVerticalStrut(40));

        JButton btnApply = createActionButton("APPLY CHANGES", COLOR_BTN_ACTION); 
        btnApply.setAlignmentX(Component.CENTER_ALIGNMENT); 
        if (isViewOnly) btnApply.setEnabled(false); // Lockout enforcement
        btnApply.addActionListener(e -> {
            String newPass = new String(txtPass.getPassword());
            if (newPass.length() >= 6) {
                new OwnerDAO().changePassword(currentOwnerId, newPass);
                JOptionPane.showMessageDialog(this, "Password updated successfully."); 
                txtPass.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
            }
        });
        leftBox.add(btnApply);

        // --- RIGHT BOX (Create Tenant Account) ---
        JPanel rightBox = new JPanel(); 
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setBackground(COLOR_LIST_ITEM); // Dark background
        rightBox.setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));
        
        JLabel lblCreate1 = createLabel("Create Account", 22, SwingConstants.CENTER, Font.BOLD); 
        lblCreate1.setAlignmentX(Component.CENTER_ALIGNMENT); 
        JLabel lblCreate2 = createLabel("for Tenant not yet in the System", 18, SwingConstants.CENTER, Font.BOLD); 
        lblCreate2.setAlignmentX(Component.CENTER_ALIGNMENT); 
        rightBox.add(lblCreate1); rightBox.add(lblCreate2);
        rightBox.add(Box.createVerticalStrut(30));
        
        JPanel userWrapper = new JPanel(new BorderLayout()); userWrapper.setOpaque(false); 
        userWrapper.add(createLabel("Username", 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        JTextField txtTenantUser = new JTextField(); 
        txtTenantUser.setBackground(Color.WHITE); txtTenantUser.setForeground(Color.BLACK); 
        txtTenantUser.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); txtTenantUser.setPreferredSize(new Dimension(0, 40));
        userWrapper.add(txtTenantUser, BorderLayout.CENTER); 
        rightBox.add(userWrapper); 
        rightBox.add(Box.createVerticalStrut(15));
        
        JPanel tenantPassWrapper = new JPanel(new BorderLayout()); tenantPassWrapper.setOpaque(false); 
        tenantPassWrapper.add(createLabel("Password", 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        JTextField txtTenantPass = new JTextField(); 
        txtTenantPass.setBackground(Color.WHITE); txtTenantPass.setForeground(Color.BLACK); 
        txtTenantPass.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); txtTenantPass.setPreferredSize(new Dimension(0, 40));
        tenantPassWrapper.add(txtTenantPass, BorderLayout.CENTER); 
        rightBox.add(tenantPassWrapper); 
        rightBox.add(Box.createVerticalStrut(40));
        
        JButton btnCreate = createActionButton("CREATE", COLOR_BTN_ACTION); 
        btnCreate.setAlignmentX(Component.CENTER_ALIGNMENT); 
        if (isViewOnly) btnCreate.setEnabled(false); // Lockout enforcement
        btnCreate.addActionListener(e -> {
            String tUser = txtTenantUser.getText().trim();
            String tPass = txtTenantPass.getText().trim();
            if (tUser.isEmpty() || tPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in both fields.");
            } else {
                if (ownerDao.createOfficialTenantAccount(currentApartmentId, tUser, tPass)) {
                    JOptionPane.showMessageDialog(this, "Official Tenant Account created! Give these credentials to your tenant.");
                    txtTenantUser.setText(""); txtTenantPass.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create account. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        rightBox.add(btnCreate);

        grid.add(leftBox);
        grid.add(rightBox);
        card.add(grid, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 30)); card.setBackground(COLOR_MAIN_BG); card.setOpaque(false); 
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false); header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(createLabel(title, 42, SwingConstants.LEFT, Font.BOLD), BorderLayout.WEST);
        
        // --- LOCKOUT WARNING BANNER ---
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); rightHeader.setOpaque(false);
        if (isViewOnly) {
            JLabel lblWarning = createLabel("⚠ ACCOUNT SUSPENDED - PLEASE PAY PLATFORM FEE", 16, SwingConstants.RIGHT, Font.BOLD);
            lblWarning.setForeground(new Color(255, 102, 102));
            rightHeader.add(lblWarning);
        }
        rightHeader.add(createLabel(currentApartmentName, 20, SwingConstants.RIGHT, Font.BOLD));
        header.add(rightHeader, BorderLayout.EAST);
        
        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JLabel createSubHeader(String text) {
        JLabel lbl = createLabel(text, 24, SwingConstants.LEFT, Font.BOLD); lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT); return lbl;
    }

    private JPanel createContainerBox() {
        JPanel container = new JPanel(); container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS)); container.setBackground(COLOR_CONTAINER); container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); container.setAlignmentX(Component.LEFT_ALIGNMENT); return container; 
    }

    private JPanel createListItem(String line1, String line2, String line3, JComponent rightAction, int height) {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(COLOR_LIST_ITEM); panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, height)); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        JPanel leftText = new JPanel(); leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS)); leftText.setOpaque(false);
        if (line1 != null) leftText.add(createLabel(line1, 20, SwingConstants.LEFT, Font.BOLD));
        if (line2 != null && !line2.isEmpty()) { leftText.add(Box.createVerticalStrut(2)); leftText.add(createLabel(line2, 16, SwingConstants.LEFT, Font.PLAIN)); }
        if (line3 != null && !line3.isEmpty()) { leftText.add(Box.createVerticalStrut(2)); JLabel l3 = createLabel(line3, 14, SwingConstants.LEFT, Font.PLAIN); l3.setForeground(Color.LIGHT_GRAY); leftText.add(l3); }
        panel.add(leftText, BorderLayout.CENTER);
        
        if (rightAction != null) {
            JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pnlRight.setOpaque(false); pnlRight.add(rightAction); panel.add(pnlRight, BorderLayout.EAST);
        }

        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setOpaque(false); wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); 
        wrapper.add(panel, BorderLayout.CENTER); wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 10)); 
        return wrapper;
    }

    private JLabel createLabel(String text, int size, int alignment) { return createLabel(text, size, alignment, Font.PLAIN); }
    private JLabel createLabel(String text, int size, int alignment, int fontStyle) {
        JLabel label = new JLabel(text); label.setForeground(COLOR_TEXT); label.setFont(new Font("Segoe UI", fontStyle, size)); label.setHorizontalAlignment(alignment); return label;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    private JButton createCircleIconBtn(String text, Color bg, int size) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, size/2)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(size, size)); btn.setBorder(BorderFactory.createEmptyBorder()); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OwnerDashboard(1).setVisible(true));
    }
}