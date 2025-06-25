module com.example.uiuonepoint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires mysql.connector.j;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires com.google.gson;
    requires okhttp3;
    requires java.desktop;

    opens com.example.uiuonepoint to javafx.fxml;
    opens com.example.uiuonepoint.controller to javafx.fxml;
    opens com.example.uiuonepoint.model to javafx.base;
    opens com.example.uiuonepoint.dao to javafx.base;
    opens com.example.uiuonepoint.util to javafx.base;

    exports com.example.uiuonepoint;
    exports com.example.uiuonepoint.controller;
    exports com.example.uiuonepoint.model;
    exports com.example.uiuonepoint.dao;
    exports com.example.uiuonepoint.util;
}