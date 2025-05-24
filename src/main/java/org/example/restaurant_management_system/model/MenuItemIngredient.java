package org.example.restaurant_management_system.model;

import javafx.beans.property.*;
import java.util.Objects;

public class MenuItemIngredient {
    private final IntegerProperty id;
    private final IntegerProperty menuItemId;
    private final ObjectProperty<MenuItem> menuItem;

    private final IntegerProperty ingredientId;
    private final ObjectProperty<Ingredient> ingredient;

    private final DoubleProperty quantityPerUnit; // скільки йде на одну страву

    // --- конструктори ---

    public MenuItemIngredient() {
        this.id = new SimpleIntegerProperty();
        this.menuItemId = new SimpleIntegerProperty();
        this.menuItem = new SimpleObjectProperty<>();
        this.ingredientId = new SimpleIntegerProperty();
        this.ingredient = new SimpleObjectProperty<>();
        this.quantityPerUnit = new SimpleDoubleProperty();
    }

    //завантаження з бд з id та id зв'язаних об'єктів
    public MenuItemIngredient(int id, int menuItemId, int ingredientId, double quantityPerUnit) {
        this.id = new SimpleIntegerProperty(id);
        this.menuItemId = new SimpleIntegerProperty(menuItemId);
        this.menuItem = new SimpleObjectProperty<>();
        this.ingredientId = new SimpleIntegerProperty(ingredientId);
        this.ingredient = new SimpleObjectProperty<>();
        this.quantityPerUnit = new SimpleDoubleProperty(quantityPerUnit);
    }

    //завантаження з бд або створення з повними об'єктами
    public MenuItemIngredient(int id, MenuItem menuItem, Ingredient ingredient, double quantityPerUnit) {
        this.id = new SimpleIntegerProperty(id);
        this.menuItem = new SimpleObjectProperty<>(menuItem);
        this.menuItemId = new SimpleIntegerProperty(menuItem != null ? menuItem.getId() : 0);
        this.ingredient = new SimpleObjectProperty<>(ingredient);
        this.ingredientId = new SimpleIntegerProperty(ingredient != null ? ingredient.getId() : 0);
        this.quantityPerUnit = new SimpleDoubleProperty(quantityPerUnit);
    }

    //створення нового запису без id з об'єктами
    public MenuItemIngredient(MenuItem menuItem, Ingredient ingredient, double quantityPerUnit) {
        this.id = new SimpleIntegerProperty();
        this.menuItem = new SimpleObjectProperty<>(menuItem);
        this.menuItemId = new SimpleIntegerProperty(menuItem != null ? menuItem.getId() : 0);
        this.ingredient = new SimpleObjectProperty<>(ingredient);
        this.ingredientId = new SimpleIntegerProperty(ingredient != null ? ingredient.getId() : 0);
        this.quantityPerUnit = new SimpleDoubleProperty(quantityPerUnit);
    }


    // геттери
    public int getId() { return id.get(); }
    public int getMenuItemId() { return menuItemId.get(); }
    public MenuItem getMenuItem() { return menuItem.get(); }
    public int getIngredientId() { return ingredientId.get(); }
    public Ingredient getIngredient() { return ingredient.get(); }
    public double getQuantityPerUnit() { return quantityPerUnit.get(); }

    //сеттери
    public void setId(int id) { this.id.set(id); }
    public void setMenuItemId(int menuItemId) { this.menuItemId.set(menuItemId); }
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem.set(menuItem);
        if (menuItem != null) {
            this.menuItemId.set(menuItem.getId());
        }
    }
    public void setIngredientId(int ingredientId) { this.ingredientId.set(ingredientId); }
    public void setIngredient(Ingredient ingredient) {
        this.ingredient.set(ingredient);
        if (ingredient != null) {
            this.ingredientId.set(ingredient.getId());
        }
    }
    public void setQuantityPerUnit(double quantityPerUnit) { this.quantityPerUnit.set(quantityPerUnit); }


    public IntegerProperty idProperty() { return id; }
    public IntegerProperty menuItemIdProperty() { return menuItemId; }
    public ObjectProperty<MenuItem> menuItemProperty() { return menuItem; }
    public IntegerProperty ingredientIdProperty() { return ingredientId; }
    public ObjectProperty<Ingredient> ingredientProperty() { return ingredient; }
    public DoubleProperty quantityPerUnitProperty() { return quantityPerUnit; }

    //перевизначення
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemIngredient that = (MenuItemIngredient) o;
        return id.get() == that.id.get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }


}