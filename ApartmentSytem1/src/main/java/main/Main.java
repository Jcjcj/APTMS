package main;

import com.mycompany.apartmentsytem1.DatabaseSetup;
import com.mycompany.apartmentsytem1.DataBaseSeeder;
import com.mycompany.apartmentsytem1.BillingDAO;
import com.mycompany.apartmentsytem1.PenaltyManager;
import com.mycompany.apartmentsytem1.PenaltyScheduler;

public class Main {
    public static void main(String[] args) {
        // DEBUG: check paths
        System.out.println("Working dir: " + new java.io.File(".").getAbsolutePath());
        System.out.println("Uploads dir: " + com.mycompany.apartmentsytem1.FileStorageUtil.getUploadPath());

        // 1. Initialize Database Tables
        DatabaseSetup.createTables(); 
        
        // 2. Seed Super Admin Accounts
        DataBaseSeeder.seedMassiveData(); 
        
        BillingDAO billingDAO = new BillingDAO();
        PenaltyManager penaltyManager = new PenaltyManager(billingDAO);

        // Run once immediately when app opens
        penaltyManager.applyPenaltiesForOverdueBills();

        // Keep checking once per day while app is open
        PenaltyScheduler.startScheduler(penaltyManager);

        
        // 3. Launch UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LandingPage().setVisible(true);
        });
    }
}