package com.papercrown.desktop.viewmodel;

import com.papercrown.desktop.service.BackendClient;
import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.dto.StatsDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;
import java.util.concurrent.Executors;

public class DashboardViewModel {

    private final BackendClient client;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dashboard-vm");
        t.setDaemon(true);
        return t;
    });

    public final SimpleObjectProperty<StatsDTO> stats = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<List<RunDTO>> recentRuns = new SimpleObjectProperty<>(List.of());
    public final SimpleObjectProperty<List<AchievementDTO>> achievements = new SimpleObjectProperty<>(List.of());
    public final SimpleObjectProperty<RunDTO> unfinishedRun = new SimpleObjectProperty<>();
    public final SimpleBooleanProperty error = new SimpleBooleanProperty(false);

    public DashboardViewModel(BackendClient client) {
        this.client = client;
    }

    public void load() {
        error.set(false);
        executor.execute(() -> {
            try {
                StatsDTO result = client.getStats();
                Platform.runLater(() -> stats.set(result));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            }
        });
        executor.execute(() -> {
            try {
                List<RunDTO> runs = client.getAllRuns();
                List<RunDTO> recent = runs.size() > 5 ? runs.subList(0, 5) : runs;
                Platform.runLater(() -> recentRuns.set(recent));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            }
        });
        executor.execute(() -> {
            try {
                List<AchievementDTO> all = client.getAchievements();
                List<AchievementDTO> unlocked = all.stream()
                        .filter(AchievementDTO::isUnlocked)
                        .toList();
                Platform.runLater(() -> achievements.set(unlocked));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            }
        });
        executor.execute(() -> {
            try {
                RunDTO run = client.getUnfinishedRun();
                Platform.runLater(() -> unfinishedRun.set(run));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            }
        });
    }

    public void startNewRun(java.util.function.Consumer<Long> onRunCreated) {
        executor.execute(() -> {
            try {
                RunDTO run = client.startRun();
                Platform.runLater(() -> onRunCreated.accept(run.getId()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> onRunCreated.accept(null));
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
