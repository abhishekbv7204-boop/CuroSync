package com.curasync.utils;

import com.curasync.models.Hospital;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches real hospitals from OpenStreetMap Overpass API using
 * bounding-box queries (reliable, no area-name matching issues).
 * Geocodes districts via Nominatim to get centre coordinates.
 */
public class OverpassClient {

    // Radius in degrees (~0.45 deg ≈ 50 km at Indian latitudes)
    private static final double BBOX_RADIUS = 0.45;

    // Primary and fallback Overpass endpoints
    private static final String[] OVERPASS_URLS = {
        "https://overpass-api.de/api/interpreter",
        "https://lz4.overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    };

    // -------------------------------------------------------------------------
    //  Public API
    // -------------------------------------------------------------------------

    public static List<Hospital> fetchHospitalsForDistrict(String district) {
        double[] centroid = geocodeDistrict(district);
        double centerLat = centroid[0];
        double centerLng = centroid[1];

        System.out.println("[OverpassClient] Centre for " + district + ": " + centerLat + ", " + centerLng);

        List<Hospital> list = fetchViaBBox(district, centerLat, centerLng, BBOX_RADIUS);

        if (list.isEmpty()) {
            System.out.println("[OverpassClient] No real hospitals found; using generated fallback data.");
            list = generateFallbackHospitals(district, centerLat, centerLng);
        } else {
            System.out.println("[OverpassClient] Fetched " + list.size() + " real hospitals for " + district);
        }
        return list;
    }

    /** Geocode a district name to lat/lng via Nominatim. */
    public static double[] geocodeDistrict(String district) {
        String cleanDistrict = district;
        if (district.contains("(")) {
            cleanDistrict = district.substring(0, district.indexOf("(")).trim();
        }

        // Try "District, India" then just "District"
        String[] queries = {
            cleanDistrict + " District, India",
            cleanDistrict + ", India",
            cleanDistrict
        };

        for (String q : queries) {
            double[] result = nominatimGeocode(q);
            if (result != null) {
                System.out.println("[OverpassClient] Geocoded '" + q + "' → " + result[0] + ", " + result[1]);
                return result;
            }
        }

        // Hard-coded fallbacks for common Karnataka districts
        switch (cleanDistrict.toLowerCase()) {
            case "ballari": case "bellary": return new double[]{15.1394, 76.9214};
            case "bengaluru": case "bangalore": return new double[]{12.9716, 77.5946};
            case "mysuru": case "mysore": return new double[]{12.2958, 76.6394};
            case "hubli": case "dharwad": return new double[]{15.3647, 75.1240};
            case "mangaluru": case "mangalore": return new double[]{12.9141, 74.8560};
            case "belagavi": case "belgaum": return new double[]{15.8497, 74.4977};
            case "kalaburagi": case "gulbarga": return new double[]{17.3297, 76.8343};
            case "shivamogga": case "shimoga": return new double[]{13.9299, 75.5681};
            case "tumakuru": case "tumkur": return new double[]{13.3400, 77.1000};
            case "vijayapura": case "bijapur": return new double[]{16.8302, 75.7100};
            default: return new double[]{12.9716, 77.5946}; // Bengaluru default
        }
    }

    // -------------------------------------------------------------------------
    //  Private helpers
    // -------------------------------------------------------------------------

    /** Call Nominatim and extract first lat/lng result. */
    private static double[] nominatimGeocode(String query) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + "&format=json&limit=1";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "CuraSync-HospitalFinder/2.0 (edu project)")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String json = response.body();
                if (json.trim().equals("[]")) return null;

                int latIdx = json.indexOf("\"lat\":\"");
                int lonIdx = json.indexOf("\"lon\":\"");
                if (latIdx != -1 && lonIdx != -1) {
                    int latEnd = json.indexOf("\"", latIdx + 7);
                    int lonEnd = json.indexOf("\"", lonIdx + 7);
                    double lat = Double.parseDouble(json.substring(latIdx + 7, latEnd));
                    double lon = Double.parseDouble(json.substring(lonIdx + 7, lonEnd));
                    return new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            System.err.println("[OverpassClient] Nominatim error for '" + query + "': " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetch hospitals inside a bounding box centred at (lat, lng) ± radius.
     * Uses POST to avoid URL-length limits. Tries multiple Overpass mirrors.
     */
    private static List<Hospital> fetchViaBBox(String district, double lat, double lng, double radius) {
        double south = lat - radius;
        double north = lat + radius;
        double west  = lng - radius;
        double east  = lng + radius;

        // Query: nodes, ways and relations tagged amenity=hospital inside bbox
        String query = "[out:json][timeout:25];\n"
                + "(\n"
                + "  node[\"amenity\"=\"hospital\"]("+south+","+west+","+north+","+east+");\n"
                + "  way[\"amenity\"=\"hospital\"]("+south+","+west+","+north+","+east+");\n"
                + "  relation[\"amenity\"=\"hospital\"]("+south+","+west+","+north+","+east+");\n"
                + ");\n"
                + "out center tags;";

        System.out.println("[OverpassClient] BBox query: S=" + south + " W=" + west + " N=" + north + " E=" + east);

        for (String baseUrl : OVERPASS_URLS) {
            try {
                String body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", "CuraSync-HospitalFinder/2.0 (edu project)")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                System.out.println("[OverpassClient] Trying " + baseUrl + " ...");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<Hospital> hospitals = parseHospitalsJson(response.body(), district);
                    if (!hospitals.isEmpty()) {
                        System.out.println("[OverpassClient] Got " + hospitals.size() + " hospitals from " + baseUrl);
                        return hospitals;
                    }
                } else {
                    System.err.println("[OverpassClient] HTTP " + response.statusCode() + " from " + baseUrl);
                }
            } catch (Exception e) {
                System.err.println("[OverpassClient] Error with " + baseUrl + ": " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    /** Parse the Overpass JSON response into Hospital objects. */
    private static List<Hospital> parseHospitalsJson(String json, String district) {
        List<Hospital> hospitals = new ArrayList<>();
        int elementsStart = json.indexOf("\"elements\":");
        if (elementsStart == -1) {
            System.err.println("[OverpassClient] No 'elements' key in response.");
            return hospitals;
        }

        int startBracket = json.indexOf("[", elementsStart);
        if (startBracket == -1) return hospitals;

        String[] specializationsPool = {
            "Cardiology, General Medicine",
            "Neurology, General Medicine",
            "Orthopedics, Trauma",
            "Pediatrics, General Medicine",
            "Oncology, General Medicine",
            "Cardiology, Trauma",
            "Neurology, Pediatrics, Orthopedics",
            "General Medicine, Trauma",
            "Cardiology, Oncology",
            "Neurology, Orthopedics, General Medicine"
        };

        int index = startBracket + 1;
        int len = json.length();
        int nodeCounter = 10;

        while (index < len) {
            int elementStart = json.indexOf("{", index);
            if (elementStart == -1) break;

            int elementEnd = findClosingBrace(json, elementStart);
            if (elementEnd == -1) break;

            String elementStr = json.substring(elementStart, elementEnd + 1);

            try {
                long id = 0;
                String idStr = extractJsonField(elementStr, "id");
                if (idStr != null) id = Long.parseLong(idStr);

                double lat = 0.0;
                double lon = 0.0;

                // Ways/relations have a "center" object; nodes have direct lat/lon
                if (elementStr.contains("\"center\"")) {
                    String centerStr = extractJsonObject(elementStr, "center");
                    if (centerStr != null) {
                        String cLat = extractJsonField(centerStr, "lat");
                        String cLon = extractJsonField(centerStr, "lon");
                        if (cLat != null) lat = Double.parseDouble(cLat);
                        if (cLon != null) lon = Double.parseDouble(cLon);
                    }
                } else {
                    String latStr = extractJsonField(elementStr, "lat");
                    String lonStr = extractJsonField(elementStr, "lon");
                    if (latStr != null) lat = Double.parseDouble(latStr);
                    if (lonStr != null) lon = Double.parseDouble(lonStr);
                }

                if (lat == 0.0 || lon == 0.0) { index = elementEnd + 1; continue; }

                String tagsStr = extractJsonObject(elementStr, "tags");
                if (tagsStr == null) { index = elementEnd + 1; continue; }

                String name = extractJsonField(tagsStr, "name");
                if (name == null || name.trim().isEmpty()) { index = elementEnd + 1; continue; }

                // Phone number — try multiple OSM tag variants
                String contact = extractJsonField(tagsStr, "phone");
                if (contact == null) contact = extractJsonField(tagsStr, "contact:phone");
                if (contact == null) contact = extractJsonField(tagsStr, "contact:mobile");
                if (contact == null) contact = extractJsonField(tagsStr, "telephone");
                if (contact == null) contact = "108";

                // Address
                String address = extractJsonField(tagsStr, "addr:full");
                if (address == null) {
                    String street = extractJsonField(tagsStr, "addr:street");
                    String city   = extractJsonField(tagsStr, "addr:city");
                    String place  = extractJsonField(tagsStr, "addr:place");
                    if (street != null && city != null) {
                        address = street + ", " + city;
                    } else if (city != null) {
                        address = city;
                    } else if (place != null) {
                        address = place;
                    } else {
                        address = district + ", India";
                    }
                }

                // Specializations — keyword-match hospital name first, else hash
                String lowerName = name.toLowerCase();
                String specializations;
                if (lowerName.contains("heart") || lowerName.contains("cardio") || lowerName.contains("cardiac")) {
                    specializations = "Cardiology, General Medicine";
                } else if (lowerName.contains("brain") || lowerName.contains("neuro")) {
                    specializations = "Neurology, General Medicine";
                } else if (lowerName.contains("ortho") || lowerName.contains("bone") || lowerName.contains("fracture") || lowerName.contains("spine")) {
                    specializations = "Orthopedics, Trauma";
                } else if (lowerName.contains("child") || lowerName.contains("pediatr") || lowerName.contains("paediatr") || lowerName.contains("baby") || lowerName.contains("maternity")) {
                    specializations = "Pediatrics, General Medicine";
                } else if (lowerName.contains("cancer") || lowerName.contains("onco") || lowerName.contains("tumour") || lowerName.contains("tumor")) {
                    specializations = "Oncology, General Medicine";
                } else if (lowerName.contains("trauma") || lowerName.contains("emergency") || lowerName.contains("accident")) {
                    specializations = "Trauma, General Medicine";
                } else {
                    specializations = specializationsPool[Math.abs(name.hashCode()) % specializationsPool.length];
                }

                int localId = (int)(id & 0x7FFFFFFF);
                int locationNode = nodeCounter++;

                hospitals.add(new Hospital(localId, name, locationNode, contact, lat, lon, specializations, address, district));

            } catch (Exception ex) {
                // Skip malformed elements silently
            }

            index = elementEnd + 1;
        }

        System.out.println("[OverpassClient] Parsed " + hospitals.size() + " hospitals from JSON response.");
        return hospitals;
    }

    // -------------------------------------------------------------------------
    //  JSON utility methods (no external dependency)
    // -------------------------------------------------------------------------

    private static int findClosingBrace(String json, int start) {
        int braceCount = 0;
        boolean inQuotes = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) return i;
                }
            }
        }
        return -1;
    }

    private static String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + key.length());
        if (colonIndex == -1) return null;

        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start < json.length() && json.charAt(start) == '"') {
            // String value
            int end = start + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start + 1, end).replace("\\\"", "\"").replace("\\\\", "\\");
        } else {
            // Numeric / boolean value
            int end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (Character.isDigit(c) || c == '.' || c == '-' || c == 'e' || c == 'E' || c == '+') end++;
                else break;
            }
            if (end > start) return json.substring(start, end);
        }
        return null;
    }

    private static String extractJsonObject(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + key.length());
        if (colonIndex == -1) return null;

        int start = colonIndex + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;

        if (start < json.length() && json.charAt(start) == '{') {
            int end = findClosingBrace(json, start);
            if (end != -1) return json.substring(start, end + 1);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    //  Fallback generator (only used when API is completely unreachable)
    // -------------------------------------------------------------------------

    private static List<Hospital> generateFallbackHospitals(String district, double lat, double lng) {
        List<Hospital> list = new ArrayList<>();
        String cleanDistrict = district.contains("(")
                ? district.substring(0, district.indexOf("(")).trim()
                : district;

        String[] types = {
            "District General Hospital",
            "Apex Cardiac Care Centre",
            "Neurology & Brain Clinic",
            "Ortho & Trauma Hospital",
            "Oncology Institute",
            "Children's Pediatric Hospital",
            "Emergency & Trauma Centre"
        };
        String[] specs = {
            "General Medicine, Trauma",
            "Cardiology, General Medicine",
            "Neurology, General Medicine",
            "Orthopedics, Trauma",
            "Oncology, General Medicine",
            "Pediatrics, General Medicine",
            "Cardiology, Neurology, Trauma, General Medicine"
        };
        double[] latOff = { 0.012, -0.015,  0.025, -0.022,  0.005, -0.009,  0.018};
        double[] lngOff = { 0.015,  0.022, -0.018, -0.012, -0.025,  0.008, -0.020};

        for (int i = 0; i < types.length; i++) {
            list.add(new Hospital(
                1000 + i,
                cleanDistrict + " " + types[i],
                10 + i,
                "+91 " + (9800000000L + (long)(Math.random() * 199999999L)),
                lat + latOff[i], lng + lngOff[i],
                specs[i],
                (i * 12 + 10) + " Main Road, " + cleanDistrict,
                district
            ));
        }
        return list;
    }
}
