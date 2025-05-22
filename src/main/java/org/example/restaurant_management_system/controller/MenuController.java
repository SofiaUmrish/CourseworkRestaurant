package org.example.restaurant_management_system.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.restaurant_management_system.model.Category;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.service.CategoryService;
import org.example.restaurant_management_system.service.MenuItemService;
import org.example.restaurant_management_system.util.DatabaseConnection; // Переконайтеся, що цей шлях правильний

import java.sql.Connection; // Додано імпорт Connection
import java.sql.SQLException;
import java.util.List;

public class MenuController {

    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private CheckBox vegetarianCheckBox;
    @FXML private CheckBox allergenCheckBox;
    @FXML private CheckBox glutenFreeCheckBox;
    @FXML private TableView<MenuItem> menuTableView;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private TableColumn<MenuItem, Category> categoryColumn;
    @FXML private TableColumn<MenuItem, Boolean> vegetarianColumn;
    @FXML private TableColumn<MenuItem, Boolean> allergenColumn;
    @FXML private TableColumn<MenuItem, Boolean> glutenFreeColumn;


    private MenuItemService menuItemService;
    private CategoryService categoryService;

    private ObservableList<MenuItem> menuItems;

    private MenuItem selectedItem;

    @FXML
    public void initialize() {

        Platform.runLater(() -> {
            Scene scene = menuTableView.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/MenuStyle.css").toExternalForm());
            } else {
                System.err.println("Scene is null. Cannot load CSS.");
            }
        });
        try {
            Connection dbConnection = DatabaseConnection.getConnection();
            this.menuItemService = new MenuItemService(dbConnection);
            this.categoryService = new CategoryService(dbConnection);
        } catch (SQLException e) {
            showAlert("Помилка підключення до БД", "Не вдалося встановити з'єднання з базою даних: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        vegetarianColumn.setCellValueFactory(cellData -> cellData.getValue().vegetarianProperty());
        allergenColumn.setCellValueFactory(cellData -> cellData.getValue().allergenProperty());
        glutenFreeColumn.setCellValueFactory(cellData -> cellData.getValue().glutenFreeProperty());

        loadCategories();
        loadMenuItems();
        menuTableView.setItems(menuItems);


    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            showAlert("Помилка завантаження категорій", "Не вдалося завантажити категорії: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMenuItems() {
        try {
            List<MenuItem> items = menuItemService.getAllMenuItems();
            // Завантажуємо всі категорії через categoryService для зв'язування
            List<Category> allCategories = categoryService.getAllCategories();
            for (MenuItem item : items) {
                for (Category cat : allCategories) {
                    if (item.getCategoryId() == cat.getId()) {
                        item.setCategory(cat); // Встановлюємо об'єкт Category
                        break;
                    }
                }
            }
            menuItems = FXCollections.observableArrayList(items);
        } catch (SQLException e) {
            showAlert("Помилка завантаження меню", "Не вдалося завантажити пункти меню: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddItem(ActionEvent event) {
        String name = nameField.getText();
        double price;
        try {
            price = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Ціна повинна бути числом.");
            return;
        }

        Category category = categoryComboBox.getValue();
        if (category == null) {
            showAlert("Помилка вводу", "Будь ласка, оберіть категорію.");
            return;
        }

        boolean vegetarian = vegetarianCheckBox.isSelected();
        boolean allergen = allergenCheckBox.isSelected();
        boolean glutenFree = glutenFreeCheckBox.isSelected();

        MenuItem newItem = new MenuItem(name, price, category, vegetarian, allergen, glutenFree);
        try {
            menuItemService.saveMenuItem(newItem);
            menuItems.add(newItem);
            clearForm();
            showAlert("Успіх", "Пункт меню додано.");
        } catch (SQLException e) {
            showAlert("Помилка додавання", "Не вдалося додати пункт меню: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditItem(ActionEvent event) {
        if (selectedItem == null) {
            showAlert("Не вибрано елемент", "Будь ласка, виберіть позицію для редагування.");
            return;
        }

        selectedItem.setName(nameField.getText());
        try {
            selectedItem.setPrice(Double.parseDouble(priceField.getText()));
        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Ціна повинна бути числом.");
            return;
        }
        Category category = categoryComboBox.getValue();
        if (category == null) {
            showAlert("Помилка вводу", "Будь ласка, оберіть категорію.");
            return;
        }
        selectedItem.setCategory(category);
        selectedItem.setVegetarian(vegetarianCheckBox.isSelected());
        selectedItem.setAllergen(allergenCheckBox.isSelected());
        selectedItem.setGlutenFree(glutenFreeCheckBox.isSelected());

        try {
            menuItemService.updateMenuItem(selectedItem);
            menuTableView.refresh();
            clearForm();
            showAlert("Успіх", "Пункт меню оновлено.");
        } catch (SQLException e) {
            showAlert("Помилка редагування", "Не вдалося оновити пункт меню: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteItem(ActionEvent event) {
        if (selectedItem != null) {
            try {
                menuItemService.deleteMenuItem(selectedItem.getId());
                menuItems.remove(selectedItem);
                clearForm();
                showAlert("Успіх", "Пункт меню видалено.");
            } catch (SQLException e) {
                showAlert("Помилка видалення", "Не вдалося видалити пункт меню: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Не вибрано елемент", "Будь ласка, виберіть позицію для видалення.");
        }
    }

    @FXML
    private void handleRowClick(MouseEvent event) {
        selectedItem = menuTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            nameField.setText(selectedItem.getName());
            priceField.setText(String.valueOf(selectedItem.getPrice()));
            categoryComboBox.setValue(selectedItem.getCategory());
            vegetarianCheckBox.setSelected(selectedItem.isVegetarian());
            allergenCheckBox.setSelected(selectedItem.isAllergen());
            glutenFreeCheckBox.setSelected(selectedItem.isGlutenFree());
        }
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.setValue(null);
        vegetarianCheckBox.setSelected(false);
        allergenCheckBox.setSelected(false);
        glutenFreeCheckBox.setSelected(false);
        selectedItem = null;
        menuTableView.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}