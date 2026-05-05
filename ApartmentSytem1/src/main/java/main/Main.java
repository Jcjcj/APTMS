package main;

import com.mycompany.apartmentsytem1.DatabaseSetup;
import com.mycompany.apartmentsytem1.DataBaseSeeder;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database Tables
        DatabaseSetup.createTables(); 
        
        // 2. Seed Super Admin Accounts
        DataBaseSeeder.seedMassiveData(); 
        
        // 3. Launch UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LandingPage().setVisible(true);
        });
    }
}