package com.papercrown.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PaperCrownApp extends Application {

    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 768;

    @Override
    public void start(Stage primaryStage) {
        var mainView = new com.papercrown.desktop.view.MainView(primaryStage);
        Scene scene = new Scene(mainView, 1280, 800);

        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

        primaryStage.setTitle("Paper Crown");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
