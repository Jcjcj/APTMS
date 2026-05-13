package Dashboard;

import com.mycompany.apartmentsytem1.BillingDAO;
import com.mycompany.apartmentsytem1.ExpenseDAO;
import com.mycompany.apartmentsytem1.OwnerDashboardDAO;
import com.mycompany.apartmentsytem1.FinanceService;
import com.mycompany.apartmentsytem1.FileStorageUtil;
import com.mycompany.apartmentsytem1.OwnerDAO;
import com.mycompany.apartmentsytem1.ViewingDAO;
import com.mycompany.apartmentsytem1.RoomOccupancyDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

public class OwnerDashboard extends JFrame implements ActionListener {
    
    private JButton btnPayments;
    
        // Reporting filters
    private JComboBox<String> cbYear;
    private JComboBox<String> cbMonth;


    private int currentOwnerId;
    private int currentApartmentId; 
    private String currentApartmentName = "My Apartment"; 
    private String currentOwnerUsername = "";
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

        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement("SELECT username FROM owners WHERE owner_id = ?")) {
            ps.setInt(1, currentOwnerId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) this.currentOwnerUsername = rs.getString("username");
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
        cardsContainer.add(createPaymentsCard(), "Payments");

        this.add(cardsContainer, BorderLayout.CENTER);

        if (this.isViewOnly) {
            activateButton(btnExpenses);
            cardLayout.show(cardsContainer, "Expenses");
            JOptionPane.showMessageDialog(this, 
                "Your account is currently SUSPENDED due to an unpaid Platform Service Fee.\n\n" +
                "Your dashboard is in View-Only mode. Please submit your payment\n" +
                "via this Expenses tab to restore full access to your apartment.", 
                "Account Suspended", JOptionPane.WARNING_MESSAGE);
        } else {
            // Normal Login
            activateButton(btnAptDash);
            cardLayout.show(cardsContainer, "AptDash");
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setMaximumSize(new Dimension(250, 0));
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
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(COLOR_SIDEBAR);

        // Create all nav buttons here
        btnAptDash  = createNavButton("Apartment Dashboard");
        btnProfit   = createNavButton("Profit");
        btnExpenses = createNavButton("Expenses");
        btnRooms    = createNavButton("Rooms");
        btnToDo     = createNavButton("To Do's");
        btnHistory  = createNavButton("History");
        btnInquiries= createNavButton("Inquiries");
        btnTenants  = createNavButton("Tenants");
        btnAccount  = createNavButton("Account");
        btnPayments = createNavButton("Payments");

        navButtons = new JButton[]{
            btnAptDash, btnProfit, btnExpenses, btnRooms,
            btnToDo, btnHistory, btnInquiries, btnTenants,
            btnAccount, btnPayments
        };

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
        else if (source == btnPayments) { activateButton(btnPayments); cardLayout.show(cardsContainer, "Payments"); }
    }

    private void activateButton(JButton btn) { btn.setBackground(COLOR_BTN_ACTIVE); }

    // =========================================================================
    // EXPENSES PANEL (Platform Fee & Payment Form)
    // =========================================================================

    private JPanel createExpensesCard() {
        JPanel card = createBaseCard("Expenses");
        
        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
        grid.setOpaque(false);

        // --- LEFT COLUMN (Owner Operating Expenses) ---
        JPanel leftCol = new JPanel(new BorderLayout());
        leftCol.setOpaque(false);
        leftCol.add(createSubHeader("Owner Expenses (This Month)"), BorderLayout.NORTH);

        JPanel utilitiesBox = createContainerBox();
        utilitiesBox.setLayout(new GridLayout(3, 1));

        // Use the same month key as Profit tab
        String currMonth = LocalDate.now().toString().substring(0, 7); // "YYYY-MM"

        // Pull expenses from the expenses table
        ExpenseDAO expenseDao = new ExpenseDAO();
        double buildingExp = expenseDao.getBuildingExpensesOnly(currentApartmentId, currMonth);
        double roomExp     = expenseDao.getRoomExpensesOnly(currentApartmentId, currMonth);
        double totalExp    = buildingExp + roomExp;

        utilitiesBox.add(createUtilityBlock("Building Expenses", "₱ " + String.format("%,.2f", buildingExp)));
        utilitiesBox.add(createUtilityBlock("Room Expenses",     "₱ " + String.format("%,.2f", roomExp)));
        utilitiesBox.add(createUtilityBlock("Total Expenses",    "₱ " + String.format("%,.2f", totalExp)));

        leftCol.add(utilitiesBox, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        btnPanel.setOpaque(false);

        JButton btnAddBuilding = createActionButton("ADD BUILDING EXPENSE", COLOR_BTN_ACTION);
        JButton btnAddRoom     = createActionButton("ADD ROOM EXPENSE", COLOR_BTN_ACTION);

        btnAddBuilding.addActionListener(e -> openExpenseDialog(false));
        btnAddRoom.addActionListener(e -> openExpenseDialog(true));

        if (isViewOnly) { btnAddBuilding.setEnabled(false); btnAddRoom.setEnabled(false); }

        btnPanel.add(btnAddBuilding);
        btnPanel.add(btnAddRoom);
        leftCol.add(btnPanel, BorderLayout.SOUTH);

        grid.add(leftCol);


        // --- RIGHT COLUMN (CardLayout for Table vs Form) ---
        JPanel rightCol = new JPanel(new BorderLayout()); rightCol.setOpaque(false);
        rightCol.add(createSubHeader("Platform Service Fee"), BorderLayout.NORTH);
        
        CardLayout rightCardLayout = new CardLayout();
        JPanel rightContentPanel = new JPanel(rightCardLayout);
        rightContentPanel.setOpaque(false);

        rightContentPanel.add(createFeeTablePanel(rightCardLayout, rightContentPanel), "TABLE");
        rightContentPanel.add(createPaymentFormPanel(rightCardLayout, rightContentPanel), "FORM");

        rightCol.add(rightContentPanel, BorderLayout.CENTER);
        grid.add(rightCol);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }
    
    private void openExpenseDialog(boolean isRoomExpense) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.getContentPane().setBackground(COLOR_LIST_ITEM);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_CONTAINER, 2),
            new EmptyBorder(20, 30, 20, 30)
        ));

        panel.add(createLabel(isRoomExpense ? "Add Room Expense" : "Add Building Expense",
                              20, SwingConstants.CENTER, Font.BOLD));
        panel.add(Box.createVerticalStrut(15));

        // Always declare the room field; only use it when isRoomExpense==true
        final JTextField roomField = new JTextField();
        if (isRoomExpense) {
            panel.add(createLabel("Room Number", 14, SwingConstants.LEFT, Font.PLAIN));
            roomField.setMaximumSize(new Dimension(250, 30));
            panel.add(roomField);
            panel.add(Box.createVerticalStrut(10));
        }

        panel.add(createLabel("Category", 14, SwingConstants.LEFT, Font.PLAIN));
        JTextField txtCat = new JTextField();
        txtCat.setMaximumSize(new Dimension(250, 30));
        panel.add(txtCat);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Amount", 14, SwingConstants.LEFT, Font.PLAIN));
        JTextField txtAmt = new JTextField();
        txtAmt.setMaximumSize(new Dimension(250, 30));
        panel.add(txtAmt);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Date (YYYY-MM-DD)", 14, SwingConstants.LEFT, Font.PLAIN));
        JTextField txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setMaximumSize(new Dimension(250, 30));
        panel.add(txtDate);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Month Key (YYYY-MM)", 14, SwingConstants.LEFT, Font.PLAIN));
        JTextField txtMonth = new JTextField(LocalDate.now().toString().substring(0, 7));
        txtMonth.setMaximumSize(new Dimension(250, 30));
        panel.add(txtMonth);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabel("Description", 14, SwingConstants.LEFT, Font.PLAIN));
        JTextField txtDesc = new JTextField();
        txtDesc.setMaximumSize(new Dimension(250, 30));
        panel.add(txtDesc);
        panel.add(Box.createVerticalStrut(15));

        JButton btnSave   = createActionButton("SAVE",   COLOR_BTN_ACTION);
        JButton btnCancel = createActionButton("CANCEL", new Color(150, 150, 150));

        btnSave.addActionListener(e -> {
            try {
                double amount = parseMoney(txtAmt.getText());
                String cat    = txtCat.getText().trim();
                String date   = txtDate.getText().trim();
                String month  = txtMonth.getText().trim();
                String desc   = txtDesc.getText().trim();

                if (cat.isEmpty() || date.isEmpty() || month.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                    return;
                }

                ExpenseDAO expDao = new ExpenseDAO();
                boolean ok;

                if (isRoomExpense) {
                    String room = roomField.getText().trim();
                    if (room.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter a room number.");
                        return;
                    }
                    ok = expDao.addRoomExpense(currentApartmentId, room, cat, amount, date, month, desc);
                } else {
                    ok = expDao.addBuildingExpense(currentApartmentId, cat, amount, date, month, desc);
                }

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Expense saved.");
                    dialog.dispose();
                    refreshDashboard(); // so Expenses & Profit reflect the change
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save expense.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.");
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSave);
        btnRow.add(btnCancel);
        panel.add(btnRow);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }



    private JPanel createFeeTablePanel(CardLayout layout, JPanel parent) {
        JPanel panel = new JPanel(new BorderLayout()); panel.setOpaque(false);
        
        JPanel tableContainer = new JPanel();
        tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
        tableContainer.setOpaque(false);

        JPanel headerRow = new JPanel(new GridLayout(1, 3, 5, 0)); headerRow.setOpaque(false);
        headerRow.add(createFeeCell("ROOMS", true));
        headerRow.add(createFeeCell("RENTS", true));
        headerRow.add(createFeeCell("SERVICE FEE", true));
        tableContainer.add(headerRow);
        tableContainer.add(Box.createVerticalStrut(5));

        double totalFee = 0.0;
        List<String[]> activeRooms = ownerDao.getActiveRoomsForServiceFee(currentApartmentId);
        
        if (activeRooms.isEmpty()) {
            JLabel empty = createLabel("No rooms listed.", 16, SwingConstants.CENTER);
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

        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); totalRow.setOpaque(false);
        totalRow.add(createLabel("TOTAL", 18, SwingConstants.RIGHT, Font.BOLD));
        totalRow.add(Box.createHorizontalStrut(10));
        JPanel totalCell = createFeeCell(String.format("%,.2f", totalFee), false);
        totalCell.setMaximumSize(new Dimension(150, 40));
        totalRow.add(totalCell);
        tableContainer.add(Box.createVerticalStrut(15));
        tableContainer.add(totalRow);

        panel.add(tableContainer, BorderLayout.NORTH);

        JButton btnPay = createActionButton("PAY", COLOR_BTN_ACTION);
        btnPay.setMaximumSize(new Dimension(150, 40));
        btnPay.addActionListener(e -> layout.show(parent, "FORM")); 
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlBottom.setOpaque(false);
        pnlBottom.add(btnPay);
        panel.add(pnlBottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFeeCell(String text, boolean isHeader) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isHeader ? new Color(0, 153, 76) : COLOR_CONTAINER);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cell.add(createLabel(text, 16, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER);
        return cell;
    }

    private JPanel createPaymentFormPanel(CardLayout layout, JPanel parent) {
        JPanel panel = createContainerBox();
        panel.setLayout(new BorderLayout());

        JPanel innerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        innerGrid.setOpaque(false);

        JPanel pnlMethods = new JPanel(); pnlMethods.setLayout(new BoxLayout(pnlMethods, BoxLayout.Y_AXIS)); pnlMethods.setOpaque(false);
        pnlMethods.add(createLabel("Payment Methods", 18, SwingConstants.LEFT, Font.BOLD));
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createLabel("GCash (System Admin)", 16, SwingConstants.LEFT, Font.BOLD));
        pnlMethods.add(createLabel("0917 123 4567", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("(Apartment System Official)", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(Box.createVerticalStrut(20));
        
        pnlMethods.add(createLabel("Paymaya (System Admin)", 16, SwingConstants.LEFT, Font.BOLD));
        pnlMethods.add(createLabel("0918 987 6543", 16, SwingConstants.LEFT, Font.PLAIN));
        pnlMethods.add(createLabel("(Apartment System Official)", 16, SwingConstants.LEFT, Font.PLAIN));
        innerGrid.add(pnlMethods);

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

        JButton btnSubmit = createActionButton("SUBMIT", COLOR_BTN_ACTION);
        btnSubmit.addActionListener(e -> {
            if (ownerDao.submitPlatformFeePayment(currentApartmentId, txtTin.getText(), txtMethod.getText(), txtDate.getText(), txtRef.getText())) {
                JOptionPane.showMessageDialog(this, "Payment Submitted! Pending Super Admin Verification.", "Success", JOptionPane.INFORMATION_MESSAGE);
                layout.show(parent, "TABLE"); 
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
    // REMAINING CARDS & HELPERS 
    // =========================================================================
    
    private JPanel createAptDashboardCard() {
    JPanel card = createBaseCard("Apartment Dashboard");
    JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
    grid.setOpaque(false);

    // ================= LEFT COLUMN =================
    JPanel leftCol = new JPanel();
    leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
    leftCol.setOpaque(false);

    // --- To Do's ---
    leftCol.add(createSubHeader("To Do's"));
    JPanel pnlToDo = createContainerBox();
    List<String[]> activeReqs = ownerDao.getPendingMaintenance(currentApartmentId);
    if (activeReqs.isEmpty()) {
        pnlToDo.add(createLabel("No pending to-dos.", 14, SwingConstants.LEFT));
    } else {
        for (String[] req : activeReqs) {
            pnlToDo.add(createListItem("Room " + req[1], req[2], null, null, 80));
        }
    }
    JScrollPane toDoScroll = new JScrollPane(pnlToDo);
    toDoScroll.setBorder(null);
    toDoScroll.setOpaque(false);
    toDoScroll.getViewport().setOpaque(false);
    grid.setOpaque(false);
    leftCol.add(toDoScroll);
    leftCol.add(Box.createVerticalStrut(20));

    // --- Expenses (Owner Operating Expenses – This Month) ---
    leftCol.add(createSubHeader("Expenses"));
    JPanel pnlExpenses = createContainerBox();
    pnlExpenses.setLayout(new BorderLayout());

    // Use same month key as the Expenses tab: "YYYY-MM"
    String currMonth = LocalDate.now().toString().substring(0, 7);
    ExpenseDAO expenseDao = new ExpenseDAO();
    double buildingExp = expenseDao.getBuildingExpensesOnly(currentApartmentId, currMonth);
    double roomExp     = expenseDao.getRoomExpensesOnly(currentApartmentId, currMonth);
    double totalExp    = buildingExp + roomExp;

    pnlExpenses.add(
        createLabel("Combined Utilities (This Month)", 16, SwingConstants.LEFT),
        BorderLayout.NORTH
    );
    pnlExpenses.add(
        createLabel("₱ " + String.format("%,.2f", totalExp), 60, SwingConstants.LEFT, Font.BOLD),
        BorderLayout.CENTER
    );
    leftCol.add(pnlExpenses);

    // ================= RIGHT COLUMN =================
    JPanel rightCol = new JPanel();
    rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
    rightCol.setOpaque(false);

    // --- Complaints & Suggestions ---
    rightCol.add(createSubHeader("Complaints & Suggestions"));
    JPanel pnlComplaints = createContainerBox();
    List<String[]> comps = ownerDao.getRecentComplaints(currentApartmentId);
    if (comps.isEmpty()) {
        pnlComplaints.add(createLabel("No complaints.", 14, SwingConstants.LEFT));
    } else {
        for (String[] c : comps) {
            pnlComplaints.add(createListItem("Room " + c[0], c[1], c[2], null, 100));
        }
    }
    JScrollPane compScroll = new JScrollPane(pnlComplaints);
    compScroll.setBorder(null);
    compScroll.setOpaque(false);
    compScroll.getViewport().setOpaque(false);
    rightCol.add(compScroll);
    rightCol.add(Box.createVerticalStrut(20));

    // --- Rooms Stats ---
    rightCol.add(createSubHeader("Rooms"));
    JPanel pnlRooms = createContainerBox();
    pnlRooms.setLayout(new GridLayout(2, 3));
    int[] occStats = ownerDao.getRoomOccupancyStats(currentApartmentId);
    pnlRooms.add(createLabel("Occupied", 16, SwingConstants.CENTER));
    pnlRooms.add(createLabel("Vacant",   16, SwingConstants.CENTER));
    pnlRooms.add(createLabel("Total",    16, SwingConstants.CENTER));
    pnlRooms.add(createLabel(String.valueOf(occStats[0]), 70, SwingConstants.CENTER, Font.BOLD));
    pnlRooms.add(createLabel(String.valueOf(occStats[1]), 70, SwingConstants.CENTER, Font.BOLD));
    pnlRooms.add(createLabel(String.valueOf(occStats[2]), 70, SwingConstants.CENTER, Font.BOLD));
    rightCol.add(pnlRooms);

    grid.add(leftCol);
    grid.add(rightCol);
    card.add(grid, BorderLayout.CENTER);
    return card;
}


    private JPanel createProfitCard() {
    JPanel card = createBaseCard("Profit");

    // === FILTER BAR ===
    JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    filterBar.setOpaque(false);

    // Years: you can adjust the range as needed
    cbYear = new JComboBox<>(new String[]{"2024", "2025", "2026", "2027"});
    cbYear.setSelectedItem(String.valueOf(LocalDate.now().getYear()));

    // Months: "All" = whole year, or specific month numbers
    cbMonth = new JComboBox<>(new String[]{
        "All", "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"
    });
    cbMonth.setSelectedIndex(0); // All by default

    JButton btnApply = createActionButton("APPLY", COLOR_BTN_ACTION);
    btnApply.addActionListener(e -> refreshProfitCard());

    filterBar.add(new JLabel("Year:"));
    filterBar.add(cbYear);
    filterBar.add(new JLabel("Month:"));
    filterBar.add(cbMonth);
    filterBar.add(btnApply);

    card.add(filterBar, BorderLayout.NORTH);

    // The actual content goes into CENTER – built by helper
    card.add(buildProfitContent(), BorderLayout.CENTER);
    return card;
}
    
    // Rebuild the Profit card when filters change
private void refreshProfitCard() {
    cardsContainer.remove(cardsContainer.getComponent(1)); // temporary
    // Better (if you track a reference or re-add cleanly):
    cardsContainer.add(createProfitCard(), "Profit");
    cardLayout.show(cardsContainer, "Profit");
}


// Builds the inner content using current cbYear/cbMonth selection
private JPanel buildProfitContent() {
    JPanel content = new JPanel(new GridLayout(1, 2, 30, 0));
    content.setOpaque(false);

    String year  = (cbYear != null) ? cbYear.getSelectedItem().toString() : String.valueOf(LocalDate.now().getYear());
    String month = (cbMonth != null) ? cbMonth.getSelectedItem().toString() : "All";

    FinanceService finance = new FinanceService();

    // Determine month filter for MonthlyReport
    String monthFilter;
    if ("All".equals(month)) {
        // No specific month: use current month by default or you can disable Monthly
        monthFilter = year + "-" + String.format("%02d", LocalDate.now().getMonthValue());
    } else {
        monthFilter = year + "-" + month; // e.g. "2026-06"
    }

    FinanceService.MonthlyReport monthly = finance.getMonthlyReport(currentApartmentId, monthFilter);
    FinanceService.AnnualReport  annual  = finance.getAnnualReport(currentApartmentId, year);

    // LEFT: Capital ROI
    JPanel leftCol = new JPanel(new BorderLayout());
    leftCol.setOpaque(false);
    JPanel capitalBox = createContainerBox();
    capitalBox.add(createSubHeader("Capital ROI"));

    if (annual.capital <= 0) {
        capitalBox.add(createLabel("Initial: ₱ 0.00 (Set Capital to see ROI)", 16, SwingConstants.LEFT));
        capitalBox.add(createLabel("ROI: 0.00% (Capital missing)", 16, SwingConstants.LEFT));
    } else {
        capitalBox.add(createLabel("Initial: ₱ " + String.format("%,.2f", annual.capital), 20, SwingConstants.LEFT));
        capitalBox.add(createLabel("ROI: " + String.format("%.2f%%", annual.roiPercentage), 40, SwingConstants.LEFT, Font.BOLD));
    }

    leftCol.add(capitalBox, BorderLayout.NORTH);

    // RIGHT: Monthly & Annual Net Profit
    JPanel rightCol = new JPanel(new GridLayout(2, 1, 0, 30));
    rightCol.setOpaque(false);

    JPanel monthlyBox = createContainerBox();
    monthlyBox.add(createSubHeader("Monthly Net Profit (" + monthFilter + ")"));
    monthlyBox.add(createLabel("₱ " + String.format("%,.2f", monthly.netProfit), 50, SwingConstants.LEFT, Font.BOLD));
    rightCol.add(monthlyBox);

    JPanel annuallyBox = createContainerBox();
    annuallyBox.add(createSubHeader("Annual Net Profit (" + year + ")"));
    annuallyBox.add(createLabel("₱ " + String.format("%,.2f", annual.netProfit), 50, SwingConstants.LEFT, Font.BOLD));
    rightCol.add(annuallyBox);

    content.add(leftCol);
    content.add(rightCol);
    return content;
}



    private JPanel createRoomsCard() {
        JPanel card = createBaseCard("Rooms");
        JPanel mainContent = new JPanel(new BorderLayout()); mainContent.setOpaque(false);
        mainContent.add(createSubHeader("Room Rents and Utilities"), BorderLayout.NORTH);
        
        JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setOpaque(false);
        JPanel headerRow = new JPanel(new GridLayout(1, 6, 2, 0)); headerRow.setOpaque(false);
        String[] headers = {"Room No.", "Rent", "Electricity", "Water", "Internet", "Due Date"};
        
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
                boolean hasInternet = rowData[6].equals("true"); 
                bodyRows.add(createEditableTableRow(rowData[0], rowData[1], rowData[2], rowData[3], rowData[4], rowData[5], hasInternet));
            }
        }

        JScrollPane scrollPane = new JScrollPane(bodyRows);
        scrollPane.setOpaque(false); scrollPane.getViewport().setOpaque(false); scrollPane.setBorder(null);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        JButton btnUpdate = new JButton("UPDATE"); 
        btnUpdate.setBackground(COLOR_BTN_ACTION); btnUpdate.setForeground(Color.WHITE); btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        btnUpdate.setFocusPainted(false); btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnUpdate.setMaximumSize(new Dimension(0, 50));
        
        if (isViewOnly) btnUpdate.setEnabled(false);

        btnUpdate.addActionListener(e -> {
            boolean success = true;
            for (int i = 0; i < roomNumbersList.size(); i++) {
                String rNum = roomNumbersList.get(i);
                double rRent = parseMoney(rentFields.get(i).getText());
                double rElec = parseMoney(elecFields.get(i).getText());
                double rWat = parseMoney(waterFields.get(i).getText());
                double rNet = parseMoney(netFields.get(i).getText());
                
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

    private JPanel createEditableTableRow(String roomNo, String rent, String elec, String water, String internet, String dueDate, boolean hasInternet) {
        JPanel row = new JPanel(new GridLayout(1, 6, 2, 0)); 
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

        row.add(wrapInCell(txtRent)); row.add(wrapInCell(txtElec)); row.add(wrapInCell(txtWater)); row.add(wrapInCell(txtNet)); row.add(wrapInDisplayCell(dueDate));
        return row;
    }

    private JTextField createTableInputField(String value, boolean isEnabled) {
        JTextField txt = new JTextField(value);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setBackground(COLOR_CONTAINER); txt.setForeground(Color.WHITE); txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE)); txt.setOpaque(false);
        if (!isEnabled || isViewOnly) { 
            txt.setText(isEnabled ? value : "N/A"); txt.setEnabled(false); txt.setForeground(Color.GRAY); txt.setBorder(null);
        }
        return txt;
    }

    private JPanel wrapInCell(JTextField field) {
        JPanel cell = new JPanel(new BorderLayout()); cell.setBackground(COLOR_CONTAINER); cell.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); cell.add(field, BorderLayout.CENTER); return cell;
    }

    private JPanel wrapInDisplayCell(String value) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(COLOR_CONTAINER);
        cell.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        cell.add(createLabel(value != null ? value : "N/A", 16, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER);
        return cell;
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
                if (isViewOnly) btnDone.setEnabled(false); 
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
        txtAnnounce.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); txtAnnounce.setMaximumSize(new Dimension(0, 150)); txtAnnounce.setLineWrap(true); txtAnnounce.setWrapStyleWord(true);
        if (isViewOnly) txtAnnounce.setEnabled(false); 
        pnlAnnounce.add(txtAnnounce, BorderLayout.CENTER);
        
        JPanel pnlSend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlSend.setOpaque(false);
        JComboBox<String> comboSend = new JComboBox<>(new String[]{"TO ALL TENANTS"}); 
        // --- BUG FIX ---
        comboSend.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        // ---------------
        comboSend.setBackground(COLOR_LIST_ITEM); comboSend.setForeground(Color.WHITE);
        
        JButton btnSend = createActionButton("SEND", COLOR_BTN_ACTION); 
        if (isViewOnly) btnSend.setEnabled(false); 
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
        List<String> notifs = ownerDao.getNotificationHistory(currentApartmentId, currentOwnerUsername);
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
                String vId = v[0]; String vName = v[1]; String vRoom = v[2]; String vDate = v[3]; String vType = v.length > 4 ? v[4] : "Viewing";

                JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actionBtns.setOpaque(false);
                
                // CORRECTED VIEWING ACCEPT BUTTON
                JButton btnAccept = createCircleIconBtn("✓", new Color(0, 204, 102), 40);
                if (isViewOnly) btnAccept.setEnabled(false); 
                btnAccept.addActionListener(e -> {
                    if (viewingDao.updateViewingStatus(Integer.parseInt(vId), "APPROVED")) {
                        JOptionPane.showMessageDialog(this, vType + " Approved!");
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                // CORRECTED VIEWING REJECT BUTTON
                JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60), 40);
                if (isViewOnly) btnReject.setEnabled(false); 
                btnReject.addActionListener(e -> {
                    if (viewingDao.rejectViewing(Integer.parseInt(vId), "Schedule conflict")) {
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                actionBtns.add(btnAccept); actionBtns.add(btnReject);
                pnlViewings.add(createListItem(vName, vType + " for Room " + vRoom, "Date: " + vDate, actionBtns, 85));
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
                
                // CORRECTED TENANT ACCEPT BUTTON WITH OCCUPANCY FIX
                JButton btnAccept = createCircleIconBtn("✓", new Color(0, 204, 102), 40);
                if (isViewOnly) btnAccept.setEnabled(false); 
                btnAccept.addActionListener(e -> {
                    if (baseOwnerDao.updateTenantStatus(Integer.parseInt(tId), "APPROVED")) {
                        
                        // --- THE CRITICAL MISSING LINK ---
                        RoomOccupancyDAO occDao = new RoomOccupancyDAO();
                        occDao.assignTenantToRoom(currentApartmentId, tRoom, Integer.parseInt(tId));
                        // ---------------------------------

                        JOptionPane.showMessageDialog(this, tName + " is now an official tenant!");
                        this.dispose(); new OwnerDashboard(currentOwnerId).setVisible(true);
                    }
                });

                JButton btnReject = createCircleIconBtn("✖", new Color(220, 60, 60), 40);
                if (isViewOnly) btnReject.setEnabled(false); 
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
                int tId = Integer.parseInt(t[0]); String name = t[1]; String room = t[2]; String date = t[5];
                list.add(createListItem(name, "Room " + room, "Joined: " + (date != null ? date : "N/A"), createTenantActions(tId, name, room), 80));
            }
        }

        // --- NEW TRANSPARENT SCROLL PANE FIX ---
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainContent.add(scrollPane, BorderLayout.CENTER); 
        // ---------------------------------------

        card.add(mainContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTenantActions(int tId, String name, String room) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pnl.setOpaque(false);
        JButton btnView = createActionButton("VIEW", new Color(0, 120, 70));
        btnView.setPreferredSize(new Dimension(80, 35));
        btnView.addActionListener(e -> showTenantDetailsPopup(tId));
        
        JButton btnTrash = new JButton("🗑"); 
        btnTrash.setFont(new Font("Segoe UI", Font.PLAIN, 28)); btnTrash.setForeground(new Color(220, 60, 60)); btnTrash.setContentAreaFilled(false); btnTrash.setBorderPainted(false); btnTrash.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        if (!isViewOnly) btnTrash.addActionListener(e -> showDarkPopup("delete", tId, name, room)); 
        
        pnl.add(btnView); pnl.add(btnTrash); return pnl;
    }

    private void showTenantDetailsPopup(int tenantId) {
        String[] details = ownerDao.getTenantRegistrationDetails(tenantId);
        if (details == null) {
            showThemedMessage("Tenant Registration Details", "Unable to load tenant details.");
            return;
        }

        JDialog dialog = new JDialog(this, "Tenant Registration Details", true);
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
        header.add(createLabel("Tenant Registration Details", 24, SwingConstants.LEFT, Font.BOLD), BorderLayout.WEST);

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
        detailsPanel.add(createDetailRow("Name", details[0]));
        detailsPanel.add(createDetailRow("Contact Number", details[1]));
        detailsPanel.add(createDetailRow("Email", details[2]));
        detailsPanel.add(createDetailRow("Address", details[3]));
        detailsPanel.add(createDetailRow("Emergency Contact", details[4]));
        detailsPanel.add(createDetailRow("Apartment Name", details[5]));
        detailsPanel.add(createDetailRow("Room Number", details[6]));
        detailsPanel.add(createDetailRow("Move-in Date", details[7]));
        detailsPanel.add(createDetailRow("Occupants", details[8]));

        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.setBorder(null);
        detailsScroll.setOpaque(false);
        detailsScroll.getViewport().setOpaque(false);

        content.add(detailsScroll);
        content.add(createValidIdPreview(details[9]));
        shell.add(content, BorderLayout.CENTER);

        dialog.add(shell);
        dialog.setPreferredSize(new Dimension(860, 520));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel lblKey = createLabel(label + ":", 14, SwingConstants.LEFT, Font.BOLD);
        lblKey.setPreferredSize(new Dimension(145, 24));

        JLabel lblValue = createLabel(safeText(value), 14, SwingConstants.LEFT, Font.PLAIN);
        lblValue.setVerticalAlignment(SwingConstants.TOP);

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
    }

    private JPanel createValidIdPreview(String validIdPath) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(COLOR_CONTAINER);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        panel.add(createLabel("Valid ID", 18, SwingConstants.LEFT, Font.BOLD), BorderLayout.NORTH);

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
                imageLabel.setIcon(scaleIcon(icon, 330, 320));
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
        panel.add(createLabel(title, 22, SwingConstants.LEFT, Font.BOLD), BorderLayout.NORTH);
        panel.add(createLabel(message, 15, SwingConstants.LEFT), BorderLayout.CENTER);

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

    private void showDarkPopup(String type, int tId, String name, String room) {
        JDialog dialog = new JDialog(this, true); dialog.setUndecorated(true); dialog.getContentPane().setBackground(COLOR_LIST_ITEM);
        JPanel panel = new JPanel(); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); panel.setOpaque(false); panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_CONTAINER, 2), BorderFactory.createEmptyBorder(40, 50, 40, 50)));

        if (type.equals("delete")) {
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
        JPanel grid = new JPanel(new GridLayout(1, 2, 40, 0));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] profile = ownerDao.getOwnerApartmentProfile(currentOwnerId, currentApartmentId);
        if (profile == null) {
            JPanel fallback = new JPanel(new BorderLayout());
            fallback.setOpaque(false);
            fallback.add(createLabel("Unable to load owner and apartment profile.", 18, SwingConstants.CENTER, Font.BOLD), BorderLayout.CENTER);
            card.add(fallback, BorderLayout.CENTER);
            return card;
        }

        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setBackground(COLOR_LIST_ITEM);
        leftBox.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        leftBox.add(createSubHeader("Edit Registration Details"));
        leftBox.add(Box.createVerticalStrut(10));

        JTextField txtOwnerName = createAccountTextField(profile[0]);
        JTextField txtOwnerContact = createAccountTextField(profile[1]);
        JTextField txtOwnerEmail = createAccountTextField(profile[2]);
        JTextField txtOwnerAddress = createAccountTextField(profile[3]);
        JTextField txtOwnerEmergency = createAccountTextField(profile[4]);
        JTextField txtGcashNo = createAccountTextField(profile[5]);
        JTextField txtGcashName = createAccountTextField(profile[6]);
        JTextField txtPaymayaNo = createAccountTextField(profile[7]);
        JTextField txtPaymayaName = createAccountTextField(profile[8]);
        JTextField txtApartmentName = createAccountTextField(profile[9]);
        JTextField txtTinNo = createAccountTextField(profile[10]);
        JTextField txtDescription = createAccountTextField(profile[11]);
        JTextField txtPolicy = createAccountTextField(profile[12]);
        JTextField txtBarangay = createAccountTextField(profile[13]);
        JTextField txtStreet = createAccountTextField(profile[14]);
        JTextField txtApartmentContact = createAccountTextField(profile[15]);
        JTextField txtApartmentEmail = createAccountTextField(profile[16]);
        JTextField txtApartmentEmergency = createAccountTextField(profile[17]);

        leftBox.add(createFormField("Owner Name", txtOwnerName));
        leftBox.add(createFormField("Owner Contact", txtOwnerContact));
        leftBox.add(createFormField("Owner Email", txtOwnerEmail));
        leftBox.add(createFormField("Owner Address", txtOwnerAddress));
        leftBox.add(createFormField("Owner Emergency", txtOwnerEmergency));
        leftBox.add(createFormField("GCash Number", txtGcashNo));
        leftBox.add(createFormField("GCash Name", txtGcashName));
        leftBox.add(createFormField("Paymaya Number", txtPaymayaNo));
        leftBox.add(createFormField("Paymaya Name", txtPaymayaName));
        leftBox.add(createFormField("Apartment Name", txtApartmentName));
        leftBox.add(createFormField("TIN Number", txtTinNo));
        leftBox.add(createFormField("Description", txtDescription));
        leftBox.add(createFormField("Policy", txtPolicy));
        leftBox.add(createFormField("Barangay", txtBarangay));
        leftBox.add(createFormField("Street", txtStreet));
        leftBox.add(createFormField("Apartment Contact", txtApartmentContact));
        leftBox.add(createFormField("Apartment Email", txtApartmentEmail));
        leftBox.add(createFormField("Apartment Emergency", txtApartmentEmergency));
        leftBox.add(Box.createVerticalStrut(20));

        JButton btnSaveProfile = createActionButton("SAVE REGISTRATION CHANGES", COLOR_BTN_ACTION);
        btnSaveProfile.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isViewOnly) btnSaveProfile.setEnabled(false);
        btnSaveProfile.addActionListener(e -> {
            boolean ok = ownerDao.updateOwnerApartmentProfile(
                    currentOwnerId,
                    currentApartmentId,
                    txtOwnerName.getText().trim(),
                    txtOwnerContact.getText().trim(),
                    txtOwnerEmail.getText().trim(),
                    txtOwnerAddress.getText().trim(),
                    txtOwnerEmergency.getText().trim(),
                    txtGcashNo.getText().trim(),
                    txtGcashName.getText().trim(),
                    txtPaymayaNo.getText().trim(),
                    txtPaymayaName.getText().trim(),
                    txtApartmentName.getText().trim(),
                    txtTinNo.getText().trim(),
                    txtDescription.getText().trim(),
                    txtPolicy.getText().trim(),
                    txtBarangay.getText().trim(),
                    txtStreet.getText().trim(),
                    txtApartmentContact.getText().trim(),
                    txtApartmentEmail.getText().trim(),
                    txtApartmentEmergency.getText().trim()
            );

            if (ok) {
                JOptionPane.showMessageDialog(this, "Registration details updated successfully.");
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update registration details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        leftBox.add(btnSaveProfile);

        JScrollPane leftScroll = new JScrollPane(leftBox);
        leftScroll.setBorder(null);
        leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);

        JPanel rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setOpaque(false);

        JPanel passwordBox = new JPanel();
        passwordBox.setLayout(new BoxLayout(passwordBox, BoxLayout.Y_AXIS));
        passwordBox.setBackground(COLOR_LIST_ITEM);
        passwordBox.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        passwordBox.add(createSubHeader("Change Password"));
        passwordBox.add(Box.createVerticalStrut(10));

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_MAIN_BG);
        txtPass.setForeground(Color.WHITE);
        txtPass.setCaretColor(Color.WHITE);
        txtPass.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        passwordBox.add(createFormField("New Password", txtPass));
        passwordBox.add(Box.createVerticalStrut(20));

        JButton btnApply = createActionButton("APPLY PASSWORD CHANGE", COLOR_BTN_ACTION);
        btnApply.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (isViewOnly) btnApply.setEnabled(false);
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
        passwordBox.add(btnApply);

        rightBox.add(passwordBox);
        rightBox.add(Box.createVerticalGlue());

        grid.add(leftScroll);
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
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(COLOR_LIST_ITEM); panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

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
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, size/2)); btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false); btn.setMaximumSize(new Dimension(size, size)); btn.setBorder(BorderFactory.createEmptyBorder()); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    private JTextField createAccountTextField(String value) {
        JTextField field = new JTextField(value != null ? value : "");
        field.setBackground(COLOR_MAIN_BG);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    private JPanel createFormField(String label, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        wrapper.add(createLabel(label, 14, SwingConstants.LEFT, Font.PLAIN), BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "N/A";
    }

    private double parseMoney(String value) {
        String text = value != null ? value.trim().replace(",", "").replace("₱", "").replace("PHP", "").trim() : "";
        return text.isEmpty() ? 0.0 : Double.parseDouble(text);
    }
    
    private JPanel createPaymentsCard() {
        JPanel card = createBaseCard("Payments");
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(createSubHeader("Pending Tenant Payments"), BorderLayout.NORTH);

        JPanel list = createContainerBox();
        List<String[]> payments = ownerDao.getPendingPayments(currentApartmentId);

        if (payments.isEmpty()) {
            list.add(createListItem("No pending payments.", null, null, null, 80));
        } else {
            BillingDAO billingDAO = new BillingDAO();

            for (String[] p : payments) {
                String txId   = p[0];
                String tenant = p[1];
                String room   = p[2];
                String method = p[3];
                String ref    = p[4];
                String date   = p[5];

                JButton btnApprove = createActionButton("APPROVE", COLOR_BTN_ACTION);
                if (isViewOnly) btnApprove.setEnabled(false);

                btnApprove.addActionListener(e -> {
    try {
        // 1. Find latest unpaid bill for this tenant in this apartment
        int tenantId = Integer.parseInt(tenant);
        int billId = findLatestUnpaidBill(currentApartmentId, tenantId);
        if (billId == -1) {
            JOptionPane.showMessageDialog(this,
                    "No unpaid bill found for this tenant.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Compute remaining balance on that bill
        double remaining = getRemainingBalance(billId);
        if (remaining <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Bill already fully paid.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 3. Apply full remaining balance as payment
        billingDAO.payBill(billId, remaining, date, method, ref);

        // 4. Mark the payment transaction as APPROVED
        markPaymentApproved(Integer.parseInt(txId));

        JOptionPane.showMessageDialog(this, "Payment approved and applied to bill.");
        refreshDashboard();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Error approving payment: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
});


                list.add(createListItem(
                    "Room " + room + " | Tenant ID: " + tenant,
                    method + " (" + ref + ")",
                    "Date: " + date,
                    btnApprove,
                    80
                ));
            }
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        main.add(sp, BorderLayout.CENTER);
        card.add(main, BorderLayout.CENTER);
        return card;
    }

    // Find the most recent unpaid bill for this tenant in this apartment
    private int findLatestUnpaidBill(int apartmentId, int tenantId) {
        String sql = "SELECT bill_id FROM bills " +
                     "WHERE apartment_id = ? AND tenant_id = ? AND paid = 0 " +
                     "ORDER BY bill_id DESC LIMIT 1";

        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, apartmentId);
            ps.setInt(2, tenantId);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("bill_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // no unpaid bill found
    }

    private void markPaymentApproved(int transactionId) {
        String sql = "UPDATE payment_transactions SET status = 'APPROVED' WHERE transaction_id = ?";
        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, transactionId);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private double getRemainingBalance(int billId) {
        String sql = "SELECT total, amount_paid FROM bills WHERE bill_id = ?";
        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                double paid  = rs.getDouble("amount_paid");
                return Math.max(0, total - paid);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }
        
        private void refreshDashboard() {
    this.dispose();
    new OwnerDashboard(currentOwnerId).setVisible(true);
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OwnerDashboard(1).setVisible(true));
    }
}
