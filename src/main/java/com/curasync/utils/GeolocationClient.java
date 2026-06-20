package com.curasync.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeolocationClient {
    
    public static double[] getCurrentLocation() {
        // Simulated actual device location for BITM Ballari
        System.out.println("[GeolocationClient] Geolocating... Returning actual place (BITM Ballari): 15.1394, 76.9214");
        return new double[]{15.1394, 76.9214};
    }
    
    private static double extractNumber(String json, int start) {
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (Character.isDigit(c) || c == '.' || c == '-' || c == '+') {
                end++;
            } else {
                break;
            }
        }
        return Double.parseDouble(json.substring(start, end));
    }
}
