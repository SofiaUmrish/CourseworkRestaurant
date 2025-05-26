package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.util.Objects;

public class Salary {
    private final IntegerProperty id;
    private final IntegerProperty positionId;
    private final ObjectProperty<Position> position;
    private final DoubleProperty monthlySalary;

    public Salary() {
        this.id = new SimpleIntegerProperty();
        this.positionId = new SimpleIntegerProperty();
        this.position = new SimpleObjectProperty<>();
        this.monthlySalary = new SimpleDoubleProperty();
    }

    public Salary(int id, int positionId, double monthlySalary) {
        this.id = new SimpleIntegerProperty(id);
        this.positionId = new SimpleIntegerProperty(positionId);
        this.position = new SimpleObjectProperty<>();
        this.monthlySalary = new SimpleDoubleProperty(monthlySalary);
    }

    public Salary(int id, Position position, double monthlySalary) {
        this.id = new SimpleIntegerProperty(id);
        this.position = new SimpleObjectProperty<>(position);
        this.positionId = new SimpleIntegerProperty(position != null ? position.getId() : 0);
        this.monthlySalary = new SimpleDoubleProperty(monthlySalary);
    }

    // Для створення нової зарплати позиції без ID
    public Salary(Position position, double monthlySalary) {
        this.id = new SimpleIntegerProperty();
        this.position = new SimpleObjectProperty<>(position);
        this.positionId = new SimpleIntegerProperty(position != null ? position.getId() : 0);
        this.monthlySalary = new SimpleDoubleProperty(monthlySalary);
    }

    // Getters
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public int getPositionId() { return positionId.get(); }
    public IntegerProperty positionIdProperty() { return positionId; }

    public Position getPosition() { return position.get(); }
    public ObjectProperty<Position> positionProperty() { return position; }

    public double getMonthlySalary() { return monthlySalary.get(); }
    public DoubleProperty monthlySalaryProperty() { return monthlySalary; }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setPositionId(int positionId) { this.positionId.set(positionId); }
    public void setPosition(Position position) {
        this.position.set(position);
        if (position != null) {
            this.positionId.set(position.getId());
        }
    }
    public void setMonthlySalary(double monthlySalary) { this.monthlySalary.set(monthlySalary); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Salary that = (Salary) o;
        return id.get() == that.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }


}