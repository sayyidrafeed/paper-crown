package com.papercrown.desktop.view;

import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.util.AudioManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainView extends BorderPane {

    private static final double SIDEBAR_WIDTH = 220;

    private final BackendClient backendClient = new BackendClient();
    private final AudioManager audioManager = new AudioManager();
    private final StackPane contentArea;
    private final StackPane rootStack;
    private final VBox healthOverlay;
    private final SidebarItem dashboardItem;
    private final SidebarItem playItem;
    private final SidebarItem historyItem;
    private final SidebarItem achievementsItem;
    private final SidebarItem settingsItem;
    private SidebarItem activeNavItem;
    private final Stage primaryStage;
    private final SimpleBooleanProperty animationEnabled = new SimpleBooleanProperty(true);
    private ScheduledExecutorService healthScheduler;

    public MainView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        rootStack = new StackPane();

        BorderPane mainLayout = new BorderPane();

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        VBox nav = (VBox) sidebar.getChildren().get(1);
        dashboardItem = (SidebarItem) nav.getChildren().get(0);
        playItem = (SidebarItem) nav.getChildren().get(1);
        historyItem = (SidebarItem) nav.getChildren().get(2);
        achievementsItem = (SidebarItem) nav.getChildren().get(3);
        settingsItem = (SidebarItem) nav.getChildren().get(4);

        contentArea = createContentArea();
        mainLayout.setCenter(contentArea);

        healthOverlay = createHealthOverlay();
        healthOverlay.setVisible(false);

        rootStack.getChildren().addAll(mainLayout, healthOverlay);
        setCenter(rootStack);

        startHealthCheck();
        showDashboard();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.setMinWidth(SIDEBAR_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_WIDTH);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setSpacing(8);

        Label title = new Label("Paper Crown");
        title.getStyleClass().add("sidebar-title");
        VBox.setMargin(title, new Insets(0, 0, 24, 0));

        VBox nav = new VBox();
        nav.setSpacing(4);

        SidebarItem dashboardItem = new SidebarItem("Dashboard", "mdi2v:view-dashboard");
        SidebarItem playItem = new SidebarItem("Play", "mdi2p:play-circle");
        SidebarItem historyItem = new SidebarItem("History", "mdi2h:history");
        SidebarItem achievementsItem = new SidebarItem("Achievements", "mdi2t:trophy");
        SidebarItem settingsItem = new SidebarItem("Settings", "mdi2c:cog");

        dashboardItem.setOnAction(e -> { audioManager.play("click"); showDashboard(); });
        playItem.setOnAction(e -> { audioManager.play("click"); showPlay(null); });
        historyItem.setOnAction(e -> { audioManager.play("click"); showHistory(); });
        achievementsItem.setOnAction(e -> { audioManager.play("click"); showAchievements(); });
        settingsItem.setOnAction(e -> { audioManager.play("click"); showSettings(); });

        dashboardItem.setActive(true);
        activeNavItem = dashboardItem;

        nav.getChildren().addAll(dashboardItem, playItem, historyItem, achievementsItem, settingsItem);

        sidebar.getChildren().addAll(title, nav);
        return sidebar;
    }

    private StackPane createContentArea() {
        StackPane area = new StackPane();
        area.getStyleClass().add("content-area");
        return area;
    }

    private VBox createHealthOverlay() {
        VBox overlay = new VBox(16);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(15,15,20,0.95);");

        Label title = new Label("Backend Not Running");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label desc = new Label("Start the backend with:\ndocker compose up\n./gradlew :backend-service:bootRun");
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #8888a0; -fx-alignment: center;");

        Button retryBtn = new Button("Retry Connection");
        retryBtn.setStyle("-fx-background-color: #c9a84c; -fx-text-fill: #0f0f14; -fx-padding: 12 24; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8px;");
        retryBtn.setOnAction(e -> checkHealth());

        overlay.getChildren().addAll(title, desc, retryBtn);
        return overlay;
    }

    private void startHealthCheck() {
        healthScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "health-check");
            t.setDaemon(true);
            return t;
        });
        checkHealth();
        healthScheduler.scheduleWithFixedDelay(this::checkHealth, 5, 5, TimeUnit.SECONDS);
    }

    private void checkHealth() {
        boolean ok = backendClient.isHealthy();
        Platform.runLater(() -> healthOverlay.setVisible(!ok));
    }

    private void setActiveNav(SidebarItem item) {
        if (activeNavItem != null) {
            activeNavItem.setActive(false);
        }
        activeNavItem = item;
        activeNavItem.setActive(true);
    }

    private void fadeInContent(javafx.scene.Node content) {
        content.setOpacity(0);
        contentArea.getChildren().setAll(content);
        if (animationEnabled.get()) {
            FadeTransition ft = new FadeTransition(Duration.millis(200), content);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } else {
            content.setOpacity(1);
        }
    }

    public void showDashboard() {
        setActiveNav(dashboardItem);
        fadeInContent(new DashboardView(backendClient, audioManager, this::showPlay, this::showDashboard, animationEnabled));
    }

    public void showPlay(Long runId) {
        setActiveNav(playItem);
        fadeInContent(new PlayView(backendClient, audioManager, runId, this::showDashboard, animationEnabled));
    }

    public void showHistory() {
        setActiveNav(historyItem);
        fadeInContent(new HistoryView(backendClient));
    }

    public void showAchievements() {
        setActiveNav(achievementsItem);
        fadeInContent(new AchievementsView(backendClient));
    }

    public void showSettings() {
        setActiveNav(settingsItem);
        fadeInContent(new SettingsView(backendClient, audioManager,
                val -> primaryStage.setFullScreen(val),
                val -> animationEnabled.set(val)));
    }
}
