package com.mycompany.apartmentsytem1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PenaltyScheduler {
    public static void startScheduler(PenaltyManager penaltyManager) { 
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Run once every 24 hours
        scheduler.scheduleAtFixedRate(() -> {
            
            // 1. First, calculate and apply the mathematical penalties
            penaltyManager.applyPenaltiesForOverdueBills(); 
            
            // --- NEW SCHEDULED ALERTS GO HERE ---
            // 2. Trigger the notification engine to write messages to the database
            try {
                NotificationDAO notificationEngine = new NotificationDAO();
                notificationEngine.generateUpcomingBillAlerts();      // Warns tenants 3 days before due date
                notificationEngine.generateLateBillAlerts();          // Alerts tenant AND owner of delayed bills
                notificationEngine.generateOwnerSubscriptionAlerts(); // Warns owners if 2% fee is due in 3 days
                
                System.out.println("Daily Background Check: Penalties and Notifications successfully processed.");
            } catch (Exception e) {
                System.err.println("Daily Background Check Error: " + e.getMessage());
            }
            // ------------------------------------
            
        }, 0, 1, TimeUnit.DAYS);
    }
}