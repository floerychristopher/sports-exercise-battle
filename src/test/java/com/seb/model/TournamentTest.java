package com.seb.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class TournamentTest {

    @Test
    public void testNewTournament() {
        // Act
        Tournament tournament = new Tournament();

        // Assert
        assertEquals("ACTIVE", tournament.getStatus(), "New tournament should be active");
        assertNotNull(tournament.getStartTime(), "Start time should not be null");
        assertTrue(tournament.getParticipants().isEmpty(), "Participants list should be empty");
    }

    @Test
    public void testAddParticipant() {
        // Arrange
        Tournament tournament = new Tournament();
        TournamentParticipant participant = new TournamentParticipant(
                tournament.getTournamentId(), 1, 40, "testUser");

        // Act
        tournament.addParticipant(participant);

        // Assert
        assertEquals(1, tournament.getParticipants().size(), "Tournament should have one participant");
        assertEquals(participant, tournament.getParticipants().get(0), "Tournament should contain added participant");
    }

    @Test
    public void testIsExpired_NotExpired() {
        // Arrange
        Tournament tournament = new Tournament(); // Current time

        // Act & Assert
        assertFalse(tournament.isExpired(), "New tournament should not be expired");
    }

    @Test
    public void testIsExpired_Expired() {
        // Arrange
        LocalDateTime oldTime = LocalDateTime.now().minusMinutes(3); // 3 minutes ago
        Tournament tournament = new Tournament(1, oldTime, "ACTIVE");

        // Act & Assert
        assertTrue(tournament.isExpired(), "Tournament from 3 minutes ago should be expired");
    }
}