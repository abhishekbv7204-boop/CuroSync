package com.curasync.algorithms;

import com.curasync.models.EmergencyCase;
import java.util.PriorityQueue;

public class EmergencyQueue {
    // Max-Heap priority queue based on severity level (via Comparable in EmergencyCase)
    private PriorityQueue<EmergencyCase> queue;

    public EmergencyQueue() {
        queue = new PriorityQueue<>();
    }

    public void addCase(EmergencyCase eCase) {
        queue.add(eCase);
    }

    public EmergencyCase getNextCase() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    public int size() {
        return queue.size();
    }

    public PriorityQueue<EmergencyCase> getQueue() {
        return queue;
    }
}
