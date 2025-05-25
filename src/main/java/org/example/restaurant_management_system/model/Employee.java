package org.example.restaurant_management_system.model;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private Position position;
    private String password;  // додаємо пароль

    // Конструктор з паролем
    public Employee(int id, String firstName, String lastName, Position position, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.password = password;
    }

    // Гетери та сетери
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Position getPosition() { return position; }
    public String getPassword() { return password; }  // гетер для пароля

    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPosition(Position position) { this.position = position; }
    public void setPassword(String password) { this.password = password; }  // сетер для пароля

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " (" + getPosition().getName() + ")";
    }
}
