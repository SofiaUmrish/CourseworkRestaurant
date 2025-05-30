package org.example.restaurant_management_system.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class KitchenTaskTest {

    @Test
    @DisplayName("Test no-argument constructor creates an empty KitchenTask")
    void noArgConstructor_CreatesEmptyTask() {
        KitchenTask task = new KitchenTask();
        assertEquals(0, task.getId());
        assertEquals(0, task.getOrderItemId());
        assertNull(task.getCookingStatus());
        assertEquals(0, task.getPriority());
        assertNull(task.getStartCookingTime());
        assertNull(task.getEndCookingTime());
        assertEquals(0, task.getEstimatedCookingTime());
        assertEquals(0, task.getOrderId());
        assertNull(task.getMenuItemName());
        assertEquals(0, task.getQuantity());
    }

    @Test
    @DisplayName("Test full constructor sets all fields correctly")
    void fullConstructor_SetsAllFields() {
        int id = 1;
        int orderItemId = 101;
        String cookingStatus = "В очікуванні";
        int priority = 2;
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();
        int estimatedTime = 30; // minutes
        int orderId = 50;
        String menuItemName = "Піца Маргарита";
        int quantity = 1;

        KitchenTask task = new KitchenTask(id, orderItemId, cookingStatus, priority,
                startTime, endTime, estimatedTime,
                orderId, menuItemName, quantity);

        assertEquals(id, task.getId());
        assertEquals(orderItemId, task.getOrderItemId());
        assertEquals(cookingStatus, task.getCookingStatus());
        assertEquals(priority, task.getPriority());
        assertEquals(startTime, task.getStartCookingTime());
        assertEquals(endTime, task.getEndCookingTime());
        assertEquals(estimatedTime, task.getEstimatedCookingTime());
        assertEquals(orderId, task.getOrderId());
        assertEquals(menuItemName, task.getMenuItemName());
        assertEquals(quantity, task.getQuantity());
    }

    @Test
    @DisplayName("Test setId and getId work correctly")
    void setIdAndGetId() {
        KitchenTask task = new KitchenTask();
        int testId = 99;
        task.setId(testId);
        assertEquals(testId, task.getId());
    }

    @Test
    @DisplayName("Test setCookingStatus and getCookingStatus work correctly")
    void setCookingStatusAndGetCookingStatus() {
        KitchenTask task = new KitchenTask();
        String status = "Готово";
        task.setCookingStatus(status);
        assertEquals(status, task.getCookingStatus());
    }

    @Test
    @DisplayName("Test setStartCookingTime allows null")
    void setStartCookingTime_AllowsNull() {
        KitchenTask task = new KitchenTask();
        task.setStartCookingTime(LocalDateTime.now());
        assertNotNull(task.getStartCookingTime());
        task.setStartCookingTime(null);
        assertNull(task.getStartCookingTime());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'Готово' when status is 'Готово' and times are null")
    void getTimerDisplay_StatusReady_TimesNull_ReturnsReadyString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Готово");
        task.setStartCookingTime(null);
        task.setEndCookingTime(null);
        assertEquals("Готово", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'Готово' when status is 'Готово' and start time is null")
    void getTimerDisplay_StatusReady_StartTimeNull_ReturnsReadyString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Готово");
        task.setStartCookingTime(null);
        task.setEndCookingTime(LocalDateTime.now());
        assertEquals("Готово", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'Готово' when status is 'Готово' and end time is null")
    void getTimerDisplay_StatusReady_EndTimeNull_ReturnsReadyString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Готово");
        task.setStartCookingTime(LocalDateTime.now());
        task.setEndCookingTime(null);
        assertEquals("Готово", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay formats 'Виконано за' correctly when status is 'Готово'")
    void getTimerDisplay_StatusReady_CalculatesExecutionTime() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Готово");
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(15).minusSeconds(30);
        LocalDateTime endTime = LocalDateTime.now();
        task.setStartCookingTime(startTime);
        task.setEndCookingTime(endTime);

        String expectedDisplay = String.format("Виконано за: %02d:%02d", 15, 30);
        assertEquals(expectedDisplay, task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'Виконано за: 00:00' for zero duration")
    void getTimerDisplay_StatusReady_ZeroDuration() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Готово");
        LocalDateTime time = LocalDateTime.now();
        task.setStartCookingTime(time);
        task.setEndCookingTime(time);
        assertEquals("Виконано за: 00:00", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'В очікуванні' when status is 'В очікуванні'")
    void getTimerDisplay_StatusWaiting_ReturnsWaitingString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В очікуванні");
        assertEquals("В очікуванні", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns empty string for unknown or null status")
    void getTimerDisplay_UnknownOrNullStatus_ReturnsEmptyString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("Невідомий статус");
        assertEquals("", task.getTimerDisplay());

        task.setCookingStatus(null);
        assertEquals("", task.getTimerDisplay());
    }

    @Test
    @DisplayName("getTimerDisplay returns 'У процесі: MM:SS' when status 'В роботі', estimated time is 0")
    void getTimerDisplay_StatusInProgress_EstimatedTimeZero_ShowsElapsedTime() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В роботі");
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(5);
        task.setStartCookingTime(startTime);
        task.setEstimatedCookingTime(0);

        LocalDateTime currentTimeForCalc = LocalDateTime.now();
        Duration elapsed = Duration.between(startTime, currentTimeForCalc);
        long minutes = elapsed.toMinutes();
        long seconds = elapsed.minusMinutes(minutes).getSeconds();
        String expectedDisplay = String.format("У процесі: %02d:%02d", minutes, seconds);

        String actualDisplay = task.getTimerDisplay();

        if (!expectedDisplay.equals(actualDisplay)) {
            seconds++;
            if (seconds >= 60) { seconds = 0; minutes++;}
            expectedDisplay = String.format("У процесі: %02d:%02d", minutes, seconds);
        }
        if (!expectedDisplay.equals(actualDisplay)) {
            seconds--;
            if (seconds < 0 && minutes > 0) { seconds = 59; minutes--;}
            else if (seconds < 0 && minutes == 0) { seconds = 0; }
            expectedDisplay = String.format("У процесі: %02d:%02d", minutes, seconds);
        }
        assertEquals(expectedDisplay, actualDisplay,
                "Відображення часу 'У процесі' має бути коректним.");
    }

    @Test
    @DisplayName("getTimerDisplay returns empty string when status 'В роботі' but start time is null")
    void getTimerDisplay_StatusInProgress_StartTimeNull_ReturnsEmptyString() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В роботі");
        task.setStartCookingTime(null);
        assertEquals("", task.getTimerDisplay());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 20",
            "2, 70",
            "5, 150"
    })
    @DisplayName("getTimerDisplay formats 'Залишилось: MM:SS' correctly")
    void getTimerDisplay_StatusInProgress_ShowsRemainingTime(int estimatedMinutes, int elapsedSeconds) {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В роботі");
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(elapsedSeconds);
        task.setStartCookingTime(startTime);
        task.setEstimatedCookingTime(estimatedMinutes);

        LocalDateTime currentTimeForCalc = LocalDateTime.now();
        Duration currentElapsed = Duration.between(startTime, currentTimeForCalc);
        Duration estimatedDuration = Duration.ofMinutes(estimatedMinutes);

        assertTrue(estimatedMinutes > 0, "Estimated minutes " +
                "should be > 0 for this test path.");
        assertTrue(currentElapsed.compareTo(estimatedDuration) < 0,
                "Elapsed time should be less than estimated for 'Залишилось'. Current elapsed: "
                        +currentElapsed+", estimated: "+estimatedDuration);

        Duration remainingDuration = estimatedDuration.minus(currentElapsed);
        long remMinutes = remainingDuration.toMinutes();
        long remSeconds = remainingDuration.minusMinutes(remMinutes).getSeconds();
        if (remMinutes < 0) remMinutes = 0;
        if (remSeconds < 0) remSeconds = 0;

        String expectedDisplay = String.format("Залишилось: %02d:%02d", remMinutes, remSeconds);
        String actualDisplay = task.getTimerDisplay();

        if (!expectedDisplay.equals(actualDisplay)) {
            Duration plusOneSec = remainingDuration.plusSeconds(1);
            String expectedPlus = String.format("Залишилось: %02d:%02d", plusOneSec.toMinutes(),
                    plusOneSec.minusMinutes(plusOneSec.toMinutes()).getSeconds());
            Duration minusOneSec = remainingDuration.minusSeconds(1);
            String expectedMinus = String.format("Залишилось: %02d:%02d", Math.max(0,minusOneSec.toMinutes()),
                    Math.max(0,minusOneSec.minusMinutes(minusOneSec.toMinutes()).getSeconds()));


            if (expectedPlus.equals(actualDisplay) || expectedMinus.equals(actualDisplay)) {

            } else {
                assertEquals(expectedDisplay, actualDisplay,
                        "Залишковий час має бути коректним (допускається похибка в 1с).");
            }
        } else {
            assertEquals(expectedDisplay, actualDisplay);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, 70",
            "2, 150",
            "1, 125"
    })
    @DisplayName("getTimerDisplay formats 'Запізнення: +MM:SS' correctly")
    void getTimerDisplay_StatusInProgress_ShowsOverdueTime(int estimatedMinutes, int elapsedSeconds) {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В роботі");
        LocalDateTime startTime = LocalDateTime.now().minusSeconds(elapsedSeconds);
        task.setStartCookingTime(startTime);
        task.setEstimatedCookingTime(estimatedMinutes);

        LocalDateTime currentTimeForCalc = LocalDateTime.now();
        Duration currentElapsed = Duration.between(startTime, currentTimeForCalc);
        Duration estimatedDuration = Duration.ofMinutes(estimatedMinutes);

        assertTrue(estimatedMinutes > 0,
                "Estimated minutes should be > 0 for this test path.");
        assertTrue(currentElapsed.compareTo(estimatedDuration) >= 0,
                "Elapsed time should be greater or equal to estimated for 'Запізнення'." +
                        " Current elapsed: "+currentElapsed+", estimated: "+estimatedDuration);

        Duration overdueDuration = currentElapsed.minus(estimatedDuration);
        long ovdMinutes = overdueDuration.toMinutes();
        long ovdSeconds = overdueDuration.minusMinutes(ovdMinutes).getSeconds();
        if (ovdMinutes < 0) ovdMinutes = 0;
        if (ovdSeconds < 0) ovdSeconds = 0;

        String expectedDisplay = String.format("Запізнення: +%02d:%02d", ovdMinutes, ovdSeconds);
        String actualDisplay = task.getTimerDisplay();

        if (!expectedDisplay.equals(actualDisplay)) {
            Duration plusOneSec = overdueDuration.plusSeconds(1);
            String expectedPlus = String.format("Запізнення: +%02d:%02d",
                    plusOneSec.toMinutes(), plusOneSec.minusMinutes(plusOneSec.toMinutes()).getSeconds());
            Duration minusOneSec = overdueDuration.minusSeconds(1);
            String expectedMinus = String.format("Запізнення: +%02d:%02d",
                    Math.max(0,minusOneSec.toMinutes()), Math.max(0,minusOneSec.minusMinutes
                            (minusOneSec.toMinutes()).getSeconds()));

            if (expectedPlus.equals(actualDisplay) || expectedMinus.equals(actualDisplay)) {

            } else {
                assertEquals(expectedDisplay, actualDisplay,
                        "Час запізнення має бути коректним (допускається похибка в 1с).");
            }
        } else {
            assertEquals(expectedDisplay, actualDisplay);
        }
    }

    @Test
    @DisplayName("getTimerDisplay handles edge case where elapsed time equals estimated time for 'Запізнення'")
    void getTimerDisplay_StatusInProgress_ElapsedEqualsEstimated_ShowsZeroOverdueOrSlightOverdue() {
        KitchenTask task = new KitchenTask();
        task.setCookingStatus("В роботі");
        int estimatedMinutes = 1;
        task.setEstimatedCookingTime(estimatedMinutes);

        LocalDateTime currentTimeForCalc = LocalDateTime.now();
        LocalDateTime startTime = currentTimeForCalc.minusMinutes(estimatedMinutes);
        task.setStartCookingTime(startTime);

        String actualDisplay = task.getTimerDisplay();

        Duration currentElapsed = Duration.between(startTime, LocalDateTime.now());
        Duration estimatedDuration = Duration.ofMinutes(estimatedMinutes);

        if (currentElapsed.compareTo(estimatedDuration) >= 0) {
            Duration overdueDuration = currentElapsed.minus(estimatedDuration);
            long ovdMinutes = overdueDuration.toMinutes();
            long ovdSeconds = overdueDuration.minusMinutes(ovdMinutes).getSeconds();
            String expectedOverdue = String.format("Запізнення: +%02d:%02d", ovdMinutes, ovdSeconds);
            assertEquals(expectedOverdue, actualDisplay);
        } else {
            fail("Очікувалося, що час вийшов або дорівнює орієнтовному.");
        }
    }


    @Test
    @DisplayName("Test getters and setters work together correctly")
    void gettersAndSetters_WorkTogether() {
        KitchenTask task = new KitchenTask();

        int id = 10;
        int orderItemId = 110;
        String cookingStatus = "В роботі";
        int priority = 1;
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endTime = null;
        int estimatedTime = 15;
        int orderId = 55;
        String menuItemName = "Суп";
        int quantity = 2;

        task.setId(id);
        task.setOrderItemId(orderItemId);
        task.setCookingStatus(cookingStatus);
        task.setPriority(priority);
        task.setStartCookingTime(startTime);
        task.setEndCookingTime(endTime);
        task.setEstimatedCookingTime(estimatedTime);
        task.setOrderId(orderId);
        task.setMenuItemName(menuItemName);
        task.setQuantity(quantity);

        assertEquals(id, task.getId());
        assertEquals(orderItemId, task.getOrderItemId());
        assertEquals(cookingStatus, task.getCookingStatus());
        assertEquals(priority, task.getPriority());
        assertEquals(startTime, task.getStartCookingTime());
        assertNull(task.getEndCookingTime());
        assertEquals(estimatedTime, task.getEstimatedCookingTime());
        assertEquals(orderId, task.getOrderId());
        assertEquals(menuItemName, task.getMenuItemName());
        assertEquals(quantity, task.getQuantity());
    }
}