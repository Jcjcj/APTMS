package com.mycompany.apartmentssystem1;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotHelper {

    // returns all 1-hour time slots for viewing
    // frontend: call this to populate dropdown
    public static List<String[]> getAllSlots() {
        
        List<String[]> slots = new ArrayList<>();
        
        // 7:00 AM - 8:00 AM
        slots.add(new String[]{"07:00", "08:00"});
        // 8:00 AM - 9:00 AM
        slots.add(new String[]{"08:00", "09:00"});
        // 9:00 AM - 10:00 AM
        slots.add(new String[]{"09:00", "10:00"});
        // 10:00 AM - 11:00 AM
        slots.add(new String[]{"10:00", "11:00"});
        
        // skip 11:00 AM - 1:00 PM (lunch break)
        
        // 1:00 PM - 2:00 PM
        slots.add(new String[]{"13:00", "14:00"});
        // 2:00 PM - 3:00 PM
        slots.add(new String[]{"14:00", "15:00"});
        // 3:00 PM - 4:00 PM
        slots.add(new String[]{"15:00", "16:00"});
        
        return slots;
    }
}