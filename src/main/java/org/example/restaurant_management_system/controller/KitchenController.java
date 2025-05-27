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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KitchenController {

    private static final Logger LOGGER = LogManager.getLogger(KitchenController.class);

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
    private Timeline timerTimeline;

    @FXML
    public void initialize() {
        LOGGER.info("Ініціалізація KitchenController.");
        kitchenService = new KitchenService();
        observableKitchenTasks = FXCollections.observableArrayList();

        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        menuItemCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("cookingStatus"));
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        timerCol.setCellValueFactory(new PropertyValueFactory<>("timerDisplay"));

        statusFilterComboBox.setItems(FXCollections.observableArrayList("Всі статуси", "В очікуванні", "В роботі", "Готово", "Скасовано"));
        statusFilterComboBox.setValue("Всі статуси");
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            LOGGER.debug("Змінено фільтр статусу з '{}' на '{}'.", oldVal, newVal);
            applyFiltersAndSorting();
        });

        sortByComboBox.setItems(FXCollections.observableArrayList("Пріоритет", "Час замовлення (від старого)",
                "Час замовлення (від нового)"));
        sortByComboBox.setValue("Пріоритет");
        sortByComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            LOGGER.debug("Змінено сортування з '{}' на '{}'.", oldVal, newVal);
            applyFiltersAndSorting();
        });

        kitchenTasksTableView.setItems(observableKitchenTasks);

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button statusButton = new Button("Почати");
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
                                newStatus = "Скасовано";
                                break;
                            case "Скасовано":
                                newStatus = "В очікуванні";
                                break;
                            default:
                                newStatus = "В очікуванні";
                        }
                        LOGGER.info("Спроба змінити статус завдання {} з '{}' на '{}'.",
                                task.getId(), currentStatus, newStatus);

                        if (kitchenService.updateKitchenTaskStatus(task.getId(), newStatus)) {
                            task.setCookingStatus(newStatus);
                            if ("В роботі".equals(newStatus)) {
                                task.setStartCookingTime(LocalDateTime.now());
                                LOGGER.debug("Встановлено час початку готування для завдання {}.",
                                        task.getId());

                            } else if ("Готово".equals(newStatus) || "Скасовано".equals(newStatus)) {
                                task.setEndCookingTime(LocalDateTime.now());
                                LOGGER.debug("Встановлено час завершення готування для завдання {}.",
                                        task.getId());

                            } else if ("В очікуванні".equals(newStatus) && "Скасовано".equals(currentStatus)) {
                                task.setStartCookingTime(null);
                                task.setEndCookingTime(null);
                                LOGGER.debug("Скинуто таймер для завдання {} після відновлення.",
                                        task.getId());

                            }
                            loadKitchenTasks();
                            LOGGER.info("Статус завдання {} успішно змінено на {}.",
                                    task.getId(), newStatus);

                        } else {
                            LOGGER.error("Не вдалося змінити статус завдання {} на {}.",
                                    task.getId(), newStatus);
                            System.err.println("Не вдалося змінити статус завдання " + task.getId());
                        }
                    }
                });

                priorityUpButton.setOnAction(event -> {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null && task.getPriority() > 1) {
                        LOGGER.info("Спроба збільшити пріоритет завдання {} з {} на {}.",
                                task.getId(), task.getPriority(), task.getPriority() - 1);

                        if (kitchenService.setKitchenTaskPriority(task.getId(), task.getPriority() - 1)) {
                            loadKitchenTasks();
                            LOGGER.info("Пріоритет завдання {} успішно змінено на {}.",
                                    task.getId(), task.getPriority() - 1);

                        } else {
                            LOGGER.error("Не вдалося збільшити пріоритет завдання {}.",
                                    task.getId());
                        }
                    }
                });

                priorityDownButton.setOnAction(event -> {
                    KitchenTask task = getTableView().getItems().get(getIndex());
                    if (task != null) {
                        LOGGER.info("Спроба зменшити пріоритет завдання {} з {} на {}.",
                                task.getId(), task.getPriority(), task.getPriority() + 1);

                        if (kitchenService.setKitchenTaskPriority(task.getId(), task.getPriority() + 1)) {
                            loadKitchenTasks();
                            LOGGER.info("Пріоритет завдання {} успішно змінено на {}.",
                                    task.getId(), task.getPriority() + 1);

                        } else {
                            LOGGER.error("Не вдалося зменшити пріоритет завдання {}.",
                                    task.getId());
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
                        priorityUpButton.setDisable("Готово".equals(task.getCookingStatus()) ||
                                "Скасовано".equals(task.getCookingStatus()));

                        priorityDownButton.setDisable("Готово".equals(task.getCookingStatus())
                                || "Скасовано".equals(task.getCookingStatus()));

                        HBox buttons = new HBox(10, statusButton, priorityUpButton, priorityDownButton);
                        buttons.setAlignment(Pos.CENTER);
                        setGraphic(buttons);
                    }
                }
            }

            private String getStatusButtonText(String status) {
                return switch (status) {
                    case "В очікуванні": yield "Почати";
                    case "В роботі": yield "Завершити";
                    case "Готово": yield "Завершено";
                    case "Скасовано": yield "Відновити";
                    default: yield "Змінити статус";
                };
            }
        });

        loadKitchenTasks();

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            kitchenTasksTableView.refresh();
        }));
        timerTimeline.setCycleCount(Animation.INDEFINITE);
        timerTimeline.play();
        LOGGER.info("Запущено таймлайн для оновлення таймерів.");
    }

    private void loadKitchenTasks() {
        LOGGER.info("Завантаження кухонних завдань.");

        System.out.println("KitchenController: Завантажую кухонні завдання...");
        List<KitchenTask> allTasksFromService = kitchenService.getAllKitchenTasks();
        System.out.println("KitchenController: Отримано " + allTasksFromService.size() +
                " завдань від KitchenService (до фільтрації).");

        observableKitchenTasks.setAll(allTasksFromService);
        System.out.println("KitchenController: observableKitchenTasks містить " +
                observableKitchenTasks.size() + " елементів перед застосуванням фільтрів.");

        applyFiltersAndSorting();
        System.out.println("KitchenController: Таблиця оновлена. observableKitchenTasks після фільтрації/сортування має "
                + observableKitchenTasks.size() + " елементів.");
        LOGGER.info("Кухонні завдання завантажено та застосовано фільтри/сортування.");
    }

    @FXML
    private void applyFiltersAndSorting() {
        LOGGER.info("Застосування фільтрів та сортування до кухонних завдань.");
        System.out.println("KitchenController: Застосовую фільтри та сортування...");

        List<KitchenTask> currentTasks = new ArrayList<>(kitchenService.getAllKitchenTasks());
        System.out.println("  KitchenController: Завдань отримано для фільтрації/сортування (з сервісу): "
                + currentTasks.size());


        String selectedStatus = statusFilterComboBox.getValue();
        System.out.println("  KitchenController: Фільтр за статусом: " + selectedStatus);
        if (selectedStatus != null && !selectedStatus.equals("Всі статуси")) {
            currentTasks.removeIf(task -> !task.getCookingStatus().equals(selectedStatus));
            LOGGER.debug("Завдань після фільтрації за статусом '{}': {}."
                    , selectedStatus, currentTasks.size());

        }
        System.out.println("  KitchenController: Завдань після фільтрації за статусом: "
                + currentTasks.size());


        String selectedSortBy = sortByComboBox.getValue();
        System.out.println("  KitchenController: Сортування за: "
                + selectedSortBy);
        if ("Пріоритет".equals(selectedSortBy)) {
            currentTasks.sort(Comparator.comparing(KitchenTask::getPriority));
            LOGGER.debug("Відсортовано за пріоритетом.");

        } else if ("Час замовлення (від старого)".equals(selectedSortBy)) {
            currentTasks.sort(Comparator.comparing(KitchenTask::getStartCookingTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            LOGGER.debug("Відсортовано за часом замовлення (від старого).");

        } else if ("Час замовлення (від нового)".equals(selectedSortBy)) {
            currentTasks.sort(Comparator.comparing(KitchenTask::getStartCookingTime,
                    Comparator.nullsFirst(Comparator.reverseOrder())));
            LOGGER.debug("Відсортовано за часом замовлення (від нового).");
        }
        System.out.println("  KitchenController: Завдань після сортування: "
                + currentTasks.size());

        observableKitchenTasks.setAll(currentTasks);
        System.out.println("  KitchenController: ObservableList оновлено, містить: "
                + observableKitchenTasks.size() + " елементів.");
        LOGGER.info("Фільтри та сортування успішно застосовано. Кількість завдань у таблиці: {}.", observableKitchenTasks.size());
    }

    public void updateTimers() {
        LOGGER.debug("Оновлення таймерів у таблиці.");
        kitchenTasksTableView.refresh();
    }
}