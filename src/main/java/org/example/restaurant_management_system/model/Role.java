package org.example.restaurant_management_system.model;

public class Role {
    private int id;
    private String position;
    private String firstName;
    private String lastName;

    // Конструктор без імені працівника
    public Role(int id, String position) {
        this.id = id;
        this.position = position;
    }

    // Конструктор з іменем і прізвищем працівника
    public Role(int id, String position, String firstName, String lastName) {
        this.id = id;
        this.position = position;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Конструктор, якщо не вимагається ім'я працівника
    public Role(String position) {
        this.position = position;
    }

    // Геттери
    public int getId() {
        return id;
    }

    public String getPosition() {
        return position;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // отримання повного імені
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return position + " - " + getFullName(); // Тепер виводить посаду і повне ім'я
    }
}
