/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author carlj
 */
import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class Owner {
    
    Scanner sc = new Scanner(System.in);
    
    public Owner()
    {
        System.out.println("Rent per month: ");
        double rentPerMonth = sc.nextDouble();
        
        System.out.println("Rent Payment Date in Month: ");
        int rentDay = sc.nextInt();
        
        System.out.println("Water and Electricty Bill (submetrics/fixed amount): ");
        
        System.out.println("Bill for water and electricty: ");
        System.out.println("If submetrics, peso/watts or meter: ");
        System.out.println("If fixed amount, enter amount: ");
        int waterAndElectricityBill = sc.nextInt();
        
        System.out.println("Enter username: ");
        String ownerUsername = sc.nextLine();
        
        System.out.println("Enter password ");
        String ownerPass1 = sc.nextLine();
        
        System.out.println("Confirm password ");
        String ownerPass2 = sc.nextLine();
        
        if(ownerPass1 != ownerPass2)
        {
            System.out.println("Password don't match!");
            return;
        }
        else
        {
            System.out.println("Account Successfully Created!");
            System.out.println("Please Log-in with your account");
            
            login(ownerUsername, ownerPass1);
        }
    }
    
    public void login(String ownerUsername, String ownerPass1)
    {
        System.out.println("Enter Username: ");
        String ownerUsername1 = sc.nextLine();
        
        System.out.println("Enter password ");
        String ownerPass3 = sc.nextLine();
        
        int attempts = 3;
        while(attempts != 0)
        {
            if(!ownerUsername1.equals(ownerUsername))
            {
                System.out.println("Invalid username");
                attempts--;
            }
            if(!ownerPass3.equals(ownerPass1))
            {
                System.out.println("Invalid password");
                attempts--;
            }
            if(attempts == 0)
            {
                System.out.println("Please close the program and try again.");
            }
        }
    }
}
