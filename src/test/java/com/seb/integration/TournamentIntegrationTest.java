package com.seb.integration;

import com.seb.controller.PushupController;
import com.seb.controller.TournamentController;
import com.seb.controller.UserController;
import com.seb.model.Tournament;
import com.seb.model.TournamentParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class TournamentIntegrationTest {

    private UserController userController;
    private PushupController pushupController;
    private TournamentController tournamentController;
    private int userId1;
    private int userId2;

    @BeforeEach
    public void setup() {
        userController = new UserController();
        pushupController = new PushupController();
        tournamentController = new TournamentController();

        // Create test users
        String username1 = "tournament1" + System.currentTimeMillis();
        String username2 = "tournament2" + System.currentTimeMillis();

        Map<String, Object> result1 = userController.register(username1, "password");
        Map<String, Object> result2 = userController.register(username2, "password");

        userId1 = (Integer)result1.get("userId");
        userId2 = (Integer)result2.get("userId");
    }

    @Test
    public void testTournamentParticipation() {
        // Act - Get active tournament
        Map<String, Object> tournamentResult = tournamentController.getActiveTournament();

        // Assert - Tournament created
        assertTrue((Boolean)tournamentResult.get("success"), "Should get active tournament");
        assertNotNull(tournamentResult.get("tournamentId"), "Tournament ID should be returned");
        assertEquals("ACTIVE", tournamentResult.get("status"), "Tournament should be active");

        // Act - Record pushups for both users
        pushupController.recordPushups(userId1, 40, 60);
        pushupController.recordPushups(userId2, 30, 45);

        // Act - Get updated tournament
        Map<String, Object> updatedTournament = tournamentController.getActiveTournament();

        // Assert - Participants added
        @SuppressWarnings("unchecked")
        List<TournamentParticipant> participants = (List<TournamentParticipant>)updatedTournament.get("participants");

        boolean user1Found = false;
        boolean user2Found = false;

        for (TournamentParticipant participant : participants) {
            int participantUserId = participant.getUserId();
            if (participantUserId == userId1) {
                user1Found = true;
                assertEquals(40, participant.getTotalPushups(), "User 1's pushups should be recorded");
            } else if (participantUserId == userId2) {
                user2Found = true;
                assertEquals(30, participant.getTotalPushups(), "User 2's pushups should be recorded");
            }
        }

        assertTrue(user1Found, "User 1 should be in tournament");
        assertTrue(user2Found, "User 2 should be in tournament");
    }
}