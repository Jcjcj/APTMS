
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.border.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author carlj
 */
public class MainPage extends javax.swing.JFrame {
    
    /**
     * Creates new form MainPage
     */
    private JPanel sideMenu;
    private boolean isMenuOpen = false;
    private final int MAX_WIDTH = 250; // The final width of your green menu
    BackgroundPanel bg;
   // FlatLightLaf.setup();
    
public MainPage() {
    // 1. Initialize NetBeans generated components
    initComponents();
    
    // 2. Create the stretching background
    // Ensure "Background.png" is in your source package root
    bg = new BackgroundPanel("/Background.png");
    
   jTextField1.setBorder(javax.swing.UIManager.getBorder("TextField.border"));
   
   sideMenu = new JPanel();
    sideMenu.setBackground(new java.awt.Color(0, 102, 51)); //Dark green
    sideMenu.setLayout(null);
    sideMenu.setBounds(getWidth(), 0, 0, getHeight()); // Start hidden at 0 width

    bg.add(sideMenu);
    bg.setComponentZOrder(sideMenu, 0); // Keep it on top of the search bar
    
   ImageIcon searchIcon = new ImageIcon(getClass().getResource("/searchicon_1.png"));
   
   ImageIcon userIcon = new ImageIcon(getClass().getResource("/usericon.png"));
   
    //1. Set the search icon to appear on the left side of the text field
    jTextField1.putClientProperty("JTextField.leadingIcon", new javax.swing.ImageIcon(getClass().getResource("/searchicon_1.png")));
    
     //2. Makes the text field ends perfectly round
    jTextField1.putClientProperty("JTextField.arc", 999); 
    
    //3. Forces the text field to use the FlatLaf styling rules
    jTextField1.putClientProperty("JComponent.roundRect", true);

    // 4. Set the background as the Content Pane
    // This makes the image the very bottom layer that stretches
    this.setContentPane(bg);
    
    // 5. Set the Layout of the background so it can hold your components
    bg.setLayout(null);     
    // 6. ADD your components to the new background
    //bg.add(jButton1);

    // 7. FlatLaf & Style Customizations
    jTextField1.putClientProperty("JTextField.placeholderText", "Search apartment or place");
    // 8. Color and Text
    jTextField1.setBackground(new Color(7, 66, 38)); // Match the dark green
    jTextField1.setForeground(Color.WHITE);          // White text
    jTextField1.setCaretColor(Color.WHITE);          // White blinking cursor

    // 9. Remove Border
    jTextField1.setBorder(null);

    // 10. Rounded Corners (FlatLaf property)
    jTextField1.putClientProperty("JTextField.arc", 20);
    
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
    @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            int w = getWidth();
            int h = getHeight();

            // Position search bar on the left side (matching your split-screen design)
            int searchWidth = (int) (w * 0.35);
            int searchX = 50; // Padding from the left edge
            int searchY = (int) (h * 0.3); // Positioned in the white area

            jTextField1.setBounds(searchX, searchY, searchWidth, 45);

            // Keep user button on the top right
            //jButton1.setBounds(w - 120, 30, 80, 80);

            // Keep the side menu pinned to the right edge during resize
            if (isMenuOpen) {
                sideMenu.setBounds(w - MAX_WIDTH, 0, MAX_WIDTH, h);
            } else {
                sideMenu.setBounds(w, 0, 0, h);
            }
        }

    // Keep side menu on the far right
        int currentMenuWidth = sideMenu.getWidth();
      //  sideMenu.setBounds(getWidth() - currentMenuWidth, 0, currentMenuWidth, getHeight());
    });
    
    /*jButton1.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent enter)
            {
                jButton1.setContentAreaFilled(true);
                jButton1.setBackground(Color.darkGray);
            }
        
        @Override
        public void mouseExited(MouseEvent exit)
            {
                jButton1.setContentAreaFilled(false);
                jButton1.setBackground(null);
            }
    });*/
 
    // 8. Make components transparent so the background shows through
    jTextField1.setOpaque(true); 
    bg.add(jTextField1);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField1.setBackground(new java.awt.Color(51, 138, 73));
        jTextField1.setForeground(new java.awt.Color(0, 0, 0));
        jTextField1.setToolTipText("");
        jTextField1.setBorder(null);
        jTextField1.setCaretColor(new java.awt.Color(0, 0, 0));
        jTextField1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextField1.setName(""); // NOI18N
        jTextField1.addCaretListener(this::jTextField1CaretUpdate);
        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 210, 390, 40));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Background.png"))); // NOI18N
        jLabel1.setFocusable(false);
        jLabel1.setRequestFocusEnabled(false);
        jLabel1.setVerifyInputWhenFocusTarget(false);
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 730, 400));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1CaretUpdate
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1CaretUpdate

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // 1. Setup the theme
        com.formdev.flatlaf.FlatLightLaf.setup(); 

        // 2. Open the window 
        java.awt.EventQueue.invokeLater(() -> {
            new MainPage().setVisible(true);
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}