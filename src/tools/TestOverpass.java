import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestOverpass {
    public static void main(String[] args) {
        try {
            String district = "Mysuru (Mysore)";
            List<String> names = new ArrayList<>();
            names.add(district);
            if (district.contains("(")) {
                String primary = district.substring(0, district.indexOf("(")).trim();
                String secondary = district.substring(district.indexOf("(") + 1, district.indexOf(")")).trim();
                names.add(primary);
                names.add(secondary);
            }
            
            StringBuilder areaBuilder = new StringBuilder();
            for (String name : names) {
                areaBuilder.append("  area[\"name\"=\"").append(name).append("\"];\n");
            }
            
            String query = "[out:json][timeout:15];\n" +
                           "(\n" +
                           areaBuilder.toString() +
                           ")->.searchArea;\n" +
                           "(\n" +
                           "  node[\"amenity\"=\"hospital\"](area.searchArea);\n" +
                           "  way[\"amenity\"=\"hospital\"](area.searchArea);\n" +
                           "  relation[\"amenity\"=\"hospital\"](area.searchArea);\n" +
                           ");\n" +
                           "out center;";
            
            System.out.println("Query:\n" + query);
            
            String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "CuraSync Hospital Finder Upgrade (contact: user@curasync.org)")
                    .GET()
                    .build();
            
            System.out.println("Sending request...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            String body = response.body();
            System.out.println("Response snippet:\n" + (body.length() > 2000 ? body.substring(0, 2000) : body));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
