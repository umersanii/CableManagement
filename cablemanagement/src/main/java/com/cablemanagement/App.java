package com.cablemanagement;

import com.cablemanagement.views.signin_page;
import com.cablemanagement.views.home_page;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        // initialize database
        config.database.connect(null, null, null);

        // Launch JavaFX application
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load home page temporarily
        com.cablemanagement.views.home_page.getHomeScene();
        primaryStage.setScene(com.cablemanagement.views.home_page.getHomeScene());
        primaryStage.setTitle("Cable Management - Home");
        primaryStage.show();
    }
}