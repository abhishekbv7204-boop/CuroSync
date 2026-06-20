package com.curasync.dao;

import com.curasync.models.Area;
import com.curasync.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreaDAO {

    public List<Area> getAllAreas() {
        List<Area> areas = new ArrayList<>();
        String sql = "SELECT * FROM Areas";
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                areas.add(new Area(rs.getInt("id"), rs.getString("name"), rs.getDouble("lat"), rs.getDouble("lng")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return areas;
    }

    public void addArea(String name, double lat, double lng) {
        String sql = "INSERT INTO Areas (name, lat, lng) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lng);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
