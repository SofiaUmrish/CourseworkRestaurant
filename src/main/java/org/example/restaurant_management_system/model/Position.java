package org.example.restaurant_management_system.model;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return id == position.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}