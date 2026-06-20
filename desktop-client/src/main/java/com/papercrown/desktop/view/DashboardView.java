package com.papercrown.desktop.view;

import com.papercrown.desktop.component.*;
import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.util.AudioManager;
import com.papercrown.desktop.viewmodel.DashboardViewModel;
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
import javafx.scene.layout.*;
import javafx.util.Duration;

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
    private HBox statsRow;
    private VBox chartsArea;
    private VBox recentRunsArea;
    private HBox achievementsArea;
    private HBox actions;
    private Label noRunsLabel;
    private Label noAchievementsLabel;
    private Label resumeBtn;

    public DashboardView(BackendClient client, AudioManager audioManager, Consumer<Long> onNavigateToPlay, Runnable onNavigateToDashboard, ObservableBooleanValue animationEnabled) {
        this.vm = new DashboardViewModel(client);
        this.audioManager = audioManager;
        this.onNavigateToPlay = onNavigateToPlay;
        this.onNavigateToDashboard = onNavigateToDashboard;
        this.animationEnabled = animationEnabled;

        getStyleClass().add("page-view");
        setPadding(new Insets(32));
        setSpacing(24);

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        statsRow = new HBox(16);
        chartsArea = new VBox(16);

        Label quickActions = new Label("Quick Actions");
        quickActions.getStyleClass().add("section-title");

        actions = new HBox(16);

        recentRunsArea = new VBox(8);
        Label recentLabel = new Label("Recent Runs");
        recentLabel.getStyleClass().add("section-title");
        noRunsLabel = new Label("No runs yet. Start your first run!");
        noRunsLabel.getStyleClass().add("empty-state");

        Label achievementsLabel = new Label("Achievements");
        achievementsLabel.getStyleClass().add("section-title");
        noAchievementsLabel = new Label("No achievements yet.");
        noAchievementsLabel.getStyleClass().add("empty-state");
        achievementsArea = new HBox(12);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        VBox content = new VBox(16);
        content.getChildren().addAll(title, statsRow, quickActions, actions, recentLabel, noRunsLabel, recentRunsArea, achievementsLabel, noAchievementsLabel, achievementsArea, chartsArea);
        scrollPane.setContent(content);

        getChildren().add(scrollPane);

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

        vm.load();
        animateCardEntrance();
    }

    private void updateStats(StatsDTO stats) {
        statsRow.getChildren().clear();
        statsRow.getChildren().addAll(
                createStatCard("Total Wins", String.valueOf(stats.getTotalWins()), "gold"),
                createStatCard("Total Losses", String.valueOf(stats.getTotalLosses()), "red"),
                createStatCard("Total Draws", String.valueOf(stats.getTotalDraws()), "purple"),
                createStatCard("Win Rate", String.format("%.1f%%", stats.getWinRate() * 100), "gold"),
                createStatCard("Best Streak", String.valueOf(stats.getBestStreak()), "blue")
        );

        chartsArea.getChildren().clear();
        HBox chartRow = new HBox(16);
        chartRow.getChildren().addAll(
                createMoveUsageChart(stats.getMoveUsage()),
                createWinTrendChart(),
                createRunLengthChart()
        );
        chartsArea.getChildren().add(chartRow);

        // Update New Run button
        actions.getChildren().clear();
        createActionButtons();
        animateCardEntrance();
    }

    private VBox createStatCard(String label, String value, String accent) {
        StatCard card = new StatCard(
                switch (label) {
                    case "Total Wins" -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TROPHY;
                    case "Total Losses" -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SKULL;
                    case "Total Draws" -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HAND_PAPER;
                    case "Win Rate" -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.PERCENTAGE;
                    default -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FIRE;
                },
                label, accent);
        card.setValue(value);
        return card;
    }

    private void createActionButtons() {
        var playBtn = new Label("New Run");
        playBtn.getStyleClass().addAll("action-button", "button-primary");
        playBtn.setOnMouseClicked(e -> { audioManager.play("click"); vm.startNewRun(runId -> {
            if (runId != null) onNavigateToPlay.accept(runId);
        }); });

        resumeBtn = new Label("Resume");
        resumeBtn.getStyleClass().addAll("action-button", "button-secondary");

        RunDTO unfinished = vm.unfinishedRun.get();
        if (unfinished != null) {
            resumeBtn.setOnMouseClicked(e -> { audioManager.play("click"); onNavigateToPlay.accept(unfinished.getId()); });
            actions.getChildren().addAll(resumeBtn, playBtn);
        } else {
            actions.getChildren().add(playBtn);
        }
    }

    private void updateUnfinishedRun(RunDTO run) {
        createActionButtons();
    }

    private void updateRecentRuns(List<RunDTO> runs) {
        noRunsLabel.setVisible(runs.isEmpty());
        noRunsLabel.setManaged(runs.isEmpty());
        recentRunsArea.getChildren().clear();
        if (!runs.isEmpty()) {
            for (RunDTO run : runs) {
                recentRunsArea.getChildren().add(new RunCard(run));
            }
        }
    }

    private void updateAchievements(List<com.papercrown.shared.dto.AchievementDTO> achievements) {
        noAchievementsLabel.setVisible(achievements.isEmpty());
        noAchievementsLabel.setManaged(achievements.isEmpty());
        achievementsArea.getChildren().clear();
        for (var a : achievements) {
            achievementsArea.getChildren().add(new AchievementCard(a));
        }
    }

    private void animateCardEntrance() {
        if (!animationEnabled.get()) return;
        for (int i = 0; i < statsRow.getChildren().size(); i++) {
            var card = statsRow.getChildren().get(i);
            card.setTranslateY(30);
            card.setOpacity(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300).multiply(1 + i * 0.15), card);
            tt.setToY(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300).multiply(1 + i * 0.15), card);
            ft.setToValue(1);
            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.setDelay(Duration.millis(i * 80));
            pt.play();
        }
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
        pieSectionPaintFallback(plot, dataset);
        return new ChartContainer(chart);
    }

    @SuppressWarnings("unchecked")
    private void pieSectionPaintFallback(PiePlot plot, DefaultPieDataset dataset) {
        int i = 0;
        java.awt.Color[] fallbacks = {new java.awt.Color(201, 168, 76), new java.awt.Color(139, 47, 58), new java.awt.Color(107, 91, 149)};
        for (Object key : dataset.getKeys()) {
            Comparable<Object> cKey = (Comparable<Object>) key;
            if (plot.getSectionPaint(cKey) == null) {
                plot.setSectionPaint(cKey, fallbacks[i % fallbacks.length]);
            }
            i++;
        }
    }

    private ChartContainer createWinTrendChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<RunDTO> runs = vm.recentRuns.get();
        if (runs != null) {
            for (int i = 0; i < runs.size(); i++) {
                dataset.addValue(runs.get(i).getTotalWins(), "Wins", "Run " + (i + 1));
            }
        }
        JFreeChart chart = ChartFactory.createLineChart("Win Trend", "Run", "Wins", dataset);
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
}
