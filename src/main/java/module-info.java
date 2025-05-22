module org.example.restaurant_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;


    opens org.example.restaurant_management_system to javafx.fxml;
    exports org.example.restaurant_management_system;
    //exports org.example.restaurant_management_system.model;
    //exports org.example.restaurant_management_system.service;
    //exports org.example.restaurant_management_system.util;
    //exports org.example.restaurant_management_system.view; // Можливо, вам також знадобиться експортувати цей пакет
    opens org.example.restaurant_management_system.controller to javafx.fxml;
    exports org.example.restaurant_management_system.util;
    opens org.example.restaurant_management_system.util to javafx.fxml; // Додайте цей рядок
}