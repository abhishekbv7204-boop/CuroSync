package com.curasync.algorithms;

import com.curasync.models.Doctor;
import com.curasync.models.Hospital;
import java.util.ArrayList;
import java.util.List;

public class SearchEngine {

    /**
     * Performs a linear search simulating DFS to find all doctors of a certain specialization.
     * (A true DFS would traverse a tree/graph, but here we treat the list as a flat structure 
     * where we match a target condition)
     */
    public static List<Doctor> searchDoctorsBySpecialization(List<Doctor> allDoctors, String targetSpecialization) {
        List<Doctor> results = new ArrayList<>();
        dfsSearch(allDoctors, targetSpecialization.toLowerCase(), 0, results);
        return results;
    }

    private static void dfsSearch(List<Doctor> doctors, String target, int index, List<Doctor> results) {
        if (index >= doctors.size()) return;

        Doctor d = doctors.get(index);
        if (d.getSpecialization().toLowerCase().contains(target)) {
            results.add(d);
        }
        dfsSearch(doctors, target, index + 1, results); // Deep search the rest
    }

    // --- HOSPITAL DISTANCE WRAPPER ---
    public static class HospitalDistance {
        public Hospital hospital;
        public double distance;

        public HospitalDistance(Hospital hospital, double distance) {
            this.hospital = hospital;
            this.distance = distance;
        }
        
        public Hospital getHospital() { return hospital; }
        public double getDistance() { return distance; }
    }

    // --- MERGE SORT BY DISTANCE ---
    public static void mergeSortHospitalsByDistance(List<HospitalDistance> list, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSortHospitalsByDistance(list, l, m);
            mergeSortHospitalsByDistance(list, m + 1, r);
            mergeHospitalsByDistance(list, l, m, r);
        }
    }

    private static void mergeHospitalsByDistance(List<HospitalDistance> list, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        List<HospitalDistance> L = new ArrayList<>(n1);
        List<HospitalDistance> R = new ArrayList<>(n2);

        for (int i = 0; i < n1; ++i) L.add(list.get(l + i));
        for (int j = 0; j < n2; ++j) R.add(list.get(m + 1 + j));

        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            if (L.get(i).distance <= R.get(j).distance) {
                list.set(k, L.get(i));
                i++;
            } else {
                list.set(k, R.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) {
            list.set(k, L.get(i));
            i++;
            k++;
        }

        while (j < n2) {
            list.set(k, R.get(j));
            j++;
            k++;
        }
    }

    // --- MERGE SORT BY NAME ---
    public static void mergeSortHospitalsByName(List<Hospital> list, int l, int r) {
        if (l < r) {
            int m = l + (r - l) / 2;
            mergeSortHospitalsByName(list, l, m);
            mergeSortHospitalsByName(list, m + 1, r);
            mergeHospitalsByName(list, l, m, r);
        }
    }

    private static void mergeHospitalsByName(List<Hospital> list, int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        List<Hospital> L = new ArrayList<>(n1);
        List<Hospital> R = new ArrayList<>(n2);

        for (int i = 0; i < n1; ++i) L.add(list.get(l + i));
        for (int j = 0; j < n2; ++j) R.add(list.get(m + 1 + j));

        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            if (L.get(i).getName().compareToIgnoreCase(R.get(j).getName()) <= 0) {
                list.set(k, L.get(i));
                i++;
            } else {
                list.set(k, R.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) {
            list.set(k, L.get(i));
            i++;
            k++;
        }

        while (j < n2) {
            list.set(k, R.get(j));
            j++;
            k++;
        }
    }

    // --- BINARY SEARCH FOR HOSPITALS BY NAME ---
    public static Hospital binarySearchHospitalByName(List<Hospital> hospitals, String targetName) {
        if (hospitals == null || hospitals.isEmpty() || targetName == null || targetName.trim().isEmpty()) {
            return null;
        }

        // Create a copy and sort it by name using Merge Sort
        List<Hospital> sorted = new ArrayList<>(hospitals);
        mergeSortHospitalsByName(sorted, 0, sorted.size() - 1);

        int l = 0, r = sorted.size() - 1;
        String target = targetName.toLowerCase().trim();
        while (l <= r) {
            int m = l + (r - l) / 2;
            String mName = sorted.get(m).getName().toLowerCase().trim();
            int comp = mName.compareTo(target);
            if (comp == 0) {
                return sorted.get(m);
            }
            if (comp < 0) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }
        
        // Linear fallback for partial contains match
        for (Hospital h : sorted) {
            if (h.getName().toLowerCase().contains(target)) {
                return h;
            }
        }
        return null;
    }
}

