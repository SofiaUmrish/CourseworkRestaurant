package org.example.restaurant_management_system.model;

public class Position {
    private int id;
    private String name;

    // Конструктори
    public Position() {
    }

    public Position(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Position(String name) {
        this.name = name;
    }

    // Гетери
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Сетери
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}