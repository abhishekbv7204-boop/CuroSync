package com.curasync.ui;

import com.curasync.algorithms.HospitalRouter;
import com.curasync.dao.HospitalDAO;
import com.curasync.models.Hospital;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * HospitalMapViewer — upgraded to write a Leaflet.js and OpenStreetMap HTML map,
 * open it in the user's default browser, and display a summary of the shortest route.
 */
@SuppressWarnings({"serial", "this-escape"})
public class HospitalMapViewer extends JDialog {

    private final double userLat;
    private final double userLng;
    private final List<Hospital> hospitals;
    private final Hospital nearest;
    private final List<Hospital> route;
    private final String patientLocationName; // Named starting location for Google Maps origin
    private final String patientName;
    private final String patientCondition;

    // Backward compatible constructor
    public HospitalMapViewer(Frame parent, double userLat, double userLng, String assignedDoctor) {
        this(parent, userLat, userLng, assignedDoctor, new HospitalDAO().getAllHospitals(), "Your Location", "Emergency Patient", "General");
    }

    // Constructor with district-filtered hospitals
    public HospitalMapViewer(Frame parent, double userLat, double userLng, String assignedDoctor, List<Hospital> districtHospitals) {
        this(parent, userLat, userLng, assignedDoctor, districtHospitals, "Your Location", "Emergency Patient", "General");
    }

    // Full constructor with named location
    public HospitalMapViewer(Frame parent, double userLat, double userLng, String assignedDoctor, List<Hospital> districtHospitals, String locationName, String patientName, String patientCondition) {
        super(parent, "Hospital Map — Nearest Route", true);
        this.userLat = userLat;
        this.userLng = userLng;
        this.hospitals = districtHospitals;
        this.patientLocationName = (locationName != null && !locationName.isEmpty()) ? locationName : "Your Location";
        this.patientName = patientName;
        this.patientCondition = patientCondition;

        // Calculate nearest hospital in this district list
        this.nearest = HospitalRouter.findNearestByCoordinates(userLat, userLng, hospitals);
        
        // Calculate dynamic Dijkstra route to nearest hospital
        HospitalRouter.DijkstraResult res = HospitalRouter.calculateRoute(userLat, userLng, nearest, hospitals);
        this.route = res.path;

        setSize(520, 360);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 60, 130));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("🗺  CuraSync Map Viewer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // Center content area
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(4, 0, 4, 0);

        JLabel statusLabel = new JLabel("✅ Interactive Map Opened in Web Browser");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statusLabel.setForeground(new Color(22, 160, 90));
        center.add(statusLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 4, 0);
        
        String nearestName = nearest != null ? nearest.getName() : "N/A";
        String spec = nearest != null ? nearest.getSpecializations() : "N/A";
        double distKm = res.distance;
        
        gbc.gridy++;
        JLabel patLabel = new JLabel("<html><b>Patient Name:</b> " + patientName + "</html>");
        patLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(patLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel condLabel = new JLabel("<html><b>Condition:</b> " + patientCondition + "</html>");
        condLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(condLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 4, 0);
        JLabel hospLabel = new JLabel("<html><b>Recommended Hospital:</b> " + nearestName + "</html>");
        hospLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(hospLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 0, 4, 0);
        JLabel specLabel = new JLabel("<html><b>Specialization:</b> " + spec + "</html>");
        specLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(specLabel, gbc);

        gbc.gridy++;
        JLabel distLabel = new JLabel(String.format("<html><b>Navigation Distance:</b> %.2f km</html>", distKm));
        distLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(distLabel, gbc);

        gbc.gridy++;
        JLabel docLabel = new JLabel("<html><b>Assigned Doctor:</b> " + assignedDoctor + "</html>");
        docLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        center.add(docLabel, gbc);

        // Build route text
        StringBuilder routeSb = new StringBuilder();
        routeSb.append("Patient Location");
        for (Hospital h : route) {
            routeSb.append(" ➔ ").append(h.getName());
        }
        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        JLabel routeLabel = new JLabel("<html><b>Shortest Route:</b> <font color='#1e64c8'>" + routeSb.toString() + "</font></html>");
        routeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        center.add(routeLabel, gbc);

        root.add(center, BorderLayout.CENTER);

        // Footer with Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(new Color(240, 242, 245));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));

        JButton openBrowserBtn = new JButton("🌐  Re-open Map in Browser");
        openBrowserBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        openBrowserBtn.setBackground(new Color(30, 100, 200));
        openBrowserBtn.setForeground(Color.WHITE);
        openBrowserBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openBrowserBtn.addActionListener(e -> generateAndOpenMap());
        footer.add(openBrowserBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(new Color(180, 60, 60));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);

        // Generate and open map instantly on load
        generateAndOpenMap();
    }

    private void generateAndOpenMap() {
        try {
            // Read the map template
            File templateFile = new File("map_template.html");
            if (!templateFile.exists()) {
                JOptionPane.showMessageDialog(this, "map_template.html not found in the workspace!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String template = Files.readString(templateFile.toPath(), java.nio.charset.StandardCharsets.UTF_8);

            // Serialize data to JSON strings
            String hospitalsJson = serializeHospitalsList(hospitals);
            String routeJson = serializeHospitalsList(route);
            int recId = nearest != null ? nearest.getId() : -1;

            // Replace template placeholders
            String mapHtmlContent = template
                    .replace("__PATIENT_LAT__", String.valueOf(userLat))
                    .replace("__PATIENT_LNG__", String.valueOf(userLng))
                    .replace("__PATIENT_LOCATION_NAME__", patientLocationName
                            .replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", ""))
                    .replace("__HOSPITALS_JSON__", hospitalsJson)
                    .replace("__ROUTE_JSON__", routeJson)
                    .replace("__SELECTED_HOSPITAL_ID__", String.valueOf(recId));

            // Write output HTML file
            File outputFile = new File("map.html");
            Files.writeString(outputFile.toPath(), mapHtmlContent, java.nio.charset.StandardCharsets.UTF_8);

            // Open in system default browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(outputFile.toURI());
            } else {
                // Fallback command execution for Windows
                java.awt.Desktop.getDesktop().browse(outputFile.toURI());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate map page: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // --- JSON SERIALIZATION UTILITIES ---

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String serializeHospital(Hospital h) {
        return "{" +
                "\"id\":" + h.getId() + "," +
                "\"name\":\"" + escapeJson(h.getName()) + "\"," +
                "\"contact\":\"" + escapeJson(h.getContact()) + "\"," +
                "\"lat\":" + h.getLat() + "," +
                "\"lng\":" + h.getLng() + "," +
                "\"address\":\"" + escapeJson(h.getAddress()) + "\"," +
                "\"specializations\":\"" + escapeJson(h.getSpecializations()) + "\"," +
                "\"district\":\"" + escapeJson(h.getDistrict()) + "\"" +
                "}";
    }

    private static String serializeHospitalsList(List<Hospital> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(serializeHospital(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}

