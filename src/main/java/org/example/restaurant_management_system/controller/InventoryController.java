package org.example.restaurant_management_system.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import org.example.restaurant_management_system.model.Ingredient;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.model.MenuItemIngredient;
import org.example.restaurant_management_system.model.Stock;
import org.example.restaurant_management_system.service.InventoryService;
import org.example.restaurant_management_system.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class InventoryController {

    // для управління СКЛАДОМ
    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, String> nameColumn;
    @FXML private TableColumn<Ingredient, String> unitColumn;
    @FXML private TableColumn<Ingredient, LocalDate> expirationColumn;
    @FXML private TableColumn<Ingredient, Double> stockColumn;

    @FXML private TextField inputNameField;
    @FXML private TextField inputQuantityField;

    @FXML private TextField inputUnitField;
    @FXML private DatePicker inputExpirationDatePicker;
    @FXML private ChoiceBox<String> filterChoiceBox;


    //для управління РЕЦЕПТАМИ СТРАВ
    @FXML private ComboBox<MenuItem> menuItemRecipeComboBox;

    @FXML private TableView<MenuItemIngredient> recipeIngredientTable;

    @FXML private TableColumn<MenuItemIngredient, String> recipeIngredientNameColumn;
    @FXML private TableColumn<MenuItemIngredient, String> recipeIngredientUnitColumn;
    @FXML private TableColumn<MenuItemIngredient, Double> recipeIngredientQuantityColumn;
    @FXML private ComboBox<Ingredient> ingredientForRecipeComboBox;

    @FXML private TextField quantityForRecipeField;


    @FXML private Label messageLabel;



    private InventoryService inventoryService;
    private ObservableList<Ingredient> masterIngredientsList = FXCollections.observableArrayList(); // усі інгредієнти
    private FilteredList<Ingredient> filteredIngredients;
    private SortedList<Ingredient> sortedIngredients;

    private ObservableList<MenuItem> masterMenuItemsList = FXCollections.observableArrayList(); // усі страви
    private ObservableList<MenuItemIngredient> currentRecipeIngredientsList = FXCollections.observableArrayList(); // інгредієнти поточного рецепту

    @FXML
    public void initialize() {
        try {
            Connection dbConnection = DatabaseConnection.getConnection();
            this.inventoryService = new InventoryService(dbConnection);
        } catch (SQLException e) {
            showAlert("Помилка підключення до БД", "Не вдалося встановити з'єднання з базою даних для інвентаризації: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // таблиця СКЛАДУ
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        unitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        expirationColumn.setCellValueFactory(cellData -> cellData.getValue().expirationDateProperty());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().currentStockProperty().asObject());

        ingredientTable.setPlaceholder(new Label("Немає інгредієнтів"));

        // жирний шрифт для деяких термінів придатності
        expirationColumn.setCellFactory(getExpirationHighlightingFactory());

        filteredIngredients = new FilteredList<>(masterIngredientsList, p -> true);
        sortedIngredients = new SortedList<>(filteredIngredients);
        sortedIngredients.comparatorProperty().bind(ingredientTable.comparatorProperty());
        ingredientTable.setItems(sortedIngredients);

        ingredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                inputNameField.setText(newSelection.getName());
                inputUnitField.setText(newSelection.getUnit());
                inputExpirationDatePicker.setValue(newSelection.getExpirationDate());
                inputQuantityField.setText(String.valueOf(newSelection.getCurrentStock()));

                messageLabel.setText(""); // очищення повідомлення при виборі нового елемента
            } else {
                clearInputFieldsForStock(); // очищення поля для СКЛАДУ
            }
        });

        // фільтр складу
        filterChoiceBox.setItems(FXCollections.observableArrayList("Усі інгредієнти", "Прострочені", "Скоро зіпсуються (7 днів)"));
        filterChoiceBox.setValue("Усі інгредієнти");
        filterChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> applyStockFilter());

        // таблиця РЕЦЕПТІВ
        recipeIngredientTable.setItems(currentRecipeIngredientsList);
        recipeIngredientNameColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getIngredient() != null) {
                return cellData.getValue().getIngredient().nameProperty();
            }
            return new SimpleStringProperty("N/A");
        });
        recipeIngredientUnitColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getIngredient() != null) {
                return cellData.getValue().getIngredient().unitProperty();
            }
            return new SimpleStringProperty("N/A");
        });
        recipeIngredientQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityPerUnitProperty().asObject());

        recipeIngredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (newSelection.getIngredient() != null) {
                    ingredientForRecipeComboBox.getSelectionModel().select(newSelection.getIngredient());
                    quantityForRecipeField.setText(String.valueOf(newSelection.getQuantityPerUnit()));

                } else {
                    clearInputFieldsForRecipe();
                }
            } else {
                clearInputFieldsForRecipe(); // очищення поля для РЕЦЕПТУ
            }

        });
        recipeIngredientTable.setPlaceholder(new Label("Оберіть страву"));

        // Виберіть страву
        loadMenuItems();
        menuItemRecipeComboBox.setItems(masterMenuItemsList);
        menuItemRecipeComboBox.setPromptText("Оберіть страву для рецепту");
        menuItemRecipeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getId() != 0) { // перевірка на пустий MenuItem

                try {
                    currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(newSelection.getId()));

                } catch (SQLException e) {
                    showAlert("Помилка завантаження рецепту", "Не вдалося завантажити інгредієнти рецепту: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                currentRecipeIngredientsList.clear(); // очищення таблиці рецептів
                clearInputFieldsForRecipe(); // очищення поля для рецепту
            }
        });
        // Виберіть інгредієнт
        ingredientForRecipeComboBox.setItems(masterIngredientsList);
        ingredientForRecipeComboBox.setPromptText("Виберіть інгредієнт для рецепту");

        // початкове завантаження даних
        loadIngredientsData();
    }

    // ЗАВАНТАЖЕННЯ ДАНИХ
    private void loadIngredientsData() {
        try {
            masterIngredientsList.clear();
            masterIngredientsList.addAll(inventoryService.getAllIngredientsWithStock());
            messageLabel.setText("");
            applyStockFilter();
        } catch (SQLException e) {
            showAlert("Помилка завантаження", "Не вдалося завантажити дані інгредієнтів: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMenuItems() {
        try {
            masterMenuItemsList.clear();
            masterMenuItemsList.add(new MenuItem(0, "Не обрано", 0, 0.0, false, false, false));
            masterMenuItemsList.addAll(inventoryService.getAllMenuItems());
        } catch (SQLException e) {
            showAlert("Помилка завантаження меню", "Не вдалося завантажити список страв: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ОЧИЩЕННЯ ПОЛІВ
    private void clearInputFieldsForStock() {
        inputNameField.clear();
        inputQuantityField.clear();
        inputUnitField.clear();
        inputExpirationDatePicker.setValue(null);
        ingredientTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private void clearInputFieldsForRecipe() {
        ingredientForRecipeComboBox.getSelectionModel().clearSelection();
        quantityForRecipeField.clear();
        recipeIngredientTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    // ФІЛЬТРАЦІЯ СКЛАДУ
    private void applyStockFilter() {
        String currentStockFilterType = filterChoiceBox.getValue();

        Predicate<Ingredient> stockFilterPredicate = ingredient -> {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysFromNow = today.plusDays(7);

            // Застосовуємо фільтр складу
            switch (currentStockFilterType) {
                case "Усі інгредієнти":
                    return true;
                case "Прострочені":
                    return ingredient.getExpirationDate() != null && ingredient.getExpirationDate().isBefore(today);
                case "Скоро зіпсуються (7 днів)":
                    return ingredient.getExpirationDate() != null &&
                            (ingredient.getExpirationDate().isAfter(today.minusDays(1)) &&
                                    ingredient.getExpirationDate().isBefore(sevenDaysFromNow.plusDays(1)));
                default:
                    return true; // За замовчуванням показуємо все
            }
        };
        filteredIngredients.setPredicate(stockFilterPredicate);
        ingredientTable.refresh(); // Оновлюємо таблицю
    }


    // КНОПКИ СКЛАДУ

    @FXML
    private void handleAddNewIngredient() {
        String nameInput = inputNameField.getText().trim();
        String unitInput = inputUnitField.getText().trim();
        String qtyInput = inputQuantityField.getText().trim();
        LocalDate expirationDate = inputExpirationDatePicker.getValue();

        if (nameInput.isEmpty() || unitInput.isEmpty()) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Заповніть поля: Назва, Одиниці.");
            return;
        }

        try {
            double qty = 0;
            if (!qtyInput.isEmpty()) {
                qty = Double.parseDouble(qtyInput);
                if (qty < 0) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Кількість запасу не може бути від'ємною.");
                    return;
                }
            }

            Ingredient existingIngredient = masterIngredientsList.stream()
                    .filter(i -> i.getName().equalsIgnoreCase(nameInput))
                    .findFirst()
                    .orElse(null);

            if (existingIngredient != null) {

                // оновлення існуючого інгредієнта
                StringBuilder statusMessage = new StringBuilder();
                boolean changed = false;

                if (!existingIngredient.getUnit().equalsIgnoreCase(unitInput)) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Зміна одиниць виміру для існуючого інгредієнта не підтримується через цей інтерфейс.");
                    return;
                }

                if (!Objects.equals(expirationDate, existingIngredient.getExpirationDate())) {
                    inventoryService.updateIngredientExpiration(existingIngredient.getId(), expirationDate);
                    statusMessage.append("Термін придатності оновлено; ");
                    changed = true;
                }

                if (qty > 0) {
                    Stock income = new Stock(existingIngredient, qty, "income");
                    inventoryService.addStockMovement(income);
                    statusMessage.append("Запас додано (").append(qty).append(" ").append(existingIngredient.getUnit()).append("); ");
                    changed = true;
                }

                if (changed) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText(statusMessage.toString().trim().replaceAll(";$", "") + " для '" + nameInput + "'.");
                } else {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Нічого не змінено для '" + nameInput + "'.");
                }
            } else {
                // додавання нового інгредієнта
                if (qty == 0 && qtyInput.isEmpty()) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Для нового інгредієнта необхідно вказати початковий запас (Кількість).");
                    return;
                }
                inventoryService.createNewIngredient(nameInput, unitInput, qty, expirationDate);
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Новий інгредієнт '" + nameInput + "' успішно додано.");
            }

            loadIngredientsData();
            clearInputFieldsForStock();

        } catch (NumberFormatException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Невірний формат кількості. Введіть число.");
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка додавання/оновлення інгредієнта: " + e.getMessage());
            e.printStackTrace();
        }
    }

// КНОПКИ для списання продукту, перевірки зіпсованих продуктів і їх списання
    @FXML
    private void handleUseIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
        if (selectedIngredient == null) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Виберіть інгредієнт для списання.");
            return;
        }

        String qtyInput = inputQuantityField.getText().trim();
        if (qtyInput.isEmpty()) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Введіть кількість для списання.");
            return;
        }

        try {
            double qtyToUse = Double.parseDouble(qtyInput);
            if (qtyToUse <= 0) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Кількість має бути позитивним числом.");
                return;
            }

            double currentStock = inventoryService.calculateCurrentStock(selectedIngredient.getId());
            if (currentStock < qtyToUse) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Недостатньо " + selectedIngredient.getName() + " на складі. Доступно: " + currentStock + " " + selectedIngredient.getUnit());
                return;
            }

            Stock expense = new Stock(selectedIngredient, -qtyToUse, "expense");
            inventoryService.addStockMovement(expense);

            loadIngredientsData();
            clearInputFieldsForStock();

            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Успішно списано " + qtyToUse + " " + selectedIngredient.getUnit() + " з " + selectedIngredient.getName());
        } catch (NumberFormatException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Невірний формат кількості. Введіть число.");
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка списання: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckExpiration() {
        filterChoiceBox.setValue("Прострочені");
        messageLabel.setTextFill(Color.PINK);
        messageLabel.setText("Відфільтровано прострочені інгредієнти.");
    }

    @FXML
    private void handleDiscardSpoiled() {
        try {
            List<Ingredient> expired = inventoryService.getExpiredIngredients();
            if (expired.isEmpty()) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Немає прострочених інгредієнтів для списання.");
                return;
            }

            int discardedCount = 0;
            for (Ingredient ing : expired) {
                double currentStock = inventoryService.calculateCurrentStock(ing.getId());
                if (currentStock > 0) {
                    Stock spoilage = new Stock(ing, -currentStock, "spoilage");
                    inventoryService.addStockMovement(spoilage);
                    inventoryService.updateIngredientExpiration(ing.getId(), null); // Зберігаємо логіку обнулення терміну придатності
                    discardedCount++;
                }
            }
            loadIngredientsData(); // Оновлюємо дані у таблиці
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Успішно списано " + discardedCount + " типів прострочених інгредієнтів.");

        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка списання простроченого: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // КНОПОКИ ДЛЯ РЕЦЕПТІВ
    // додавання нового інгредієнта в рецепт
    @FXML
    private void handleAddIngredientToRecipe() {
        MenuItem selectedMenuItem = menuItemRecipeComboBox.getSelectionModel().getSelectedItem();
        Ingredient selectedIngredient = ingredientForRecipeComboBox.getSelectionModel().getSelectedItem();

        if (selectedMenuItem == null || selectedMenuItem.getId() == 0) {
            messageLabel.setText("Будь ласка, виберіть страву для рецепту.");
            return;
        }
        if (selectedIngredient == null) {
            messageLabel.setText("Будь ласка, виберіть інгредієнт для додавання до рецепту.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityForRecipeField.getText().trim());
            if (quantity <= 0) {
                messageLabel.setText("Кількість інгредієнта в рецепті повинна бути позитивною.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Будь ласка, введіть коректну кількість для рецепту.");
            return;
        }

        try {
            // перевірка чи інгредієнт вже є у рецепті
            MenuItemIngredient existingRecipeIngredient = null;
            for (MenuItemIngredient mii : currentRecipeIngredientsList) {
                if (mii.getIngredient().getId() == selectedIngredient.getId()) {
                    existingRecipeIngredient = mii;
                    break;
                }
            }

            if (existingRecipeIngredient != null) {

                // оновлення існуючого інгредієнта у рецепті
                existingRecipeIngredient.setQuantityPerUnit(quantity);
                inventoryService.updateMenuItemIngredient(existingRecipeIngredient);
                messageLabel.setText(String.format("Кількість '%s' для страви '%s' оновлено.", selectedIngredient.getName(), selectedMenuItem.getName()));
            } else {
                // Додати новий інгредієнт до рецепту
                MenuItemIngredient newRecipeIngredient = new MenuItemIngredient(
                        0, // id тимчасово 0, після запису в БД буде згенеровано
                        selectedMenuItem,
                        selectedIngredient,
                        quantity
                );
                inventoryService.addMenuItemIngredient(newRecipeIngredient);
                messageLabel.setText(String.format("Інгредієнт '%s' додано до рецепту '%s'.", selectedIngredient.getName(), selectedMenuItem.getName()));
            }
            // оновлення таблиці інгредієнтів рецепту
            currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(selectedMenuItem.getId()));
            clearInputFieldsForRecipe();
        } catch (SQLException e) {
            showAlert("Помилка БД", "Не вдалося додати/оновити інгредієнт в рецепті: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // видалення інгредієнту з рецепта
    @FXML
    private void handleRemoveIngredientFromRecipe() {
        MenuItemIngredient selectedRecipeIngredient = recipeIngredientTable.getSelectionModel().getSelectedItem();
        MenuItem selectedMenuItem = menuItemRecipeComboBox.getSelectionModel().getSelectedItem();

        if (selectedRecipeIngredient == null) {
            messageLabel.setText("Будь ласка, виберіть інгредієнт з рецепту, щоб видалити.");
            return;
        }
        if (selectedMenuItem == null || selectedMenuItem.getId() == 0) {
            messageLabel.setText("Будь ласка, виберіть страву для рецепту.");
            return;
        }

        try {
            inventoryService.removeMenuItemIngredient(selectedRecipeIngredient.getId());
            messageLabel.setText(String.format("Інгредієнт '%s' видалено з рецепту '%s'.",
                    selectedRecipeIngredient.getIngredient().getName(), selectedMenuItem.getName()));

            // оновлення таблиці інгредієнтів рецепта
            currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(selectedMenuItem.getId()));
            clearInputFieldsForRecipe();
        } catch (SQLException e) {
            showAlert("Помилка БД", "Не вдалося видалити інгредієнт з рецепту: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // додаткові повідомлення
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);


        alert.showAndWait();
    }

    // виділення прострочених/тих що скоро прострочаться інгредієнтів
    private Callback<TableColumn<Ingredient, LocalDate>, TableCell<Ingredient, LocalDate>> getExpirationHighlightingFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setTextFill(Color.BLACK);

                    LocalDate today = LocalDate.now();
                    LocalDate sevenDaysFromNowThreshold = today.plusDays(7);

                    if (item.isBefore(today)) {
                        setTextFill(Color.PINK);
                        setStyle("-fx-font-weight: bold;");
                    } else if (item.isBefore(sevenDaysFromNowThreshold.plusDays(1))) {
                        setTextFill(Color.PINK);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(Color.BLACK);
                        setStyle("");
                    }
                }
            }
        };
    }
}