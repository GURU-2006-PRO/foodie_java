package com.foodscanner.dao;

import com.foodscanner.patterns.observer.ScanObserver;
import com.foodscanner.patterns.singleton.DatabaseConnection;
import org.json.JSONObject;
import java.sql.*;
import java.util.*;

/**
 * Data Access Object for scan history.
 * Implements ScanObserver — auto-saves scans when Observer notifies.
 */
public class ScanHistoryDAO implements ScanObserver {

    @Override
    public void onScanCompleted(String foodName, JSONObject scanResult, String warnings) {
        saveScan(foodName, scanResult.toString(), warnings,
                 scanResult.optInt("health_score", 0));
    }

    public void saveScan(String foodName, String scanResult, String warnings, int healthScore) {
        String sql = "INSERT INTO scan_history (food_name, scan_result, health_warnings, health_score) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, foodName);
            ps.setString(2, scanResult);
            ps.setString(3, warnings);
            ps.setInt(4, healthScore);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save scan: " + e.getMessage());
        }
    }

    public List<Map<String, String>> getRecentScans(int limit) {
        List<Map<String, String>> results = new ArrayList<>();
        String sql = "SELECT id, food_name, health_score, scanned_at FROM scan_history ORDER BY scanned_at DESC LIMIT ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("id", String.valueOf(rs.getInt("id")));
                row.put("food_name", rs.getString("food_name"));
                row.put("health_score", String.valueOf(rs.getInt("health_score")));
                row.put("scanned_at", rs.getString("scanned_at"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch scans: " + e.getMessage());
        }
        return results;
    }

    public List<String> getActiveConditions() {
        List<String> conditions = new ArrayList<>();
        String sql = "SELECT condition_name FROM user_conditions";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                conditions.add(rs.getString("condition_name"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch conditions: " + e.getMessage());
        }
        return conditions;
    }
}
