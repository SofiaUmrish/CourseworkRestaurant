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
import org.example.restaurant_management_system.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MenuController {

    private static final Logger LOGGER = LogManager.getLogger(ReportingController.class);

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
        LOGGER.info("Ініціалізація MenuController.");

        Platform.runLater(() -> {
            Scene scene = menuTableView.getScene();
            if (scene != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/MenuStyle.css").toExternalForm());
                LOGGER.debug("Файл MenuStyle.css завантажено успішно.");
            } else {
                System.err.println("Scene is null. Cannot load CSS.");
                LOGGER.warn("Неможливо завантажити CSS для MenuController.");
            }
        });
        try {
            Connection dbConnection = DatabaseConnection.getConnection();
            this.menuItemService = new MenuItemService(dbConnection);
            this.categoryService = new CategoryService(dbConnection);
            LOGGER.info("З'єднання з базою даних та сервіси ініціалізовано.");
        } catch (SQLException e) {
            showAlert("Помилка підключення до БД", "Не вдалося встановити з'єднання з базою даних: " + e.getMessage());
            LOGGER.fatal("Не вдалося підключитися до бази даних: " + e.getMessage(), e);
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
        LOGGER.info("MenuController ініціалізовано успішно.");
    }

    private void loadCategories() {
        LOGGER.info("Завантаження категорій.");
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            LOGGER.info("Категорії завантажено успішно. Кількість: " + categories.size());
        } catch (SQLException e) {
            showAlert("Помилка завантаження категорій", "Не вдалося завантажити категорії: " + e.getMessage());
            LOGGER.error("Помилка SQL при завантаженні категорій: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void loadMenuItems() {
        LOGGER.info("Завантаження пунктів меню.");
        try {
            List<MenuItem> items = menuItemService.getAllMenuItems();
            //всі категорії через categoryService для зв'язування
            List<Category> allCategories = categoryService.getAllCategories();
            for (MenuItem item : items) {
                for (Category cat : allCategories) {
                    if (item.getCategoryId() == cat.getId()) {
                        item.setCategory(cat);
                        break;
                    }
                }
            }
            menuItems = FXCollections.observableArrayList(items);
            LOGGER.info("Пункти меню завантажено успішно. Кількість: " + menuItems.size());
        } catch (SQLException e) {
            showAlert("Помилка завантаження меню", "Не вдалося завантажити пункти меню: " + e.getMessage());
            LOGGER.error("Помилка SQL при завантаженні пунктів меню: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddItem(ActionEvent event) {
        LOGGER.info("Спроба додати новий пункт меню.");
        String name = nameField.getText();
        double price;
        try {
            price = Double.parseDouble(priceField.getText());
            LOGGER.debug("Розпізнана ціна: " + price);
        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Ціна повинна бути числом.");
            LOGGER.warn("Введено невірний формат ціни: " + priceField.getText(), e);
            return;
        }

        Category category = categoryComboBox.getValue();
        if (category == null) {
            showAlert("Помилка вводу", "Будь ласка, оберіть категорію.");
            LOGGER.warn("Додавання пункту не вдалося: категорія не обрана.");
            return;
        }

        boolean vegetarian = vegetarianCheckBox.isSelected();
        boolean allergen = allergenCheckBox.isSelected();
        boolean glutenFree = glutenFreeCheckBox.isSelected();
        LOGGER.debug("Деталі нового пункту меню: Назва=" + name + ", Ціна=" + price + ", Категорія=" + category.getName() +
                ", Вегетаріанський=" + vegetarian + ", Алергенний=" + allergen + ", Безглютеновий=" + glutenFree);

        MenuItem newItem = new MenuItem(name, price, category, vegetarian, allergen, glutenFree);
        try {
            menuItemService.saveMenuItem(newItem);
            menuItems.add(newItem);
            clearForm();
            showAlert("Успіх", "Пункт меню додано.");
            LOGGER.info("Пункт меню '" + name + "' успішно додано.");
        } catch (SQLException e) {
            showAlert("Помилка додавання", "Не вдалося додати пункт меню: " + e.getMessage());
            LOGGER.error("Помилка SQL при додаванні пункту меню: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditItem(ActionEvent event) {
        LOGGER.info("Спроба редагувати пункт меню.");
        if (selectedItem == null) {
            showAlert("Не вибрано елемент", "Будь ласка, виберіть позицію для редагування.");
            LOGGER.warn("Редагування пункту не вдалося: пункт не обраний.");
            return;
        }
        LOGGER.debug("Обраний пункт для редагування: " + selectedItem.getName());

        selectedItem.setName(nameField.getText());
        try {
            selectedItem.setPrice(Double.parseDouble(priceField.getText()));
            LOGGER.debug("Розпізнана ціна для редагування: " + selectedItem.getPrice());
        } catch (NumberFormatException e) {
            showAlert("Помилка вводу", "Ціна повинна бути числом.");
            LOGGER.warn("Введено невірний формат ціни для редагування: " + priceField.getText(), e);
            return;
        }
        Category category = categoryComboBox.getValue();
        if (category == null) {
            showAlert("Помилка вводу", "Будь ласка, оберіть категорію.");
            LOGGER.warn("Редагування пункту не вдалося: категорія не обрана.");
            return;
        }
        selectedItem.setCategory(category);
        selectedItem.setVegetarian(vegetarianCheckBox.isSelected());
        selectedItem.setAllergen(allergenCheckBox.isSelected());
        selectedItem.setGlutenFree(glutenFreeCheckBox.isSelected());
        LOGGER.debug("Оновлені деталі пункту меню: Назва=" + selectedItem.getName() + ", Ціна=" + selectedItem.getPrice() +
                ", Категорія=" + selectedItem.getCategory().getName() + ", Вегетаріанський=" + selectedItem.isVegetarian() +
                ", Алергенний=" + selectedItem.isAllergen() + ", Безглютеновий=" + selectedItem.isGlutenFree());

        try {
            menuItemService.updateMenuItem(selectedItem);
            menuTableView.refresh();
            clearForm();
            showAlert("Успіх", "Пункт меню оновлено.");
            LOGGER.info("Пункт меню '" + selectedItem.getName() + "' успішно оновлено.");
        } catch (SQLException e) {
            showAlert("Помилка редагування", "Не вдалося оновити пункт меню: " + e.getMessage());
            LOGGER.error("Помилка SQL при оновленні пункту меню: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteItem(ActionEvent event) {
        LOGGER.info("Спроба видалити пункт меню.");
        if (selectedItem != null) {
            LOGGER.debug("Обраний пункт для видалення: " + selectedItem.getName());
            try {
                menuItemService.deleteMenuItem(selectedItem.getId());
                menuItems.remove(selectedItem);
                clearForm();
                showAlert("Успіх", "Пункт меню видалено.");
                LOGGER.info("Пункт меню '" + selectedItem.getName() + "' успішно видалено.");
            } catch (SQLException e) {
                showAlert("Помилка видалення", "Не вдалося видалити пункт меню: " + e.getMessage());
                LOGGER.error("Помилка SQL при видаленні пункту меню: " + e.getMessage(), e);
                e.printStackTrace();
            }
        } else {
            showAlert("Не вибрано елемент", "Будь ласка, виберіть позицію для видалення.");
            LOGGER.warn("Видалення пункту не вдалося: пункт не обраний.");
        }
    }

    @FXML
    private void handleRowClick(MouseEvent event) {
        selectedItem = menuTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            LOGGER.debug("Клік по рядку, обраний пункт: " + selectedItem.getName());
            nameField.setText(selectedItem.getName());
            priceField.setText(String.valueOf(selectedItem.getPrice()));
            categoryComboBox.setValue(selectedItem.getCategory());
            vegetarianCheckBox.setSelected(selectedItem.isVegetarian());
            allergenCheckBox.setSelected(selectedItem.isAllergen());
            glutenFreeCheckBox.setSelected(selectedItem.isGlutenFree());
        } else {
            LOGGER.debug("Клік по рядку, але жоден пункт не обраний.");
        }
    }

    private void clearForm() {
        LOGGER.info("Очищення полів форми.");
        nameField.clear();
        priceField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        categoryComboBox.setValue(null);
        vegetarianCheckBox.setSelected(false);
        allergenCheckBox.setSelected(false);
        glutenFreeCheckBox.setSelected(false);
        selectedItem = null;
        menuTableView.getSelectionModel().clearSelection();
        LOGGER.debug("Поля форми очищено та обраний пункт скинуто.");
    }

    private void showAlert(String title, String message) {
        LOGGER.info(String.format("Відображення сповіщення: Заголовок='%s', Повідомлення='%s'", title, message));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}