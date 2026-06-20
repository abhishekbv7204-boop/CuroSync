import com.curasync.utils.OverpassClient;
import com.curasync.algorithms.HospitalRouter;
import com.curasync.algorithms.SearchEngine;
import com.curasync.models.Hospital;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Generates a demo map.html with real Ballari hospitals from OpenStreetMap
 * so we can demo the browser map component.
 */
public class GenerateDemoMap {
    public static void main(String[] args) throws Exception {
        System.out.println("=== CuraSync Demo Map Generator ===");
        System.out.println("Fetching real hospitals from Ballari via Overpass API...");

        double patientLat = 15.1394;  // Ballari Institute of Technology and Management
        double patientLng = 76.9214;
        String locationName = "Ballari Institute of Technology and Management";

        List<Hospital> hospitals = OverpassClient.fetchHospitalsForDistrict("Ballari (Bellary)");
        System.out.println("Fetched " + hospitals.size() + " hospitals.");

        // Sort by distance using Merge Sort
        List<SearchEngine.HospitalDistance> distList = new ArrayList<>();
        for (Hospital h : hospitals) {
            double d = HospitalRouter.haversineKm(patientLat, patientLng, h.getLat(), h.getLng());
            distList.add(new SearchEngine.HospitalDistance(h, d));
        }
        SearchEngine.mergeSortHospitalsByDistance(distList, 0, distList.size() - 1);
        Hospital nearest = distList.get(0).hospital;

        System.out.println("Nearest hospital (Merge Sort): " + nearest.getName() + " at " + String.format("%.2f km", distList.get(0).distance));

        // Dijkstra route
        HospitalRouter.DijkstraResult route = HospitalRouter.calculateRoute(patientLat, patientLng, nearest, hospitals);
        System.out.println("Dijkstra route steps: " + route.path.size());

        // Build JSON
        StringBuilder hospJson = new StringBuilder("[");
        for (int i = 0; i < hospitals.size(); i++) {
            Hospital h = hospitals.get(i);
            hospJson.append("{\"id\":").append(h.getId())
                    .append(",\"name\":\"").append(escape(h.getName())).append("\"")
                    .append(",\"contact\":\"").append(escape(h.getContact())).append("\"")
                    .append(",\"lat\":").append(h.getLat())
                    .append(",\"lng\":").append(h.getLng())
                    .append(",\"address\":\"").append(escape(h.getAddress())).append("\"")
                    .append(",\"specializations\":\"").append(escape(h.getSpecializations())).append("\"")
                    .append(",\"district\":\"").append(escape(h.getDistrict())).append("\"")
                    .append("}");
            if (i < hospitals.size() - 1) hospJson.append(",");
        }
        hospJson.append("]");

        StringBuilder routeJson = new StringBuilder("[");
        for (int i = 0; i < route.path.size(); i++) {
            Hospital h = route.path.get(i);
            routeJson.append("{\"id\":").append(h.getId())
                    .append(",\"name\":\"").append(escape(h.getName())).append("\"")
                    .append(",\"contact\":\"").append(escape(h.getContact())).append("\"")
                    .append(",\"lat\":").append(h.getLat())
                    .append(",\"lng\":").append(h.getLng())
                    .append(",\"address\":\"").append(escape(h.getAddress())).append("\"")
                    .append(",\"specializations\":\"").append(escape(h.getSpecializations())).append("\"")
                    .append(",\"district\":\"").append(escape(h.getDistrict())).append("\"")
                    .append("}");
            if (i < route.path.size() - 1) routeJson.append(",");
        }
        routeJson.append("]");

        String template = Files.readString(Paths.get("map_template.html"));
        String html = template
                .replace("__PATIENT_LAT__", String.valueOf(patientLat))
                .replace("__PATIENT_LNG__", String.valueOf(patientLng))
                .replace("__PATIENT_LOCATION_NAME__", locationName)
                .replace("__HOSPITALS_JSON__", hospJson.toString())
                .replace("__ROUTE_JSON__", routeJson.toString())
                .replace("__SELECTED_HOSPITAL_ID__", String.valueOf(nearest.getId()));

        Files.writeString(Paths.get("map.html"), html);
        System.out.println("✅ map.html written successfully!");
        System.out.println("Nearest: " + nearest.getName());
        System.out.println("Total hospitals on map: " + hospitals.size());
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "");
    }
}
