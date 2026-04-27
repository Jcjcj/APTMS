

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author carlj
 */
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.CardLayout;

import com.formdev.flatlaf.FlatLightLaf;

//import java.awt.;
public class Registration extends JFrame implements ActionListener{
    CardLayout cl = new CardLayout();
    JPanel mainPanel = new JPanel(cl);
    BackgroundPanel selectionPage;
    BackgroundPanel ownerPage;
    JButton owner;
    JButton tenant;
    JLabel usertype;
    JTextField nameOwner;
    JLabel askName;
    JPanel ownerReg;
    
    public Registration()
    {
        ownerReg = new JPanel();
        ownerReg.setBackground(new java.awt.Color(0, 102, 51)); //Dark green
        ownerReg.setLayout(null);
        ownerReg.setBounds(getWidth(), 0, 0, getHeight()); 
        
        selectionPage = new BackgroundPanel ("/background.jpg");
        selectionPage.setLayout(null);
        
        ownerPage = new BackgroundPanel("/background.jpg");
        ownerPage.setLayout(null);
        
        mainPanel.add(selectionPage, "Selection");
        mainPanel.add(ownerPage, "OwnerPage");
        this.setContentPane(mainPanel);
       
        owner = new JButton("Owner");
        tenant = new JButton("Tenant");
        usertype = new JLabel("I'm the...");
        usertype.setFont(new Font("Arial",Font.BOLD,45));
        
        askName = new JLabel("Enter Full Name: ");
        askName.setFont(new Font("Arial",Font.BOLD,13));
        nameOwner = new JTextField();
        nameOwner.putClientProperty("JTextField.arc", 15);
        
        selectionPage.add(owner);
        selectionPage.add(tenant);
        selectionPage.add(usertype);
        
        ownerPage.add(askName);
        ownerPage.add(nameOwner);
        
        owner.addActionListener(this);
        tenant.addActionListener(this);
        
        this.setTitle("Owner Registration");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e)
                {
                    int containerWidth = getContentPane().getWidth();
                    int containerHeight = getContentPane().getHeight();
                    int newWidth = (int) (containerWidth * 0.4);
                     if(newWidth > 400)
                        {
                            newWidth = 400;
                        }
                    int newHeight = 35;
                    int newX = (getWidth() - newWidth) / 2;
                    int newY = (containerHeight - (newHeight * 2 + 20)) / 2;

                    owner.setBackground(Color.RED);
                    tenant.setBackground(Color.GREEN);
                    owner.setBounds(newX, newY, newWidth, newHeight);
                    tenant.setBounds(newX, newY + 60, newWidth, newHeight);
                   
                    // Centering the Label
                    int labelWidth = 500;
                    int labelX = (containerWidth - labelWidth) / 2; // Centers the label relative to window
                    usertype.setBounds(labelX, newY - 100, labelWidth, 100);
                    // Align text inside the label to the middle
                    usertype.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    
                    askName.setBounds(newX, newY- 75, newWidth,newHeight);
                    nameOwner.setBounds(newX, newY - 50, newWidth, newHeight);
                            
                    mainPanel.revalidate();
                    mainPanel.repaint();
                }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == owner)
        {
            cl.show(mainPanel, "OwnerPage");
            nameOwner.requestFocusInWindow();
        }
    }
}