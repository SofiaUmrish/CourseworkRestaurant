package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.util.Objects;

public class Table {
    private final IntegerProperty id;
    private final StringProperty tableNumber;
    private final IntegerProperty capacity;

    //конструктори

    public Table() {
        this.id = new SimpleIntegerProperty();
        this.tableNumber = new SimpleStringProperty();
        this.capacity = new SimpleIntegerProperty();
    }

    // завантаження з бд
    public Table(int id, String tableNumber, int capacity) {
        this.id = new SimpleIntegerProperty(id);
        this.tableNumber = new SimpleStringProperty(tableNumber);
        this.capacity = new SimpleIntegerProperty(capacity);
    }

    // новий запис без id
    public Table(String tableNumber, int capacity) {
        this.id = new SimpleIntegerProperty(); // id буде згенеровано бд
        this.tableNumber = new SimpleStringProperty(tableNumber);
        this.capacity = new SimpleIntegerProperty(capacity);
    }

    // геттери
    public int getId() { return id.get(); }
    public String getTableNumber() { return tableNumber.get(); }
    public int getCapacity() { return capacity.get(); }

    //сеттери
    public void setId(int id) { this.id.set(id); }
    public void setTableNumber(String tableNumber) { this.tableNumber.set(tableNumber); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }


    public IntegerProperty idProperty() { return id; }
    public StringProperty tableNumberProperty() { return tableNumber; }
    public IntegerProperty capacityProperty() { return capacity; }

    //перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return id.get() == table.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "Столик " + getTableNumber() + " (місць: " + getCapacity() + ")";
    }
}
