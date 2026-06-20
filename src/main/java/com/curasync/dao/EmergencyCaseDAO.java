package com.curasync.dao;

import com.curasync.models.EmergencyCase;
import com.curasync.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyCaseDAO {

    public void addEmergencyCase(EmergencyCase emergencyCase) {
        String sql = "INSERT INTO EmergencyCases (patient_name, severity_level, status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, emergencyCase.getPatientName());
            stmt.setInt(2, emergencyCase.getSeverityLevel());
            stmt.setString(3, emergencyCase.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EmergencyCase> getAllPendingCases() {
        List<EmergencyCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM EmergencyCases WHERE status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cases.add(new EmergencyCase(
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getInt("severity_level"),
                        rs.getString("timestamp"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cases;
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE EmergencyCases SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
