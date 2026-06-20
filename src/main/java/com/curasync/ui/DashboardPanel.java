package com.curasync.ui;

import com.curasync.algorithms.HospitalRouter;
import com.curasync.algorithms.SearchEngine;
import com.curasync.dao.*;
import com.curasync.models.*;
import com.curasync.utils.DistrictLoader;
import com.curasync.utils.GeolocationClient;
import com.curasync.utils.OverpassClient;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings({"serial", "this-escape"})
public class DashboardPanel extends JPanel {

    private JComboBox<String> stateCombo;
    private JComboBox<String> districtCombo;
    private JComboBox<String> diseaseCombo;
    private JLabel resultLabel;
    private final double[] customCoords = new double[]{-1.0, -1.0};

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel pageTitle = new JLabel("Dashboard Overview");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitle.setForeground(new Color(25, 35, 55));
        add(pageTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        PatientDAO patDao = new PatientDAO();
        DoctorDAO docDao = new DoctorDAO();
        AppointmentDAO apptDao = new AppointmentDAO();
        EmergencyCaseDAO emgDao = new EmergencyCaseDAO();

        int patCount = patDao.getAllPatients().size();
        int docCount = docDao.getAllDoctors().size();
        int apptCount = apptDao.getAllAppointments().size();
        int emgCount = emgDao.getAllPendingCases().size();

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 0.28;
        center.add(statCard("Total Patients", String.valueOf(patCount), "👤", new Color(30, 100, 200), new Color(230, 238, 255)), gbc);
        gbc.gridx = 1;
        center.add(statCard("Active Doctors", String.valueOf(docCount), "🩺", new Color(22, 160, 90), new Color(225, 248, 235)), gbc);
        gbc.gridx = 2;
        center.add(statCard("Appointments", String.valueOf(apptCount), "📅", new Color(200, 120, 20), new Color(255, 243, 220)), gbc);
        gbc.gridx = 3;
        center.add(statCard("Pending Emergencies", String.valueOf(emgCount), "🚨", new Color(200, 40, 40), new Color(255, 230, 230)), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 0.72;
        center.add(buildHospitalFinder(), gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 2;
        center.add(buildRecentActivity(patDao, apptDao, emgDao), gbc);

        add(center, BorderLayout.CENTER);
    }

    private JPanel statCard(String label, String value, String icon, Color accent, Color bg) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valLabel.setForeground(accent);
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(100, 110, 130));
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        right.add(valLabel, BorderLayout.CENTER);
        right.add(nameLabel, BorderLayout.SOUTH);
        card.add(iconLabel, BorderLayout.WEST);
        card.add(right, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHospitalFinder() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("🗺 Real-world Hospital Recommendation System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(25, 35, 55));

        JLabel subtitle = new JLabel("Dynamic Dijkstra router using real-world coordinates from OpenStreetMap");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        subtitle.setForeground(new Color(140, 150, 170));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(Color.WHITE);
        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.insets = new Insets(6, 8, 6, 8);
        cgbc.fill = GridBagConstraints.HORIZONTAL;

        // Load States and Districts
        Map<String, List<String>> stateDistrictMap = DistrictLoader.loadStatesAndDistricts();
        stateCombo = new JComboBox<>(stateDistrictMap.keySet().toArray(new String[0]));
        stateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        stateCombo.setPreferredSize(new Dimension(160, 32));

        districtCombo = new JComboBox<>();
        districtCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        districtCombo.setPreferredSize(new Dimension(180, 32));

        // Populate first state districts
        if (stateCombo.getItemCount() > 0) {
            String firstState = stateCombo.getItemAt(0);
            for (String dist : stateDistrictMap.get(firstState)) {
                districtCombo.addItem(dist);
            }
        }

        stateCombo.addActionListener(ae -> {
            String selState = (String) stateCombo.getSelectedItem();
            districtCombo.removeAllItems();
            if (selState != null && stateDistrictMap.containsKey(selState)) {
                for (String dist : stateDistrictMap.get(selState)) {
                    districtCombo.addItem(dist);
                }
            }
        });

        // Disease categories JComboBox
        String[] diseases = {"Cardiology", "Neurology", "Orthopedics", "Trauma", "Pediatrics", "Oncology", "General Medicine"};
        diseaseCombo = new JComboBox<>(diseases);
        diseaseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        diseaseCombo.setPreferredSize(new Dimension(160, 32));

        // Load manual areas from DB (existing project functionality)
        com.curasync.dao.AreaDAO areaDao = new com.curasync.dao.AreaDAO();
        java.util.List<com.curasync.models.Area> areas = areaDao.getAllAreas();
        JComboBox<com.curasync.models.Area> nodeCombo = new JComboBox<>(areas.toArray(new com.curasync.models.Area[0]));
        nodeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nodeCombo.setPreferredSize(new Dimension(180, 32));

        JButton addAreaBtn = new JButton("+ Add Area");
        addAreaBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addAreaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addAreaBtn.addActionListener(ae -> {
            JTextField nameF = new JTextField();
            JTextField latF = new JTextField();
            JTextField lngF = new JTextField();
            Object[] msg = {"Name:", nameF, "Latitude:", latF, "Longitude:", lngF};
            int res = JOptionPane.showConfirmDialog(this, msg, "Add Area", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    String name = nameF.getText().trim();
                    double lat = Double.parseDouble(latF.getText().trim());
                    double lng = Double.parseDouble(lngF.getText().trim());
                    areaDao.addArea(name, lat, lng);
                    // refresh combo
                    nodeCombo.removeAllItems();
                    for (com.curasync.models.Area ar : areaDao.getAllAreas()) nodeCombo.addItem(ar);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Geolocation button
        JButton locationBtn = new JButton("📍 Geolocate Device");
        locationBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locationBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        locationBtn.addActionListener(ae -> {
            JDialog loading = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Locating...", true);
            loading.setLayout(new BorderLayout());
            JLabel lbl = new JLabel("<html><center>Requesting coordinates from Geolocation API...<br>Fetching device location...</center></html>", SwingConstants.CENTER);
            lbl.setBorder(new EmptyBorder(20, 20, 20, 20));
            loading.add(lbl, BorderLayout.CENTER);
            loading.setSize(320, 110);
            loading.setLocationRelativeTo(this);
            
            SwingWorker<double[], Void> worker = new SwingWorker<>() {
                protected double[] doInBackground() {
                    return GeolocationClient.getCurrentLocation();
                }
                protected void done() {
                    try {
                        double[] coords = get();
                        customCoords[0] = coords[0];
                        customCoords[1] = coords[1];
                        loading.dispose();
                        JOptionPane.showMessageDialog(DashboardPanel.this, 
                            String.format("Location updated successfully!\nGPS Coordinates: %.5f, %.5f", coords[0], coords[1]), 
                            "Location Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        loading.dispose();
                        JOptionPane.showMessageDialog(DashboardPanel.this, "Failed to fetch location: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            loading.setVisible(true);
        });

        JButton findBtn = new JButton("🗺  View on Map");
        findBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        findBtn.setBackground(new Color(30, 100, 200));
        findBtn.setForeground(Color.WHITE);
        findBtn.setBorderPainted(false);
        findBtn.setFocusPainted(false);
        findBtn.setOpaque(true);
        findBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        findBtn.setPreferredSize(new Dimension(150, 32));

        // Layout GridBag controls
        // Row 0
        cgbc.gridx = 0; cgbc.gridy = 0; cgbc.weightx = 0.1;
        controls.add(new JLabel("State:"), cgbc);
        cgbc.gridx = 1; cgbc.weightx = 0.4;
        controls.add(stateCombo, cgbc);
        cgbc.gridx = 2; cgbc.weightx = 0.1;
        controls.add(new JLabel("District:"), cgbc);
        cgbc.gridx = 3; cgbc.weightx = 0.4;
        controls.add(districtCombo, cgbc);

        // Patient Name field
        JTextField patientNameF = new JTextField();
        patientNameF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        patientNameF.setPreferredSize(new Dimension(160, 32));

        // Row 1
        cgbc.gridx = 0; cgbc.gridy = 1; cgbc.weightx = 0.1;
        controls.add(new JLabel("Patient Name:"), cgbc);
        cgbc.gridx = 1; cgbc.weightx = 0.4;
        controls.add(patientNameF, cgbc);
        cgbc.gridx = 2; cgbc.weightx = 0.1;
        controls.add(new JLabel("Condition:"), cgbc);
        cgbc.gridx = 3; cgbc.weightx = 0.4;
        controls.add(diseaseCombo, cgbc);

        // Row 2
        cgbc.gridx = 0; cgbc.gridy = 2; cgbc.weightx = 0.1;
        controls.add(new JLabel("Your Area:"), cgbc);
        cgbc.gridx = 1; cgbc.weightx = 0.4;
        controls.add(nodeCombo, cgbc);
        cgbc.gridx = 2; cgbc.gridwidth = 2; cgbc.weightx = 0.5;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(locationBtn);
        btnPanel.add(addAreaBtn);
        btnPanel.add(findBtn);
        controls.add(btnPanel, cgbc);

        // Quick result label
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(new Color(240, 245, 255));
        resultPanel.setBorder(new EmptyBorder(10, 14, 10, 14));
        resultLabel = new JLabel("Select criteria and click View on Map to view Dijkstra route mapping");
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultLabel.setForeground(new Color(80, 90, 110));
        resultPanel.add(resultLabel);

        HospitalDAO dao = new HospitalDAO();

        findBtn.addActionListener(e -> {
            String selDistrict = (String) districtCombo.getSelectedItem();
            if (selDistrict == null) {
                JOptionPane.showMessageDialog(this, "Please select a district first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Determine patient coordinates
            double[] coords = new double[2];
            boolean hasCoords = false;
            
            com.curasync.models.Area selArea = (com.curasync.models.Area) nodeCombo.getSelectedItem();
            if (customCoords[0] != -1.0) {
                coords[0] = customCoords[0];
                coords[1] = customCoords[1];
                hasCoords = true;
            } else if (selArea != null) {
                coords[0] = selArea.getLat();
                coords[1] = selArea.getLng();
                hasCoords = true;
            }

            // If patient has no manually added area coordinates or geolocation, get centroid of district
            final double patientLat;
            final double patientLng;
            if (!hasCoords) {
                double[] centroid = OverpassClient.geocodeDistrict(selDistrict);
                patientLat = centroid[0];
                patientLng = centroid[1];
            } else {
                patientLat = coords[0];
                patientLng = coords[1];
            }

            // Always fetch fresh real-world hospitals from Overpass API
            JDialog loading = new JDialog((Frame) SwingUtilities.getWindowAncestor(DashboardPanel.this), "Fetching real-world hospitals...", true);
            loading.setLayout(new BorderLayout());
            JLabel lbl = new JLabel("<html><center><b>Connecting to OpenStreetMap Overpass API...</b><br>"
                    + "Downloading real hospitals in <b>" + selDistrict + "</b>...<br>"
                    + "<small>This may take up to 30 seconds on first load.</small></center></html>", SwingConstants.CENTER);
            lbl.setBorder(new EmptyBorder(20, 20, 20, 20));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JProgressBar progress = new JProgressBar();
            progress.setIndeterminate(true);
            progress.setBorder(new EmptyBorder(0, 20, 16, 20));
            loading.add(lbl, BorderLayout.CENTER);
            loading.add(progress, BorderLayout.SOUTH);
            loading.setSize(420, 160);
            loading.setLocationRelativeTo(DashboardPanel.this);

            SwingWorker<List<Hospital>, Void> fetchWorker = new SwingWorker<>() {
                protected List<Hospital> doInBackground() {
                    return OverpassClient.fetchHospitalsForDistrict(selDistrict);
                }
                protected void done() {
                    try {
                        List<Hospital> fetched = get();
                        dao.saveHospitals(fetched, selDistrict);
                        loading.dispose();
                        int realCount = (int) fetched.stream()
                                .filter(h -> !h.getName().contains("City General Hospital")
                                          && !h.getName().contains("Apex Cardiac")
                                          && !h.getName().contains("District General Hospital"))
                                .count();
                        if (realCount > 0) {
                            resultLabel.setText("✅ Loaded " + fetched.size() + " real hospitals from OpenStreetMap for " + selDistrict);
                        } else {
                            resultLabel.setText("⚠ No OSM data found for " + selDistrict + " — using generated data. " + fetched.size() + " hospitals loaded.");
                        }
                        processRecommendation(selDistrict, patientLat, patientLng, patientNameF.getText().trim());
                    } catch (Exception ex) {
                        loading.dispose();
                        JOptionPane.showMessageDialog(DashboardPanel.this, "Failed to load hospitals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            fetchWorker.execute();
            loading.setVisible(true);
        });

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);
        panel.add(resultPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void processRecommendation(String district, double lat, double lng, String patientName) {
        HospitalDAO dao = new HospitalDAO();
        DoctorDAO docDao = new DoctorDAO();
        AppointmentDAO apptDao = new AppointmentDAO();

        List<Hospital> allDistrictHospitals = dao.getHospitalsByDistrict(district);
        if (allDistrictHospitals.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hospitals found in " + district, "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Filter based on disease specialization
        String selDisease = (String) diseaseCombo.getSelectedItem();
        List<Hospital> filtered = new ArrayList<>();
        for (Hospital h : allDistrictHospitals) {
            if (h.getSpecializations().toLowerCase().contains(selDisease.toLowerCase())) {
                filtered.add(h);
            }
        }

        // Fallback if no matching specialization found
        if (filtered.isEmpty()) {
            filtered.addAll(allDistrictHospitals);
        }

        // Sort using Merge Sort by distance
        List<SearchEngine.HospitalDistance> distList = new ArrayList<>();
        for (Hospital h : filtered) {
            double d = HospitalRouter.haversineKm(lat, lng, h.getLat(), h.getLng());
            distList.add(new SearchEngine.HospitalDistance(h, d));
        }
        SearchEngine.mergeSortHospitalsByDistance(distList, 0, distList.size() - 1);

        Hospital nearest = distList.get(0).hospital;
        double nearestDist = distList.get(0).distance;

        // Calculate dynamic Dijkstra route
        HospitalRouter.DijkstraResult routeRes = HospitalRouter.calculateRoute(lat, lng, nearest, allDistrictHospitals);

        // Assign available doctor: prefer doctors at the nearest hospital
        String assignedDoctor = "(No doctor available)";
        var doctors = docDao.getAllDoctors();
        if (doctors != null) {
            // Prefer doctor matching disease specialization at nearest hospital
            for (var d : doctors) {
                if (d.isAvailable() && d.getHospitalId() == nearest.getId() && d.getSpecialization().toLowerCase().contains(selDisease.toLowerCase())) {
                    assignedDoctor = d.getName() + " — " + d.getSpecialization();
                    break;
                }
            }
            // Fallback: any doctor at nearest hospital
            if (assignedDoctor.equals("(No doctor available)")) {
                for (var d : doctors) {
                    if (d.isAvailable() && d.getHospitalId() == nearest.getId()) {
                        assignedDoctor = d.getName() + " — " + d.getSpecialization();
                        break;
                    }
                }
            }
            // Fallback: any doctor matching disease specialization in district
            if (assignedDoctor.equals("(No doctor available)")) {
                for (var d : doctors) {
                    if (d.isAvailable() && d.getSpecialization().toLowerCase().contains(selDisease.toLowerCase())) {
                        assignedDoctor = d.getName() + " — " + d.getSpecialization();
                        break;
                    }
                }
            }
            // Fallback: any doctor
            if (assignedDoctor.equals("(No doctor available)")) {
                for (var d : doctors) {
                    if (d.isAvailable()) {
                        assignedDoctor = d.getName() + " — " + d.getSpecialization();
                        break;
                    }
                }
            }
        }

        // Format Dijkstra route description
        StringBuilder routeSb = new StringBuilder();
        routeSb.append("Patient Location");
        for (Hospital h : routeRes.path) {
            routeSb.append(" ➔ ").append(h.getName());
        }

        if (nearest != null) {
            resultLabel.setText(String.format("<html><b>Nearest:</b> %s (%s)  |  Navigation Distance: %.2f km  |  Assigned: %s<br><b>Route:</b> <font color='#1e64c8'>%s</font></html>",
                nearest.getName(), selDisease, routeRes.distance, assignedDoctor, routeSb.toString()));
            resultLabel.setForeground(new Color(22, 160, 90));
        }

        // Build a human-readable origin name for Google Maps
        // Prefer geolocation address; fallback to district name
        String originName;
        if (customCoords[0] != -1.0) {
            // User used geolocation — try to get a readable name via reverse geocode
            originName = reverseGeocodeLabel(customCoords[0], customCoords[1], district);
        } else {
            originName = district + ", India";
        }

        // Open map dialog with the named origin so Google Maps shows it correctly
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        String condition = selDisease;
        if (patientName == null || patientName.isEmpty()) patientName = "Emergency Patient";
        HospitalMapViewer viewer = new HospitalMapViewer(parent, lat, lng, assignedDoctor, allDistrictHospitals, originName, patientName, condition);
        viewer.setVisible(true);

        // Persist assignment as an Appointment (patient_id=0 placeholder, date=today, time_slot=ASAP)
        try {
            if (!assignedDoctor.equals("(No doctor available)")) {
                com.curasync.models.Doctor matched = null;
                for (var d : doctors) {
                    String label = d.getName() + " — " + d.getSpecialization();
                    if (label.equals(assignedDoctor)) { matched = d; break; }
                }
                if (matched != null) {
                    java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
                    com.curasync.models.Appointment appt = new com.curasync.models.Appointment(0, 0, matched.getId(), today, "ASAP", "Assigned");
                    apptDao.addAppointment(appt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reverse-geocode lat/lng via Nominatim to get a human-readable location label.
     * Falls back to the district name if the API call fails.
     */
    private String reverseGeocodeLabel(double lat, double lng, String fallbackDistrict) {
        if (Math.abs(lat - 15.1394) < 0.001 && Math.abs(lng - 76.9214) < 0.001) {
            return "BITM Ballari";
        }
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lng + "&zoom=16";
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "CuraSync-HospitalFinder/2.0")
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET().build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                String json = resp.body();
                // Try "display_name" first (full readable address)
                int idx = json.indexOf("\"display_name\":\"");
                if (idx != -1) {
                    int start = idx + 16;
                    int end = json.indexOf("\"", start);
                    if (end != -1) {
                        String display = json.substring(start, end);
                        // Shorten to first two comma-separated parts
                        String[] parts = display.split(",");
                        if (parts.length >= 2) {
                            return parts[0].trim() + ", " + parts[1].trim();
                        }
                        return display;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DashboardPanel] Reverse geocode failed: " + e.getMessage());
        }
        return fallbackDistrict + ", India";
    }

    private JPanel buildRecentActivity(PatientDAO patDao, AppointmentDAO apptDao, EmergencyCaseDAO emgDao) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));
        JLabel title = new JLabel("📋 Recent Activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(25, 35, 55));

        DefaultListModel<String> model = new DefaultListModel<>();
        for (EmergencyCase ec : emgDao.getAllPendingCases())
            model.addElement("🚨  Emergency — " + ec.getPatientName() + " (Severity " + ec.getSeverityLevel() + ")");
        for (Appointment a : apptDao.getAllAppointments())
            model.addElement("📅  Appointment #" + a.getId() + " — Patient " + a.getPatientId() + "  [" + a.getStatus() + "]");
        for (Patient p : patDao.getAllPatients())
            model.addElement("👤  Patient — " + p.getName() + " (" + p.getAge() + ", " + p.getGender() + ")");
        if (model.isEmpty()) model.addElement("No recent activity");

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        list.setBackground(Color.WHITE);
        list.setSelectionBackground(new Color(230, 238, 255));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}
