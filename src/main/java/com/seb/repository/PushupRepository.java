package com.seb.repository;

import com.seb.config.DatabaseConfig;
import com.seb.model.PushupRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushupRepository {
    private final DatabaseConfig dbConfig;

    public PushupRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Add a new pushup record
     */
    public PushupRecord addRecord(PushupRecord record) throws SQLException {
        String sql = "INSERT INTO pushup_records (user_id, count, record_date) VALUES (?, ?, ?) RETURNING record_id";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, record.getUserId());
            stmt.setInt(2, record.getCount());
            stmt.setTimestamp(3, Timestamp.valueOf(record.getRecordDate()));

            rs = stmt.executeQuery();

            if (rs.next()) {
                record.setRecordId(rs.getInt("record_id"));
                return record;
            } else {
                throw new SQLException("Failed to create pushup record, no ID returned");
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Get pushup history for a user
     */
    public List<PushupRecord> getUserHistory(int userId) throws SQLException {
        String sql = "SELECT record_id, user_id, count, record_date FROM pushup_records " +
                "WHERE user_id = ? ORDER BY record_date DESC";

        List<PushupRecord> records = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                PushupRecord record = new PushupRecord(
                        rs.getInt("record_id"),
                        rs.getInt("user_id"),
                        rs.getInt("count"),
                        rs.getTimestamp("record_date").toLocalDateTime()
                );
                records.add(record);
            }

            return records;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Get user's stats (total pushups, average, best record)
     */
    public Map<String, Object> getUserStats(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as entry_count, " +
                "COALESCE(SUM(count), 0) as total_pushups, " +
                "COALESCE(AVG(count), 0) as avg_pushups, " +
                "COALESCE(MAX(count), 0) as max_pushups " +
                "FROM pushup_records WHERE user_id = ?";

        Map<String, Object> stats = new HashMap<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("entryCount", rs.getInt("entry_count"));
                stats.put("totalPushups", rs.getInt("total_pushups"));
                stats.put("avgPushups", rs.getDouble("avg_pushups"));
                stats.put("maxPushups", rs.getInt("max_pushups"));

                // Get user ELO from users table
                String eloSql = "SELECT elo FROM users WHERE user_id = ?";
                try (PreparedStatement eloStmt = conn.prepareStatement(eloSql)) {
                    eloStmt.setInt(1, userId);
                    try (ResultSet eloRs = eloStmt.executeQuery()) {
                        if (eloRs.next()) {
                            stats.put("elo", eloRs.getInt("elo"));
                        }
                    }
                }
            }

            return stats;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }
}