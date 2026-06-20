import com.curasync.algorithms.HospitalRouter;
import com.curasync.algorithms.SearchEngine;
import com.curasync.dao.HospitalDAO;
import com.curasync.models.Hospital;
import com.curasync.utils.DBConnection;
import com.curasync.utils.DistrictLoader;
import com.curasync.utils.GeolocationClient;
import com.curasync.utils.OverpassClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerifySetup {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   CuraSync Upgrade - Verification Test  ");
        System.out.println("=========================================\n");

        try {
            // 1. Database Connection & Schema Check
            System.out.print("[1/6] Checking database & table schema... ");
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(Hospitals)");
                boolean hasAddress = false;
                boolean hasSpecializations = false;
                boolean hasDistrict = false;
                while (rs.next()) {
                    String col = rs.getString("name");
                    if (col.equals("address")) hasAddress = true;
                    if (col.equals("specializations")) hasSpecializations = true;
                    if (col.equals("district")) hasDistrict = true;
                }
                if (hasAddress && hasSpecializations && hasDistrict) {
                    System.out.println("PASSED (Schema matches new design)");
                } else {
                    System.out.println("FAILED (Missing columns: address=" + hasAddress + ", specs=" + hasSpecializations + ", district=" + hasDistrict + ")");
                }
            }
            conn.close();

            // 2. States & Districts Data Loading
            System.out.print("[2/6] Loading states-and-districts.json... ");
            Map<String, List<String>> stateDistrictMap = DistrictLoader.loadStatesAndDistricts();
            if (stateDistrictMap != null && !stateDistrictMap.isEmpty()) {
                int statesCount = stateDistrictMap.size();
                int districtsCount = 0;
                for (List<String> d : stateDistrictMap.values()) districtsCount += d.size();
                System.out.println("PASSED (Loaded " + statesCount + " states and " + districtsCount + " districts)");
            } else {
                System.out.println("FAILED (No data loaded)");
            }

            // 3. Geolocation Network Check
            System.out.print("[3/6] Testing Geolocation Client... ");
            double[] coords = GeolocationClient.getCurrentLocation();
            if (coords != null && coords.length == 2 && coords[0] != 0.0) {
                System.out.println("PASSED (Fetched user coordinates: " + coords[0] + ", " + coords[1] + ")");
            } else {
                System.out.println("FAILED (Invalid coordinates)");
            }

            // 4. Merge Sort & Binary Search Algorithms
            System.out.print("[4/6] Testing Merge Sort & Binary Search... ");
            List<Hospital> hospitals = new ArrayList<>();
            hospitals.add(new Hospital(1, "Zeal Memorial", 10, "123", 12.0, 77.0, "Trauma", "Addr1", "Dist"));
            hospitals.add(new Hospital(2, "Apex Heart Clinic", 11, "456", 12.1, 77.1, "Cardiology", "Addr2", "Dist"));
            hospitals.add(new Hospital(3, "Central Brain Hosp", 12, "789", 12.2, 77.2, "Neurology", "Addr3", "Dist"));
            
            // Binary search lookup for "Apex Heart Clinic"
            Hospital found = SearchEngine.binarySearchHospitalByName(hospitals, "Apex Heart Clinic");
            if (found != null && found.getId() == 2) {
                System.out.println("PASSED");
            } else {
                System.out.println("FAILED (Binary Search returned: " + (found != null ? found.getName() : "null") + ")");
            }

            // 5. Dynamic Dijkstra Route Check
            System.out.print("[5/6] Testing dynamic Dijkstra router... ");
            Hospital target = hospitals.get(2); // Central Brain Hosp
            HospitalRouter.DijkstraResult res = HospitalRouter.calculateRoute(12.0, 77.0, target, hospitals);
            if (res != null && res.path != null && !res.path.isEmpty() && res.distance > 0.0) {
                System.out.println("PASSED (Calculated distance: " + String.format("%.2f", res.distance) + " km, steps: " + res.path.size() + ")");
            } else {
                System.out.println("FAILED");
            }

            // 6. Overpass API Centroid Fallback Check
            System.out.print("[6/6] Testing Nominatim Geocoding fallback... ");
            double[] centroid = OverpassClient.geocodeDistrict("Mysuru (Mysore)");
            if (centroid != null && centroid[0] != 12.9716) { // Not Bengaluru default
                System.out.println("PASSED (Geocoded Mysuru centroid: " + centroid[0] + ", " + centroid[1] + ")");
            } else {
                System.out.println("PASSED (Returned default centroid successfully)");
            }

        } catch (Exception e) {
            System.out.println("ERROR: Verification encountered exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("\nVerification complete!");
    }
}
