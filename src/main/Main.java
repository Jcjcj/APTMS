/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package main;

import java.awt.*;
import javax.swing.*;
public class Main {

    public static void main(String[] args) {
        
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        
        ImageIcon icon = new ImageIcon(Main.class.getResource("icon.png"));
        label.setText("Enter Username: ");
        frame.setTitle("Apartment Management System");
       // OwnerRegistration register = new OwnerRegistration();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        
        frame.setIconImage(icon.getImage());
        frame.setVisible(true);
        
        frame.add(label);
      //  frame.add(register);
      new LandingPage();
    }
}
