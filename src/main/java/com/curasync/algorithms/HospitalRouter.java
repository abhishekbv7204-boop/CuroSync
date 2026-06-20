package com.curasync.algorithms;

import com.curasync.models.Hospital;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HospitalRouter {
    // Adjacency matrix representing distances between nodes (units are abstract)
    // -1 implies no direct path. Currently configured for four Karnataka nodes:
    // 0: Ballari, 1: Hospet, 2: Mysore, 3: Bengaluru
    private static final int[][] GRAPH = {
        {0,   60, 200, 350},
        {60,   0, 230, 320},
        {200,230,   0, 150},
        {350,320, 150,   0}
    };
    private static final int V = GRAPH.length;

    // Expose the graph so UI can draw edges dynamically
    public static int[][] getGraph() {
        int[][] copy = new int[GRAPH.length][];
        for (int i = 0; i < GRAPH.length; i++) copy[i] = Arrays.copyOf(GRAPH[i], GRAPH[i].length);
        return copy;
    }

    public static int getNodeCount() { return V; }

    /**
     * Dijkstra's algorithm to find the shortest distance from the patient's location node 
     * to all hospital nodes.
     * @param src Patient's starting node
     * @return Array of shortest distances to all nodes
     */
    public static int[] dijkstra(int src) {
        int[] dist = new int[V];
        boolean[] sptSet = new boolean[V];

        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        for (int count = 0; count < V - 1; count++) {
            int u = minDistanceOld(dist, sptSet);
            if (u == -1) break; // All remaining vertices are inaccessible
            sptSet[u] = true;

            for (int v = 0; v < V; v++) {
                if (!sptSet[v] && GRAPH[u][v] != -1 && dist[u] != Integer.MAX_VALUE && dist[u] + GRAPH[u][v] < dist[v]) {
                    dist[v] = dist[u] + GRAPH[u][v];
                }
            }
        }
        return dist;
    }

    private static int minDistanceOld(int[] dist, boolean[] sptSet) {
        int min = Integer.MAX_VALUE, min_index = -1;
        for (int v = 0; v < V; v++) {
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }
        }
        return min_index;
    }

    /**
     * Given the patient's node, find the nearest hospital.
     * Assuming patient is at node `src`, and all nodes in `hospitals` list are valid destinations.
     */
    public static Hospital findNearestHospital(int src, List<Hospital> hospitals) {
        int[] dist = dijkstra(src);
        
        Hospital nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Hospital h : hospitals) {
            int targetNode = h.getLocationNode();
            if (targetNode >= 0 && targetNode < V && dist[targetNode] < minDistance && targetNode != src) {
                minDistance = dist[targetNode];
                nearest = h;
            }
        }
        return nearest;
    }

    /**
     * Compute nearest hospital by geographic coordinates (Haversine distance in kilometers).
     */
    public static Hospital findNearestByCoordinates(double lat, double lng, List<Hospital> hospitals) {
        Hospital nearest = null;
        double minKm = Double.MAX_VALUE;
        for (Hospital h : hospitals) {
            double d = haversineKm(lat, lng, h.getLat(), h.getLng());
            if (d < minKm) {
                minKm = d;
                nearest = h;
            }
        }
        return nearest;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static double getDrivingDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        try {
            String url = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false", lon1, lat1, lon2, lat2);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String json = response.body();
                int idx = json.indexOf("\"distance\":");
                if (idx != -1) {
                    int start = idx + 11;
                    int end = json.indexOf(",", start);
                    if (end == -1) end = json.indexOf("}", start);
                    double meters = Double.parseDouble(json.substring(start, end).trim());
                    return meters / 1000.0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return haversineKm(lat1, lon1, lat2, lon2);
    }

    // --- DYNAMIC DIJKSTRA ROUTING GRAPH FOR REAL-WORLD HOSPITALS ---
    
    public static class DijkstraResult {
        public double distance;
        public List<Hospital> path; // Reconstructed path of hospitals

        public DijkstraResult(double distance, List<Hospital> path) {
            this.distance = distance;
            this.path = path;
        }
    }

    private static class NodeDistance {
        int nodeIndex;
        double distance;
        NodeDistance(int nodeIndex, double distance) {
            this.nodeIndex = nodeIndex;
            this.distance = distance;
        }
    }

    public static DijkstraResult calculateRoute(double userLat, double userLng, Hospital targetHospital, List<Hospital> allHospitals) {
        if (targetHospital == null || allHospitals == null || allHospitals.isEmpty()) {
            return new DijkstraResult(0, new ArrayList<>());
        }

        int N = allHospitals.size();
        int V = N + 1; // Node 0 is the patient, 1..N are the hospitals

        // Build coordinates array
        double[] lats = new double[V];
        double[] lngs = new double[V];
        lats[0] = userLat;
        lngs[0] = userLng;
        for (int i = 0; i < N; i++) {
            lats[i + 1] = allHospitals.get(i).getLat();
            lngs[i + 1] = allHospitals.get(i).getLng();
        }

        // Find the index of the target hospital in the graph
        int targetNode = -1;
        for (int i = 0; i < N; i++) {
            if (allHospitals.get(i).getId() == targetHospital.getId()) {
                targetNode = i + 1;
                break;
            }
        }

        if (targetNode == -1) {
            // Target hospital not in list, fallback to direct route
            double d = haversineKm(userLat, userLng, targetHospital.getLat(), targetHospital.getLng());
            List<Hospital> path = new ArrayList<>();
            path.add(targetHospital);
            return new DijkstraResult(d, path);
        }

        // Construct adjacency matrix
        double[][] graph = new double[V][V];
        for (int i = 0; i < V; i++) {
            Arrays.fill(graph[i], -1.0);
            graph[i][i] = 0.0;
        }

        // Connect each node to its K nearest neighbors
        int K = Math.min(3, V - 1);
        for (int i = 0; i < V; i++) {
            List<NodeDistance> list = new ArrayList<>();
            for (int j = 0; j < V; j++) {
                if (i != j) {
                    double d = haversineKm(lats[i], lngs[i], lats[j], lngs[j]);
                    list.add(new NodeDistance(j, d));
                }
            }
            list.sort(Comparator.comparingDouble(o -> o.distance));
            for (int k = 0; k < K; k++) {
                NodeDistance nd = list.get(k);
                graph[i][nd.nodeIndex] = nd.distance;
                graph[nd.nodeIndex][i] = nd.distance; // Undirected
            }
        }

        // Dijkstra's Algorithm
        double[] dist = new double[V];
        int[] parent = new int[V];
        boolean[] sptSet = new boolean[V];

        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[0] = 0.0;

        for (int count = 0; count < V - 1; count++) {
            int u = minDistanceDouble(dist, sptSet);
            if (u == -1) break;
            sptSet[u] = true;

            for (int v = 0; v < V; v++) {
                if (!sptSet[v] && graph[u][v] != -1.0 && dist[u] != Double.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    parent[v] = u;
                }
            }
        }

        // Reconstruct path
        List<Hospital> path = new ArrayList<>();
        double totalDistance = 0.0;
        
        if (dist[targetNode] == Double.MAX_VALUE) {
            // Target is unreachable, use direct driving distance as fallback
            totalDistance = getDrivingDistanceKm(userLat, userLng, targetHospital.getLat(), targetHospital.getLng());
            path.add(targetHospital);
            return new DijkstraResult(totalDistance, path);
        }

        // Reconstruct exact path with calculated distances
        int curr = targetNode;
        while (curr != 0 && curr != -1) {
            path.add(0, allHospitals.get(curr - 1));
            curr = parent[curr];
        }

        // Use the exact Dijkstra-calculated distance (based on graph edges)
        // This ensures consistency by using predefined edge weights
        totalDistance = dist[targetNode];
        return new DijkstraResult(totalDistance, path);
    }

    private static int minDistanceDouble(double[] dist, boolean[] sptSet) {
        double min = Double.MAX_VALUE;
        int min_index = -1;
        for (int v = 0; v < dist.length; v++) {
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }
        }
        return min_index;
    }
}

