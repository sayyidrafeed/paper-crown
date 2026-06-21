package com.papercrown.desktop.view;

import com.papercrown.desktop.component.BuffCard;
import com.papercrown.desktop.component.Toast;
import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.util.AudioManager;
import com.papercrown.desktop.viewmodel.PlayViewModel;
import com.papercrown.shared.dto.BuffDTO;
import com.papercrown.shared.dto.RoundDTO;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import javafx.animation.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class PlayView extends VBox {

    private final PlayViewModel vm;
    private final AudioManager audioManager;
    private final Runnable onNavigateToDashboard;
    private final StackPane rootStack;
    private final ObservableBooleanValue animationEnabled;

    private HBox hpRow;
    private Label roundInfo;
    private HBox moveButtons;
    private Label resultLabel;
    private VBox resultSection;
    private VBox historyFeed;
    private HBox activeBuffsRow;
    private Label noBuffsLabel;
    private VBox buffModal;
    private VBox runSummary;

    public PlayView(BackendClient client, AudioManager audioManager, Long runId, Runnable onNavigateToDashboard, ObservableBooleanValue animationEnabled) {
        this.vm = new PlayViewModel(client);
        this.audioManager = audioManager;
        this.onNavigateToDashboard = onNavigateToDashboard;
        this.animationEnabled = animationEnabled;

        rootStack = new StackPane();

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(32));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add("page-view");

        Label title = new Label("Play");
        title.getStyleClass().add("page-title");

        hpRow = new HBox(6);
        hpRow.setAlignment(Pos.CENTER);

        roundInfo = new Label("Round 0");
        roundInfo.getStyleClass().add("round-info");

        VBox hpSection = new VBox(8, hpRow, roundInfo);
        hpSection.setAlignment(Pos.CENTER);

        VBox moveSection = new VBox(16);
        moveSection.setAlignment(Pos.CENTER);
        Label movePrompt = new Label("Choose your move");
        movePrompt.getStyleClass().add("move-prompt");

        moveButtons = new HBox(16);
        moveButtons.setAlignment(Pos.CENTER);
        Label rockBtn = new Label("Rock");
        rockBtn.getStyleClass().addAll("move-button", "move-rock");
        Label paperBtn = new Label("Paper");
        paperBtn.getStyleClass().addAll("move-button", "move-paper");
        Label scissorsBtn = new Label("Scissors");
        scissorsBtn.getStyleClass().addAll("move-button", "move-scissors");

        rockBtn.setOnMouseClicked(e -> { audioManager.play("click"); vm.submitMove(Move.ROCK); });
        paperBtn.setOnMouseClicked(e -> { audioManager.play("click"); vm.submitMove(Move.PAPER); });
        scissorsBtn.setOnMouseClicked(e -> { audioManager.play("click"); vm.submitMove(Move.SCISSORS); });

        moveButtons.getChildren().addAll(rockBtn, paperBtn, scissorsBtn);
        moveSection.getChildren().addAll(movePrompt, moveButtons);

        resultSection = new VBox(8);
        resultSection.setAlignment(Pos.CENTER);
        resultLabel = new Label("Make a move to begin");
        resultLabel.getStyleClass().add("result-text");
        resultSection.getChildren().add(resultLabel);

        VBox buffSection = new VBox(8);
        buffSection.setAlignment(Pos.CENTER);
        Label buffLabel = new Label("Active Buffs");
        buffLabel.getStyleClass().add("section-title");
        activeBuffsRow = new HBox(8);
        activeBuffsRow.setAlignment(Pos.CENTER);
        noBuffsLabel = new Label("No active buffs");
        noBuffsLabel.getStyleClass().add("empty-state");
        activeBuffsRow.getChildren().add(noBuffsLabel);
        buffSection.getChildren().addAll(buffLabel, activeBuffsRow);

        VBox historySection = new VBox(8);
        Label historyLabel = new Label("Round History");
        historyLabel.getStyleClass().add("section-title");
        historyFeed = new VBox(4);
        historyFeed.setPadding(new Insets(8, 0, 0, 0));
        historySection.getChildren().addAll(historyLabel, historyFeed);

        ScrollPane scrollContent = new ScrollPane();
        scrollContent.setFitToWidth(true);
        VBox content = new VBox(24);
        content.getChildren().addAll(title, hpSection, moveSection, resultSection, buffSection, historySection);
        scrollContent.setContent(content);

        mainContent.getChildren().add(scrollContent);

        buffModal = createBuffModal();
        buffModal.setVisible(false);
        buffModal.setManaged(false);

        runSummary = createRunSummary();
        runSummary.setVisible(false);
        runSummary.setManaged(false);

        VBox mainWithModal = new VBox();
        mainWithModal.getChildren().addAll(mainContent, buffModal, runSummary);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        rootStack.getChildren().add(mainWithModal);

        getChildren().add(rootStack);

        bindViewModel();

        if (runId != null) {
            vm.initialize(runId);
        }
    }

    private void bindViewModel() {
        vm.currentHp.addListener((obs, old, val) -> renderHp(val.intValue(), vm.maxHp.get()));
        vm.maxHp.addListener((obs, old, val) -> renderHp(vm.currentHp.get(), val.intValue()));
        vm.roundNumber.addListener((obs, old, val) -> roundInfo.setText("Round " + val.intValue()));
        vm.lastOutcome.addListener((obs, old, val) -> {
            if (val != null) showResult(val);
        });
        vm.buffChoice.addListener((obs, old, val) -> showBuffChoice(val));
        vm.activeBuffs.addListener((obs, old, val) -> updateActiveBuffs(val));
        vm.roundHistory.addListener((obs, old, val) -> updateHistory(val));
        vm.runEnded.addListener((obs, old, val) -> {
            if (val) {
                Toast.show(rootStack, "Run completed! Returning to dashboard...", Toast.Type.INFO);
                showRunSummary();
            }
        });
        vm.error.addListener((obs, old, val) -> {
            if (val) showError("Network error. Try again.");
        });
        vm.loading.addListener((obs, old, val) -> {
            moveButtons.setDisable(val);
        });
    }

    private void renderHp(int current, int max) {
        hpRow.getChildren().clear();
        for (int i = 0; i < max; i++) {
            Label heart = new Label(i < current ? "\u2764" : "\u2661");
            heart.setStyle("-fx-font-size: 28px; -fx-text-fill: " + (i < current ? "#e65c6c" : "#525266") + ";");
            hpRow.getChildren().add(heart);
        }
    }

    private void showResult(RoundOutcome outcome) {
        switch (outcome) {
            case WIN -> {
                resultLabel.setText("You Win!");
                resultLabel.getStyleClass().removeAll("result-loss", "result-draw");
                resultLabel.getStyleClass().add("result-win");
                audioManager.play("win");
                animateWin();
            }
            case LOSS -> {
                resultLabel.setText("You Lose!");
                resultLabel.getStyleClass().removeAll("result-win", "result-draw");
                resultLabel.getStyleClass().add("result-loss");
                audioManager.play("lose");
                animateLoss();
            }
            case DRAW -> {
                resultLabel.setText("Draw!");
                resultLabel.getStyleClass().removeAll("result-win", "result-loss");
                resultLabel.getStyleClass().add("result-draw");
                animateDraw();
            }
        }

        if (vm.lastPlayerMove.get() != null && vm.lastBotMove.get() != null) {
            resultLabel.setText(resultLabel.getText() + " (You: " + vm.lastPlayerMove.get()
                    + " vs Bot: " + vm.lastBotMove.get() + ")");
        }
    }

    private void animateWin() {
        if (!animationEnabled.get()) return;

        ScaleTransition st = new ScaleTransition(Duration.millis(300), resultSection);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        Label plusOne = new Label("+1");
        plusOne.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #61a5c2;");
        StackPane.setAlignment(plusOne, Pos.TOP_CENTER);
        StackPane.setMargin(plusOne, new Insets(16, 0, 0, 0));
        rootStack.getChildren().add(plusOne);

        TranslateTransition floatUp = new TranslateTransition(Duration.millis(800), plusOne);
        floatUp.setFromY(0);
        floatUp.setToY(-60);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), plusOne);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        ParallelTransition floatAnim = new ParallelTransition(floatUp, fadeOut);
        floatAnim.setOnFinished(e -> rootStack.getChildren().remove(plusOne));
        floatAnim.play();
    }

    private void animateLoss() {
        if (!animationEnabled.get()) {
            resultSection.setStyle("-fx-background-color: rgba(139,47,58,0.3); -fx-background-radius: 12;");
            PauseTransition reset = new PauseTransition(Duration.millis(300));
            reset.setOnFinished(e -> resultSection.setStyle(""));
            reset.play();
            return;
        }

        TranslateTransition tt = new TranslateTransition(Duration.millis(50), hpRow);
        tt.setFromX(0);
        tt.setToX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.play();

        resultSection.setStyle("-fx-background-color: rgba(139,47,58,0.3); -fx-background-radius: 12;");
        PauseTransition reset = new PauseTransition(Duration.millis(300));
        reset.setOnFinished(e -> resultSection.setStyle(""));
        reset.play();
    }

    private void animateDraw() {
        resultSection.setStyle("-fx-background-color: rgba(201,168,76,0.2); -fx-background-radius: 12;");
        PauseTransition reset = new PauseTransition(Duration.millis(300));
        reset.setOnFinished(e -> resultSection.setStyle(""));
        reset.play();
    }

    private void showBuffChoice(java.util.List<BuffDTO> choices) {
        if (choices == null || choices.isEmpty()) {
            buffModal.setVisible(false);
            buffModal.setManaged(false);
            return;
        }
        buffModal.getChildren().clear();
        buffModal.setVisible(true);
        buffModal.setManaged(true);

        Label title = new Label("Choose a Buff");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #c9a84c; -fx-padding: 0 0 16 0;");

        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        for (BuffDTO buff : choices) {
            BuffCard card = new BuffCard(buff);
            card.setOnMouseClicked(e -> vm.selectBuff(buff.getId()));

            if (animationEnabled.get()) {
                card.setScaleX(0.8);
                card.setScaleY(0.8);
                ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                st.setToX(1);
                st.setToY(1);
                st.play();

                card.setOnMouseEntered(e -> {
                    ScaleTransition hover = new ScaleTransition(Duration.millis(100), card);
                    hover.setToX(1.05);
                    hover.setToY(1.05);
                    hover.play();
                });
                card.setOnMouseExited(e -> {
                    ScaleTransition unhover = new ScaleTransition(Duration.millis(100), card);
                    unhover.setToX(1);
                    unhover.setToY(1);
                    unhover.play();
                });
            }

            cards.getChildren().add(card);
        }

        buffModal.getChildren().addAll(title, cards);
    }

    private VBox createBuffModal() {
        VBox modal = new VBox(16);
        modal.setAlignment(Pos.CENTER);
        modal.setStyle("-fx-background-color: rgba(15,15,20,0.92); -fx-padding: 32; -fx-spacing: 16;");
        modal.setMaxWidth(Double.MAX_VALUE);
        modal.setMaxHeight(Double.MAX_VALUE);
        return modal;
    }

    private void updateActiveBuffs(java.util.List<BuffDTO> buffs) {
        activeBuffsRow.getChildren().clear();
        if (buffs == null || buffs.isEmpty()) {
            activeBuffsRow.getChildren().add(noBuffsLabel);
        } else {
            for (BuffDTO buff : buffs) {
                BuffCard card = new BuffCard(buff);
                card.setPrefWidth(120);
                card.setMinWidth(100);
                activeBuffsRow.getChildren().add(card);
            }
        }
    }

    private void updateHistory(java.util.List<RoundDTO> rounds) {
        historyFeed.getChildren().clear();
        if (rounds == null || rounds.isEmpty()) {
            historyFeed.getChildren().add(new Label("No rounds played yet"));
        } else {
            for (RoundDTO round : rounds) {
                HBox row = new HBox(12);
                row.setPadding(new Insets(4, 8, 4, 8));
                row.setStyle("-fx-background-color: #1a1a24; -fx-background-radius: 6;");

                String outcomeIcon = switch (round.getOutcome()) {
                    case WIN -> "\u2713";
                    case LOSS -> "\u2717";
                    case DRAW -> "\u2014";
                };
                String outcomeColor = switch (round.getOutcome()) {
                    case WIN -> "#61a5c2";
                    case LOSS -> "#e65c6c";
                    case DRAW -> "#c9a84c";
                };

                Label iconLabel = new Label(outcomeIcon);
                iconLabel.setStyle("-fx-text-fill: " + outcomeColor + "; -fx-font-weight: bold; -fx-font-size: 16px; -fx-min-width: 24;");
                Label moveLabel = new Label("R" + round.getRoundNumber() + ": You: " + round.getPlayerMove() + " | Bot: " + round.getBotMove());
                moveLabel.setStyle("-fx-text-fill: #b0b0c8; -fx-font-size: 13px;");
                row.getChildren().addAll(iconLabel, moveLabel);
                historyFeed.getChildren().add(row);
            }
        }
    }

    private VBox createRunSummary() {
        VBox summary = new VBox(16);
        summary.setAlignment(Pos.CENTER);
        summary.setStyle("-fx-background-color: rgba(15,15,20,0.95); -fx-padding: 48;");

        Label runOver = new Label("Run Over");
        runOver.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;");

        VBox statsBox = new VBox(8);
        statsBox.setAlignment(Pos.CENTER);

        Label roundsLabel = new Label();
        roundsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label winsLabel = new Label();
        winsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label lossesLabel = new Label();
        lossesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label drawsLabel = new Label();
        drawsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        statsBox.getChildren().addAll(roundsLabel, winsLabel, lossesLabel, drawsLabel);

        Button returnBtn = new Button("Return to Dashboard");
        returnBtn.setStyle("-fx-background-color: #c9a84c; -fx-text-fill: #0f0f14; -fx-padding: 12 24; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8px;");
        returnBtn.setOnAction(e -> onNavigateToDashboard.run());

        summary.getChildren().addAll(runOver, statsBox, returnBtn);

        vm.finalRun.addListener((obs, old, val) -> {
            if (val != null) {
                roundsLabel.setText("Rounds: " + val.getRoundNumber());
                winsLabel.setText("Wins: " + val.getTotalWins());
                lossesLabel.setText("Losses: " + val.getTotalLosses());
                drawsLabel.setText("Draws: " + val.getTotalDraws());
            }
        });

        return summary;
    }

    private void showRunSummary() {
        runSummary.setVisible(true);
        runSummary.setManaged(true);
        if (animationEnabled.get()) {
            runSummary.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(500), runSummary);
            ft.setToValue(1);
            ft.play();
        } else {
            runSummary.setOpacity(1);
        }

        PauseTransition autoReturn = new PauseTransition(Duration.seconds(3));
        autoReturn.setOnFinished(e -> onNavigateToDashboard.run());
        autoReturn.play();
    }

    private void showError(String msg) {
        Toast.show(rootStack, msg, Toast.Type.ERROR);
    }
}
