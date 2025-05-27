package org.example.restaurant_management_system.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.example.restaurant_management_system.model.Client;
import org.example.restaurant_management_system.model.Employee;
import org.example.restaurant_management_system.model.MenuItem;
import org.example.restaurant_management_system.model.Order;
import org.example.restaurant_management_system.model.OrderItem;
import org.example.restaurant_management_system.model.Table;
import org.example.restaurant_management_system.service.OrderService;
import org.example.restaurant_management_system.service.InventoryService;
import org.example.restaurant_management_system.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OrderController {

    private static final Logger LOGGER = LogManager.getLogger(OrderController.class);

    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, LocalDateTime> orderTimeColumn;
    @FXML private TableColumn<Order, String> orderTableColumn;
    @FXML private TableColumn<Order, String> orderClientColumn;
    @FXML private TableColumn<Order, String> orderEmployeeColumn;
    @FXML private TableColumn<Order, String> orderPaymentMethodColumn;
    @FXML private TableColumn<Order, Double> orderTotalAmountColumn;

    @FXML private ComboBox<Table> tableComboBox;
    @FXML private ComboBox<Client> clientComboBox;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private ChoiceBox<String> paymentMethodChoiceBox;
    @FXML private Label totalAmountLabel;
    @FXML private Label messageLabel;

    @FXML private TextField newClientFirstNameField;
    @FXML private TextField newClientLastNameField;
    @FXML private TextField newClientPhoneField;
    @FXML private TextField newClientEmailField;
    @FXML private TextField newClientLoyaltyPointsField;
    @FXML private Button addClientButton;

    @FXML private TableView<OrderItem> orderItemTable;
    @FXML private TableColumn<OrderItem, String> orderItemMenuItemColumn;
    @FXML private TableColumn<OrderItem, Integer> orderItemQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> orderItemPriceAtOrderColumn;

    @FXML private ComboBox<MenuItem> menuItemComboBox;
    @FXML private TextField orderItemQuantityField;
    @FXML private Button addOrderItemButton;
    @FXML private Button removeOrderItemButton;

    private OrderService orderService;
    private InventoryService inventoryService;
    private ObservableList<Order> masterOrdersList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> currentOrderItemsList = FXCollections.observableArrayList();

    private ObservableList<MenuItem> menuItemsList = FXCollections.observableArrayList();
    private ObservableList<Table> tablesList = FXCollections.observableArrayList();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private ObservableList<Employee> employeesList = FXCollections.observableArrayList();

    private Connection dbConnection;
    private final AtomicBoolean isProgrammaticallyUpdating = new AtomicBoolean(false);
    private double currentOrderAppliedBonusDiscount = 0.0;

    @FXML
    public void initialize() {
        LOGGER.info("Ініціалізація OrderController.");
        //перевірка надсилання критичних помилок на email
        LOGGER.error("Тест критичної помилки для email!");

        try {
            this.dbConnection = DatabaseConnection.getConnection();
            if (this.dbConnection == null || this.dbConnection.isClosed()) {
                showAlert("Критична помилка БД", "Не вдалося отримати з'єднання або воно закрите.");
                LOGGER.fatal("Не вдалося отримати з'єднання з БД або воно закрите.");
                disableUIComponents(); return;
            }
            this.orderService = new OrderService(this.dbConnection);
            this.inventoryService = new InventoryService(this.dbConnection);
            LOGGER.info("OrderService та InventoryService ініціалізовано.");
        } catch (SQLException e) {
            showAlert("Помилка підключення до БД", "Не вдалося ініціалізувати сервіси: " + e.getMessage());
            LOGGER.fatal("Не вдалося ініціалізувати сервіси: " + e.getMessage(), e);
            e.printStackTrace(); disableUIComponents(); return;
        }
        setupTableViews();
        setupComboBoxesAndChoiceBox();
        loadInitialData();
        setupListeners();
        LOGGER.info("OrderController успішно ініціалізовано.");
    }

    // на випадок критичних помиок з бд
    private void disableUIComponents() {
        LOGGER.warn("Вимикання елементів інтерфейсу через критичну помилку БД.");
        if (tableComboBox != null) tableComboBox.setDisable(true);
        if (clientComboBox != null) clientComboBox.setDisable(true);
        if (employeeComboBox != null) employeeComboBox.setDisable(true);
        if (paymentMethodChoiceBox != null) paymentMethodChoiceBox.setDisable(true);
        if (addClientButton != null) addClientButton.setDisable(true);
        if (menuItemComboBox != null) menuItemComboBox.setDisable(true);
        if (addOrderItemButton != null) addOrderItemButton.setDisable(true);
        if (removeOrderItemButton != null) removeOrderItemButton.setDisable(true);
        if (orderTable != null && orderTable.getScene() != null) {
            Button createBtn = (Button) orderTable.getScene().lookup("#createNewOrderButton_fxid");
            if (createBtn != null) createBtn.setDisable(true);
            Button updateBtn = (Button) orderTable.getScene().lookup("#updateOrderButton_fxid");
            if (updateBtn != null) updateBtn.setDisable(true);
            Button deleteBtn = (Button) orderTable.getScene().lookup("#deleteOrderButton_fxid");
            if (deleteBtn != null) deleteBtn.setDisable(true);
        }
        messageLabel.setTextFill(Color.PINK);
        messageLabel.setText("Робота неможлива: критична помилка з'єднання з БД.");
        LOGGER.error("Інтерфейс вимкнено, відображено повідомлення: " + messageLabel.getText());
    }

    // налаштування стовпців і позицій в таблиці
    private void setupTableViews() {
        LOGGER.info("Налаштування табличних представлень.");
        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        orderTimeColumn.setCellValueFactory(cellData -> cellData.getValue().orderTimeProperty());
        orderTableColumn.setCellValueFactory(cellData -> Optional.ofNullable(cellData.getValue().getTable()).map(Table::tableNumberProperty).orElse(null));
        orderClientColumn.setCellValueFactory(cellData -> Optional.ofNullable(cellData.getValue().getClient()).map(c -> new javafx.beans.property.SimpleStringProperty(c.getFirstName() + " " + c.getLastName())).orElse(null));
        orderEmployeeColumn.setCellValueFactory(cellData -> Optional.ofNullable(cellData.getValue().getEmployee()).map(e -> new javafx.beans.property.SimpleStringProperty(e.getFirstName() + " " + e.getLastName())).orElse(null));
        orderPaymentMethodColumn.setCellValueFactory(cellData -> cellData.getValue().paymentMethodProperty());
        orderTotalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());
        orderTable.setPlaceholder(new Label("Немає замовлень"));
        orderTable.setItems(masterOrdersList);

        orderItemMenuItemColumn.setCellValueFactory(cellData -> Optional.ofNullable(cellData.getValue().getMenuItem()).map(mi -> new javafx.beans.property.SimpleStringProperty(mi.getName())).orElse(null));
        orderItemQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        orderItemPriceAtOrderColumn.setCellValueFactory(cellData -> cellData.getValue().priceAtOrderProperty().asObject());
        orderItemTable.setPlaceholder(new Label("Оберіть замовлення для перегляду позицій"));
        orderItemTable.setItems(currentOrderItemsList);
        LOGGER.debug("Налаштування табличних представлень завершено.");
    }

    // робота з випадаючими списками/полями вибору
    private void setupComboBoxesAndChoiceBox() {
        LOGGER.info("Налаштування комбінованих списків та полів вибору.");
        paymentMethodChoiceBox.setItems(FXCollections.observableArrayList("Готівка", "Картка", "Бонуси"));
        paymentMethodChoiceBox.setValue("Готівка");

        tableComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Table t) { return t != null ? t.getTableNumber() : "Не обрано"; }
            @Override public Table fromString(String s) { return tablesList.stream().filter(t -> t.getTableNumber().equals(s)).findFirst().orElse(null); }
        });
        clientComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Client c) { return c != null ? c.getFirstName() + " " + c.getLastName() + " (" + String.format("%.2f", c.getLoyaltyPoints()) + " балів)" : "Не обрано"; }
            @Override public Client fromString(String s) {
                if (s == null || s.equals("Не обрано") || !s.contains(" (")) return null;
                return clientsList.stream().filter(c -> (c.getFirstName() + " " + c.getLastName()).equals(s.substring(0, s.lastIndexOf(" (")))).findFirst().orElse(null);
            }
        });
        employeeComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e != null ? e.getFirstName() + " " + e.getLastName() : "Не обрано"; }
            @Override public Employee fromString(String s) { return employeesList.stream().filter(e -> (e.getFirstName() + " " + e.getLastName()).equals(s)).findFirst().orElse(null); }
        });
        menuItemComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(MenuItem mi) { return mi != null ? mi.getName() : "Оберіть страву"; }
            @Override public MenuItem fromString(String s) { return menuItemsList.stream().filter(mi -> mi.getName().equals(s)).findFirst().orElse(null); }
        });
        LOGGER.debug("Налаштування комбінованих списків та полів вибору завершено.");
    }

    // для реагування на дії + автоматичне оновлення інформації
    private void setupListeners() {
        LOGGER.info("Налаштування слухачів.");
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (isProgrammaticallyUpdating.get()) {
                LOGGER.debug("Програмне оновлення, слухач пропущено.");
                return;
            }
            currentOrderAppliedBonusDiscount = 0.0;
            if (newSelection != null) {
                LOGGER.debug("Обрано замовлення: ID " + newSelection.getId());
                displayOrderDetails(newSelection);
                loadOrderItemsAndUpdateDisplay(newSelection.getId());
            } else {
                LOGGER.debug("Замовлення не обрано, очищення полів.");
                clearOrderInputFields();
            }
            messageLabel.setText("");
        });

        paymentMethodChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticallyUpdating.get() || newVal == null || orderTable.getSelectionModel().getSelectedItem() == null) {
                LOGGER.debug("Слухач способу оплати пропущено через програмне оновлення або нульові значення.");
                return;
            }
            LOGGER.info("Спосіб оплати змінено на: " + newVal);
            if (!"Бонуси".equals(newVal)) currentOrderAppliedBonusDiscount = 0.0;
            recalculateAndDisplayTotalWithPotentialBonuses();
        });

        clientComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticallyUpdating.get() || orderTable.getSelectionModel().getSelectedItem() == null) {
                LOGGER.debug("Слухач клієнта пропущено через програмне оновлення або відсутність обраного замовлення.");
                return;
            }
            LOGGER.info("Клієнта змінено на: " + (newVal != null ? newVal.getFirstName() : "null"));
            currentOrderAppliedBonusDiscount = 0.0;
            recalculateAndDisplayTotalWithPotentialBonuses();
        });

        orderItemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newSelection) -> {
            if (newSelection != null) {
                LOGGER.debug("Обрано позицію замовлення: " + newSelection.getMenuItem().getName());
                menuItemComboBox.getSelectionModel().select(newSelection.getMenuItem());
                orderItemQuantityField.setText(String.valueOf(newSelection.getQuantity()));
            } else {
                LOGGER.debug("Позицію замовлення не обрано, очищення полів.");
                menuItemComboBox.getSelectionModel().clearSelection();
                orderItemQuantityField.clear();
            }
        });
        LOGGER.debug("Налаштування слухачів завершено.");
    }

    // завантаження початкових даних
    private void loadInitialData() {
        LOGGER.info("Завантаження початкових даних.");
        if (orderService == null || inventoryService == null) {
            System.err.println("Сервіси не ініціалізовані, завантаження даних скасовано.");
            LOGGER.error("Сервіси не ініціалізовано, завантаження даних скасовано.");
            return;
        }
        loadAllOrders(); loadMenuItems(); loadTables();
        try { if (tablesList.isEmpty()) { orderService.insertInitialTables(5); loadTables();
            LOGGER.info("Початкові столики вставлено та завантажено.");
        }}
        catch (SQLException e) {
            showAlert("Помилка ініціалізації столиків", e.getMessage());
            LOGGER.error("Помилка SQL при ініціалізації столиків: " + e.getMessage(), e);
        }
        loadClients(); loadEmployees();
        LOGGER.debug("Завантаження початкових даних завершено.");
    }

    //завантаження всіх замовлень з бд
    private void loadAllOrders() {
        LOGGER.info("Завантаження всіх замовлень.");
        if (orderService != null)
            try { masterOrdersList.setAll(orderService.getAllOrders());
                LOGGER.debug("Усі замовлення завантажено. Кількість: " + masterOrdersList.size());
            }
            catch (SQLException e)
            { showAlert("Помилка завантаження замовлень", e.getMessage());
                LOGGER.error("Помилка SQL при завантаженні всіх замовлень: " + e.getMessage(), e);
                e.printStackTrace();}
    }

    // завантаження конкретного замовлення за його id
    private void loadOrderItemsAndUpdateDisplay(int orderId) {
        LOGGER.info("Завантаження позицій замовлення для ID: " + orderId);
        if (orderService == null) {
            LOGGER.warn("OrderService є null, неможливо завантажити позиції замовлення.");
            return;
        }
        try {
            currentOrderItemsList.setAll(orderService.getOrderItemsByOrderId(orderId));
            orderItemTable.refresh();
            recalculateAndDisplayTotalWithPotentialBonuses();
            LOGGER.debug("Позиції замовлення завантажено для ID " + orderId + ". Кількість: " + currentOrderItemsList.size());
        } catch (SQLException e) { showAlert("Помилка завантаження позицій", "ID: " + orderId + ": " + e.getMessage());
            LOGGER.error("Помилка SQL при завантаженні позицій замовлення для ID " + orderId + ": " + e.getMessage(), e);
            e.printStackTrace(); }
    }

    //завантаження всіх страв з бд
    private void loadMenuItems() {
        LOGGER.info("Завантаження пунктів меню.");
        if (inventoryService != null)
            try { menuItemsList.setAll(inventoryService.getAllMenuItems());
                menuItemComboBox.setItems(menuItemsList);
                LOGGER.debug("Пункти меню завантажено. Кількість: " + menuItemsList.size());
            }
            catch (SQLException e)
            { showAlert("Помилка завантаження меню", e.getMessage());
                LOGGER.error("Помилка SQL при завантаженні пунктів меню: " + e.getMessage(), e);
                e.printStackTrace();}
    }

    //завантаження всіх столиків з бд
    private void loadTables() {
        LOGGER.info("Завантаження столиків.");
        if (orderService != null)
            try { tablesList.setAll(orderService.getAllTables());
                tableComboBox.setItems(tablesList);
                LOGGER.debug("Столики завантажено. Кількість: " + tablesList.size());
            }
            catch (SQLException e)
            { showAlert("Помилка завантаження столів", e.getMessage());
                LOGGER.error("Помилка SQL при завантаженні столів: " + e.getMessage(), e);
                e.printStackTrace();}
    }

    //завантаження всіх клієнтів з бд
    private void loadClients() {
        LOGGER.info("Завантаження клієнтів.");
        if (orderService != null)
            try { clientsList.setAll(orderService.getAllClients());
                clientComboBox.setItems(clientsList);
                LOGGER.debug("Клієнти завантажено. Кількість: " + clientsList.size());
            } catch
            (SQLException e)
            { showAlert("Помилка завантаження клієнтів", e.getMessage());
                LOGGER.error("Помилка SQL при завантаженні клієнтів: " + e.getMessage(), e);
                e.printStackTrace();}
    }

    //завантаження всіх офіціантів з бд
    private void loadEmployees() {
        LOGGER.info("Завантаження співробітників.");
        if (orderService != null)
            try { employeesList.setAll(orderService.getAllEmployees("Офіціант"));
                employeeComboBox.setItems(employeesList);
                LOGGER.debug("Співробітники завантажено. Кількість: " + employeesList.size());
            } catch (SQLException e) {
                showAlert("Помилка завантаження співробітників", e.getMessage());
                LOGGER.error("Помилка SQL при завантаженні співробітників: " + e.getMessage(), e);
                e.printStackTrace();}
    }

    // відображення детелей замовлення
    private void displayOrderDetails(Order order) {
        LOGGER.info("Відображення деталей замовлення: ID " + (order != null ? order.getId() : "null"));
        if (order == null) {
            LOGGER.warn("Спроба відобразити деталі замовлення для null об'єкта.");
            return;
        }
        isProgrammaticallyUpdating.set(true);
        try {
            tableComboBox.getSelectionModel().select(order.getTable());
            clientComboBox.getSelectionModel().select(order.getClient());
            employeeComboBox.getSelectionModel().select(order.getEmployee());
            paymentMethodChoiceBox.setValue(order.getPaymentMethod());
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", order.getTotalAmount()));
            LOGGER.debug("Деталі замовлення ID " + order.getId() + " відображено.");
        } finally {
            isProgrammaticallyUpdating.set(false);
        }
    }

    // очищення полів вводу для замовлень
    private void clearOrderInputFields() {
        LOGGER.info("Очищення полів вводу замовлення.");
        isProgrammaticallyUpdating.set(true);
        try {
            orderTable.getSelectionModel().clearSelection();
            tableComboBox.getSelectionModel().clearSelection();
            clientComboBox.getSelectionModel().clearSelection();
            employeeComboBox.getSelectionModel().clearSelection();
            paymentMethodChoiceBox.setValue("Готівка");
        } finally {
            isProgrammaticallyUpdating.set(false);
        }
        totalAmountLabel.setText("Загальна сума: 0.00");
        messageLabel.setText("");
        clearNewClientInputFields();
        currentOrderItemsList.clear();
        currentOrderAppliedBonusDiscount = 0.0;
        LOGGER.debug("Поля вводу замовлення очищено.");
    }
    private void clearOrderItemInputFields() {
        LOGGER.info("Очищення полів вводу позиції замовлення.");
        orderItemTable.getSelectionModel().clearSelection();
        menuItemComboBox.getSelectionModel().clearSelection();
        orderItemQuantityField.clear();
        LOGGER.debug("Поля вводу позиції замовлення очищено.");
    }

    //очищення полів вводу для клієнтів
    private void clearNewClientInputFields() {
        LOGGER.info("Очищення полів вводу нового клієнта.");
        newClientFirstNameField.clear(); newClientLastNameField.clear(); newClientPhoneField.clear();
        newClientEmailField.clear(); newClientLoyaltyPointsField.clear();
        LOGGER.debug("Поля вводу нового клієнта очищено.");
    }

    //перераховує та відображає загальну суму замовлення (з врахуванням бонусів)
    private void recalculateAndDisplayTotalWithPotentialBonuses() {
        LOGGER.info("Перерахунок та відображення загальної суми замовлення з потенційними бонусами.");
        Client currentClientInUI = clientComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();
        double baseTotalFromItems = currentOrderItemsList.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPriceAtOrder())
                .sum();
        LOGGER.debug("Базова сума з позицій: " + baseTotalFromItems);

        if ("Бонуси".equals(paymentMethod) && currentClientInUI != null) {
            if (currentOrderAppliedBonusDiscount == 0.0 && baseTotalFromItems > 0) {
                double pointsAvailable = currentClientInUI.getLoyaltyPoints();
                currentOrderAppliedBonusDiscount = Math.min(baseTotalFromItems, pointsAvailable);
                LOGGER.debug("Застосовано бонусів: " + currentOrderAppliedBonusDiscount + " з " + pointsAvailable + " доступних.");
            }
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", Math.max(0, baseTotalFromItems - currentOrderAppliedBonusDiscount)));
        } else {
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", baseTotalFromItems));
            LOGGER.debug("Бонуси не застосовані. Загальна сума: " + baseTotalFromItems);
        }
        LOGGER.debug("Оновлено відображення загальної суми: " + totalAmountLabel.getText());
    }

    @FXML
    private void handleCreateNewOrder() {
        LOGGER.info("Обробка створення нового замовлення.");
        if (orderService == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!");
            LOGGER.error("Створення нового замовлення не вдалося: сервіс не ініціалізовано.");
            return;
        }
        Table table = tableComboBox.getSelectionModel().getSelectedItem();
        Client client = clientComboBox.getSelectionModel().getSelectedItem();
        Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();

        LOGGER.debug("Дані для нового замовлення: Столик=" + (table != null ? table.getTableNumber() : "null") +
                ", Клієнт=" + (client != null ? client.getFirstName() + " " + client.getLastName() : "null") +
                ", Співробітник=" + (employee != null ? employee.getFirstName() + " " + employee.getLastName() : "null") +
                ", Метод оплати=" + paymentMethod);

        if ((table == null && client == null)) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть столик або клієнта.");
            LOGGER.warn("Створення замовлення не вдалося: не обрано столик або клієнта.");
            return;
        }
        if (employee == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть співробітника.");
            LOGGER.warn("Створення замовлення не вдалося: не обрано співробітника.");
            return;
        }

        try {
            Order newOrder = new Order(0, LocalDateTime.now(),
                    table != null ? table.getId() : null,
                    client != null ? client.getId() : null,
                    employee.getId(), paymentMethod, 0.0);
            newOrder.setTable(table); newOrder.setClient(client); newOrder.setEmployee(employee);
            currentOrderAppliedBonusDiscount = 0.0;

            Order createdOrder = orderService.createOrder(newOrder, 0.0);
            LOGGER.info("Нове замовлення створено з ID: " + createdOrder.getId());

            masterOrdersList.add(createdOrder);
            final Order finalCreatedOrder = createdOrder;
            Platform.runLater(() -> {
                isProgrammaticallyUpdating.set(true);
                try { orderTable.getSelectionModel().select(finalCreatedOrder); }
                finally { isProgrammaticallyUpdating.set(false); }
                LOGGER.debug("Обрано щойно створене замовлення в таблиці.");
            });

            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Замовлення ID " + createdOrder.getId() + " створено. Додайте страви.");

            if (client != null && "Картка".equals(paymentMethod)) {
                loadClients(); // оновити бали, якщо були нараховані
                LOGGER.debug("Клієнтські дані оновлено після створення замовлення (метод оплати 'Картка').");
            }
        } catch (SQLException e) {
            showAlert("Помилка створення замовлення", e.getMessage());
            LOGGER.error("Помилка SQL при створенні нового замовлення: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateOrder() {
        LOGGER.info("Обробка оновлення замовлення.");
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null || selectedOrder.getId() == 0) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Виберіть існуюче замовлення.");
            LOGGER.warn("Оновлення замовлення не вдалося: не вибрано існуюче замовлення.");
            return;
        }
        if (orderService == null) {
            messageLabel.setTextFill(Color.RED); messageLabel.setText("Сервіс недоступний.");
            LOGGER.error("Оновлення замовлення не вдалося: сервіс недоступний.");
            return;
        }

        Table table = tableComboBox.getSelectionModel().getSelectedItem();
        Client client = clientComboBox.getSelectionModel().getSelectedItem();
        Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();

        LOGGER.debug("Дані для оновлення замовлення ID " + selectedOrder.getId() + ": Столик=" + (table != null ? table.getTableNumber() : "null") +
                ", Клієнт=" + (client != null ? client.getFirstName() + " " + client.getLastName() : "null") +
                ", Співробітник=" + (employee != null ? employee.getFirstName() + " " + employee.getLastName() : "null") +
                ", Метод оплати=" + paymentMethod);

        if ((table == null && client == null)) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть столик або клієнта.");
            LOGGER.warn("Оновлення замовлення не вдалося: не обрано столик або клієнта.");
            return;
        }
        if (employee == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть співробітника.");
            LOGGER.warn("Оновлення замовлення не вдалося: не обрано співробітника.");
            return;
        }

        double baseTotalFromItems = currentOrderItemsList.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPriceAtOrder())
                .sum();
        LOGGER.debug("Базова сума з позицій для оновлення: " + baseTotalFromItems);

        double discountToCommit = 0.0;
        if ("Бонуси".equals(paymentMethod) && client != null) {
            Client clientFromUI = clientComboBox.getSelectionModel().getSelectedItem();
            if(clientFromUI != null) {
                double pointsAvailableNow = clientFromUI.getLoyaltyPoints();
                discountToCommit = Math.min(baseTotalFromItems, pointsAvailableNow);
                discountToCommit = Math.min(currentOrderAppliedBonusDiscount, discountToCommit); // Не більше "зафіксованої"
                LOGGER.debug("Бонусна знижка для коміту: " + discountToCommit + " (доступно: " + pointsAvailableNow + ", зафіксовано: " + currentOrderAppliedBonusDiscount + ")");
            }
        }

        try {
            selectedOrder.setTotalAmount(baseTotalFromItems);
            selectedOrder.setTable(table); selectedOrder.setClient(client);
            selectedOrder.setEmployee(employee); selectedOrder.setPaymentMethod(paymentMethod);

            orderService.updateOrder(selectedOrder, discountToCommit);
            LOGGER.info("Замовлення ID " + selectedOrder.getId() + " оновлено в базі даних.");

            Order updatedOrderFromDB = orderService.getOrderById(selectedOrder.getId());
            LOGGER.debug("Отримано оновлене замовлення з БД: ID " + (updatedOrderFromDB != null ? updatedOrderFromDB.getId() : "null"));

            if (updatedOrderFromDB != null) {
                if("Бонуси".equals(updatedOrderFromDB.getPaymentMethod())){
                    currentOrderAppliedBonusDiscount = baseTotalFromItems - updatedOrderFromDB.getTotalAmount();
                    LOGGER.debug("Оновлено currentOrderAppliedBonusDiscount: " + currentOrderAppliedBonusDiscount);
                } else {
                    currentOrderAppliedBonusDiscount = 0.0;
                    LOGGER.debug("currentOrderAppliedBonusDiscount скинуто до 0.");
                }

                final Order finalUpdatedOrder = updatedOrderFromDB;
                Platform.runLater(() -> {
                    isProgrammaticallyUpdating.set(true);
                    try {
                        int index = -1;
                        for(int i=0; i<masterOrdersList.size(); i++){
                            if(masterOrdersList.get(i).getId() == finalUpdatedOrder.getId()){
                                masterOrdersList.set(i, finalUpdatedOrder); index = i; break;
                            }
                        }
                        orderTable.refresh();
                        if(index != -1) orderTable.getSelectionModel().select(index);
                        else orderTable.getSelectionModel().clearSelection();
                        LOGGER.debug("Таблиця замовлень оновлена, вибрано оновлене замовлення.");
                    } finally {
                        isProgrammaticallyUpdating.set(false);
                    }
                });
                loadClients();
                LOGGER.debug("Дані клієнтів оновлено після оновлення замовлення.");
                messageLabel.setTextFill(Color.GREEN);
                messageLabel.setText("Замовлення ID " + selectedOrder.getId() + " оновлено.");
            }
        } catch (SQLException e) {
            showAlert("Помилка оновлення", e.getMessage());
            LOGGER.error("Помилка SQL при оновленні замовлення: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddOrUpdateOrderItem() {
        LOGGER.info("Обробка додавання/оновлення позиції замовлення.");
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть замовлення.");
            LOGGER.warn("Додавання/оновлення позиції замовлення не вдалося: замовлення не обрано.");
            return;
        }
        if (selectedOrder.getId() == 0) {
            messageLabel.setTextFill(Color.ORANGE); messageLabel.setText("Збережіть нове замовлення перед додаванням страв.");
            LOGGER.warn("Додавання/оновлення позиції замовлення не вдалося: нове замовлення не збережено.");
            return;
        }
        if (orderService == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс недоступний.");
            LOGGER.error("Додавання/оновлення позиції замовлення не вдалося: сервіс недоступний.");
            return;
        }

        MenuItem selectedMenuItem = menuItemComboBox.getSelectionModel().getSelectedItem();
        String qtyText = orderItemQuantityField.getText().trim();
        if (selectedMenuItem == null || qtyText.isEmpty()) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть страву та кількість.");
            LOGGER.warn("Додавання/оновлення позиції замовлення не вдалося: не обрано страву або кількість відсутня.");
            return;
        }
        LOGGER.debug("Дані позиції замовлення: Страва=" + selectedMenuItem.getName() + ", Кількість (текст)=" + qtyText);

        try {
            int quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) {
                messageLabel.setTextFill(Color.PINK); messageLabel.setText("Кількість > 0.");
                LOGGER.warn("Невірна кількість для позиції замовлення: " + quantity);
                return;
            }
            LOGGER.debug("Розпізнана кількість: " + quantity);

            OrderItem existingOrderItem = null;
            for(OrderItem item : currentOrderItemsList){
                if(item.getMenuItemId() == selectedMenuItem.getId() && item.getOrderId() == selectedOrder.getId()){
                    existingOrderItem = item; break;
                }
            }

            if (existingOrderItem != null) {
                LOGGER.info("Оновлення існуючої позиції замовлення: ID " + existingOrderItem.getId());
                existingOrderItem.setQuantity(quantity);
                existingOrderItem.setPriceAtOrder(selectedMenuItem.getPrice());
                orderService.updateOrderItem(existingOrderItem);
                int idx = currentOrderItemsList.indexOf(existingOrderItem);
                if(idx != -1) currentOrderItemsList.set(idx, existingOrderItem);
                LOGGER.debug("Позиція замовлення ID " + existingOrderItem.getId() + " оновлена.");
            } else {
                LOGGER.info("Додавання нової позиції замовлення.");
                OrderItem newOrderItem = new OrderItem(0, selectedOrder.getId(), selectedMenuItem.getId(), quantity, selectedMenuItem.getPrice());
                newOrderItem.setMenuItem(selectedMenuItem);
                OrderItem createdOrderItem = orderService.createOrderItem(newOrderItem);
                currentOrderItemsList.add(createdOrderItem);
                LOGGER.debug("Нова позиція замовлення створена з ID: " + createdOrderItem.getId());
            }
            orderItemTable.refresh();
            recalculateAndDisplayTotalWithPotentialBonuses();
            clearOrderItemInputFields();
            LOGGER.info("Позицію замовлення успішно додано/оновлено.");
        } catch (NumberFormatException e) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Невірний формат кількості.");
            LOGGER.warn("Невірний формат кількості при додаванні/оновленні позиції замовлення: " + qtyText, e);
        }
        catch (SQLException e) {
            showAlert("Помилка позиції", "Помилка при роботі з позицією: " + e.getMessage());
            LOGGER.error("Помилка SQL при додаванні/оновленні позиції замовлення: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemoveOrderItem() {
        LOGGER.info("Обробка видалення позиції замовлення.");
        OrderItem selectedOrderItem = orderItemTable.getSelectionModel().getSelectedItem();
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrderItem == null || selectedOrder == null || orderService == null) {
            LOGGER.warn("Видалення позиції замовлення не вдалося: не обрано позицію, замовлення або сервіс недоступний.");
            return;
        }
        LOGGER.debug("Обрано позицію замовлення для видалення: ID " + selectedOrderItem.getId() + ", Замовлення ID: " + selectedOrder.getId());

        try {
            orderService.deleteOrderItem(selectedOrderItem.getId());
            currentOrderItemsList.remove(selectedOrderItem);
            orderItemTable.refresh();
            recalculateAndDisplayTotalWithPotentialBonuses();
            clearOrderItemInputFields();
            LOGGER.info("Позицію замовлення ID " + selectedOrderItem.getId() + " успішно видалено.");
        } catch (SQLException e) {
            showAlert("Помилка видалення позиції", e.getMessage());
            LOGGER.error("Помилка SQL при видаленні позиції замовлення: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteOrder() {
        LOGGER.info("Обробка видалення замовлення.");
        if (orderService == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!");
            LOGGER.error("Видалення замовлення не вдалося: сервіс не ініціалізовано.");
            return;
        }
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Виберіть замовлення для видалення.");
            LOGGER.warn("Видалення замовлення не вдалося: замовлення не обрано.");
            return;
        }
        LOGGER.debug("Обрано замовлення для видалення: ID " + selectedOrder.getId());

        try {
            orderService.deleteOrder(selectedOrder.getId());
            masterOrdersList.remove(selectedOrder);
            if (orderTable.getItems().isEmpty()) clearOrderInputFields();
            else orderTable.getSelectionModel().clearSelection();
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Замовлення ID " + selectedOrder.getId() + " видалено.");
            loadClients();
            LOGGER.info("Замовлення ID " + selectedOrder.getId() + " успішно видалено.");
            LOGGER.debug("Дані клієнтів оновлено після видалення замовлення.");
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Помилка видалення: " + e.getMessage());
            LOGGER.error("Помилка SQL при видаленні замовлення: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearOrderSelection() {
        LOGGER.info("Обробка очищення виділення замовлення.");
        clearOrderInputFields();
        clearOrderItemInputFields();
        LOGGER.debug("Виділення замовлення та позицій замовлення очищено.");
    }

    @FXML
    private void handleAddClient() {
        LOGGER.info("Обробка додавання нового клієнта.");
        if (orderService == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!");
            LOGGER.error("Додавання клієнта не вдалося: сервіс не ініціалізовано.");
            return;
        }
        String firstName = newClientFirstNameField.getText().trim();
        String lastName = newClientLastNameField.getText().trim();

        String phoneNumber = newClientPhoneField.getText().trim();
        String email = newClientEmailField.getText().trim();
        String loyaltyPointsText = newClientLoyaltyPointsField.getText().trim();

        LOGGER.debug("Дані нового клієнта: Ім'я=" + firstName + ", Прізвище=" + lastName +
                ", Телефон=" + phoneNumber + ", Email=" + email + ", Бали лояльності (текст)=" + loyaltyPointsText);

        if (firstName.isEmpty() || lastName.isEmpty()) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Ім'я та прізвище обов'язкові.");
            LOGGER.warn("Додавання клієнта не вдалося: ім'я або прізвище відсутні.");
            return;
        }
        try {
            double loyaltyPoints = 0.0;
            if (!loyaltyPointsText.isEmpty()) {
                try {
                    loyaltyPoints = Double.parseDouble(loyaltyPointsText);
                    if (loyaltyPoints < 0) loyaltyPoints = 0;
                    LOGGER.debug("Розпізнані бали лояльності: " + loyaltyPoints);
                } catch (NumberFormatException e) {
                    messageLabel.setTextFill(Color.PINK); messageLabel.setText("Невірний формат бонусів.");
                    LOGGER.warn("Невірний формат бонусів при додаванні клієнта: " + loyaltyPointsText, e);
                    return;
                }
            }
            Client newClient = new Client(0, firstName, lastName, phoneNumber, email, loyaltyPoints);
            Client createdClient = orderService.addClient(newClient);
            loadClients();
            clearNewClientInputFields();
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Клієнт '" + createdClient.getFirstName() + " " + createdClient.getLastName() + "' доданий.");
            LOGGER.info("Клієнт '" + createdClient.getFirstName() + " " + createdClient.getLastName() + "' успішно доданий з ID: " + createdClient.getId());
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Помилка додавання клієнта: " + e.getMessage());
            LOGGER.error("Помилка SQL при додаванні клієнта: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    //відображення вікна для помилок/підтвердження дій
    private void showAlert(String title, String message) {
        LOGGER.info(String.format("Відображення сповіщення: Заголовок='%s', Повідомлення='%s'", title, message));
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String message) {
        LOGGER.info(String.format("Відображення підтвердження: Заголовок='%s', Повідомлення='%s'", title, message));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        return alert.showAndWait();
    }
}