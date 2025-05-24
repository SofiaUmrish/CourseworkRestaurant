package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Objects;

public class Ingredient {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty unit; // г, мл, шт
    private final ObjectProperty<LocalDate> expirationDate;
    private final DoubleProperty currentStock; //обчислюється для відображення

    // конструктори

    public Ingredient() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.unit = new SimpleStringProperty();
        this.expirationDate = new SimpleObjectProperty<>();
        this.currentStock = new SimpleDoubleProperty(0.0); // початковий запас 0
    }

    //завантаження з бд з id
    public Ingredient(int id, String name, String unit, LocalDate expirationDate) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.unit = new SimpleStringProperty(unit);
        this.expirationDate = new SimpleObjectProperty<>(expirationDate);
        this.currentStock = new SimpleDoubleProperty(0.0);
    }

    // currentStock вже обчислений
    public Ingredient(int id, String name, String unit, double currentStock, LocalDate expirationDate) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.unit = new SimpleStringProperty(unit);
        this.currentStock = new SimpleDoubleProperty(currentStock); // обчислений запас
        this.expirationDate = new SimpleObjectProperty<>(expirationDate);
    }


    // створення нового інгредієнта без id
    public Ingredient(String name, String unit, LocalDate expirationDate) {
        this.id = new SimpleIntegerProperty(); // id буде після збереження в бд
        this.name = new SimpleStringProperty(name);
        this.unit = new SimpleStringProperty(unit);
        this.expirationDate = new SimpleObjectProperty<>(expirationDate);
        this.currentStock = new SimpleDoubleProperty(0.0);
    }

    // геттери
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getUnit() { return unit.get(); }
    public LocalDate getExpirationDate() { return expirationDate.get(); }
    public double getCurrentStock() { return currentStock.get(); }

    // сеттери
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setUnit(String unit) { this.unit.set(unit); }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate.set(expirationDate); }
    public void setCurrentStock(double currentStock) { this.currentStock.set(currentStock); }


    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty unitProperty() { return unit; }
    public ObjectProperty<LocalDate> expirationDateProperty() { return expirationDate; }
    public DoubleProperty currentStockProperty() { return currentStock; }

    // перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id.get() == that.id.get(); // порівнюємо за id
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return getName();
    }
}