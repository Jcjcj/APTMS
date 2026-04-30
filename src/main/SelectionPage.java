package main;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.CardLayout;

public class SelectionPage extends JFrame implements ActionListener{
    CardLayout cl = new CardLayout();
    JPanel mainPanel = new JPanel(cl);
    JButton owner;
    JButton tenant;
    JLabel usertype;
    
    public SelectionPage()
    {
        mainPanel.setBackground(new Color(0, 102, 51));

        JPanel selectionPage = new JPanel(null);
        selectionPage.setBackground(new Color(0, 102, 51));
        mainPanel.add(selectionPage, "Selection");
        this.setContentPane(mainPanel);
       
        owner = new JButton("Owner");
        tenant = new JButton("Tenant");
        usertype = new JLabel("I'm the");
        usertype.setFont(new Font("Arial",Font.BOLD,45));
        usertype.setForeground(Color.WHITE);
        usertype.setBounds(150, 100, 500, 100);
        usertype.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        owner.setFont(new Font("Arial",Font.BOLD,20));
        owner.setForeground(Color.WHITE);
        owner.setBackground(new Color(0, 153, 76));
        owner.setBounds(200, 250, 400, 40);
        owner.setFocusable(false);
        owner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        tenant.setFont(new Font("Arial",Font.BOLD,20));
        tenant.setForeground(Color.WHITE);
        tenant.setBackground(new Color(0, 153, 76));
        tenant.setBounds(200, 310, 400, 40);
        tenant.setFocusable(false);
        tenant.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        selectionPage.add(owner);
        selectionPage.add(tenant);
        selectionPage.add(usertype);
        
        owner.addActionListener(this);
        tenant.addActionListener(this);
        
        this.setTitle("User Registration");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == owner)
        {
            // Open owner registration in a new maximized window
            OwnerRegistration ownerReg = new OwnerRegistration();
            ownerReg.setVisible(true);
            
            this.dispose();
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            SelectionPage reg = new SelectionPage();
            reg.setVisible(true);
        });
    }
}