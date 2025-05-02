package com.seb.repository;

import com.seb.config.DatabaseConfig;
import com.seb.model.Tournament;
import com.seb.model.TournamentParticipant;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TournamentRepository {
    private final DatabaseConfig dbConfig;

    public TournamentRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // Create new tournament
    public Tournament createTournament() throws SQLException {
        String sql = "INSERT INTO tournaments (start_time, status) VALUES (?, ?) RETURNING tournament_id";

        Tournament tournament = new Tournament();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(tournament.getStartTime()));
            stmt.setString(2, tournament.getStatus());

            rs = stmt.executeQuery();

            if (rs.next()) {
                tournament.setTournamentId(rs.getInt("tournament_id"));
                return tournament;
            } else {
                throw new SQLException("Failed to create tournament, no ID returned");
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Get active tournament/create new one
    public Tournament getOrCreateActiveTournament() throws SQLException {
        // Try to find active tournament
        String sql = "SELECT tournament_id, start_time, status FROM tournaments " +
                "WHERE status = 'ACTIVE' ORDER BY start_time DESC LIMIT 1";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            if (rs.next()) {
                Tournament tournament = new Tournament(
                        rs.getInt("tournament_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getString("status")
                );

                // Check if tournament is expired
                if (tournament.isExpired()) {
                    // Complete tournament and create a new one
                    completeTournament(tournament.getTournamentId());
                    return createTournament();
                }

                // Load participants
                loadTournamentParticipants(tournament);
                return tournament;
            } else {
                // No active tournament -> create new one
                return createTournament();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Add participant to tournament
    public TournamentParticipant addParticipant(int tournamentId, int userId, int pushupCount) throws SQLException {
        // Check if user is already a participant
        String checkSql = "SELECT total_pushups FROM tournament_participants WHERE tournament_id = ? AND user_id = ?";

        Connection conn = null;
        PreparedStatement checkStmt = null;
        ResultSet checkRs = null;

        try {
            conn = dbConfig.getConnection();
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, tournamentId);
            checkStmt.setInt(2, userId);

            checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                // User already exists -> update pushup count
                int currentTotal = checkRs.getInt("total_pushups");
                int newTotal = currentTotal + pushupCount;

                String updateSql = "UPDATE tournament_participants SET total_pushups = ? " +
                        "WHERE tournament_id = ? AND user_id = ?";

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newTotal);
                    updateStmt.setInt(2, tournamentId);
                    updateStmt.setInt(3, userId);

                    int rows = updateStmt.executeUpdate();
                    if (rows != 1) {
                        throw new SQLException("Failed to update tournament participant");
                    }

                    // Get username for display
                    String username = getUsernameById(conn, userId);
                    return new TournamentParticipant(tournamentId, userId, newTotal, username);
                }
            } else {
                // New participant -> insert record
                String insertSql = "INSERT INTO tournament_participants (tournament_id, user_id, total_pushups) " +
                        "VALUES (?, ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, tournamentId);
                    insertStmt.setInt(2, userId);
                    insertStmt.setInt(3, pushupCount);

                    int rows = insertStmt.executeUpdate();
                    if (rows != 1) {
                        throw new SQLException("Failed to add tournament participant");
                    }

                    // Get username for display
                    String username = getUsernameById(conn, userId);
                    return new TournamentParticipant(tournamentId, userId, pushupCount, username);
                }
            }
        } finally {
            if (checkRs != null) checkRs.close();
            if (checkStmt != null) checkStmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Complete tournament (and update elo)
    public void completeTournament(int tournamentId) throws SQLException {
        Connection conn = null;

        try {
            conn = dbConfig.getConnection();
            // Set autocommit to false for transaction
            conn.setAutoCommit(false);

            // Mark tournament as completed
            String updateTournamentSql = "UPDATE tournaments SET status = 'COMPLETED' WHERE tournament_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateTournamentSql)) {
                stmt.setInt(1, tournamentId);
                stmt.executeUpdate();
            }

            // Get all participants
            List<TournamentParticipant> participants = new ArrayList<>();
            String getParticipantsSql = "SELECT tp.tournament_id, tp.user_id, tp.total_pushups, u.username " +
                    "FROM tournament_participants tp " +
                    "JOIN users u ON tp.user_id = u.user_id " +
                    "WHERE tournament_id = ? " +
                    "ORDER BY total_pushups DESC";

            try (PreparedStatement stmt = conn.prepareStatement(getParticipantsSql)) {
                stmt.setInt(1, tournamentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        participants.add(new TournamentParticipant(
                                rs.getInt("tournament_id"),
                                rs.getInt("user_id"),
                                rs.getInt("total_pushups"),
                                rs.getString("username")
                        ));
                    }
                }
            }

            // If there are participants -> update ELO scores
            if (!participants.isEmpty()) {
                // Determine winners (highest pushup count)
                int highestCount = participants.get(0).getTotalPushups();
                List<Integer> winnerIds = new ArrayList<>();

                for (TournamentParticipant p : participants) {
                    if (p.getTotalPushups() == highestCount) {
                        winnerIds.add(p.getUserId());
                    }
                }

                // Update ELO scores
                for (TournamentParticipant p : participants) {
                    int userId = p.getUserId();
                    int currentElo = getCurrentElo(conn, userId);
                    int eloChange;

                    if (winnerIds.contains(userId)) {
                        // Winners
                        eloChange = winnerIds.size() == 1 ? 2 : 1; // +2 for one winner and +1 for tie
                    } else {
                        // Losers
                        eloChange = -1;
                    }

                    updateElo(conn, userId, currentElo + eloChange);

                    // Add log entry
                    addLogEntry(conn, tournamentId,
                            "User " + p.getUsername() + " scored " + p.getTotalPushups() +
                                    " pushups and " + (eloChange >= 0 ? "gained " : "lost ") +
                                    Math.abs(eloChange) + " ELO points.");
                }

                // Add tournament summary log
                if (winnerIds.size() == 1) {
                    String winnerName = participants.get(0).getUsername();
                    addLogEntry(conn, tournamentId,
                            "Tournament completed. Winner: " + winnerName +
                                    " with " + highestCount + " pushups.");
                } else {
                    StringBuilder winnerNames = new StringBuilder();
                    for (int i = 0; i < winnerIds.size(); i++) {
                        int winnerId = winnerIds.get(i);
                        for (TournamentParticipant p : participants) {
                            if (p.getUserId() == winnerId) {
                                winnerNames.append(p.getUsername());
                                if (i < winnerIds.size() - 1) {
                                    winnerNames.append(", ");
                                }
                                break;
                            }
                        }
                    }
                    addLogEntry(conn, tournamentId,
                            "Tournament completed. It's a tie between: " + winnerNames.toString() +
                                    " with " + highestCount + " pushups each.");
                }
            } else {
                addLogEntry(conn, tournamentId, "Tournament completed with no participants.");
            }

            // Commit transaction
            conn.commit();

        } catch (SQLException e) {
            // Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    dbConfig.closeConnection(conn);
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Load tournament participants
    private void loadTournamentParticipants(Tournament tournament) throws SQLException {
        String sql = "SELECT tp.tournament_id, tp.user_id, tp.total_pushups, u.username " +
                "FROM tournament_participants tp " +
                "JOIN users u ON tp.user_id = u.user_id " +
                "WHERE tp.tournament_id = ? " +
                "ORDER BY tp.total_pushups DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tournament.getTournamentId());

            rs = stmt.executeQuery();

            while (rs.next()) {
                TournamentParticipant participant = new TournamentParticipant(
                        rs.getInt("tournament_id"),
                        rs.getInt("user_id"),
                        rs.getInt("total_pushups"),
                        rs.getString("username")
                );
                tournament.addParticipant(participant);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Get username by user ID
    private String getUsernameById(Connection conn, int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                } else {
                    return "Unknown";
                }
            }
        }
    }

    // Get current elo of user
    private int getCurrentElo(Connection conn, int userId) throws SQLException {
        String sql = "SELECT elo FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("elo");
                } else {
                    throw new SQLException("User not found");
                }
            }
        }
    }

    // Update elo of user
    private void updateElo(Connection conn, int userId, int newElo) throws SQLException {
        String sql = "UPDATE users SET elo = ? WHERE user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newElo);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    // Add log entry
    private void addLogEntry(Connection conn, int tournamentId, String message) throws SQLException {
        String sql = "INSERT INTO logs (tournament_id, message) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tournamentId);
            stmt.setString(2, message);
            stmt.executeUpdate();
        }
    }

    // Get tournament logs
    public List<Map<String, Object>> getTournamentLogs(int tournamentId) throws SQLException {
        String sql = "SELECT log_id, tournament_id, message, log_time FROM logs " +
                "WHERE tournament_id = ? ORDER BY log_time";

        List<Map<String, Object>> logs = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tournamentId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("logId", rs.getInt("log_id"));
                log.put("tournamentId", rs.getInt("tournament_id"));
                log.put("message", rs.getString("message"));
                log.put("logTime", rs.getTimestamp("log_time").toLocalDateTime());

                logs.add(log);
            }

            return logs;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Get recent tournaments
    public List<Map<String, Object>> getRecentTournaments(int limit) throws SQLException {
        String sql = "SELECT t.tournament_id, t.start_time, t.status, " +
                "(SELECT COUNT(*) FROM tournament_participants tp WHERE tp.tournament_id = t.tournament_id) as participant_count " +
                "FROM tournaments t ORDER BY t.start_time DESC LIMIT ?";

        List<Map<String, Object>> tournaments = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> tournament = new HashMap<>();
                int tournamentId = rs.getInt("tournament_id");

                tournament.put("tournamentId", tournamentId);
                tournament.put("startTime", rs.getTimestamp("start_time").toLocalDateTime());
                tournament.put("status", rs.getString("status"));
                tournament.put("participantCount", rs.getInt("participant_count"));

                // Get tournament winners
                if ("COMPLETED".equals(rs.getString("status"))) {
                    tournament.put("winners", getTournamentWinners(conn, tournamentId));
                }

                tournaments.add(tournament);
            }

            return tournaments;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Get tournament winners
    private List<Map<String, Object>> getTournamentWinners(Connection conn, int tournamentId) throws SQLException {
        String sql = "SELECT tp.user_id, u.username, tp.total_pushups " +
                "FROM tournament_participants tp " +
                "JOIN users u ON tp.user_id = u.user_id " +
                "WHERE tp.tournament_id = ? " +
                "ORDER BY tp.total_pushups DESC";

        List<Map<String, Object>> winners = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tournamentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int highestCount = rs.getInt("total_pushups");

                    // Add first winner
                    Map<String, Object> winner = new HashMap<>();
                    winner.put("userId", rs.getInt("user_id"));
                    winner.put("username", rs.getString("username"));
                    winner.put("pushups", highestCount);
                    winners.add(winner);

                    // Check for ties
                    while (rs.next()) {
                        int count = rs.getInt("total_pushups");
                        if (count == highestCount) {
                            winner = new HashMap<>();
                            winner.put("userId", rs.getInt("user_id"));
                            winner.put("username", rs.getString("username"));
                            winner.put("pushups", count);
                            winners.add(winner);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return winners;
    }
}
