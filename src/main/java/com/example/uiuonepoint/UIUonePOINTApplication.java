package com.example.uiuonepoint;

import com.example.uiuonepoint.util.ImageCache;
import com.example.uiuonepoint.util.OrderSchedulerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class UIUonePOINTApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UIUonePOINTApplication.class.getResource("login.fxml"));


        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
        stage.setTitle("UIUonePOINT - Integrated Management System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();


        OrderSchedulerService.getInstance().start();
    }

    @Override
    public void stop() throws Exception {

        OrderSchedulerService.getInstance().stop();


        ImageCache.shutdown();
        
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
} 