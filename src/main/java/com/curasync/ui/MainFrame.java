package com.curasync.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings({"serial", "this-escape"})
public class MainFrame extends JFrame {

    private String role;
    private JLabel clockLabel;
    private Timer clockTimer;

    public MainFrame(String role) {
        this.role = role;
        setTitle("CuraSync — Healthcare Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        // --- Sidebar ---
        JPanel sidebar = buildSidebar();

        // --- Header ---
        JPanel header = buildHeader();

        // --- Tabbed content ---
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(new Color(245, 247, 250));
        tabs.addTab("📊  Dashboard", new DashboardPanel());
        tabs.addTab("👤  Patients", new PatientPanel());
        tabs.addTab("🩺  Doctors", new DoctorPanel());
        tabs.addTab("📅  Appointments", new AppointmentPanel());
        tabs.addTab("🚨  Emergency", new EmergencyPanel());

        // Restrict tabs by role
        if (role.equals("Receptionist")) {
            tabs.setEnabledAt(2, false); // hide Doctors tab
        }

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(new Color(245, 247, 250));
        mainArea.add(header, BorderLayout.NORTH);
        mainArea.add(tabs, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(20, 60, 130));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("  🏥 CuraSync");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(new EmptyBorder(0, 16, 20, 0));

        JLabel roleTag = new JLabel("  " + role);
        roleTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleTag.setForeground(new Color(150, 180, 230));
        roleTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleTag.setBorder(new EmptyBorder(0, 16, 30, 0));

        String[] menuItems = {"📊 Dashboard", "👤 Patients", "🩺 Doctors", "📅 Appointments", "🚨 Emergency"};
        for (String item : menuItems) {
            JLabel menuItem = new JLabel("  " + item);
            menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            menuItem.setForeground(new Color(180, 200, 240));
            menuItem.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuItem.setBorder(new EmptyBorder(10, 16, 10, 0));
            menuItem.setMaximumSize(new Dimension(200, 40));
            sidebar.add(menuItem);
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel logout = new JLabel("  ⎋  Sign Out");
        logout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logout.setForeground(new Color(255, 120, 120));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setBorder(new EmptyBorder(10, 16, 10, 0));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainFrame.this, "Sign out of CuraSync?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            }
        });

        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(logout);

        sidebar.add(logo, 0);
        sidebar.add(roleTag, 1);

        return sidebar;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 235)),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("Healthcare Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(30, 40, 60));

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clockLabel.setForeground(new Color(100, 110, 130));
        updateClock();

        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        JLabel userInfo = new JLabel("👤 admin  |  " + role);
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userInfo.setForeground(new Color(80, 100, 140));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(Color.WHITE);
        right.add(clockLabel);
        right.add(userInfo);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void updateClock() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd  HH:mm:ss"));
        clockLabel.setText(now);
    }
}
