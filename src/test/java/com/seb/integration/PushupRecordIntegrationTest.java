package com.seb.integration;

import com.seb.controller.PushupController;
import com.seb.controller.UserController;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PushupRecordIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testRecordPushupsAndViewHistory() {
        // Arrange
        UserController userController = new UserController();
        PushupController pushupController = new PushupController();

        // Create test user
        String username = "pushup" + System.currentTimeMillis();
        Map<String, Object> registerResult = userController.register(username, "password");
        int userId = (Integer)registerResult.get("userId");

        // Act - Record pushups
        int count1 = 40;
        int duration1 = 60;
        Map<String, Object> record1Result = pushupController.recordPushups(userId, count1, duration1);

        // Assert - Recording successful
        assertTrue((Boolean)record1Result.get("success"), "First pushup record should succeed");

        // Act - Record more pushups
        int count2 = 30;
        int duration2 = 45;
        Map<String, Object> record2Result = pushupController.recordPushups(userId, count2, duration2);

        // Assert - Second recording successful
        assertTrue((Boolean)record2Result.get("success"), "Second pushup record should succeed");

        // Act - Get history
        Map<String, Object> historyResult = pushupController.getUserHistory(userId);

        // Assert - History includes both records
        assertTrue((Boolean)historyResult.get("success"), "History retrieval should succeed");
        List<Map<String, Object>> history = (List<Map<String, Object>>)historyResult.get("history");

        assertEquals(2, history.size(), "History should contain 2 entries");

        boolean found1 = false;
        boolean found2 = false;

        for (Map<String, Object> entry : history) {
            int count = (Integer)entry.get("Count");
            int duration = (Integer)entry.get("DurationInSeconds");

            if (count == count1 && duration == duration1) {
                found1 = true;
            } else if (count == count2 && duration == duration2) {
                found2 = true;
            }
        }

        assertTrue(found1, "First record should be in history");
        assertTrue(found2, "Second record should be in history");

        // Act - Get stats
        Map<String, Object> statsResult = pushupController.getUserStats(userId);

        // Assert - Stats are correct
        assertTrue((Boolean)statsResult.get("success"), "Stats retrieval should succeed");
        assertEquals(2, statsResult.get("entryCount"), "Entry count should be 2");
        assertEquals(count1 + count2, (int)(statsResult.get("totalPushups")), "Total pushups should be the sum");
    }
}