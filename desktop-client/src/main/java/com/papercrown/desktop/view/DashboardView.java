package com.papercrown.desktop.view;

import com.papercrown.desktop.component.*;
import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.util.AudioManager;
import com.papercrown.desktop.viewmodel.DashboardViewModel;
import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.dto.StatsDTO;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class DashboardView extends VBox {

    private final DashboardViewModel vm;
    private final AudioManager audioManager;
    private final Consumer<Long> onNavigateToPlay;
    private final Runnable onNavigateToDashboard;
    private final ObservableBooleanValue animationEnabled;

    // layout roots
    private StackPane rootStack;
    private VBox content;
    private VBox discardConfirmModal;
    private Label errorBanner;
    private VBox loadingOverlay;
    private VBox analyticsSection;
    private Label analyticsToggle;
    private FlowPane chartRow;

    // pre-built stat cards (updated in-place, never rebuilt)
    private StatCard winsCard;
    private StatCard lossesCard;
    private StatCard drawsCard;
    private StatCard winRateCard;
    private StatCard streakCard;

    // action area
    private FlowPane actions;
    private Label playBtn;

    // recent runs / achievements
    private VBox recentRunsArea;
    private Label noRunsLabel;
    private FlowPane achievementsArea;
    private Label noAchievementsLabel;

    // animation gate
    private boolean entranceAnimationsPlayed = false;
    private boolean chartsBuilt = false;

    public DashboardView(BackendClient client, AudioManager audioManager,
                         Consumer<Long> onNavigateToPlay, Runnable onNavigateToDashboard,
                         ObservableBooleanValue animationEnabled) {
        this.vm = new DashboardViewModel(client);
        this.audioManager = audioManager;
        this.onNavigateToPlay = onNavigateToPlay;
        this.onNavigateToDashboard = onNavigateToDashboard;
        this.animationEnabled = animationEnabled;

        getStyleClass().add("page-view");

        // ─── title ────────────────────────────────────────────────
        Label title = new Label("War Table");
        title.getStyleClass().add("page-title");

        // ─── action bar ───────────────────────────────────────────
        actions = new FlowPane(12, 12);
        actions.setPadding(new Insets(12, 0, 8, 0));

        // ─── stats row (pre-built cards, updated in-place) ────────
        winsCard = new StatCard(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TROPHY,
                "Victories", "gold");
        Tooltip.install(winsCard, createTooltip("Total matches won across all campaigns."));
        lossesCard = new StatCard(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SKULL,
                "Defeats", "red");
        Tooltip.install(lossesCard, createTooltip("Total matches lost across all campaigns."));
        drawsCard = new StatCard(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HAND_PAPER,
                "Stalemates", "purple");
        Tooltip.install(drawsCard, createTooltip("Total matches ending in a draw."));
        winRateCard = new StatCard(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PERCENTAGE,
                "Victory Rate", "gold");
        Tooltip.install(winRateCard, createTooltip("Percentage of matches won."));
        streakCard = new StatCard(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FIRE,
                "Best Streak", "blue");
        Tooltip.install(streakCard, createTooltip("Your longest winning streak."));

        FlowPane statsRow = new FlowPane(16, 16);
        statsRow.getChildren().addAll(winsCard, lossesCard, drawsCard, winRateCard, streakCard);

        // ─── recent runs section ──────────────────────────────────
        Label recentLabel = new Label("Campaign History");
        recentLabel.getStyleClass().add("section-title");
        noRunsLabel = new Label("No campaigns yet. Begin your first!");
        noRunsLabel.getStyleClass().add("empty-state");
        recentRunsArea = new VBox(8);

        // ─── achievements section ─────────────────────────────────
        Label achievementsLabel = new Label("Trophies & Deeds");
        achievementsLabel.getStyleClass().add("section-title");
        noAchievementsLabel = new Label("No trophies yet. Keep fighting!");
        noAchievementsLabel.getStyleClass().add("empty-state");
        achievementsArea = new FlowPane(12, 12);

        // ─── analytics (charts, collapsible) ──────────────────────
        analyticsToggle = new Label("\u25B6 Scouting Reports");
        analyticsToggle.getStyleClass().add("analytics-toggle");
        analyticsToggle.setOnMouseClicked(e -> toggleAnalytics());
        chartRow = new FlowPane(16, 16);
        analyticsSection = new VBox(8);
        analyticsSection.getChildren().addAll(analyticsToggle, chartRow);
        chartRow.setVisible(false);
        chartRow.setManaged(false);

        // ─── scrollable content ───────────────────────────────────
        content = new VBox(16);
        content.setPadding(new Insets(32));

        // Build initial children list
        content.getChildren().addAll(
                title,
                actions,              // CTA row
                statsRow,             // stats
                recentLabel,
                noRunsLabel,
                recentRunsArea,       // campaign history
                achievementsLabel,
                noAchievementsLabel,
                achievementsArea,     // trophies & deeds
                analyticsSection      // charts
        );

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setContent(content);

        // ─── error banner ─────────────────────────────────────────
        errorBanner = new Label();
        errorBanner.getStyleClass().add("error-banner");
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);

        // ─── loading overlay ──────────────────────────────────────
        loadingOverlay = createLoadingOverlay();
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);

        // ─── discard confirm modal ────────────────────────────────
        discardConfirmModal = createDiscardConfirmModal();
        discardConfirmModal.setVisible(false);
        discardConfirmModal.setManaged(false);

        rootStack = new StackPane();
        rootStack.getChildren().addAll(scrollPane, errorBanner, loadingOverlay, discardConfirmModal);
        StackPane.setAlignment(errorBanner, Pos.TOP_CENTER);
        StackPane.setMargin(errorBanner, new Insets(12, 32, 0, 32));

        getChildren().add(rootStack);

        // ─── data listeners ───────────────────────────────────────
        vm.stats.addListener((obs, old, val) -> {
            if (val != null) Platform.runLater(() -> updateStats(val));
        });
        vm.recentRuns.addListener((obs, old, val) -> {
            if (val != null) Platform.runLater(() -> updateRecentRuns(val));
        });
        vm.achievements.addListener((obs, old, val) -> {
            if (val != null) Platform.runLater(() -> updateAchievements(val));
        });
        vm.unfinishedRun.addListener((obs, old, val) -> {
            Platform.runLater(() -> updateUnfinishedRun(val));
        });
        vm.error.addListener((obs, old, val) -> {
            if (val) Platform.runLater(this::showErrorBanner);
            else Platform.runLater(this::hideErrorBanner);
        });
        vm.loading.addListener((obs, old, val) -> {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(val);
                loadingOverlay.setManaged(val);
            });
        });

        // ─── keyboard shortcuts ───────────────────────────────────
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });

        // ─── initial load ─────────────────────────────────────────
        createActionButtons();
        vm.load();
    }

    // ═══════════════════════════════════════════════════════════════
    // Keyboard shortcuts
    // ═══════════════════════════════════════════════════════════════

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            if (discardConfirmModal.isVisible()) {
                audioManager.play("click");
                hideDiscardConfirmModal();
                event.consume();
            }
        }
        if (event.getCode() == KeyCode.ENTER) {
            // trigger primary action
            firePrimaryAction();
            event.consume();
        }
    }

    private void firePrimaryAction() {
        RunDTO unfinished = vm.unfinishedRun.get();
        if (unfinished != null) {
            audioManager.play("click");
            onNavigateToPlay.accept(unfinished.getId());
        } else {
            audioManager.play("click");
            vm.startNewRun(runId -> {
                if (runId != null) onNavigateToPlay.accept(runId);
                else Toast.show(rootStack, "Failed to begin campaign. Please try again.", Toast.Type.ERROR);
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Action buttons
    // ═══════════════════════════════════════════════════════════════

    private void createActionButtons() {
        actions.getChildren().clear();

        playBtn = new Label("Begin Campaign");
        playBtn.getStyleClass().addAll("action-button", "button-primary");
        playBtn.setGraphic(new FontIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CROWN));
        playBtn.setGraphicTextGap(10);

        RunDTO unfinished = vm.unfinishedRun.get();
        if (unfinished != null) {
            // show resume as primary, play becomes "discard & restart"
            Label resumeBtn = new Label("Continue Campaign");
            resumeBtn.getStyleClass().addAll("action-button", "button-primary");
            resumeBtn.setGraphic(new FontIcon(org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PLAY_CIRCLE));
            resumeBtn.setGraphicTextGap(10);
            resumeBtn.setOnMouseClicked(e -> {
                audioManager.play("click");
                onNavigateToPlay.accept(unfinished.getId());
            });
            Tooltip tip = createTooltip("Resume your ongoing campaign (Round " + unfinished.getRoundNumber() + ").");
            resumeBtn.setTooltip(tip);

            playBtn.getStyleClass().clear();
            playBtn.getStyleClass().addAll("action-button", "button-secondary");
            playBtn.setOnMouseClicked(e -> {
                audioManager.play("click");
                showDiscardConfirmModal(unfinished);
            });
            playBtn.setTooltip(createTooltip("Discard the current campaign and begin a new one."));

            actions.getChildren().addAll(resumeBtn, playBtn);
        } else {
            playBtn.setOnMouseClicked(e -> firePrimaryAction());
            playBtn.setTooltip(createTooltip("Start a new campaign against the bot."));
            actions.getChildren().add(playBtn);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Loading overlay
    // ═══════════════════════════════════════════════════════════════

    private VBox createLoadingOverlay() {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("loading-overlay");

        VBox spinner = new VBox(12);
        spinner.setAlignment(Pos.CENTER);

        Label icon = new Label("\u2694");
        icon.getStyleClass().add("loading-spinner");

        Label text = new Label("Rallying troops...");
        text.getStyleClass().add("loading-text");

        spinner.getChildren().addAll(icon, text);
        overlay.getChildren().add(spinner);
        return overlay;
    }

    // ═══════════════════════════════════════════════════════════════
    // Error banner
    // ═══════════════════════════════════════════════════════════════

    private void showErrorBanner() {
        errorBanner.setText(vm.errorMessage.get());
        errorBanner.setVisible(true);
        errorBanner.setManaged(true);
        errorBanner.setOnMouseClicked(e -> {
            audioManager.play("click");
            vm.load(); // retry
        });
    }

    private void hideErrorBanner() {
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // Stats update (in-place, no rebuild)
    // ═══════════════════════════════════════════════════════════════

    private void updateStats(StatsDTO stats) {
        winsCard.setValue(stats.getTotalWins());
        lossesCard.setValue(stats.getTotalLosses());
        drawsCard.setValue(stats.getTotalDraws());
        winRateCard.setValue(String.format("%.1f%%", stats.getWinRate()));
        streakCard.setValue(stats.getBestStreak());

        // charts: build once on first data, update datasets thereafter
        if (!chartsBuilt) {
            buildCharts(stats);
            chartsBuilt = true;
        }

        // entrance animation: only on first load
        if (!entranceAnimationsPlayed) {
            animateCardEntrance();
            entranceAnimationsPlayed = true;
        }

        createActionButtons();
    }

    private void buildCharts(StatsDTO stats) {
        chartRow.getChildren().clear();
        chartRow.getChildren().addAll(
                createMoveUsageChart(stats.getMoveUsage()),
                createRunLengthChart()
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // Recent runs update
    // ═══════════════════════════════════════════════════════════════

    private void updateRecentRuns(List<RunDTO> runs) {
        noRunsLabel.setVisible(runs.isEmpty());
        noRunsLabel.setManaged(runs.isEmpty());
        recentRunsArea.getChildren().clear();
        for (RunDTO run : runs) {
            recentRunsArea.getChildren().add(new RunCard(run));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Achievements update (all states: locked / progress / unlocked)
    // ═══════════════════════════════════════════════════════════════

    private void updateAchievements(List<AchievementDTO> achievements) {
        noAchievementsLabel.setVisible(achievements.isEmpty());
        noAchievementsLabel.setManaged(achievements.isEmpty());
        achievementsArea.getChildren().clear();
        for (var a : achievements) {
            achievementsArea.getChildren().add(new AchievementCard(a));
        }
    }

    private void updateUnfinishedRun(RunDTO run) {
        createActionButtons();
    }

    // ═══════════════════════════════════════════════════════════════
    // Card entrance animation (plays ONCE)
    // ═══════════════════════════════════════════════════════════════

    private void animateCardEntrance() {
        if (!animationEnabled.get()) return;
        // stats row is at index 2 in content children (after title[0], actions[1])
        FlowPane statsRow = (FlowPane) content.getChildren().get(2);
        for (int i = 0; i < statsRow.getChildren().size(); i++) {
            var card = statsRow.getChildren().get(i);
            card.setTranslateY(30);
            card.setOpacity(0);
            TranslateTransition tt = new TranslateTransition(
                    Duration.millis(300).multiply(1 + i * 0.15), card);
            tt.setToY(0);
            FadeTransition ft = new FadeTransition(
                    Duration.millis(300).multiply(1 + i * 0.15), card);
            ft.setToValue(1);
            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.setDelay(Duration.millis(i * 80));
            pt.play();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Charts (collapsible)
    // ═══════════════════════════════════════════════════════════════

    private void toggleAnalytics() {
        boolean visible = !chartRow.isVisible();
        chartRow.setVisible(visible);
        chartRow.setManaged(visible);
        analyticsToggle.setText(visible ? "\u25BC Scouting Reports" : "\u25B6 Scouting Reports");
    }

    private ChartContainer createMoveUsageChart(Map<String, Integer> moveUsage) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        if (moveUsage != null && !moveUsage.isEmpty()) {
            for (Map.Entry<String, Integer> entry : moveUsage.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        } else {
            dataset.setValue("No data", 1);
        }
        JFreeChart chart = ChartFactory.createPieChart("Move Usage", dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("ROCK", new java.awt.Color(212, 160, 106));
        plot.setSectionPaint("PAPER", new java.awt.Color(106, 154, 212));
        plot.setSectionPaint("SCISSORS", new java.awt.Color(196, 106, 154));
        return new ChartContainer(chart);
    }

    private ChartContainer createRunLengthChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<RunDTO> runs = vm.recentRuns.get();
        if (runs != null) {
            for (int i = 0; i < runs.size(); i++) {
                dataset.addValue(runs.get(i).getRoundNumber(), "Rounds", "Run " + (i + 1));
            }
        }
        JFreeChart chart = ChartFactory.createBarChart("Run Length History", "Run", "Rounds", dataset);
        return new ChartContainer(chart);
    }

    // ═══════════════════════════════════════════════════════════════
    // Discard confirm modal
    // ═══════════════════════════════════════════════════════════════

    private VBox createDiscardConfirmModal() {
        VBox modal = new VBox();
        modal.setAlignment(Pos.CENTER);
        modal.getStyleClass().add("modal-overlay");

        VBox card = new VBox(20);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);

        Label icon = new Label();
        icon.setGraphic(
                new org.kordamp.ikonli.javafx.FontIcon(
                        org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EXCLAMATION_TRIANGLE));
        icon.getGraphic().setStyle("-fx-icon-size: 36; -fx-icon-color: #c9a84c;");

        Label title = new Label("Abandon Campaign?");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        Label desc = new Label(
                "You have an unfinished campaign. Beginning a new one will abandon your current progress.");
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #8888a0; -fx-alignment: center;");
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Label cancelBtn = new Label("Return to Battle");
        cancelBtn.getStyleClass().addAll("action-button", "button-secondary");
        cancelBtn.setOnMouseClicked(e -> {
            audioManager.play("click");
            hideDiscardConfirmModal();
        });

        Label confirmBtn = new Label("Abandon & Begin New");
        confirmBtn.getStyleClass().addAll("action-button", "button-primary");
        confirmBtn.setStyle(confirmBtn.getStyle()
                + "-fx-background-color: #e65c6c;");

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        card.getChildren().addAll(icon, title, desc, buttons);
        modal.getChildren().add(card);

        confirmBtn.setOnMouseClicked(e -> {
            audioManager.play("click");
            hideDiscardConfirmModal();
            vm.discardAndStartNew(runId -> {
                if (runId != null) {
                    onNavigateToPlay.accept(runId);
                } else {
                    Toast.show(rootStack,
                            "Failed to begin campaign. Please try again.", Toast.Type.ERROR);
                    vm.load(); // refresh to recover state
                }
            });
        });

        return modal;
    }

    private void showDiscardConfirmModal(RunDTO unfinished) {
        discardConfirmModal.setVisible(true);
        discardConfirmModal.setManaged(true);
        if (animationEnabled.get()) {
            discardConfirmModal.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), discardConfirmModal);
            ft.setToValue(1);
            ft.play();
        }
    }

    private void hideDiscardConfirmModal() {
        if (animationEnabled.get()) {
            FadeTransition ft = new FadeTransition(Duration.millis(150), discardConfirmModal);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                discardConfirmModal.setVisible(false);
                discardConfirmModal.setManaged(false);
            });
            ft.play();
        } else {
            discardConfirmModal.setVisible(false);
            discardConfirmModal.setManaged(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Utilities
    // ═══════════════════════════════════════════════════════════════

    private static Tooltip createTooltip(String text) {
        Tooltip tip = new Tooltip(text);
        tip.getStyleClass().add("game-tooltip");
        return tip;
    }
}
