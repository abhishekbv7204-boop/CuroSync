package com.curasync.ui;

import com.curasync.algorithms.Scheduler;
import com.curasync.dao.*;
import com.curasync.models.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.util.*;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class AppointmentPanel extends JPanel {

    private AppointmentDAO apptDao = new AppointmentDAO();
    private PatientDAO patDao = new PatientDAO();
    private DoctorDAO docDao = new DoctorDAO();
    private DefaultTableModel tableModel;

    public AppointmentPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("Appointment Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 35, 55));

        JButton scheduleBtn = new JButton("+ Schedule Appointment");
        scheduleBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        scheduleBtn.setBackground(new Color(200, 120, 20));
        scheduleBtn.setForeground(Color.WHITE);
        scheduleBtn.setBorderPainted(false);
        scheduleBtn.setFocusPainted(false);
        scheduleBtn.setOpaque(true);
        scheduleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        scheduleBtn.addActionListener(e -> showScheduleDialog());

        header.add(title, BorderLayout.WEST);
        header.add(scheduleBtn, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Patient ID", "Doctor ID", "Date", "Time Slot", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 235, 245));
        table.setSelectionBackground(new Color(255, 238, 200));
        table.setGridColor(new Color(235, 238, 245));
        table.setShowVerticalLines(false);

        // Color status
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if ("Scheduled".equals(val)) setForeground(new Color(30, 100, 200));
                else if ("Completed".equals(val)) setForeground(new Color(22, 160, 90));
                else if ("Cancelled".equals(val)) setForeground(new Color(200, 40, 40));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));

        // Greedy scheduler panel
        JPanel schedulerPanel = buildGreedyPanel();

        // Bottom action buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setBackground(new Color(245, 247, 250));
        JButton completeBtn = new JButton("Mark Completed");
        completeBtn.setForeground(new Color(22, 160, 90));
        completeBtn.addActionListener(e -> updateStatus(table, "Completed"));
        JButton cancelBtn = new JButton("Cancel Appointment");
        cancelBtn.setForeground(new Color(200, 40, 40));
        cancelBtn.addActionListener(e -> updateStatus(table, "Cancelled"));
        bottom.add(completeBtn);
        bottom.add(cancelBtn);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(schedulerPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        loadAppointments();
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        for (Appointment a : apptDao.getAllAppointments()) {
            tableModel.addRow(new Object[]{a.getId(), a.getPatientId(), a.getDoctorId(), a.getAppointmentDate(), a.getTimeSlot(), a.getStatus()});
        }
    }

    private void updateStatus(JTable table, String status) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.setValueAt(status, row, 5);
        } else {
            JOptionPane.showMessageDialog(this, "Select an appointment first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel buildGreedyPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("⚙ Auto-Schedule (Greedy)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(new Color(25, 35, 55));

        JTextArea resultArea = new JTextArea(10, 18);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(248, 250, 252));
        resultArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        resultArea.setText("Click Run to auto-schedule\nconflict-free time slots.");

        JButton runBtn = new JButton("▶ Run Greedy Scheduler");
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        runBtn.setBackground(new Color(200, 120, 20));
        runBtn.setForeground(Color.WHITE);
        runBtn.setBorderPainted(false);
        runBtn.setFocusPainted(false);
        runBtn.setOpaque(true);
        runBtn.addActionListener(e -> {
            List<Scheduler.TimeSlot> requests = Arrays.asList(
                new Scheduler.TimeSlot("09:00 AM", 9),
                new Scheduler.TimeSlot("09:30 AM", 9),
                new Scheduler.TimeSlot("10:00 AM", 10),
                new Scheduler.TimeSlot("10:45 AM", 10),
                new Scheduler.TimeSlot("11:00 AM", 11),
                new Scheduler.TimeSlot("12:00 PM", 12),
                new Scheduler.TimeSlot("14:00 PM", 14),
                new Scheduler.TimeSlot("14:30 PM", 14),
                new Scheduler.TimeSlot("15:00 PM", 15)
            );
            List<Scheduler.TimeSlot> scheduled = Scheduler.greedySchedule(new ArrayList<>(requests));
            StringBuilder sb = new StringBuilder();
            sb.append("Requested (").append(requests.size()).append(" slots):\n");
            for (Scheduler.TimeSlot t : requests) sb.append("  • ").append(t.slot).append("\n");
            sb.append("\nOptimized (").append(scheduled.size()).append(" non-overlapping):\n");
            for (Scheduler.TimeSlot t : scheduled) sb.append("  ✓ ").append(t.slot).append("\n");
            resultArea.setText(sb.toString());
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        panel.add(runBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void showScheduleDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Schedule Appointment", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 4, 7, 4);

        // Patient combo
        List<Patient> patients = patDao.getAllPatients();
        String[] patNames = patients.stream().map(p -> p.getId() + " - " + p.getName()).toArray(String[]::new);
        JComboBox<String> patC = new JComboBox<>(patNames.length > 0 ? patNames : new String[]{"No patients"});

        // Doctor combo
        List<Doctor> doctors = docDao.getAllDoctors();
        String[] docNames = doctors.stream().map(d -> d.getId() + " - " + d.getName() + " (" + d.getSpecialization() + ")").toArray(String[]::new);
        JComboBox<String> docC = new JComboBox<>(docNames.length > 0 ? docNames : new String[]{"No doctors"});

        JSpinner dateS = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateS, "yyyy-MM-dd");
        dateS.setEditor(dateEditor);

        String[] slots = {"09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"};
        JComboBox<String> slotC = new JComboBox<>(slots);

        String[] labels = {"Patient", "Doctor", "Date", "Time Slot"};
        Component[] fields = {patC, docC, dateS, slotC};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton("Book Appointment");
        saveBtn.setBackground(new Color(200, 120, 20));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            if (patients.isEmpty() || doctors.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Add patients and doctors first.", "Missing Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int patId = patients.get(patC.getSelectedIndex()).getId();
            int docId = doctors.get(docC.getSelectedIndex()).getId();
            java.util.Date d = (java.util.Date) dateS.getValue();
            Appointment appt = new Appointment(0, patId, docId, new Date(d.getTime()), (String) slotC.getSelectedItem(), "Scheduled");
            apptDao.addAppointment(appt);
            loadAppointments();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Appointment booked successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnP, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
