package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchWindow extends JFrame {

    // Theme Colors
    private final Color COLOR_BG_DARK = new Color(0, 35, 20);
    private final Color COLOR_HEADER = new Color(0, 102, 51);
    private final Color COLOR_TEXT = Color.WHITE;

    private JPanel gridPanel;

    public SearchWindow(String initialBarangay) {
        this.setTitle("Search Results - " + initialBarangay);
        this.setSize(1100, 750);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        // =========================================================
        // 1. HEADER PANEL
        // =========================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        logoPanel.setOpaque(false);
        URL logoUrl = getClass().getResource("/main/logowhite.png");
        JLabel logoLabel = new JLabel("<html>Apartment<br>Management<br>System</html>");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(COLOR_TEXT);
        if (logoUrl != null) {
            Image scaledLogo = new ImageIcon(logoUrl).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        }
        logoPanel.add(logoLabel);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        searchBarPanel.setOpaque(false);
        
        JComboBox<String> dropdown = new JComboBox<>(barangayList);
        dropdown.setSelectedItem(initialBarangay); 
        dropdown.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dropdown.setPreferredSize(new Dimension(300, 40));
        dropdown.setBackground(Color.WHITE);
        dropdown.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        dropdown.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowBtn = super.createArrowButton(); 
                arrowBtn.setBackground(new Color(0, 102, 51)); 
                arrowBtn.setForeground(Color.WHITE); 
                arrowBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
                arrowBtn.setFocusPainted(false);
                return arrowBtn;
            }
        });

        dropdown.addActionListener(e -> {
            String newBarangay = dropdown.getSelectedItem().toString();
            this.setTitle("Search Results - " + newBarangay);
            refreshGrid(newBarangay); 
        });

        searchBarPanel.add(dropdown);
        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(searchBarPanel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);

        // =========================================================
        // 2. APARTMENT GRID PANEL
        // =========================================================
        gridPanel = new JPanel(new GridLayout(0, 3, 30, 40));
        gridPanel.setBackground(COLOR_BG_DARK);
        gridPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        refreshGrid(initialBarangay);

        // =========================================================
        // 3. SCROLL PANE
        // =========================================================
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG_DARK);

        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshGrid(String barangay) {
        gridPanel.removeAll(); 
        List<ApartmentData> apartments = fetchApartmentsFromDatabase(barangay);

        if (apartments.isEmpty()) {
            JLabel noResults = new JLabel("No apartments found in " + barangay);
            noResults.setForeground(COLOR_TEXT);
            noResults.setFont(new Font("Segoe UI", Font.BOLD, 24));
            gridPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            gridPanel.add(noResults);
        } else {
            gridPanel.setLayout(new GridLayout(0, 3, 30, 40)); 
            for (ApartmentData apt : apartments) {
                gridPanel.add(createApartmentCard(apt));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createApartmentCard(ApartmentData apt) {
        JPanel outerCard = new JPanel(new BorderLayout());
        outerCard.setOpaque(false);

        JPanel card = new JPanel(new BorderLayout(0, 10)); 
        card.setOpaque(false);

        JLabel imageLabel = new JLabel();
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0, 102, 51)); 
        imageLabel.setPreferredSize(new Dimension(300, 200)); 
        
        File imgFile = new File("uploads/" + apt.imageFileName);
        if (imgFile.exists() && !apt.imageFileName.isEmpty()) {
            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
            Image scaledImg = icon.getImage().getScaledInstance(320, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imageLabel.setText("No Image Available");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setForeground(Color.WHITE);
        }
        card.add(imageLabel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);

        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);

        JLabel nameLabel = new JLabel(apt.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(COLOR_TEXT);

        JLabel addressLabel = new JLabel(apt.barangay + ", " + apt.street);
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressLabel.setForeground(Color.LIGHT_GRAY);

        JLabel vacantLabel = new JLabel(apt.vacantRooms + " Vacant Rooms");
        vacantLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        vacantLabel.setForeground(Color.WHITE);

        leftInfo.add(nameLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 2))); 
        leftInfo.add(addressLabel);
        leftInfo.add(Box.createRigidArea(new Dimension(0, 2)));
        leftInfo.add(vacantLabel);

        JLabel priceLabel = new JLabel(String.format("₱ %,.2f / mos", apt.rent));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceLabel.setForeground(COLOR_TEXT);
        priceLabel.setVerticalAlignment(SwingConstants.TOP); 

        infoPanel.add(leftInfo, BorderLayout.WEST);
        infoPanel.add(priceLabel, BorderLayout.EAST);

        card.add(infoPanel, BorderLayout.SOUTH);
        
        outerCard.add(card, BorderLayout.NORTH);
        outerCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // --- NEW: Open Room Details when clicked ---
        outerCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Opens the new Room Details Window and passes the apartment data
                new RoomDetailsWindow(apt).setVisible(true);
            }
        });
        
        return outerCard;
    }

    public static class ApartmentData {
        String name, barangay, street, imageFileName;
        int vacantRooms;
        double rent;

        public ApartmentData(String n, String b, String s, int v, double r, String img) {
            this.name = n; this.barangay = b; this.street = s;
            this.vacantRooms = v; this.rent = r; this.imageFileName = img;
        }
    }

    private List<ApartmentData> fetchApartmentsFromDatabase(String barangay) {
        List<ApartmentData> list = new ArrayList<>();
        
        // This query fetches real, approved apartments in the selected barangay
        // It also finds the lowest available rent to display as the "starting from" price.
        String sql = "SELECT a.apartment_name, a.barangay, a.street, a.rooms_available, a.profile_image, " +
                     "MIN(r.rent_amount) as starting_rent " +
                     "FROM apartments a " +
                     "LEFT JOIN rooms r ON a.apartment_id = r.apartment_id AND r.status = 'Available' " +
                     "WHERE a.barangay = ? AND a.is_active = 1 AND a.approval_status = 'APPROVED' " +
                     "GROUP BY a.apartment_id";

        try (java.sql.Connection conn = com.mycompany.apartmentsytem1.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, barangay);
            java.sql.ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("apartment_name");
                String brgy = rs.getString("barangay");
                String street = rs.getString("street");
                int vacant = rs.getInt("rooms_available");
                double rent = rs.getDouble("starting_rent");
                String img = rs.getString("profile_image");
                
                // If there are no available rooms with prices, default to 0.0
                if (rs.wasNull()) {
                    rent = 0.0;
                }
                if (img == null) {
                    img = "";
                }

                // Add the real apartment to the UI grid
                list.add(new ApartmentData(name, brgy, street, vacant, rent, img));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    private static final String[] barangayList = {
        "Adlaon", "Agsungot", "Apas", "Bacayan", "Babag", "Banilad", "Basak Pardo", "Basak San Nicolas",
        "Binaliw", "Bonbon", "Budla-an", "Buhisan", "Bulacao", "Buot-Taup Pardo", "Busay", "Calamba", "Cambinocot",
        "Camputhaw", "Capitol Site", "Carreta", "Central", "Cogon Ramos", "Cogon Pardo", "Day-as", "Duljo", "Ermita",
        "Guadalupe", "Guba", "Hippodromo", "Inayawan", "Kalubihan", "Kalunasan", "Kamagayan", "Kasambagan",
        "Kinasang-an Pardo", "Labangon", "Lahug", "Lorega", "Lusaran", "Luz", "Mabini", "Mabolo", "Malubog",
        "Mambaling", "Pahina Central", "Pahina San Nicolas", "Pamutan", "Pardo", "Pari-an", "Paril", "Pasil", "Pit-os",
        "Pulangbato", "Pung-ol-Sibugay", "Punta Princesa", "Quiot Pardo", "Sambag I", "Sambag II", "San Antonio",
        "San Jose", "San Nicolas Central", "San Roque", "Santa Cruz", "Sapangdaku", "Sawang Calero", "Sinsin", "Sirao",
        "Suba Poblacion", "Sudlon I", "Sudlon II", "Tagbao", "Talamban", "Taptap", "Tejero", "Tinago", "Tisa", "To-ong Pardo",
        "T. Padilla", "Zapatera"
    };
}