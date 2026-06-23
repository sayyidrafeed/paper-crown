package com.papercrown.desktop.viewmodel;

import com.papercrown.desktop.service.BackendClient;
import com.papercrown.shared.dto.BuffDTO;
import com.papercrown.shared.dto.MoveResponse;
import com.papercrown.shared.dto.RoundDTO;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PlayViewModel {

    private final BackendClient client;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "play-vm");
        t.setDaemon(true);
        return t;
    });

    public final SimpleLongProperty runId = new SimpleLongProperty();
    public final SimpleIntegerProperty currentHp = new SimpleIntegerProperty(3);
    public final SimpleIntegerProperty maxHp = new SimpleIntegerProperty(3);
    public final SimpleIntegerProperty roundNumber = new SimpleIntegerProperty(0);
    public final SimpleObjectProperty<RoundOutcome> lastOutcome = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<Move> lastPlayerMove = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<Move> lastBotMove = new SimpleObjectProperty<>();
    public final SimpleObjectProperty<List<BuffDTO>> buffChoice = new SimpleObjectProperty<>();
    public final SimpleListProperty<RoundDTO> roundHistory = new SimpleListProperty<>(FXCollections.observableArrayList());
    public final SimpleBooleanProperty runEnded = new SimpleBooleanProperty(false);
    public final SimpleObjectProperty<RunDTO> finalRun = new SimpleObjectProperty<>();
    public final SimpleListProperty<BuffDTO> activeBuffs = new SimpleListProperty<>(FXCollections.observableArrayList());
    public final SimpleBooleanProperty error = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

    public PlayViewModel(BackendClient client) {
        this.client = client;
    }

    public void initialize(Long runId) {
        if (runId == null) return;
        this.runId.set(runId);
        error.set(false);
        executor.execute(() -> {
            try {
                RunDTO run = client.getRunById(runId);
                Platform.runLater(() -> applyRunState(run));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            }
        });
    }

    public void startNewRun() {
        loading.set(true);
        error.set(false);
        executor.execute(() -> {
            try {
                RunDTO run = client.startRun();
                Platform.runLater(() -> {
                    // Reset game states for a fresh start
                    runEnded.set(false);
                    finalRun.set(null);
                    buffChoice.set(null);
                    lastOutcome.set(null);
                    lastPlayerMove.set(null);
                    lastBotMove.set(null);
                    roundHistory.set(FXCollections.observableArrayList());
                    
                    applyRunState(run);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void submitMove(Move move) {
        loading.set(true);
        error.set(false);
        executor.execute(() -> {
            try {
                MoveResponse response = client.submitMove(runId.get(), move);
                Platform.runLater(() -> applyResponse(response));
            } catch (Exception e) {
                Platform.runLater(() -> error.set(true));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void selectBuff(Long buffId) {
        loading.set(true);
        error.set(false);
        executor.execute(() -> {
            try {
                MoveResponse response = client.selectBuff(runId.get(), buffId);
                Platform.runLater(() -> {
                    buffChoice.set(null);
                    applyResponse(response);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    public void abandonRun(Runnable onSuccess) {
        loading.set(true);
        error.set(false);
        executor.execute(() -> {
            try {
                client.abandonRun(runId.get());
                Platform.runLater(onSuccess);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> error.set(true));
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }

    private void applyRunState(RunDTO run) {
        runId.set(run.getId());
        currentHp.set(run.getCurrentHp());
        maxHp.set(run.getMaxHp());
        roundNumber.set(run.getRoundNumber());
        activeBuffs.set(run.getActiveBuffs() != null
                ? FXCollections.observableArrayList(run.getActiveBuffs())
                : FXCollections.observableArrayList());
        if (run.getRounds() != null) {
            roundHistory.set(FXCollections.observableArrayList(run.getRounds()));
        }
    }

    private void applyResponse(MoveResponse response) {
        currentHp.set(response.getCurrentHp());
        runEnded.set(response.isRunEnded());
        finalRun.set(response.getFinalRun());

        RoundDTO round = response.getRound();
        if (round != null) {
            // Update moves BEFORE outcome — lastOutcome listener triggers showResult()
            // which reads lastPlayerMove/lastBotMove, so they must be current.
            lastPlayerMove.set(round.getPlayerMove());
            lastBotMove.set(round.getBotMove());
            roundNumber.set(round.getRoundNumber());

            List<RoundDTO> history = new ArrayList<>(roundHistory);
            history.add(round);
            roundHistory.set(FXCollections.observableArrayList(history));
        }

        // Set outcome LAST so the UI listener sees up-to-date move values
        lastOutcome.set(response.getOutcome());

        buffChoice.set(response.getBuffChoice());
        if (response.getFinalRun() != null) {
            activeBuffs.set(response.getFinalRun().getActiveBuffs() != null
                    ? FXCollections.observableArrayList(response.getFinalRun().getActiveBuffs())
                    : FXCollections.observableArrayList());
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
