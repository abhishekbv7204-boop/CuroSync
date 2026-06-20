package com.curasync.ui;

import com.curasync.algorithms.EmergencyQueue;
import com.curasync.dao.EmergencyCaseDAO;
import com.curasync.models.EmergencyCase;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings({"serial", "this-escape"})
public class EmergencyPanel extends JPanel {

    private EmergencyQueue priorityQueue = new EmergencyQueue();
    private EmergencyCaseDAO dao = new EmergencyCaseDAO();
    private DefaultTableModel tableModel;

    public EmergencyPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("🚨 Emergency Case Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(190, 30, 30));
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Cases sorted by severity — highest priority attended first");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setForeground(new Color(120, 130, 145));
        header.add(subtitle, BorderLayout.SOUTH);

        // Left: Add form
        JPanel formCard = new JPanel(new BorderLayout(0, 10));
        formCard.setPreferredSize(new Dimension(290, 0));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel formTitle = new JLabel("Report New Emergency");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(new Color(25, 35, 55));

        JPanel fields = new JPanel(new GridLayout(0, 1, 0, 10));
        fields.setBackground(Color.WHITE);

        JTextField nameF = new JTextField();
        nameF.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 210, 225), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        nameF.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JSpinner sevS = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        sevS.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] types = {"Cardiac Arrest", "Trauma/Accident", "Stroke", "Respiratory Distress", "Poisoning", "Severe Burns", "Fracture", "Other"};
        JComboBox<String> typeC = new JComboBox<>(types);
        typeC.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        fields.add(labeledField("Patient Name", nameF));
        fields.add(labeledField("Emergency Type", typeC));
        fields.add(labeledField("Severity (1–10)", sevS));

        // Severity legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        legend.setBackground(Color.WHITE);
        legend.add(colorDot(new Color(22, 160, 90))); legend.add(new JLabel("1-3 Low"));
        legend.add(colorDot(new Color(200, 120, 20))); legend.add(new JLabel("4-6 Med"));
        legend.add(colorDot(new Color(200, 40, 40))); legend.add(new JLabel("7-10 High"));
        for (Component c : legend.getComponents()) if (c instanceof JLabel) ((JLabel)c).setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JButton addBtn = new JButton("🚨  Add to Queue");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setBackground(new Color(200, 40, 40));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        formCard.add(formTitle, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        JPanel formBottom = new JPanel(new BorderLayout(0, 6));
        formBottom.setBackground(Color.WHITE);
        formBottom.add(legend, BorderLayout.NORTH);
        formBottom.add(addBtn, BorderLayout.SOUTH);
        formCard.add(formBottom, BorderLayout.SOUTH);

        // Right: Queue table
        String[] cols = {"Priority", "Patient Name", "Type", "Severity", "Time Reported", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(255, 230, 230));
        table.setGridColor(new Color(235, 238, 245));
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(3).setMaxWidth(70);

        // Color severity
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                int sev = val instanceof Integer ? (int) val : 0;
                if (sev >= 7) setForeground(new Color(200, 40, 40));
                else if (sev >= 4) setForeground(new Color(200, 120, 20));
                else setForeground(new Color(22, 160, 90));
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            "Emergency Queue (Max-Heap Priority)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(190, 30, 30)
        ));

        JButton attendBtn = new JButton("✔  Attend Next Patient");
        attendBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        attendBtn.setBackground(new Color(22, 160, 90));
        attendBtn.setForeground(Color.WHITE);
        attendBtn.setBorderPainted(false);
        attendBtn.setFocusPainted(false);
        attendBtn.setOpaque(true);
        attendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel queuePanel = new JPanel(new BorderLayout(0, 8));
        queuePanel.setBackground(new Color(245, 247, 250));
        queuePanel.add(scroll, BorderLayout.CENTER);
        queuePanel.add(attendBtn, BorderLayout.SOUTH);

        // Add to queue action
        addBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Patient name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int sev = (int) sevS.getValue();
            String type = (String) typeC.getSelectedItem();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            EmergencyCase ec = new EmergencyCase(0, name + " [" + type + "]", sev, timestamp, "Pending");
            dao.addEmergencyCase(ec);
            priorityQueue.addCase(ec);
            nameF.setText("");
            refreshTable();
            JOptionPane.showMessageDialog(this, "Emergency case added to queue.", "Added", JOptionPane.INFORMATION_MESSAGE);
        });

        // Attend next
        attendBtn.addActionListener(e -> {
            EmergencyCase next = priorityQueue.getNextCase();
            if (next != null) {
                String assignedHosp = "Nearest Emergency Center";
                String reqSpecialty = "General Medicine";
                String tStr = next.getPatientName().toLowerCase();
                if (tStr.contains("cardiac")) reqSpecialty = "Cardiology";
                else if (tStr.contains("stroke")) reqSpecialty = "Neurology";
                else if (tStr.contains("trauma") || tStr.contains("fracture") || tStr.contains("burn")) reqSpecialty = "Trauma";
                else if (tStr.contains("respiratory")) reqSpecialty = "General Medicine";
                
                com.curasync.dao.HospitalDAO hDao = new com.curasync.dao.HospitalDAO();
                int assignedHospId = 0;
                for (com.curasync.models.Hospital h : hDao.getAllHospitals()) {
                    if (h.getSpecializations().toLowerCase().contains(reqSpecialty.toLowerCase())) {
                        assignedHosp = h.getName() + " (" + h.getDistrict() + ")";
                        assignedHospId = h.getId();
                        break;
                    }
                }
                
                String assignedDoc = "Emergency Doctor (Standby)";
                int docId = 1;
                com.curasync.dao.DoctorDAO docDao = new com.curasync.dao.DoctorDAO();
                for (com.curasync.models.Doctor d : docDao.getAllDoctors()) {
                    if (d.isAvailable() && (assignedHospId == 0 || d.getHospitalId() == assignedHospId)) {
                        assignedDoc = d.getName();
                        docId = d.getId();
                        break;
                    }
                }
                
                // Update DB to keep Dashboard in sync
                dao.updateStatus(next.getId(), "Attended");
                try {
                    java.sql.Date today = java.sql.Date.valueOf(java.time.LocalDate.now());
                    com.curasync.models.Appointment appt = new com.curasync.models.Appointment(0, 0, docId, today, "ASAP", "Emergency Dispatched");
                    new com.curasync.dao.AppointmentDAO().addAppointment(appt);
                } catch (Exception ex) { ex.printStackTrace(); }

                String msg = "<html><div style='width:250px;'><b>Now Attending (High Priority):</b><br>" +
                    "Patient: <b>" + next.getPatientName() + "</b><br>" +
                    "Severity: " + next.getSeverityLevel() + "/10<br>" +
                    "Time: " + next.getTimestamp() + "<br><br>" +
                    "<b>🚑 Auto-Dispatch Router:</b><br>" +
                    "Required Specialty: " + reqSpecialty + "<br>" +
                    "Routed to: <font color='#1e64c8'><b>" + assignedHosp + "</b></font><br>" +
                    "Assigned Doctor: <b>" + assignedDoc + "</b></div></html>";
                JOptionPane.showMessageDialog(this, new JLabel(msg), "🩺 Doctor Alert", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "No pending emergencies in queue.", "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Load from DB on startup
        for (EmergencyCase ec : dao.getAllPendingCases()) priorityQueue.addCase(ec);
        refreshTable();

        add(header, BorderLayout.NORTH);
        add(formCard, BorderLayout.WEST);
        add(queuePanel, BorderLayout.CENTER);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        // Copy queue to sorted list for display
        java.util.List<EmergencyCase> sorted = new ArrayList<>(priorityQueue.getQueue());
        sorted.sort(Comparator.reverseOrder());
        int rank = 1;
        for (EmergencyCase ec : sorted) {
            tableModel.addRow(new Object[]{"#" + rank++, ec.getPatientName(), "", ec.getSeverityLevel(), ec.getTimestamp(), ec.getStatus()});
        }
    }

    private JPanel labeledField(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(80, 90, 110));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel colorDot(Color c) {
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(c);
                g.fillOval(0, 2, 10, 10);
            }
        };
        dot.setBackground(Color.WHITE);
        dot.setPreferredSize(new Dimension(12, 14));
        return dot;
    }
}
