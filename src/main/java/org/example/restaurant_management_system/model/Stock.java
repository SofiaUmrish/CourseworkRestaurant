package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.Objects;

public class Stock {
    private final IntegerProperty id;
    private final IntegerProperty ingredientId;
    private final ObjectProperty<Ingredient> ingredient;

    private final DoubleProperty changeAmount;
    private final StringProperty movementType; // income, expense, spoilage
    private final ObjectProperty<LocalDateTime> movementTime;

    // конструктори

    public Stock() {
        this.id = new SimpleIntegerProperty();
        this.ingredientId = new SimpleIntegerProperty();
        this.ingredient = new SimpleObjectProperty<>();
        this.changeAmount = new SimpleDoubleProperty();
        this.movementType = new SimpleStringProperty();
        this.movementTime = new SimpleObjectProperty<>();
    }

    //завантаження з бд з id і ingredientid
    public Stock(int id, int ingredientId, double changeAmount, String movementType, LocalDateTime movementTime) {
        this.id = new SimpleIntegerProperty(id);
        this.ingredientId = new SimpleIntegerProperty(ingredientId);
        this.ingredient = new SimpleObjectProperty<>();
        this.changeAmount = new SimpleDoubleProperty(changeAmount);
        this.movementType = new SimpleStringProperty(movementType);
        this.movementTime = new SimpleObjectProperty<>(movementTime);
    }

    //створення нового запису без idз об'єктом ingredient
    public Stock(Ingredient ingredient, double changeAmount, String movementType) {
        this.id = new SimpleIntegerProperty();
        this.ingredient = new SimpleObjectProperty<>(ingredient);
        this.ingredientId = new SimpleIntegerProperty(ingredient != null ? ingredient.getId() : 0);
        this.changeAmount = new SimpleDoubleProperty(changeAmount);
        this.movementType = new SimpleStringProperty(movementType);
        this.movementTime = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    // геттери
    public int getId() { return id.get(); }
    public int getIngredientId() { return ingredientId.get(); }
    public Ingredient getIngredient() { return ingredient.get(); }
    public double getChangeAmount() { return changeAmount.get(); }
    public String getMovementType() { return movementType.get(); }
    public LocalDateTime getMovementTime() { return movementTime.get(); }

    // сеттери
    public void setId(int id) { this.id.set(id); }
    public void setIngredientId(int ingredientId) { this.ingredientId.set(ingredientId); }
    public void setIngredient(Ingredient ingredient) {
        this.ingredient.set(ingredient);
        if (ingredient != null) {
            this.ingredientId.set(ingredient.getId());
        }
    }
    public void setChangeAmount(double changeAmount) { this.changeAmount.set(changeAmount); }
    public void setMovementType(String movementType) { this.movementType.set(movementType); }
    public void setMovementTime(LocalDateTime movementTime) { this.movementTime.set(movementTime); }


    public IntegerProperty idProperty() { return id; }
    public IntegerProperty ingredientIdProperty() { return ingredientId; }
    public ObjectProperty<Ingredient> ingredientProperty() { return ingredient; }
    public DoubleProperty changeAmountProperty() { return changeAmount; }
    public StringProperty movementTypeProperty() { return movementType; }
    public ObjectProperty<LocalDateTime> movementTimeProperty() { return movementTime; }

    // перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock that = (Stock) o;
        return id.get() == that.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }


}