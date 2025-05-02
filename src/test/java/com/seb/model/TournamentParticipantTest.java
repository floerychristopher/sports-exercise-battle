package com.seb.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TournamentParticipantTest {

    @Test
    public void testConstructor() {
        // Arrange
        int tournamentId = 1;
        int userId = 2;
        int totalPushups = 40;
        String username = "testUser";

        // Act
        TournamentParticipant participant = new TournamentParticipant(
                tournamentId, userId, totalPushups, username);

        // Assert
        assertEquals(tournamentId, participant.getTournamentId(), "Tournament ID should match");
        assertEquals(userId, participant.getUserId(), "User ID should match");
        assertEquals(totalPushups, participant.getTotalPushups(), "Total pushups should match");
        assertEquals(username, participant.getUsername(), "Username should match");
    }

    @Test
    public void testAddPushups() {
        // Arrange
        TournamentParticipant participant = new TournamentParticipant(1, 2, 40, "testUser");
        int additionalPushups = 10;

        // Act
        participant.addPushups(additionalPushups);

        // Assert
        assertEquals(50, participant.getTotalPushups(), "Total pushups should be updated");
    }
}