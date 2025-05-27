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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InventoryController {

    private static final Logger LOGGER = LogManager.getLogger(InventoryController.class);

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
    private ObservableList<Ingredient> masterIngredientsList = FXCollections.observableArrayList();
    private FilteredList<Ingredient> filteredIngredients;
    private SortedList<Ingredient> sortedIngredients;

    private ObservableList<MenuItem> masterMenuItemsList = FXCollections.observableArrayList();
    private ObservableList<MenuItemIngredient> currentRecipeIngredientsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        LOGGER.info("Ініціалізація InventoryController.");
        try {
            Connection dbConnection = DatabaseConnection.getConnection();
            this.inventoryService = new InventoryService(dbConnection);
            LOGGER.debug("З'єднання з базою даних для InventoryService встановлено.");
        } catch (SQLException e) {
            LOGGER.fatal("Помилка підключення до БД для InventoryController: {}", e.getMessage(), e);
            showAlert("Помилка підключення до БД", "Не вдалося встановити з'єднання з базою даних для інвентаризації: "
                    + e.getMessage());
            System.exit(1);
        }

        // таблиця СКЛАДУ
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        unitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        expirationColumn.setCellValueFactory(cellData -> cellData.getValue().expirationDateProperty());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().currentStockProperty().asObject());
        LOGGER.debug("Налаштовано фабрики значень для колонок таблиці інгредієнтів.");

        ingredientTable.setPlaceholder(new Label("Немає інгредієнтів"));

        expirationColumn.setCellFactory(getExpirationHighlightingFactory());

        filteredIngredients = new FilteredList<>(masterIngredientsList, p -> true);
        sortedIngredients = new SortedList<>(filteredIngredients);
        sortedIngredients.comparatorProperty().bind(ingredientTable.comparatorProperty());
        ingredientTable.setItems(sortedIngredients);
        LOGGER.debug("Налаштовано фільтрацію та сортування для таблиці інгредієнтів.");

        ingredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                inputNameField.setText(newSelection.getName());
                inputUnitField.setText(newSelection.getUnit());
                inputExpirationDatePicker.setValue(newSelection.getExpirationDate());
                inputQuantityField.setText(String.valueOf(newSelection.getCurrentStock()));
                messageLabel.setText("");
                LOGGER.debug("Обрано інгредієнт '{}' у таблиці. Поля вводу оновлено.",
                        newSelection.getName());
            } else {
                clearInputFieldsForStock();
                LOGGER.debug("Знято виділення з інгредієнта. Поля вводу для складу очищено.");
            }
        });

        filterChoiceBox.setItems(FXCollections.observableArrayList("Усі інгредієнти",
                "Прострочені", "Скоро зіпсуються (7 днів)"));
        filterChoiceBox.setValue("Усі інгредієнти");
        filterChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.debug("Змінено фільтр складу з '{}' на '{}'.", oldValue, newValue);
            applyStockFilter();
        });

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
        recipeIngredientQuantityColumn.setCellValueFactory(cellData ->
                cellData.getValue().quantityPerUnitProperty().asObject());
        LOGGER.debug("Налаштовано фабрики значень для колонок таблиці інгредієнтів рецепту.");

        recipeIngredientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                if (newSelection.getIngredient() != null) {
                    ingredientForRecipeComboBox.getSelectionModel().select(newSelection.getIngredient());
                    quantityForRecipeField.setText(String.valueOf(newSelection.getQuantityPerUnit()));
                    LOGGER.debug("Обрано інгредієнт '{}' у таблиці рецепту. Поля вводу оновлено.",
                            newSelection.getIngredient().getName());
                } else {
                    clearInputFieldsForRecipe();
                    LOGGER.debug("Обрано порожній інгредієнт у таблиці рецепту. Поля вводу для рецепту очищено.");
                }
            } else {
                clearInputFieldsForRecipe();
                LOGGER.debug("Знято виділення з інгредієнта рецепту. Поля вводу для рецепту очищено.");
            }

        });
        recipeIngredientTable.setPlaceholder(new Label("Оберіть страву"));

        // Виберіть страву
        loadMenuItems();
        menuItemRecipeComboBox.setItems(masterMenuItemsList);
        menuItemRecipeComboBox.setPromptText("Оберіть страву для рецепту");
        menuItemRecipeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getId() != 0) {
                LOGGER.debug("Вибрано страву '{}' ({}) для рецепту.", newSelection.getName(), newSelection.getId());
                try {
                    currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(newSelection.getId()));
                    LOGGER.info("Завантажено інгредієнти для рецепту страви '{}'.", newSelection.getName());
                } catch (SQLException e) {
                    LOGGER.error("Помилка завантаження інгредієнтів для рецепту страви '{}': {}",
                            newSelection.getName(), e.getMessage(), e);
                    showAlert("Помилка завантаження рецепту", "Не вдалося завантажити інгредієнти рецепту: "
                            + e.getMessage());
                }
            } else {
                LOGGER.debug("Виділення страви для рецепту знято або обрано 'Не обрано'.");
                currentRecipeIngredientsList.clear();
                clearInputFieldsForRecipe();
            }
        });
        // Виберіть інгредієнт
        ingredientForRecipeComboBox.setItems(masterIngredientsList);
        ingredientForRecipeComboBox.setPromptText("Виберіть інгредієнт для рецепту");
        LOGGER.debug("Налаштовано вибір страв та інгредієнтів для рецептів.");

        loadIngredientsData();
        LOGGER.info("Ініціалізація InventoryController завершена.");
    }

    // ЗАВАНТАЖЕННЯ ДАНИХ
    private void loadIngredientsData() {
        LOGGER.info("Завантаження даних інгредієнтів.");
        try {
            masterIngredientsList.clear();
            masterIngredientsList.addAll(inventoryService.getAllIngredientsWithStock());
            messageLabel.setText("");
            applyStockFilter();
            LOGGER.info("Дані інгредієнтів успішно завантажено. Кількість: {}.",
                    masterIngredientsList.size());
        } catch (SQLException e) {
            LOGGER.error("Помилка завантаження даних інгредієнтів: {}", e.getMessage(), e);
            showAlert("Помилка завантаження", "Не вдалося завантажити дані інгредієнтів: " + e.getMessage());
        }
    }

    private void loadMenuItems() {
        LOGGER.info("Завантаження даних страв.");
        try {
            masterMenuItemsList.clear();
            masterMenuItemsList.add(new MenuItem(0, "Не обрано", 0, 0.0,
                    false, false, false));
            masterMenuItemsList.addAll(inventoryService.getAllMenuItems());
            LOGGER.info("Дані страв успішно завантажено. Кількість: {}.",
                    masterMenuItemsList.size());
        } catch (SQLException e) {
            LOGGER.error("Помилка завантаження даних страв: {}", e.getMessage(), e);
            showAlert("Помилка завантаження меню", "Не вдалося завантажити список страв: " + e.getMessage());
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
        LOGGER.debug("Поля вводу для секції 'Склад' очищено.");
    }

    private void clearInputFieldsForRecipe() {
        ingredientForRecipeComboBox.getSelectionModel().clearSelection();
        quantityForRecipeField.clear();
        recipeIngredientTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
        LOGGER.debug("Поля вводу для секції 'Рецепти' очищено.");
    }

    // ФІЛЬТРАЦІЯ СКЛАДУ
    private void applyStockFilter() {
        String currentStockFilterType = filterChoiceBox.getValue();
        LOGGER.debug("Застосування фільтра складу: {}.", currentStockFilterType);

        Predicate<Ingredient> stockFilterPredicate = ingredient -> {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysFromNow = today.plusDays(7);

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
                    return true;
            }
        };
        filteredIngredients.setPredicate(stockFilterPredicate);
        ingredientTable.refresh();
        LOGGER.debug("Фільтр складу оновлено. Таблиця інгредієнтів оновлена.");
    }


    // КНОПКИ СКЛАДУ

    @FXML
    private void handleAddNewIngredient() {
        String nameInput = inputNameField.getText().trim();
        String unitInput = inputUnitField.getText().trim();
        String qtyInput = inputQuantityField.getText().trim();
        LocalDate expirationDate = inputExpirationDatePicker.getValue();
        LOGGER.info("Спроба додати/оновити інгредієнт: Назва='{}', Кількість='{}', " +
                        "Одиниця='{}', Термін придатності='{}'.",
                nameInput, qtyInput, unitInput, expirationDate);

        if (nameInput.isEmpty() || unitInput.isEmpty()) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Заповніть поля: Назва, Одиниці.");
            LOGGER.warn("Відхилено додавання/оновлення: Відсутні обов'язкові поля (Назва, Одиниці).");
            return;
        }

        try {
            double qty = 0;
            if (!qtyInput.isEmpty()) {
                qty = Double.parseDouble(qtyInput);
                if (qty < 0) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Кількість запасу не може бути від'ємною.");
                    LOGGER.warn("Відхилено додавання/оновлення: Від'ємна кількість запасу: {}.", qty);
                    return;
                }
            }

            Ingredient existingIngredient = masterIngredientsList.stream()
                    .filter(i -> i.getName().equalsIgnoreCase(nameInput))
                    .findFirst()
                    .orElse(null);

            if (existingIngredient != null) {
                LOGGER.debug("Інгредієнт '{}' вже існує. Спроба оновити його.", nameInput);
                StringBuilder statusMessage = new StringBuilder();
                boolean changed = false;

                if (!existingIngredient.getUnit().equalsIgnoreCase(unitInput)) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Зміна одиниць виміру для існуючого інгредієнта не підтримується через цей інтерфейс.");
                    LOGGER.warn("Відхилено оновлення: Спроба змінити одиниці виміру для існуючого інгредієнта '{}'.",
                            nameInput);
                    return;
                }

                if (!Objects.equals(expirationDate, existingIngredient.getExpirationDate())) {
                    inventoryService.updateIngredientExpiration(existingIngredient.getId(), expirationDate);
                    statusMessage.append("Термін придатності оновлено; ");
                    changed = true;
                    LOGGER.info("Оновлено термін придатності для '{}' на {}.",
                            nameInput, expirationDate);
                }

                if (qty > 0) {
                    Stock income = new Stock(existingIngredient, qty, "income");
                    inventoryService.addStockMovement(income);
                    statusMessage.append("Запас додано (").append(qty).append(" ").append(existingIngredient.getUnit()).append("); ");
                    changed = true;
                    LOGGER.info("Додано запас {} {} для '{}'.",
                            qty, existingIngredient.getUnit(), nameInput);
                }

                if (changed) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText(statusMessage.toString().trim().replaceAll(";$", "")
                            + " для '" + nameInput + "'.");
                } else {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Нічого не змінено для '" + nameInput + "'.");
                    LOGGER.info("Нічого не змінено для існуючого інгредієнта '{}'.", nameInput);
                }
            } else {
                LOGGER.debug("Інгредієнт '{}' не існує. Спроба створити новий.", nameInput);
                if (qty == 0 && qtyInput.isEmpty()) {
                    messageLabel.setTextFill(Color.PINK);
                    messageLabel.setText("Для нового інгредієнта необхідно вказати початковий запас (Кількість).");
                    LOGGER.warn("Відхилено створення нового інгредієнта: Відсутній початковий запас.");
                    return;
                }
                inventoryService.createNewIngredient(nameInput, unitInput, qty, expirationDate);
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Новий інгредієнт '" + nameInput + "' успішно додано.");
                LOGGER.info("Новий інгредієнт '{}' успішно додано з запасом {} {}.",
                        nameInput, qty, unitInput);
            }

            loadIngredientsData();
            clearInputFieldsForStock();

        } catch (NumberFormatException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Невірний формат кількості. Введіть число.");
            LOGGER.error("Помилка формату кількості при додаванні/оновленні інгредієнта: {}", qtyInput, e);
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка додавання/оновлення інгредієнта: " + e.getMessage());
            LOGGER.error("Помилка SQL при додаванні/оновленні інгредієнта '{}': {}",
                    nameInput, e.getMessage(), e);
        }
    }

    // КНОПКИ для списання продукту, перевірки зіпсованих продуктів і їх списання
    @FXML
    private void handleUseIngredient() {
        Ingredient selectedIngredient = ingredientTable.getSelectionModel().getSelectedItem();
        String qtyInput = inputQuantityField.getText().trim();
        LOGGER.info("Спроба списати інгредієнт. Обраний інгредієнт: {}, Кількість: '{}'.",
                (selectedIngredient != null ? selectedIngredient.getName() : "null"), qtyInput);

        if (selectedIngredient == null) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Виберіть інгредієнт для списання.");
            LOGGER.warn("Відхилено списання: Не обрано інгредієнт.");
            return;
        }

        if (qtyInput.isEmpty()) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Введіть кількість для списання.");
            LOGGER.warn("Відхилено списання: Не вказано кількість.");
            return;
        }

        try {
            double qtyToUse = Double.parseDouble(qtyInput);
            if (qtyToUse <= 0) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Кількість має бути позитивним числом.");
                LOGGER.warn("Відхилено списання: Кількість для списання не є позитивною: {}.",
                        qtyToUse);
                return;
            }

            double currentStock = inventoryService.calculateCurrentStock(selectedIngredient.getId());
            if (currentStock < qtyToUse) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Недостатньо " + selectedIngredient.getName() + " на складі. Доступно: "
                        + currentStock + " " + selectedIngredient.getUnit());
                LOGGER.warn("Відхилено списання: Недостатньо '{}' на складі. Доступно: {}, Списання: {}.",
                        selectedIngredient.getName(), currentStock, qtyToUse);
                return;
            }

            Stock expense = new Stock(selectedIngredient, -qtyToUse, "expense");
            inventoryService.addStockMovement(expense);

            loadIngredientsData();
            clearInputFieldsForStock();

            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Успішно списано " + qtyToUse + " " + selectedIngredient.getUnit()
                    + " з " + selectedIngredient.getName());
            LOGGER.info("Успішно списано {} {} з '{}'.", qtyToUse,
                    selectedIngredient.getUnit(), selectedIngredient.getName());
        } catch (NumberFormatException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Невірний формат кількості. Введіть число.");
            LOGGER.error("Помилка формату кількості при списанні: {}", qtyInput, e);
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка списання: " + e.getMessage());
            LOGGER.error("Помилка SQL при списанні інгредієнта '{}': {}",
                    selectedIngredient.getName(), e.getMessage(), e);
        }
    }

    @FXML
    private void handleCheckExpiration() {
        filterChoiceBox.setValue("Прострочені");
        messageLabel.setTextFill(Color.PINK);
        messageLabel.setText("Відфільтровано прострочені інгредієнти.");
        LOGGER.info("Виконано дію 'Перевірити термін придатності'. Встановлено фільтр на 'Прострочені'.");
    }

    @FXML
    private void handleDiscardSpoiled() {
        LOGGER.info("Спроба списати прострочені інгредієнти.");
        try {
            List<Ingredient> expired = inventoryService.getExpiredIngredients();
            if (expired.isEmpty()) {
                messageLabel.setTextFill(Color.PINK);
                messageLabel.setText("Немає прострочених інгредієнтів для списання.");
                LOGGER.info("Немає прострочених інгредієнтів для списання.");
                return;
            }

            int discardedCount = 0;
            for (Ingredient ing : expired) {
                double currentStock = inventoryService.calculateCurrentStock(ing.getId());
                if (currentStock > 0) {
                    Stock spoilage = new Stock(ing, -currentStock, "spoilage");
                    inventoryService.addStockMovement(spoilage);
                    inventoryService.updateIngredientExpiration(ing.getId(), null);
                    discardedCount++;
                    LOGGER.info("Списано {} {} простроченого інгредієнта '{}'." +
                                    " Термін придатності обнулено.",
                            currentStock, ing.getUnit(), ing.getName());
                }
            }
            loadIngredientsData();
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Успішно списано " + discardedCount + " типів прострочених інгредієнтів.");
            LOGGER.info("Успішно списано {} типів прострочених інгредієнтів.", discardedCount);

        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Помилка списання простроченого: " + e.getMessage());
            LOGGER.error("Помилка SQL при списанні прострочених інгредієнтів: {}", e.getMessage(), e);
        }
    }

    // КНОПОКИ ДЛЯ РЕЦЕПТІВ
    @FXML
    private void handleAddIngredientToRecipe() {
        MenuItem selectedMenuItem = menuItemRecipeComboBox.getSelectionModel().getSelectedItem();
        Ingredient selectedIngredient = ingredientForRecipeComboBox.getSelectionModel().getSelectedItem();
        LOGGER.info("Спроба додати/оновити інгредієнт в рецепті. Страва: {}," +
                        " Інгредієнт: {}, Кількість: '{}'.",
                (selectedMenuItem != null ? selectedMenuItem.getName() : "null"),
                (selectedIngredient != null ? selectedIngredient.getName() : "null"),
                quantityForRecipeField.getText());

        if (selectedMenuItem == null || selectedMenuItem.getId() == 0) {
            messageLabel.setText("Будь ласка, виберіть страву для рецепту.");
            LOGGER.warn("Відхилено додавання до рецепту: Не обрано страву.");
            return;
        }
        if (selectedIngredient == null) {
            messageLabel.setText("Будь ласка, виберіть інгредієнт для додавання до рецепту.");
            LOGGER.warn("Відхилено додавання до рецепту: Не обрано інгредієнт.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityForRecipeField.getText().trim());
            if (quantity <= 0) {
                messageLabel.setText("Кількість інгредієнта в рецепті повинна бути позитивною.");
                LOGGER.warn("Відхилено додавання до рецепту: Кількість інгредієнта не є позитивною: {}.", quantity);
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Будь ласка, введіть коректну кількість для рецепту.");
            LOGGER.error("Помилка формату кількості для рецепту: {}",
                    quantityForRecipeField.getText(), e);
            return;
        }

        try {
            MenuItemIngredient existingRecipeIngredient = null;
            for (MenuItemIngredient mii : currentRecipeIngredientsList) {
                if (mii.getIngredient().getId() == selectedIngredient.getId()) {
                    existingRecipeIngredient = mii;
                    break;
                }
            }

            if (existingRecipeIngredient != null) {
                existingRecipeIngredient.setQuantityPerUnit(quantity);
                inventoryService.updateMenuItemIngredient(existingRecipeIngredient);
                messageLabel.setText(String.format("Кількість '%s' для страви '%s' оновлено.",
                        selectedIngredient.getName(), selectedMenuItem.getName()));
                LOGGER.info("Оновлено кількість інгредієнта '{}' в рецепті '{}' на {}.",
                        selectedIngredient.getName(), selectedMenuItem.getName(), quantity);
            } else {
                MenuItemIngredient newRecipeIngredient = new MenuItemIngredient(
                        0,
                        selectedMenuItem,
                        selectedIngredient,
                        quantity
                );
                inventoryService.addMenuItemIngredient(newRecipeIngredient);
                messageLabel.setText(String.format("Інгредієнт '%s' додано до рецепту '%s'.",
                        selectedIngredient.getName(), selectedMenuItem.getName()));
                LOGGER.info("Додано новий інгредієнт '{}' до рецепту '{}' з кількістю {}.",
                        selectedIngredient.getName(), selectedMenuItem.getName(), quantity);
            }
            currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(selectedMenuItem.getId()));
            clearInputFieldsForRecipe();
        } catch (SQLException e) {
            LOGGER.error("Помилка SQL при додаванні/оновленні інгредієнта '{}' в рецепті '{}': {}",
                    selectedIngredient.getName(), selectedMenuItem.getName(), e.getMessage(), e);
            showAlert("Помилка БД", "Не вдалося додати/оновити інгредієнт в рецепті: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveIngredientFromRecipe() {
        MenuItemIngredient selectedRecipeIngredient = recipeIngredientTable.getSelectionModel().getSelectedItem();
        MenuItem selectedMenuItem = menuItemRecipeComboBox.getSelectionModel().getSelectedItem();
        LOGGER.info("Спроба видалити інгредієнт з рецепту. Обраний інгредієнт: {}, Страва: {}.",
                (selectedRecipeIngredient != null && selectedRecipeIngredient.getIngredient() != null ?
                        selectedRecipeIngredient.getIngredient().getName() : "null"),
                (selectedMenuItem != null ? selectedMenuItem.getName() : "null"));

        if (selectedRecipeIngredient == null) {
            messageLabel.setText("Будь ласка, виберіть інгредієнт з рецепту, щоб видалити.");
            LOGGER.warn("Відхилено видалення з рецепту: Не обрано інгредієнт.");
            return;
        }
        if (selectedMenuItem == null || selectedMenuItem.getId() == 0) {
            messageLabel.setText("Будь ласка, виберіть страву для рецепту.");
            LOGGER.warn("Відхилено видалення з рецепту: Не обрано страву.");
            return;
        }

        try {
            inventoryService.removeMenuItemIngredient(selectedRecipeIngredient.getId());
            messageLabel.setText(String.format("Інгредієнт '%s' видалено з рецепту '%s'.",
                    selectedRecipeIngredient.getIngredient().getName(), selectedMenuItem.getName()));
            LOGGER.info("Інгредієнт '{}' успішно видалено з рецепту '{}'.",
                    selectedRecipeIngredient.getIngredient().getName(), selectedMenuItem.getName());

            currentRecipeIngredientsList.setAll(inventoryService.getMenuItemIngredients(selectedMenuItem.getId()));
            clearInputFieldsForRecipe();
        } catch (SQLException e) {
            LOGGER.error("Помилка SQL при видаленні інгредієнта '{}' з рецепту '{}': {}",
                    selectedRecipeIngredient.getIngredient().getName(), selectedMenuItem.getName(), e.getMessage(), e);
            showAlert("Помилка БД", "Не вдалося видалити інгредієнт з рецепту: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        LOGGER.warn("Відображено сповіщення про помилку: {} - {}", title, message);
    }

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
                        LOGGER.trace("Інгредієнт прострочений: {}", item);
                    } else if (item.isBefore(sevenDaysFromNowThreshold.plusDays(1))) {
                        setTextFill(Color.PINK);
                        setStyle("-fx-font-weight: bold;");
                        LOGGER.trace("Термін придатності інгредієнта скоро закінчиться: {}", item);
                    } else {
                        setTextFill(Color.BLACK);
                        setStyle("");
                    }
                }
            }
        };
    }
}