package com.curasync.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scheduler {
    
    public static class TimeSlot implements Comparable<TimeSlot> {
        public String slot; // e.g., "09:00", "10:00"
        public int startHour;
        
        public TimeSlot(String slot, int startHour) {
            this.slot = slot;
            this.startHour = startHour;
        }

        @Override
        public int compareTo(TimeSlot o) {
            return Integer.compare(this.startHour, o.startHour);
        }
        
        @Override
        public String toString() { return slot; }
    }

    /**
     * Greedy algorithm to schedule appointments. 
     * Given a list of requested slots, we want to maximize the number of non-overlapping appointments.
     * (Assuming each appointment is 1 hour).
     */
    public static List<TimeSlot> greedySchedule(List<TimeSlot> requestedSlots) {
        // Step 1: Sort by finish time (for 1 hour slots, sorting by start time is equivalent)
        Collections.sort(requestedSlots);
        
        List<TimeSlot> scheduled = new ArrayList<>();
        if (requestedSlots.isEmpty()) return scheduled;

        // Step 2: Greedily pick the first slot and any subsequent slot that doesn't overlap
        TimeSlot lastPicked = requestedSlots.get(0);
        scheduled.add(lastPicked);

        for (int i = 1; i < requestedSlots.size(); i++) {
            TimeSlot current = requestedSlots.get(i);
            // Assuming 1-hour duration, they don't overlap if current start >= lastPicked start + 1
            if (current.startHour >= lastPicked.startHour + 1) {
                scheduled.add(current);
                lastPicked = current;
            }
        }

        return scheduled;
    }
}
