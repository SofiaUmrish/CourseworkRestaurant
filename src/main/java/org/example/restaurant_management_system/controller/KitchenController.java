package org.example.restaurant_management_system.controller;

import org.example.restaurant_management_system.model.KitchenTask;
import org.example.restaurant_management_system.service.KitchenService;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KitchenController {

    @FXML
    private TableView<KitchenTask> kitchenTasksTableView;
    @FXML
    private TableColumn<KitchenTask, Integer> orderIdCol;
    @FXML
    private TableColumn<KitchenTask, String> menuItemCol;
    @FXML
    private TableColumn<KitchenTask, Integer> quantityCol;
    @FXML
    private TableColumn<KitchenTask, String> statusCol;
    @FXML
    private TableColumn<KitchenTask, Integer> priorityCol;
    @FXML
    private TableColumn<KitchenTask, String> timerCol;
    @FXML
    private TableColumn<KitchenTask, Void> actionsCol;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> sortByComboBox;

    private KitchenService kitchenService;
    private ObservableList<KitchenTask> observableKitchenTasks;
    private Timeline timerTimeline; // таймлайн для оновлення таймерів

    @FXML
    public void initialize() {
        kitchenService = new KitchenService();
        observableKitchenTasks = FXCollections.observableArrayList();

        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        menuItemCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("cookingStatus"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        // timerCol оновлюється через  getTimerDisplay() в KitchenTask
        // і примусово кожну секунду через timerTimeline.
        timerCol.setCellValueFactory(new PropertyValueFactory<>("timerDisplay"));

        statusFilterComboBox.setItems(FXCollections.observableArrayList("Всі статуси", "В очікуванні", "В роботі", "Готово", "Скасовано"));
        statusFilterComboBox.setValue("Всі статуси");
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());

        sortByComboBox.setItems(FXCollections.observableArrayList("Пріоритет", "Час замовлення (від старого)", "Час замовлення (від нового)"));
        sortByComboBox.setValue("Пріоритет");
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSorting());

        kitchenTasksTableView.setItems(observableKitchenTasks);

        // колонки дій(зміни статусу, пріоритету)
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button statusButton = new Button("Взяти в роботу");
            private final Button priorityUpButton = new Button("↑");
            private final Button priorityDownButton = new Button("↓");

            {
                statusButton.setOnAction(event -> {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null) {
                        String currentStatus = task.getCookingStatus();
                        String newStatus;
                        switch (currentStatus) {
                            case "В очікуванні":
                                newStatus = "В роботі";
                                break;
                            case "В роботі":
                                newStatus = "Готово";
                                break;
                            case "Готово":
                                newStatus = "Скасовано"; // можливість скасувати навіть після "Готово"
                                break;
                            case "Скасовано": // для можливості відновити
                                newStatus = "В очікуванні";
                                break;
                            default:
                                newStatus = "В очікуванні"; // повернути до початкового стану
                        }
                        if (kitchenService.updateKitchenTaskStatus(task.getId(), newStatus)) {
                            // відображення змін
                            task.setCookingStatus(newStatus);
                            // якщо "В роботі", встановлюємо startCookingTime
                            if ("В роботі".equals(newStatus)) {
                                task.setStartCookingTime(LocalDateTime.now());
                            } else if ("Готово".equals(newStatus) || "Скасовано".equals(newStatus)) {
                                // якщо "Готово" або "Скасовано", встановлюємо endCookingTime
                                task.setEndCookingTime(LocalDateTime.now());
                            } else if ("В очікуванні".equals(newStatus) && "Скасовано".equals(currentStatus)) {
                                // при відновленні скасованого завдання скидається таймер
                                task.setStartCookingTime(null);
                                task.setEndCookingTime(null);
                            }

                            loadKitchenTasks();
                            System.out.println("Статус завдання " + task.getId() + " змінено на " + newStatus);
                        } else {
                            System.err.println("Не вдалося змінити статус завдання " + task.getId());
                        }
                    }
                });

                priorityUpButton.setOnAction(event -> {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null && task.getPriority() > 1) { // Приклад: мінімальний пріоритет 1
                        if (kitchenService.setKitchenTaskPriority(task.getId(), task.getPriority() - 1)) {
                            loadKitchenTasks(); // Перезавантажуємо для оновлення сортування
                        }
                    }
                });

                priorityDownButton.setOnAction(event -> {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null) {
                        // Перевіряємо, щоб пріоритет не перевищував певне максимальне значення, якщо потрібно
                        // наприклад, if (task.getPriority() < 5)
                        if (kitchenService.setKitchenTaskPriority(task.getId(), task.getPriority() + 1)) {
                            loadKitchenTasks(); // Перезавантажуємо для оновлення сортування
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null) {
                        statusButton.setText(getStatusButtonText(task.getCookingStatus()));
                        // Вмикаємо/вимикаємо кнопки пріоритету залежно від статусу
                        priorityUpButton.setDisable("Готово".equals(task.getCookingStatus()) || "Скасовано".equals(task.getCookingStatus()));
                        priorityDownButton.setDisable("Готово".equals(task.getCookingStatus()) || "Скасовано".equals(task.getCookingStatus()));

                        HBox buttons = new HBox(10, statusButton, priorityUpButton, priorityDownButton); // Збільшено відступ до 15px
                        buttons.setAlignment(Pos.CENTER); // Вирівнювання вмісту HBox по центру
                        setGraphic(buttons);
                    }
                }
            }

            private String getStatusButtonText(String status) {
                return switch (status) {
                    case "В очікуванні": yield "Взяти в роботу";
                    case "В роботі": yield "Завершити";
                    case "Готово": yield "Завершено"; // Текст, коли завдання виконано
                    case "Скасовано": yield "Відновити"; // Можливість відновити скасоване завдання
                    default: yield "Змінити статус";
                };
            }
        });

        // Завантаження завдань при ініціалізації
        loadKitchenTasks();

        // Запуск таймлайна для регулярного оновлення UI (кожної секунди)
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Просто оновлюємо всю таблицю, щоб PropertyValueFactory для timerCol перерахував значення
            kitchenTasksTableView.refresh();
        }));
        timerTimeline.setCycleCount(Animation.INDEFINITE);
        timerTimeline.play();
    }

    //завантаження завдань
    private void loadKitchenTasks() {
        System.out.println("KitchenController: Завантажую кухонні завдання...");
        List<KitchenTask> allTasksFromService = kitchenService.getAllKitchenTasks();
        System.out.println("KitchenController: Отримано " + allTasksFromService.size() + " завдань від KitchenService (до фільтрації).");
        observableKitchenTasks.setAll(allTasksFromService);
        System.out.println("KitchenController: observableKitchenTasks містить " + observableKitchenTasks.size() + " елементів перед застосуванням фільтрів.");

        applyFiltersAndSorting();
        System.out.println("KitchenController: Таблиця оновлена. observableKitchenTasks після фільтрації/сортування має " + observableKitchenTasks.size() + " елементів.");
    }

// застосування фольтрів і сортування
    @FXML
    private void applyFiltersAndSorting() {
        System.out.println("KitchenController: Застосовую фільтри та сортування...");

        // Отримуємо свіжий список з сервісу для фільтрації та сортування
        List<KitchenTask> currentTasks = new ArrayList<>(kitchenService.getAllKitchenTasks());
        System.out.println("  KitchenController: Завдань отримано для фільтрації/сортування (з сервісу): " + currentTasks.size());


        // 1. Фільтрація за статусом
        String selectedStatus = statusFilterComboBox.getValue();
        System.out.println("  KitchenController: Фільтр за статусом: " + selectedStatus);
        if (selectedStatus != null && !selectedStatus.equals("Всі статуси")) {
            currentTasks.removeIf(task -> !task.getCookingStatus().equals(selectedStatus));
        }
        System.out.println("  KitchenController: Завдань після фільтрації за статусом: " + currentTasks.size());


        // 2. Сортування
        String selectedSortBy = sortByComboBox.getValue();
        System.out.println("  KitchenController: Сортування за: " + selectedSortBy);
        if ("Пріоритет".equals(selectedSortBy)) {
            currentTasks.sort(Comparator.comparing(KitchenTask::getPriority));
        } else if ("Час замовлення (від старого)".equals(selectedSortBy)) {
            // Сортуємо за часом початку приготування, nullsLast для завдань "В очікуванні"
            currentTasks.sort(Comparator.comparing(KitchenTask::getStartCookingTime, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("Час замовлення (від нового)".equals(selectedSortBy)) {
            // Сортуємо за часом початку приготування, nullsFirst для завдань "В очікуванні"
            currentTasks.sort(Comparator.comparing(KitchenTask::getStartCookingTime, Comparator.nullsFirst(Comparator.reverseOrder())));
        }
        System.out.println("  KitchenController: Завдань після сортування: " + currentTasks.size());

        observableKitchenTasks.setAll(currentTasks);
        System.out.println("  KitchenController: ObservableList оновлено, містить: " + observableKitchenTasks.size() + " елементів.");
    }

    //оновлення таймера для Timeline
    public void updateTimers() {
        kitchenTasksTableView.refresh();
    }
}