package org.example.restaurant_management_system.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class KitchenTask {
    private int id;
    private int orderItemId;
    private String cookingStatus;
    private int priority;
    private LocalDateTime startCookingTime;
    private LocalDateTime endCookingTime;
    private int estimatedCookingTime;

    private int orderId;
    private String menuItemName;
    private int quantity;


    // конструктори
    public KitchenTask() {}

    public KitchenTask(int id, int orderItemId, String cookingStatus, int priority,
                       LocalDateTime startCookingTime, LocalDateTime endCookingTime,
                       int estimatedCookingTime, int orderId, String menuItemName, int quantity) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.cookingStatus = cookingStatus;
        this.priority = priority;
        this.startCookingTime = startCookingTime;
        this.endCookingTime = endCookingTime;
        this.estimatedCookingTime = estimatedCookingTime;
        this.orderId = orderId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
    }

    //геттери і сеттери

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public String getCookingStatus() { return cookingStatus; }
    public void setCookingStatus(String cookingStatus) { this.cookingStatus = cookingStatus; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDateTime getStartCookingTime() { return startCookingTime; }
    public void setStartCookingTime(LocalDateTime startCookingTime) { this.startCookingTime = startCookingTime; }

    public LocalDateTime getEndCookingTime() { return endCookingTime; }
    public void setEndCookingTime(LocalDateTime endCookingTime) { this.endCookingTime = endCookingTime; }

    public int getEstimatedCookingTime() { return estimatedCookingTime; }
    public void setEstimatedCookingTime(int estimatedCookingTime) { this.estimatedCookingTime = estimatedCookingTime; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getMenuItemName() { return menuItemName; }
    public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // відображення таймера
    // викликається TableView для колонки "Таймер"
    public String getTimerDisplay() {
        if ("Готово".equals(cookingStatus)) {
            if (startCookingTime != null && endCookingTime != null) {
                Duration duration = Duration.between(startCookingTime, endCookingTime);
                long minutes = duration.toMinutes();
                long seconds = duration.minusMinutes(minutes).getSeconds();
                return String.format("Виконано за: %02d:%02d", minutes, seconds);
            }
            return "Готово";
        } else if ("В роботі".equals(cookingStatus) && startCookingTime != null) {
            Duration elapsed = Duration.between(startCookingTime, LocalDateTime.now());
            long minutes = elapsed.toMinutes();
            long seconds = elapsed.minusMinutes(minutes).getSeconds();

            if (estimatedCookingTime > 0) {
                Duration estimatedDuration = Duration.ofMinutes(estimatedCookingTime);
                if (elapsed.compareTo(estimatedDuration) < 0) {
                    Duration remaining = estimatedDuration.minus(elapsed);
                    long remMinutes = remaining.toMinutes();
                    long remSeconds = remaining.minusMinutes(remMinutes).getSeconds();
                    return String.format("Залишилось: %02d:%02d", remMinutes, remSeconds);
                } else {
                    Duration overdue = elapsed.minus(estimatedDuration);
                    long ovdMinutes = overdue.toMinutes();
                    long ovdSeconds = overdue.minusMinutes(ovdMinutes).getSeconds();
                    return String.format("Запізнення: +%02d:%02d", ovdMinutes, ovdSeconds);
                }
            }
            return String.format("У процесі: %02d:%02d", minutes, seconds);
        } else if ("В очікуванні".equals(cookingStatus)) {
            return "В очікуванні";
        } else {
            return "";
        }
    }
}