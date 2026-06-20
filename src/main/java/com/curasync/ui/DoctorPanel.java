package com.curasync.ui;

import com.curasync.dao.DoctorDAO;
import com.curasync.models.Doctor;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

@SuppressWarnings({"serial", "this-escape"})
public class DoctorPanel extends JPanel {

    private DoctorDAO dao = new DoctorDAO();
    private DefaultTableModel tableModel;

    private static final String[] SPECIALIZATIONS = {
        "Cardiologist", "Neurologist", "Pediatrician", "Orthopedist",
        "Dermatologist", "Oncologist", "Psychiatrist", "General Practitioner",
        "Radiologist", "Surgeon", "Gynecologist", "ENT Specialist"
    };

    public DoctorPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        JLabel title = new JLabel("Doctor Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 35, 55));

        JButton addBtn = new JButton("+ Add Doctor");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setBackground(new Color(22, 160, 90));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setOpaque(true);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddDoctorDialog());

        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] cols = {"ID", "Name", "Specialization", "Contact", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 235, 245));
        table.setSelectionBackground(new Color(210, 240, 225));
        table.setGridColor(new Color(235, 238, 245));
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(100);

        // Color status column
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if ("Available".equals(val)) setForeground(new Color(22, 160, 90));
                else setForeground(new Color(200, 40, 40));
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        loadDoctors();
    }

    private void loadDoctors() {
        tableModel.setRowCount(0);
        for (Doctor d : dao.getAllDoctors()) {
            tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialization(), d.getContact(), d.isAvailable() ? "Available" : "Unavailable"});
        }
    }

    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Doctor", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField nameF = new JTextField();
        JComboBox<String> specC = new JComboBox<>(SPECIALIZATIONS);
        JTextField contactF = new JTextField();
        JCheckBox availCB = new JCheckBox("Available");
        availCB.setSelected(true);

        String[] labels = {"Full Name", "Specialization", "Contact", ""};
        Component[] fields = {nameF, specC, contactF, availCB};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            if (!labels[i].isEmpty()) form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton("Save Doctor");
        saveBtn.setBackground(new Color(22, 160, 90));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setOpaque(true);
        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Doctor d = new Doctor(0, nameF.getText().trim(), (String) specC.getSelectedItem(), contactF.getText().trim(), availCB.isSelected());
            dao.addDoctor(d);
            loadDoctors();
            dialog.dispose();
        });

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnP.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnP, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
