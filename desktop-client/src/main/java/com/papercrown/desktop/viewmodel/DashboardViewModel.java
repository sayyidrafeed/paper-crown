package com.papercrown.desktop.viewmodel;

import com.papercrown.desktop.service.BackendClient;
import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.dto.StatsDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardViewModel {

    private static final int MAX_RETRIES = 2;
    private static final long REQUEST_TIMEOUT_SECONDS = 15;

    private final BackendClient client;
    private final java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "dashboard-vm");
        t.setDaemon(true);
        return t;
    });

    public final SimpleObjectProperty<StatsDTO> stats = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<List<RunDTO>> recentRuns = new SimpleObjectProperty<>(List.of());
    public final SimpleObjectProperty<List<AchievementDTO>> achievements = new SimpleObjectProperty<>(List.of());
    public final SimpleObjectProperty<RunDTO> unfinishedRun = new SimpleObjectProperty<>();
    public final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty error = new SimpleBooleanProperty(false);
    public final SimpleStringProperty errorMessage = new SimpleStringProperty("");

    public DashboardViewModel(BackendClient client) {
        this.client = client;
    }

    public void load() {
        error.set(false);
        errorMessage.set("");
        loading.set(true);
        AtomicInteger pending = new AtomicInteger(4);
        Runnable onComplete = () -> {
            if (pending.decrementAndGet() == 0) {
                Platform.runLater(() -> loading.set(false));
            }
        };
        executor.execute(() -> {
            StatsDTO result = retry(() -> client.getStats(), "stats");
            Platform.runLater(() -> { if (result != null) stats.set(result); });
            onComplete.run();
        });
        executor.execute(() -> {
            List<RunDTO> runs = retry(() -> client.getAllRuns(), "runs");
            Platform.runLater(() -> {
                if (runs != null) {
                    List<RunDTO> recent = runs.size() > 5 ? runs.subList(0, 5) : runs;
                    recentRuns.set(recent);
                }
            });
            onComplete.run();
        });
        executor.execute(() -> {
            List<AchievementDTO> all = retry(() -> client.getAchievements(), "achievements");
            Platform.runLater(() -> {
                if (all != null) {
                    List<AchievementDTO> sorted = all.stream()
                            .sorted(Comparator.comparingInt(a -> {
                                if (a.isUnlocked()) return 0;
                                if (a.getProgress() > 0) return 1;
                                return 2;
                            }))
                            .toList();
                    achievements.set(sorted);
                }
            });
            onComplete.run();
        });
        executor.execute(() -> {
            RunDTO run = retry(() -> client.getUnfinishedRun(), "unfinished-run");
            Platform.runLater(() -> { if (run != null) unfinishedRun.set(run); });
            onComplete.run();
        });
    }

    @FunctionalInterface
    private interface BackendCall<T> {
        T call() throws Exception;
    }

    private <T> T retry(BackendCall<T> callable, String label) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        error.set(true);
                        errorMessage.set("Could not connect to the game server. Please make sure the backend is running and try again.");
                    });
                    return null;
                }
                try { Thread.sleep(500 * (attempt + 1)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return null; }
            }
        }
        return null;
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

    public void discardAndStartNew(java.util.function.Consumer<Long> onRunCreated) {
        executor.execute(() -> {
            try {
                RunDTO unfinished = client.getUnfinishedRun();
                if (unfinished != null) {
                    client.discardRun(unfinished.getId());
                }
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
