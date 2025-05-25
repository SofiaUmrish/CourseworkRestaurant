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

public class OrderController {

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
        try {
            this.dbConnection = DatabaseConnection.getConnection();
            if (this.dbConnection == null || this.dbConnection.isClosed()) {
                showAlert("Критична помилка БД", "Не вдалося отримати з'єднання або воно закрите.");
                disableUIComponents(); return;
            }
            this.orderService = new OrderService(this.dbConnection);
            this.inventoryService = new InventoryService(this.dbConnection);
        } catch (SQLException e) {
            showAlert("Помилка підключення до БД", "Не вдалося ініціалізувати сервіси: " + e.getMessage());
            e.printStackTrace(); disableUIComponents(); return;
        }
        setupTableViews();
        setupComboBoxesAndChoiceBox();
        loadInitialData();
        setupListeners();
    }

    // на випадок критичних помиок з бд
    private void disableUIComponents() {
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
    }

    // налаштування стовпців і позицій в таблиці
    private void setupTableViews() {
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
    }

    // робота з випадаючими списками/полями вибору
    private void setupComboBoxesAndChoiceBox() {
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
    }

    // для реагування на дії + автоматичне оновлення інформації
    private void setupListeners() {
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (isProgrammaticallyUpdating.get()) return;
            currentOrderAppliedBonusDiscount = 0.0;
            if (newSelection != null) {
                displayOrderDetails(newSelection);
                loadOrderItemsAndUpdateDisplay(newSelection.getId());
            } else {
                clearOrderInputFields();
            }
            messageLabel.setText("");
        });

        paymentMethodChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticallyUpdating.get() || newVal == null || orderTable.getSelectionModel().getSelectedItem() == null) return;
            if (!"Бонуси".equals(newVal)) currentOrderAppliedBonusDiscount = 0.0;
            recalculateAndDisplayTotalWithPotentialBonuses();
        });

        clientComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isProgrammaticallyUpdating.get() || orderTable.getSelectionModel().getSelectedItem() == null) return;
            currentOrderAppliedBonusDiscount = 0.0;
            recalculateAndDisplayTotalWithPotentialBonuses();
        });

        orderItemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newSelection) -> {
            if (newSelection != null) {
                menuItemComboBox.getSelectionModel().select(newSelection.getMenuItem());
                orderItemQuantityField.setText(String.valueOf(newSelection.getQuantity()));
            } else {
                menuItemComboBox.getSelectionModel().clearSelection();
                orderItemQuantityField.clear();
            }
        });
    }

    // завантаження початкових даних
    private void loadInitialData() {
        if (orderService == null || inventoryService == null) { System.err.println("Сервіси не ініціалізовані, завантаження даних скасовано."); return; }
        loadAllOrders(); loadMenuItems(); loadTables();
        try { if (tablesList.isEmpty()) { orderService.insertInitialTables(5); loadTables(); }}
        catch (SQLException e) { showAlert("Помилка ініціалізації столиків", e.getMessage());}
        loadClients(); loadEmployees();
    }

    //завантаження всіх замовлень з бд
    private void loadAllOrders() { if (orderService != null)
        try { masterOrdersList.setAll(orderService.getAllOrders()); }
    catch (SQLException e)
    { showAlert("Помилка завантаження замовлень", e.getMessage());
        e.printStackTrace();}
    }

    // завантаження конкретного замовлення за його id
    private void loadOrderItemsAndUpdateDisplay(int orderId) {
        if (orderService == null) return;
        try {
            currentOrderItemsList.setAll(orderService.getOrderItemsByOrderId(orderId));
            orderItemTable.refresh();
            recalculateAndDisplayTotalWithPotentialBonuses();
        } catch (SQLException e) { showAlert("Помилка завантаження позицій", "ID: " + orderId + ": " + e.getMessage()); e.printStackTrace(); }
    }

    //завантаження всіх страв з бд
    private void loadMenuItems() { if (inventoryService != null)
        try { menuItemsList.setAll(inventoryService.getAllMenuItems());
            menuItemComboBox.setItems(menuItemsList); }
        catch (SQLException e)
        { showAlert("Помилка завантаження меню", e.getMessage());
            e.printStackTrace();}
    }

    //завантаження всіх столиків з бд
    private void loadTables() { if (orderService != null)
        try { tablesList.setAll(orderService.getAllTables());
            tableComboBox.setItems(tablesList);
        }
        catch (SQLException e)
        { showAlert("Помилка завантаження столів", e.getMessage());
            e.printStackTrace();}
    }

    //завантаження всіх клієнтів з бд
    private void loadClients() { if (orderService != null)
        try { clientsList.setAll(orderService.getAllClients());
            clientComboBox.setItems(clientsList);
        } catch
        (SQLException e)
        { showAlert("Помилка завантаження клієнтів", e.getMessage());
            e.printStackTrace();}
    }

    //завантаження всіх офіціантів з бд
    private void loadEmployees() {
        if (orderService != null)
            try { employeesList.setAll(orderService.getAllEmployees("Офіціант"));
                employeeComboBox.setItems(employeesList);
            } catch (SQLException e) {
                showAlert("Помилка завантаження співробітників", e.getMessage());
                e.printStackTrace();}
    }

    // відображення детелей замовлення
    private void displayOrderDetails(Order order) {
        if (order == null) return;
        isProgrammaticallyUpdating.set(true);
        try {
            tableComboBox.getSelectionModel().select(order.getTable());
            clientComboBox.getSelectionModel().select(order.getClient());
            employeeComboBox.getSelectionModel().select(order.getEmployee());
            paymentMethodChoiceBox.setValue(order.getPaymentMethod());
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", order.getTotalAmount()));
        } finally {
            isProgrammaticallyUpdating.set(false);
        }
    }

    // очищення полів вводу для замовлень
    private void clearOrderInputFields() {
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
    }
    private void clearOrderItemInputFields() {
        orderItemTable.getSelectionModel().clearSelection();
        menuItemComboBox.getSelectionModel().clearSelection();
        orderItemQuantityField.clear();
    }

    //очищення полів вводу для клієнтів
    private void clearNewClientInputFields() {
        newClientFirstNameField.clear(); newClientLastNameField.clear(); newClientPhoneField.clear();
        newClientEmailField.clear(); newClientLoyaltyPointsField.clear();
    }

    //перераховує та відображає загальну суму замовлення (з врахуванням бонусів)
    private void recalculateAndDisplayTotalWithPotentialBonuses() {
        Client currentClientInUI = clientComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();
        double baseTotalFromItems = currentOrderItemsList.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPriceAtOrder())
                .sum();

        if ("Бонуси".equals(paymentMethod) && currentClientInUI != null) {
            if (currentOrderAppliedBonusDiscount == 0.0 && baseTotalFromItems > 0) {
                double pointsAvailable = currentClientInUI.getLoyaltyPoints();
                currentOrderAppliedBonusDiscount = Math.min(baseTotalFromItems, pointsAvailable);
            }
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", Math.max(0, baseTotalFromItems - currentOrderAppliedBonusDiscount)));
        } else {
            totalAmountLabel.setText(String.format("Загальна сума: %.2f", baseTotalFromItems));
        }
    }

    @FXML
    private void handleCreateNewOrder() {
        if (orderService == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!"); return; }
        Table table = tableComboBox.getSelectionModel().getSelectedItem();
        Client client = clientComboBox.getSelectionModel().getSelectedItem();
        Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();

        if ((table == null && client == null)) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть столик або клієнта."); return; }
        if (employee == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть співробітника."); return; }

        try {
            Order newOrder = new Order(0, LocalDateTime.now(),
                    table != null ? table.getId() : null,
                    client != null ? client.getId() : null,
                    employee.getId(), paymentMethod, 0.0);
            newOrder.setTable(table); newOrder.setClient(client); newOrder.setEmployee(employee);
            currentOrderAppliedBonusDiscount = 0.0;

            Order createdOrder = orderService.createOrder(newOrder, 0.0);

            masterOrdersList.add(createdOrder);
            final Order finalCreatedOrder = createdOrder;
            Platform.runLater(() -> {
                isProgrammaticallyUpdating.set(true);
                try { orderTable.getSelectionModel().select(finalCreatedOrder); }
                finally { isProgrammaticallyUpdating.set(false); }
            });

            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Замовлення ID " + createdOrder.getId() + " створено. Додайте страви.");

            if (client != null && "Картка".equals(paymentMethod)) loadClients(); // оновити бали, якщо були нараховані
        } catch (SQLException e) { showAlert("Помилка створення замовлення", e.getMessage()); e.printStackTrace(); }
    }

    @FXML
    private void handleUpdateOrder() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null || selectedOrder.getId() == 0) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Виберіть існуюче замовлення."); return;
        }
        if (orderService == null) { messageLabel.setTextFill(Color.RED); messageLabel.setText("Сервіс недоступний."); return; }

        Table table = tableComboBox.getSelectionModel().getSelectedItem();
        Client client = clientComboBox.getSelectionModel().getSelectedItem();
        Employee employee = employeeComboBox.getSelectionModel().getSelectedItem();
        String paymentMethod = paymentMethodChoiceBox.getValue();

        if ((table == null && client == null)) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть столик або клієнта."); return; }
        if (employee == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть співробітника."); return; }

        double baseTotalFromItems = currentOrderItemsList.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPriceAtOrder())
                .sum();

        double discountToCommit = 0.0;
        if ("Бонуси".equals(paymentMethod) && client != null) {
            Client clientFromUI = clientComboBox.getSelectionModel().getSelectedItem();
            if(clientFromUI != null) {
                double pointsAvailableNow = clientFromUI.getLoyaltyPoints();
                discountToCommit = Math.min(baseTotalFromItems, pointsAvailableNow);
                discountToCommit = Math.min(currentOrderAppliedBonusDiscount, discountToCommit); // Не більше "зафіксованої"
            }
        }

        try {
            selectedOrder.setTotalAmount(baseTotalFromItems);
            selectedOrder.setTable(table); selectedOrder.setClient(client);
            selectedOrder.setEmployee(employee); selectedOrder.setPaymentMethod(paymentMethod);

            orderService.updateOrder(selectedOrder, discountToCommit);

            Order updatedOrderFromDB = orderService.getOrderById(selectedOrder.getId());

            if (updatedOrderFromDB != null) {
                if("Бонуси".equals(updatedOrderFromDB.getPaymentMethod())){
                    currentOrderAppliedBonusDiscount = baseTotalFromItems - updatedOrderFromDB.getTotalAmount();
                } else {
                    currentOrderAppliedBonusDiscount = 0.0;
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
                    } finally {
                        isProgrammaticallyUpdating.set(false);
                    }
                });
                loadClients();
                messageLabel.setTextFill(Color.GREEN);
                messageLabel.setText("Замовлення ID " + selectedOrder.getId() + " оновлено.");
            }
        } catch (SQLException e) { showAlert("Помилка оновлення", e.getMessage()); e.printStackTrace(); }
    }

    @FXML
    private void handleAddOrUpdateOrderItem() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть замовлення."); return; }
        if (selectedOrder.getId() == 0) { messageLabel.setTextFill(Color.ORANGE); messageLabel.setText("Збережіть нове замовлення перед додаванням страв."); return; }
        if (orderService == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс недоступний."); return; }

        MenuItem selectedMenuItem = menuItemComboBox.getSelectionModel().getSelectedItem();
        String qtyText = orderItemQuantityField.getText().trim();
        if (selectedMenuItem == null || qtyText.isEmpty()) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Оберіть страву та кількість."); return; }

        try {
            int quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Кількість > 0."); return; }

            OrderItem existingOrderItem = null;
            for(OrderItem item : currentOrderItemsList){
                if(item.getMenuItemId() == selectedMenuItem.getId() && item.getOrderId() == selectedOrder.getId()){
                    existingOrderItem = item; break;
                }
            }

            if (existingOrderItem != null) {
                existingOrderItem.setQuantity(quantity);
                existingOrderItem.setPriceAtOrder(selectedMenuItem.getPrice());
                orderService.updateOrderItem(existingOrderItem);
                int idx = currentOrderItemsList.indexOf(existingOrderItem);
                if(idx != -1) currentOrderItemsList.set(idx, existingOrderItem);
            } else {
                OrderItem newOrderItem = new OrderItem(0, selectedOrder.getId(), selectedMenuItem.getId(), quantity, selectedMenuItem.getPrice());
                newOrderItem.setMenuItem(selectedMenuItem);
                OrderItem createdOrderItem = orderService.createOrderItem(newOrderItem);
                currentOrderItemsList.add(createdOrderItem);
            }
            orderItemTable.refresh();
            recalculateAndDisplayTotalWithPotentialBonuses();
            clearOrderItemInputFields();
        } catch (NumberFormatException e) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Невірний формат кількості.");}
        catch (SQLException e) { showAlert("Помилка позиції", "Помилка при роботі з позицією: " + e.getMessage()); e.printStackTrace();}
    }

    @FXML
    private void handleRemoveOrderItem() {
        OrderItem selectedOrderItem = orderItemTable.getSelectionModel().getSelectedItem();
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrderItem == null || selectedOrder == null || orderService == null) { return; }

           try {
                orderService.deleteOrderItem(selectedOrderItem.getId());
                currentOrderItemsList.remove(selectedOrderItem);
                orderItemTable.refresh();
                recalculateAndDisplayTotalWithPotentialBonuses();
                clearOrderItemInputFields();
            } catch (SQLException e) { showAlert("Помилка видалення позиції", e.getMessage()); e.printStackTrace(); }
    }

    @FXML
    private void handleDeleteOrder() {
        if (orderService == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!"); return; }
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Виберіть замовлення для видалення."); return;
        }
          try {
                orderService.deleteOrder(selectedOrder.getId());
                masterOrdersList.remove(selectedOrder);
                if (orderTable.getItems().isEmpty()) clearOrderInputFields();
                else orderTable.getSelectionModel().clearSelection();
                messageLabel.setTextFill(Color.PINK); messageLabel.setText("Замовлення ID " + selectedOrder.getId() + " видалено.");
                loadClients();
            } catch (SQLException e) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Помилка видалення: " + e.getMessage()); e.printStackTrace(); }
    }

    @FXML
    private void handleClearOrderSelection() {
        clearOrderInputFields();
        clearOrderItemInputFields();
    }

    @FXML
    private void handleAddClient() {
        if (orderService == null) { messageLabel.setTextFill(Color.PINK); messageLabel.setText("Сервіс не ініціалізований!"); return; }
        String firstName = newClientFirstNameField.getText().trim();
        String lastName = newClientLastNameField.getText().trim();

        String phoneNumber = newClientPhoneField.getText().trim();
        String email = newClientEmailField.getText().trim();
        String loyaltyPointsText = newClientLoyaltyPointsField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Ім'я та прізвище обов'язкові.");
            return;
        }
        try {
            double loyaltyPoints = 0.0;
            if (!loyaltyPointsText.isEmpty()) {
                try {
                    loyaltyPoints = Double.parseDouble(loyaltyPointsText);
                    if (loyaltyPoints < 0) loyaltyPoints = 0;
                } catch (NumberFormatException e) {
                    messageLabel.setTextFill(Color.PINK); messageLabel.setText("Невірний формат бонусів.");
                    return;
                }
            }
            Client newClient = new Client(0, firstName, lastName, phoneNumber, email, loyaltyPoints);
            Client createdClient = orderService.addClient(newClient);
            loadClients();
            clearNewClientInputFields();
            messageLabel.setTextFill(Color.PINK);
            messageLabel.setText("Клієнт '" + createdClient.getFirstName() + " " + createdClient.getLastName() + "' доданий.");
        } catch (SQLException e) {
            messageLabel.setTextFill(Color.PINK); messageLabel.setText("Помилка додавання клієнта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //відображення вікна для помилок/підтвердження дій
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        return alert.showAndWait();
    }
}