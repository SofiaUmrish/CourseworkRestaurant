package org.example.restaurant_management_system.model;

public class Category {
    private int id;
    private String name;

    // --- Конструктори ---

    // Конструктор без аргументів (no-arg constructor) - корисний для ORM або якщо ID встановлюється пізніше
    public Category() {
    }

    // Повний конструктор
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Конструктор без ID (для нових категорій, де ID генерується базою даних)
    public Category(String name) {
        this.name = name;
    }

    // --- Геттери ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // --- Сеттери ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Додаткові методи (опціонально, але корисно) ---

    @Override
    public String toString() {
        return name; // Це дозволить виводити об'єкт Category як його назву в ComboBox'ах тощо
    }

}