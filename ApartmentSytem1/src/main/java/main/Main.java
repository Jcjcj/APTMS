package main;

import com.mycompany.apartmentsytem1.DatabaseSetup;
import com.mycompany.apartmentsytem1.DataBaseSeeder;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize the SQLite database and tables in the current folder[cite: 6]
        DatabaseSetup.createTables(); 
        
        // 2. Seed the necessary login accounts[cite: 5]
        DataBaseSeeder.seedMassiveData(); 
        
        // 3. Launch the UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LandingPage().setVisible(true);
        });
    }
}