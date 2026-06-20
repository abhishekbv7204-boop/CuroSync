package com.curasync.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DistrictLoader {
    
    public static Map<String, List<String>> loadStatesAndDistricts() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        File file = new File("states-and-districts.json");
        if (!file.exists()) {
            System.err.println("[DistrictLoader] states-and-districts.json not found! Using Karnataka fallback.");
            map.put("Karnataka", Arrays.asList("Bagalkot", "Ballari (Bellary)", "Belagavi (Belgaum)", "Bengaluru (Bangalore) Rural", "Bengaluru (Bangalore) Urban", "Bidar", "Chamarajanagar", "Chikballapur", "Chikkamagaluru (Chikmagalur)", "Chitradurga", "Dakshina Kannada", "Davangere", "Dharwad", "Gadag", "Hassan", "Haveri", "Kalaburagi (Gulbarga)", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru (Mysore)", "Raichur", "Ramanagara", "Shivamogga (Shimoga)", "Tumakuru (Tumkur)", "Udupi", "Uttara Kannada (Karwar)", "Vijayapura (Bijapur)", "Yadgir"));
            return map;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            String currentState = null;
            List<String> currentDistricts = null;
            boolean inDistricts = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("\"state\"")) {
                    int colon = line.indexOf(":");
                    int start = line.indexOf("\"", colon + 1);
                    int end = line.indexOf("\"", start + 1);
                    if (start != -1 && end != -1) {
                        currentState = line.substring(start + 1, end).trim();
                        currentDistricts = new ArrayList<>();
                        map.put(currentState, currentDistricts);
                    }
                } else if (line.contains("\"districts\"")) {
                    inDistricts = true;
                } else if (inDistricts) {
                    if (line.contains("]")) {
                        inDistricts = false;
                    } else {
                        int start = line.indexOf("\"");
                        int end = line.indexOf("\"", start + 1);
                        if (start != -1 && end != -1) {
                            String district = line.substring(start + 1, end).trim();
                            district = district.replace("&amp;", "&");
                            if (currentState != null && currentDistricts != null) {
                                currentDistricts.add(district);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
