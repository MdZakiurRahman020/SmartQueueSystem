package com.example.smartqueuesystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

/**
 * Entry point for the Hospital Queue Management System.
 * Loads the JavaFX UI and initializes the main controller.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load UI from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UI.fxml"));
            BorderPane root = loader.load();

            // Create scene with appropriate sizing
            Scene scene = new Scene(root, 1000, 600);

            primaryStage.setTitle("🏥 Smart Hospital Queue System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("❌ Failed to load UI.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
