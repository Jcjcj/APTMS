package com.mycompany.apartmentsytem1;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotHelper {
    public static List<String[]> getAllSlots() {
        List<String[]> slots = new ArrayList<>();
        slots.add(new String[]{"07:00", "08:00"});
        slots.add(new String[]{"08:00", "09:00"});
        slots.add(new String[]{"09:00", "10:00"});
        slots.add(new String[]{"10:00", "11:00"});
        slots.add(new String[]{"13:00", "14:00"});
        slots.add(new String[]{"14:00", "15:00"});
        slots.add(new String[]{"15:00", "16:00"});
        return slots;
    }
}