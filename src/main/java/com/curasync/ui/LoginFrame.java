package com.curasync.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings({"serial", "this-escape"})
public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("CuraSync — Healthcare Management");
        setSize(480, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        // Left accent bar
        JPanel accent = new JPanel();
        accent.setBackground(new Color(30, 100, 200));
        accent.setPreferredSize(new Dimension(8, 0));
        root.add(accent, BorderLayout.WEST);

        // Main content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(245, 247, 250));
        content.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Logo / icon area
        JLabel iconLabel = new JLabel("🏥");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("CuraSync");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appName.setForeground(new Color(30, 100, 200));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Healthcare Management System");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(120, 130, 145));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(220, 225, 235));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(60, 70, 85));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        form.add(userLabel, gbc);

        JTextField userField = new JTextField();
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userField.setPreferredSize(new Dimension(320, 42));
        userField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        gbc.gridy = 1;
        form.add(userField, gbc);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(60, 70, 85));
        gbc.gridy = 2;
        form.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setPreferredSize(new Dimension(320, 42));
        passField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 210, 225), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        gbc.gridy = 3;
        form.add(passField, gbc);

        // Role selector
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleLabel.setForeground(new Color(60, 70, 85));
        gbc.gridy = 4;
        form.add(roleLabel, gbc);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "Admin", "Doctor", "Receptionist" });
        roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleBox.setPreferredSize(new Dimension(320, 42));
        gbc.gridy = 5;
        form.add(roleBox, gbc);

        // Login button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBackground(new Color(30, 100, 200));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setPreferredSize(new Dimension(320, 46));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(18, 0, 6, 0);
        form.add(loginBtn, gbc);

        JLabel hint = new JLabel("Use admin / admin to sign in");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(160, 170, 185));
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(2, 0, 0, 0);
        form.add(hint, gbc);

        // Login action
        ActionListener loginAction = e -> {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            if (user.equals("admin") && pass.equals("admin")) {
                dispose();
                new MainFrame((String) roleBox.getSelectedItem()).setVisible(true);
            } else {
                passField.setText("");
                passField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(220, 53, 69), 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
                JLabel errMsg = new JLabel("Invalid username or password");
                errMsg.setForeground(new Color(220, 53, 69));
                errMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                JOptionPane.showMessageDialog(this, errMsg, "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        };

        loginBtn.addActionListener(loginAction);
        passField.addActionListener(loginAction);
        userField.addActionListener(e -> passField.requestFocus());

        content.add(iconLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(appName);
        content.add(Box.createVerticalStrut(4));
        content.add(tagline);
        content.add(Box.createVerticalStrut(24));
        content.add(sep);
        content.add(Box.createVerticalStrut(24));
        content.add(form);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }
}
