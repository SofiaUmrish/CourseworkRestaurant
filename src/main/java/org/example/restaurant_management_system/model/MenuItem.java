package org.example.restaurant_management_system.model;

import javafx.beans.property.*; // Імпортуємо всі необхідні Property класи
import java.util.Objects; // Для equals/hashCode

public class MenuItem {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final ObjectProperty<Category> category; // ObjectProperty для об'єкта Category
    private final IntegerProperty categoryId; // Для зручності роботи з БД

    private final BooleanProperty vegetarian;
    private final BooleanProperty allergen;
    private final BooleanProperty glutenFree;

    // --- Конструктори ---

    public MenuItem() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.price = new SimpleDoubleProperty();
        this.category = new SimpleObjectProperty<>();
        this.categoryId = new SimpleIntegerProperty();
        this.vegetarian = new SimpleBooleanProperty();
        this.allergen = new SimpleBooleanProperty();
        this.glutenFree = new SimpleBooleanProperty();
    }

    // Конструктор для завантаження з БД (з ID та categoryId)
    public MenuItem(int id, String name, int categoryId, double price, boolean vegetarian, boolean allergen, boolean glutenFree) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.category = new SimpleObjectProperty<>(); // Об'єкт Category буде встановлено пізніше
        this.vegetarian = new SimpleBooleanProperty(vegetarian);
        this.allergen = new SimpleBooleanProperty(allergen);
        this.glutenFree = new SimpleBooleanProperty(glutenFree);
    }

    // Конструктор для створення нової позиції (без ID, з об'єктом Category)
    public MenuItem(String name, double price, Category category, boolean vegetarian, boolean allergen, boolean glutenFree) {
        this.id = new SimpleIntegerProperty(); // ID буде встановлено після збереження в БД
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.category = new SimpleObjectProperty<>(category);
        this.categoryId = new SimpleIntegerProperty(category != null ? category.getId() : 0); // Встановлюємо categoryId з об'єкта Category
        this.vegetarian = new SimpleBooleanProperty(vegetarian);
        this.allergen = new SimpleBooleanProperty(allergen);
        this.glutenFree = new SimpleBooleanProperty(glutenFree);
    }

    // --- Геттери для простих значень (використовуються в сервісі) ---
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getCategoryId() { return categoryId.get(); }
    public Category getCategory() { return category.get(); }
    public boolean isVegetarian() { return vegetarian.get(); }
    public boolean isAllergen() { return allergen.get(); }
    public boolean isGlutenFree() { return glutenFree.get(); }

    // --- Сеттери для простих значень (використовуються в сервісі) ---
    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public void setCategory(Category category) {
        this.category.set(category);
        if (category != null) {
            this.categoryId.set(category.getId());
        }
    }
    public void setVegetarian(boolean vegetarian) { this.vegetarian.set(vegetarian); }
    public void setAllergen(boolean allergen) { this.allergen.set(allergen); }
    public void setGlutenFree(boolean glutenFree) { this.glutenFree.set(glutenFree); }

    // --- Property-методи для JavaFX TableView (ОБОВ'ЯЗКОВІ) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public BooleanProperty vegetarianProperty() { return vegetarian; }
    public BooleanProperty allergenProperty() { return allergen; }
    public BooleanProperty glutenFreeProperty() { return glutenFree; }

    // --- Перевизначення equals/hashCode/toString ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id.get(), menuItem.id.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id.get() +
                ", name='" + name.get() + '\'' +
                ", price=" + price.get() +
                ", categoryId=" + categoryId.get() +
                ", vegetarian=" + vegetarian.get() +
                ", allergen=" + allergen.get() +
                ", glutenFree=" + glutenFree.get() +
                '}';
    }
}