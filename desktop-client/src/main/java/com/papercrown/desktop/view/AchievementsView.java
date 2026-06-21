package com.papercrown.desktop.view;

import com.papercrown.desktop.component.AchievementCard;
import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.viewmodel.AchievementsViewModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class AchievementsView extends VBox {

    private final AchievementsViewModel vm;
    private FlowPane grid;
    private Label emptyLabel;

    public AchievementsView(BackendClient client) {
        this.vm = new AchievementsViewModel(client);

        getStyleClass().add("page-view");

        Label title = new Label("Achievements");
        title.getStyleClass().add("page-title");

        emptyLabel = new Label("No achievements yet.");
        emptyLabel.getStyleClass().add("empty-state");

        grid = new FlowPane(12, 12);
        grid.setPadding(new Insets(8, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        VBox content = new VBox(16);
        content.setPadding(new Insets(32));
        content.getChildren().addAll(title, emptyLabel, grid);
        scrollPane.setContent(content);

        getChildren().add(scrollPane);

        vm.achievements.addListener((obs, old, val) -> {
            Platform.runLater(() -> {
                grid.getChildren().clear();
                boolean empty = val == null || val.isEmpty();
                emptyLabel.setVisible(empty);
                emptyLabel.setManaged(empty);
                if (val != null) {
                    for (var a : val) {
                        grid.getChildren().add(new AchievementCard(a));
                    }
                }
            });
        });

        vm.load();
    }
}
