package com.curasync.ui;

import com.curasync.dao.PatientDAO;
import com.curasync.models.Patient;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class PatientPanel extends JPanel {

    private PatientDAO dao = new PatientDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    public PatientPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("Patient Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 35, 55));

        JButton addBtn = new JButton("+ Add Patient");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setBackground(new Color(30, 100, 200));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddPatientDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        searchBar.setBackground(new Color(245, 247, 250));
        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 210, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search patients...");
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchBar.add(searchField);
        searchBar.add(Box.createHorizontalStrut(8));
        searchBar.add(searchBtn);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(new Color(245, 247, 250));
        topArea.add(header, BorderLayout.NORTH);
        topArea.add(searchBar, BorderLayout.SOUTH);

        // Table
        String[] cols = {"ID", "Name", "Age", "Gender", "Contact", "Medical History"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 235, 245));
        table.setSelectionBackground(new Color(210, 228, 255));
        table.setGridColor(new Color(235, 238, 245));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));
        scroll.setBackground(Color.WHITE);

        // Delete button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(245, 247, 250));
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteBtn.setForeground(new Color(200, 40, 40));
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tableModel.removeRow(row);
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        bottom.add(deleteBtn);

        add(topArea, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Search action
        searchBtn.addActionListener(e -> filterTable(searchField.getText(), dao.getAllPatients()));
        searchField.addActionListener(e -> filterTable(searchField.getText(), dao.getAllPatients()));

        loadPatients();
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        for (Patient p : dao.getAllPatients()) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(), p.getMedicalHistory()});
        }
    }

    private void filterTable(String query, List<Patient> all) {
        tableModel.setRowCount(0);
        for (Patient p : all) {
            if (p.getName().toLowerCase().contains(query.toLowerCase()) ||
                p.getContact().toLowerCase().contains(query.toLowerCase())) {
                tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(), p.getContact(), p.getMedicalHistory()});
            }
        }
    }

    private void showAddPatientDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Patient", true);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField nameF = new JTextField();
        JSpinner ageS = new JSpinner(new SpinnerNumberModel(25, 0, 120, 1));
        JComboBox<String> genderC = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField contactF = new JTextField();
        JTextField historyF = new JTextField();

        String[][] rows = {{"Name", null}, {"Age", null}, {"Gender", null}, {"Contact", null}, {"Medical History", null}};
        Component[] fields = {nameF, ageS, genderC, contactF, historyF};
        String[] labels = {"Full Name", "Age", "Gender", "Contact Number", "Medical History"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton("Save Patient");
        saveBtn.setBackground(new Color(30, 100, 200));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Patient p = new Patient(0, nameF.getText().trim(), (int) ageS.getValue(),
                (String) genderC.getSelectedItem(), contactF.getText().trim(), historyF.getText().trim());
            dao.addPatient(p);
            loadPatients();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Patient added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
