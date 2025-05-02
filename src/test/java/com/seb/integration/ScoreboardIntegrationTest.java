package com.seb.integration;

import com.seb.controller.ProfileController;
import com.seb.controller.PushupController;
import com.seb.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ScoreboardIntegrationTest {

    private UserController userController;
    private PushupController pushupController;
    private ProfileController profileController;
    private int userId1;
    private int userId2;
    private String username1;
    private String username2;

    @BeforeEach
    public void setup() {
        userController = new UserController();
        pushupController = new PushupController();
        profileController = new ProfileController();

        // Create test users
        username1 = "score1" + System.currentTimeMillis();
        username2 = "score2" + System.currentTimeMillis();

        Map<String, Object> result1 = userController.register(username1, "password");
        Map<String, Object> result2 = userController.register(username2, "password");

        userId1 = (Integer)result1.get("userId");
        userId2 = (Integer)result2.get("userId");

        // Record some pushups
        pushupController.recordPushups(userId1, 50, 60);
        pushupController.recordPushups(userId2, 30, 45);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testScoreboardContainsUsers() {
        // Act
        Map<String, Object> scoreboardResult = profileController.getScoreboard();

        // Assert
        assertTrue((Boolean)scoreboardResult.get("success"), "Scoreboard retrieval should succeed");
        List<Map<String, Object>> scoreboard = (List<Map<String, Object>>)scoreboardResult.get("scoreboard");

        boolean user1Found = false;
        boolean user2Found = false;

        for (Map<String, Object> entry : scoreboard) {
            String username = (String)entry.get("username");
            if (username.equals(username1)) {
                user1Found = true;
                assertEquals(50, entry.get("totalPushups"), "User 1's pushups should be correct");
            } else if (username.equals(username2)) {
                user2Found = true;
                assertEquals(30, entry.get("totalPushups"), "User 2's pushups should be correct");
            }
        }

        assertTrue(user1Found, "User 1 should be in scoreboard");
        assertTrue(user2Found, "User 2 should be in scoreboard");
    }
}