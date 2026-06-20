package com.curasync.dao;

import com.curasync.models.Hospital;
import com.curasync.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalDAO {

    public List<Hospital> getAllHospitals() {
        List<Hospital> hospitals = new ArrayList<>();
        String sql = "SELECT * FROM Hospitals";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                hospitals.add(new Hospital(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("location_node"),
                    rs.getString("contact"),
                    rs.getDouble("lat"),
                    rs.getDouble("lng"),
                    rs.getString("specializations"),
                    rs.getString("address"),
                    rs.getString("district")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("[HospitalDAO] Loaded hospitals: " + hospitals.size());
        if (hospitals.isEmpty()) {
            // Fallback: create default Karnataka hospitals in-memory
            hospitals.add(new Hospital(1, "Ballari District Hospital", 0, "08192-111111", 15.1394, 76.9214, "Cardiology,General Medicine", "Ballari, Karnataka", "Karnataka"));
            hospitals.add(new Hospital(2, "Hospet General Hospital", 1, "08932-222222", 15.2694, 76.3570, "Neurology,Orthopedics,General Medicine", "Hospet, Karnataka", "Karnataka"));
            hospitals.add(new Hospital(3, "Mysore City Hospital", 2, "0821-333333", 12.2958, 76.6394, "Pediatrics,Oncology,General Medicine", "Mysuru, Karnataka", "Karnataka"));
            hospitals.add(new Hospital(4, "Bengaluru Metro Hospital", 3, "080-4444444", 12.9716, 77.5946, "Cardiology,Neurology,Trauma,General Medicine", "Bengaluru, Karnataka", "Karnataka"));
            System.out.println("[HospitalDAO] Using in-memory fallback hospitals: " + hospitals.size());
        }
        return hospitals;
    }

    public List<Hospital> getHospitalsByDistrict(String district) {
        List<Hospital> hospitals = new ArrayList<>();
        String sql = "SELECT * FROM Hospitals WHERE LOWER(district) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, district.toLowerCase().trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hospitals.add(new Hospital(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("location_node"),
                        rs.getString("contact"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng"),
                        rs.getString("specializations"),
                        rs.getString("address"),
                        rs.getString("district")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hospitals;
    }

    public void saveHospitals(List<Hospital> list, String district) {
        if (list == null || list.isEmpty()) return;
        
        // 1. Delete existing hospitals for this district
        String deleteHospSql = "DELETE FROM Hospitals WHERE LOWER(district) = ?";
        String deleteDocSql = "DELETE FROM Doctors WHERE hospital_id IN (SELECT id FROM Hospitals WHERE LOWER(district) = ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtDoc = conn.prepareStatement(deleteDocSql);
                 PreparedStatement stmtHosp = conn.prepareStatement(deleteHospSql)) {
                stmtDoc.setString(1, district.toLowerCase().trim());
                stmtDoc.executeUpdate();
                stmtHosp.setString(1, district.toLowerCase().trim());
                stmtHosp.executeUpdate();
            }
            
            // 2. Insert new hospitals
            String insertSql = "INSERT INTO Hospitals (name, location_node, contact, lat, lng, specializations, address, district) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                for (Hospital h : list) {
                    stmt.setString(1, h.getName());
                    stmt.setInt(2, h.getLocationNode());
                    stmt.setString(3, h.getContact());
                    stmt.setDouble(4, h.getLat());
                    stmt.setDouble(5, h.getLng());
                    stmt.setString(6, h.getSpecializations());
                    stmt.setString(7, h.getAddress());
                    stmt.setString(8, district);
                    stmt.executeUpdate();
                    
                    // Retrieve generated hospital ID to assign doctors
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int generatedId = keys.getInt(1);
                            // Pass the existing conn — avoids opening a second connection (SQLITE_BUSY)
                            DBConnection.generateDoctorsForHospital(conn, generatedId, h.getName(), h.getSpecializations());
                        }
                    }
                }
            }
            conn.commit();
            System.out.println("[HospitalDAO] Saved " + list.size() + " hospitals for district " + district);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

