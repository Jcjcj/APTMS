package com.mycompany.apartmentsytem1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PenaltyScheduler {
    public static void startScheduler(PenaltyManager penaltyManager) { // Lowercase instance here
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // Run once every 24 hours
        scheduler.scheduleAtFixedRate(() -> {
          penaltyManager.applyPenaltiesForOverdueBills(); // FIXED: Use lowercase 'p'
        }, 0, 1, TimeUnit.DAYS);
    }
}

