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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class PlayView extends VBox {

    private final PlayViewModel vm;
    private final AudioManager audioManager;
    private final Runnable onNavigateToDashboard;
    private final StackPane rootStack;
    private final ObservableBooleanValue animationEnabled;
    private boolean hasNavigated = false;

    private HBox hpRow;
    private Label roundInfo;
    private HBox moveButtons;
    private Label resultLabel;
    private VBox resultSection;
    private VBox historyFeed;
    private FlowPane activeBuffsRow;
    private Label noBuffsLabel;
    private VBox buffModal;
    private VBox runSummary;
    private VBox mainContent;
    private VBox startRunOverlay;
    private VBox resumeOrAbandonOverlay;

    public PlayView(
        BackendClient client,
        AudioManager audioManager,
        Long runId,
        Runnable onNavigateToDashboard,
        ObservableBooleanValue animationEnabled
    ) {
        this.vm = new PlayViewModel(client);
        this.audioManager = audioManager;
        this.onNavigateToDashboard = onNavigateToDashboard;
        this.animationEnabled = animationEnabled;

        rootStack = new StackPane();

        mainContent = new VBox(24);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add("page-view");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(1000);

        Label title = new Label("Play");
        title.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label leaveBtn = new Label("Abandon");
        leaveBtn.getStyleClass().addAll("action-button", "button-secondary");
        leaveBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 13px;");
        leaveBtn.setOnMouseClicked(e -> {
            audioManager.play("click");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            alert
                .getDialogPane()
                .getStylesheets()
                .add(
                    getClass().getResource("/styles/main.css").toExternalForm()
                );
            alert.getDialogPane().getStyleClass().add("custom-dialog");
            alert.setTitle("Abandon Run");
            alert.setHeaderText("Are you sure you want to abandon this run?");
            alert.setContentText(
                "Abandoning will end the current run immediately and record it as a loss. This action cannot be undone."
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    vm.abandonRun(() -> navigateAway());
                }
            });
        });

        header.getChildren().addAll(title, spacer, leaveBtn);

        hpRow = new HBox(8);
        hpRow.setAlignment(Pos.CENTER);

        roundInfo = new Label("Round 0");
        roundInfo.getStyleClass().add("round-info");

        VBox hpCard = new VBox(12);
        hpCard.getStyleClass().add("stat-card");
        hpCard.setPadding(new Insets(20));
        hpCard.setAlignment(Pos.CENTER);
        hpCard.setMaxWidth(Double.MAX_VALUE);
        hpCard.getChildren().addAll(hpRow, roundInfo);

        VBox moveCard = new VBox(20);
        moveCard.getStyleClass().add("stat-card");
        moveCard.setPadding(new Insets(24));
        moveCard.setAlignment(Pos.CENTER);
        moveCard.setMaxWidth(Double.MAX_VALUE);

        Label movePrompt = new Label("Choose your move");
        movePrompt.getStyleClass().add("move-prompt");

        moveButtons = new HBox(20);
        moveButtons.setAlignment(Pos.CENTER);

        Label rockBtn = new Label("Rock");
        FontIcon rockIcon = new FontIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HAND_ROCK
        );
        rockIcon.setIconSize(48);
        rockIcon.setIconColor(javafx.scene.paint.Color.web("#d4a06a"));
        rockBtn.setGraphic(rockIcon);
        rockBtn.setContentDisplay(ContentDisplay.TOP);
        rockBtn.setGraphicTextGap(10);
        rockBtn.getStyleClass().addAll("move-button", "move-rock");

        Label paperBtn = new Label("Paper");
        FontIcon paperIcon = new FontIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HAND_PAPER
        );
        paperIcon.setIconSize(48);
        paperIcon.setIconColor(javafx.scene.paint.Color.web("#88bdf2"));
        paperBtn.setGraphic(paperIcon);
        paperBtn.setContentDisplay(ContentDisplay.TOP);
        paperBtn.setGraphicTextGap(10);
        paperBtn.getStyleClass().addAll("move-button", "move-paper");

        Label scissorsBtn = new Label("Scissors");
        FontIcon scissorsIcon = new FontIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HAND_SCISSORS
        );
        scissorsIcon.setIconSize(48);
        scissorsIcon.setIconColor(javafx.scene.paint.Color.web("#e88ab9"));
        scissorsBtn.setGraphic(scissorsIcon);
        scissorsBtn.setContentDisplay(ContentDisplay.TOP);
        scissorsBtn.setGraphicTextGap(10);
        scissorsBtn.getStyleClass().addAll("move-button", "move-scissors");

        addTactileFeedback(rockBtn, Move.ROCK);
        addTactileFeedback(paperBtn, Move.PAPER);
        addTactileFeedback(scissorsBtn, Move.SCISSORS);

        moveButtons.getChildren().addAll(rockBtn, paperBtn, scissorsBtn);
        moveCard.getChildren().addAll(movePrompt, moveButtons);

        resultSection = new VBox(8);
        resultSection.getStyleClass().add("stat-card");
        resultSection.setPadding(new Insets(20));
        resultSection.setAlignment(Pos.CENTER);
        resultSection.setMaxWidth(Double.MAX_VALUE);
        resultLabel = new Label("Make a move to begin");
        resultLabel.getStyleClass().add("result-text");
        resultSection.getChildren().add(resultLabel);

        VBox buffCard = new VBox(12);
        buffCard.getStyleClass().add("stat-card");
        buffCard.setPadding(new Insets(20));
        buffCard.setAlignment(Pos.TOP_CENTER);
        buffCard.setMaxWidth(Double.MAX_VALUE);

        Label buffLabel = new Label("Active Buffs");
        buffLabel.getStyleClass().add("section-title");
        activeBuffsRow = new FlowPane(8, 8);
        activeBuffsRow.setAlignment(Pos.CENTER);
        noBuffsLabel = new Label("No active buffs");
        noBuffsLabel.getStyleClass().add("empty-state");
        activeBuffsRow.getChildren().add(noBuffsLabel);
        buffCard.getChildren().addAll(buffLabel, activeBuffsRow);

        VBox historyCard = new VBox(12);
        historyCard.getStyleClass().add("stat-card");
        historyCard.setPadding(new Insets(20));
        historyCard.setAlignment(Pos.TOP_CENTER);
        historyCard.setMaxWidth(Double.MAX_VALUE);

        Label historyLabel = new Label("Round History");
        historyLabel.getStyleClass().add("section-title");
        historyFeed = new VBox(8);
        historyFeed.setPadding(new Insets(8, 0, 0, 0));
        historyCard.getChildren().addAll(historyLabel, historyFeed);

        VBox leftColumn = new VBox(24);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        leftColumn
            .getChildren()
            .addAll(hpCard, moveCard, resultSection, buffCard);

        VBox rightColumn = new VBox(24);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setMinWidth(280);
        rightColumn.setMaxWidth(320);
        rightColumn.setPrefWidth(300);
        rightColumn.getChildren().add(historyCard);
        VBox.setVgrow(historyCard, Priority.ALWAYS);

        HBox columnsContainer = new HBox(24);
        columnsContainer.setAlignment(Pos.TOP_CENTER);
        columnsContainer.setMaxWidth(1000);
        columnsContainer.getChildren().addAll(leftColumn, rightColumn);
        VBox.setVgrow(columnsContainer, Priority.ALWAYS);

        ScrollPane scrollContent = new ScrollPane();
        scrollContent.setFitToWidth(true);
        scrollContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollContent.setStyle(
            "-fx-background-color: transparent; -fx-control-inner-background: transparent;"
        );
        VBox.setVgrow(scrollContent, Priority.ALWAYS);

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));
        content.setAlignment(Pos.TOP_CENTER);
        content.setMaxWidth(1000);
        content.getChildren().addAll(header, columnsContainer);

        javafx.scene.layout.StackPane centeringWrapper =
            new javafx.scene.layout.StackPane(content);
        centeringWrapper.setAlignment(Pos.TOP_CENTER);
        centeringWrapper.setStyle("-fx-background-color: transparent;");
        scrollContent.setContent(centeringWrapper);

        mainContent.getChildren().add(scrollContent);

        buffModal = createBuffModal();
        buffModal.setVisible(false);
        buffModal.setManaged(false);

        runSummary = createRunSummary();
        runSummary.setVisible(false);
        runSummary.setManaged(false);

        startRunOverlay = createStartRunOverlay();
        resumeOrAbandonOverlay = createResumeOrAbandonOverlay();
        if (runId == null) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            startRunOverlay.setVisible(true);
            startRunOverlay.setManaged(true);
            resumeOrAbandonOverlay.setVisible(false);
            resumeOrAbandonOverlay.setManaged(false);
        } else {
            // Navigated from Dashboard with explicit runId — straight to game
            mainContent.setVisible(true);
            mainContent.setManaged(true);
            startRunOverlay.setVisible(false);
            startRunOverlay.setManaged(false);
            resumeOrAbandonOverlay.setVisible(false);
            resumeOrAbandonOverlay.setManaged(false);
        }

        rootStack
            .getChildren()
            .addAll(
                mainContent,
                startRunOverlay,
                resumeOrAbandonOverlay,
                buffModal,
                runSummary
            );
        VBox.setVgrow(rootStack, Priority.ALWAYS);

        getChildren().add(rootStack);

        bindViewModel();

        vm.initialize(runId);
    }

    private void bindViewModel() {
        vm.currentHp.addListener((obs, old, val) ->
            renderHp(val.intValue(), vm.maxHp.get())
        );
        vm.maxHp.addListener((obs, old, val) ->
            renderHp(vm.currentHp.get(), val.intValue())
        );
        vm.shield.addListener((obs, old, val) ->
            renderHp(vm.currentHp.get(), vm.maxHp.get())
        );
        vm.roundNumber.addListener((obs, old, val) ->
            roundInfo.setText("Round " + val.intValue())
        );
        vm.lastOutcome.addListener((obs, old, val) -> {
            if (val != null) showResult(val);
        });
        vm.buffChoice.addListener((obs, old, val) -> showBuffChoice(val));
        vm.activeBuffs.addListener((obs, old, val) -> {
            updateActiveBuffs(val);
            renderHp(vm.currentHp.get(), vm.maxHp.get());
        });
        vm.roundHistory.addListener((obs, old, val) -> updateHistory(val));
        vm.runEnded.addListener((obs, old, val) -> {
            if (val) {
                showRunSummary();
            }
        });
        vm.error.addListener((obs, old, val) -> {
            if (val) showError("Network error. Try again.");
        });
        vm.loading.addListener((obs, old, val) -> {
            moveButtons.setDisable(val);
        });
        vm.runId.addListener((obs, old, val) -> {
            if (val == null || val.longValue() <= 0) return;
            if (startRunOverlay.isVisible()) {
                mainContent.setVisible(true);
                mainContent.setManaged(true);
                if (animationEnabled.get()) {
                    FadeTransition ft = new FadeTransition(
                        Duration.millis(300),
                        startRunOverlay
                    );
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> {
                        startRunOverlay.setVisible(false);
                        startRunOverlay.setManaged(false);
                    });
                    ft.play();
                } else {
                    startRunOverlay.setVisible(false);
                    startRunOverlay.setManaged(false);
                }
            }
            if (
                resumeOrAbandonOverlay.isVisible() &&
                old != null &&
                old.longValue() > 0
            ) {
                mainContent.setVisible(true);
                mainContent.setManaged(true);
                if (animationEnabled.get()) {
                    FadeTransition ft = new FadeTransition(
                        Duration.millis(300),
                        resumeOrAbandonOverlay
                    );
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> {
                        resumeOrAbandonOverlay.setVisible(false);
                        resumeOrAbandonOverlay.setManaged(false);
                    });
                    ft.play();
                } else {
                    resumeOrAbandonOverlay.setVisible(false);
                    resumeOrAbandonOverlay.setManaged(false);
                }
            }
        });

        vm.pendingRun.addListener((obs, old, run) -> {
            if (run == null) return;
            // Entered via sidebar with an existing run — switch to resume/abandon overlay
            startRunOverlay.setVisible(false);
            startRunOverlay.setManaged(false);
            resumeOrAbandonOverlay.setVisible(true);
            resumeOrAbandonOverlay.setManaged(true);
            vm.initialize(run.getId());
        });

        // Force-refresh all display after every applyRunState (bypasses JavaFX no-change-no-fire)
        vm.onRunStateApplied = () -> {
            renderHp(vm.currentHp.get(), vm.maxHp.get());
            roundInfo.setText("Round " + vm.roundNumber.get());
            updateActiveBuffs(vm.activeBuffs.get());
            updateHistory(vm.roundHistory.get());
        };

        // Automatically shut down the executor when this view is removed from the scene to prevent thread leaks
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                vm.shutdown();
            }
        });

        // Execute immediately for initial render to sync UI with initial VM state
        vm.onRunStateApplied.run();

        // Automatically shut down the executor when this view is removed from the scene to prevent thread leaks
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                vm.shutdown();
            }
        });
    }

    private void renderHp(int current, int max) {
        hpRow.getChildren().clear();
        for (int i = 0; i < max; i++) {
            FontIcon heart = new FontIcon(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.HEART
            );
            heart.setIconSize(28);
            heart.setIconColor(
                javafx.scene.paint.Color.web(
                    i < current ? "#e65c6c" : "#323246"
                )
            );
            hpRow.getChildren().add(heart);
        }
        int shieldCount = vm.shield.get();
        long ignoreLossCount = 0;
        if (vm.activeBuffs.get() != null) {
            ignoreLossCount = vm.activeBuffs
                .get()
                .stream()
                .filter(b -> "IGNORE_LOSS".equals(b.getEffectKey()))
                .count();
        }
        int totalProtection = shieldCount + (int) ignoreLossCount;
        if (totalProtection > 0) {
            FontIcon shieldIcon = new FontIcon(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.SHIELD_ALT
            );
            shieldIcon.setIconSize(18);
            shieldIcon.setIconColor(javafx.scene.paint.Color.web("#c9a84c"));
            Label countLabel = new Label(String.valueOf(totalProtection));
            countLabel.setStyle(
                "-fx-text-fill: #c9a84c; -fx-font-size: 14px; -fx-font-weight: bold;"
            );
            hpRow.getChildren().addAll(shieldIcon, countLabel);
        }
    }

    private void showResult(RoundOutcome outcome) {
        switch (outcome) {
            case WIN -> {
                resultLabel.setText("You Win!");
                resultLabel
                    .getStyleClass()
                    .removeAll("result-loss", "result-draw");
                resultLabel.getStyleClass().add("result-win");
                audioManager.play("win");
                animateWin();
            }
            case LOSS -> {
                resultLabel.setText("You Lose!");
                resultLabel
                    .getStyleClass()
                    .removeAll("result-win", "result-draw");
                resultLabel.getStyleClass().add("result-loss");
                audioManager.play("lose");
                animateLoss();
            }
            case DRAW -> {
                resultLabel.setText("Draw!");
                resultLabel
                    .getStyleClass()
                    .removeAll("result-win", "result-loss");
                resultLabel.getStyleClass().add("result-draw");
                animateDraw();
            }
        }

        if (vm.lastPlayerMove.get() != null && vm.lastBotMove.get() != null) {
            resultLabel.setText(
                resultLabel.getText() +
                    " (You: " +
                    vm.lastPlayerMove.get() +
                    " vs Bot: " +
                    vm.lastBotMove.get() +
                    ")"
            );
        }
    }

    private void animateWin() {
        if (!animationEnabled.get()) return;

        ScaleTransition st = new ScaleTransition(
            Duration.millis(300),
            resultSection
        );
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.05);
        st.setToY(1.05);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        Label plusOne = new Label("+1");
        plusOne.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #61a5c2;"
        );
        StackPane.setAlignment(plusOne, Pos.TOP_CENTER);
        StackPane.setMargin(plusOne, new Insets(16, 0, 0, 0));
        rootStack.getChildren().add(plusOne);

        TranslateTransition floatUp = new TranslateTransition(
            Duration.millis(800),
            plusOne
        );
        floatUp.setFromY(0);
        floatUp.setToY(-60);
        FadeTransition fadeOut = new FadeTransition(
            Duration.millis(800),
            plusOne
        );
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        ParallelTransition floatAnim = new ParallelTransition(floatUp, fadeOut);
        floatAnim.setOnFinished(e -> rootStack.getChildren().remove(plusOne));
        floatAnim.play();
    }

    private void animateLoss() {
        if (!animationEnabled.get()) {
            resultSection.setStyle(
                "-fx-background-color: rgba(139,47,58,0.3); -fx-background-radius: 12;"
            );
            PauseTransition reset = new PauseTransition(Duration.millis(300));
            reset.setOnFinished(e -> resultSection.setStyle(""));
            reset.play();
            return;
        }

        TranslateTransition tt = new TranslateTransition(
            Duration.millis(50),
            mainContent
        );
        tt.setFromX(0);
        tt.setToX(15);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.play();

        Region flash = new Region();
        flash.setStyle("-fx-background-color: rgba(230, 92, 108, 0.4);");
        flash.setMouseTransparent(true);
        rootStack.getChildren().add(flash);

        FadeTransition ft = new FadeTransition(Duration.millis(400), flash);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> rootStack.getChildren().remove(flash));
        ft.play();

        resultSection.setStyle(
            "-fx-background-color: rgba(139,47,58,0.3); -fx-background-radius: 12;"
        );
        PauseTransition reset = new PauseTransition(Duration.millis(300));
        reset.setOnFinished(e -> resultSection.setStyle(""));
        reset.play();
    }

    private void addTactileFeedback(Label btn, Move move) {
        btn.setOnMouseClicked(e -> {
            audioManager.play("click");
            if (animationEnabled.get()) {
                ScaleTransition st = new ScaleTransition(
                    Duration.millis(100),
                    btn
                );
                st.setToX(0.9);
                st.setToY(0.9);
                st.setAutoReverse(true);
                st.setCycleCount(2);
                st.setOnFinished(ev -> vm.submitMove(move));
                st.play();
            } else {
                vm.submitMove(move);
            }
        });
    }

    private void animateDraw() {
        resultSection.setStyle(
            "-fx-background-color: rgba(201,168,76,0.2); -fx-background-radius: 12;"
        );
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

        VBox cardContainer = new VBox(20);
        cardContainer.getStyleClass().add("stat-card");
        cardContainer.setPadding(new Insets(32));
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setMaxWidth(600);

        Label title = new Label("Choose a Buff");
        title.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #c9a84c; -fx-padding: 0 0 16 0;"
        );

        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        for (BuffDTO buff : choices) {
            BuffCard card = new BuffCard(buff);
            card.setOnMouseClicked(e -> {
                audioManager.play("click");
                vm.selectBuff(buff.getId());
            });

            if (animationEnabled.get()) {
                card.setScaleX(0.8);
                card.setScaleY(0.8);
                ScaleTransition st = new ScaleTransition(
                    Duration.millis(200),
                    card
                );
                st.setToX(1);
                st.setToY(1);
                st.play();

                card.setOnMouseEntered(e -> {
                    ScaleTransition hover = new ScaleTransition(
                        Duration.millis(100),
                        card
                    );
                    hover.setToX(1.05);
                    hover.setToY(1.05);
                    hover.play();
                });
                card.setOnMouseExited(e -> {
                    ScaleTransition unhover = new ScaleTransition(
                        Duration.millis(100),
                        card
                    );
                    unhover.setToX(1);
                    unhover.setToY(1);
                    unhover.play();
                });
            }

            cards.getChildren().add(card);
        }

        cardContainer.getChildren().addAll(title, cards);
        buffModal.getChildren().add(cardContainer);
    }

    private VBox createBuffModal() {
        VBox modal = new VBox();
        modal.setAlignment(Pos.CENTER);
        modal.setStyle("-fx-background-color: rgba(15,15,20,0.85);");
        modal.setMaxWidth(Double.MAX_VALUE);
        modal.setMaxHeight(Double.MAX_VALUE);
        return modal;
    }

    private VBox createStartRunOverlay() {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(15,15,20,0.95);");
        overlay.setMaxWidth(Double.MAX_VALUE);
        overlay.setMaxHeight(Double.MAX_VALUE);

        VBox card = new VBox(24);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        FontIcon crownIcon = new FontIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CROWN
        );
        crownIcon.setIconSize(64);
        crownIcon.setIconColor(javafx.scene.paint.Color.web("#c9a84c"));

        Label title = new Label("Begin the Journey");
        title.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;"
        );

        Label desc = new Label(
            "A new adventure awaits. Draw your cards, challenge the forces, and claim the Paper Crown!"
        );
        desc.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: #8888a0; -fx-alignment: center;"
        );
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button startBtn = new Button("Start New Run");
        startBtn.getStyleClass().addAll("action-button", "button-primary");
        startBtn.disableProperty().bind(vm.loading);
        startBtn.setOnAction(e -> {
            audioManager.play("click");
            vm.startNewRun();
        });

        card.getChildren().addAll(crownIcon, title, desc, startBtn);
        overlay.getChildren().add(card);
        return overlay;
    }

    private VBox createResumeOrAbandonOverlay() {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(15,15,20,0.95);");
        overlay.setMaxWidth(Double.MAX_VALUE);
        overlay.setMaxHeight(Double.MAX_VALUE);

        VBox card = new VBox(24);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        FontIcon flagIcon = new FontIcon(
            org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.FLAG
        );
        flagIcon.setIconSize(64);
        flagIcon.setIconColor(javafx.scene.paint.Color.web("#c9a84c"));

        Label title = new Label("You have an active run");
        title.setStyle(
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;"
        );

        Label desc = new Label("Continue your current journey or start fresh?");
        desc.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: #8888a0; -fx-alignment: center;"
        );
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Button resumeBtn = new javafx.scene.control.Button(
            "Resume Run"
        );
        resumeBtn.getStyleClass().addAll("action-button", "button-primary");
        resumeBtn.setOnAction(e -> {
            audioManager.play("click");
            mainContent.setVisible(true);
            mainContent.setManaged(true);
            if (animationEnabled.get()) {
                FadeTransition ft = new FadeTransition(
                    Duration.millis(300),
                    overlay
                );
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(ev -> {
                    overlay.setVisible(false);
                    overlay.setManaged(false);
                });
                ft.play();
            } else {
                overlay.setVisible(false);
                overlay.setManaged(false);
            }
        });

        javafx.scene.control.Button abandonBtn =
            new javafx.scene.control.Button("Abandon & New Run");
        abandonBtn.getStyleClass().addAll("action-button", "button-secondary");
        abandonBtn.setOnAction(e -> {
            audioManager.play("click");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            alert
                .getDialogPane()
                .getStylesheets()
                .add(
                    getClass().getResource("/styles/main.css").toExternalForm()
                );
            alert.getDialogPane().getStyleClass().add("custom-dialog");
            alert.setTitle("Abandon Run");
            alert.setHeaderText("Are you sure you want to abandon this run?");
            alert.setContentText(
                "Abandoning will end the current run immediately and record it as a loss. This action cannot be undone."
            );
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    vm.abandonRun(() -> vm.startNewRun());
                }
            });
        });

        card.getChildren().addAll(flagIcon, title, desc, resumeBtn, abandonBtn);
        overlay.getChildren().add(card);
        return overlay;
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
            for (int i = rounds.size() - 1; i >= 0; i--) {
                RoundDTO round = rounds.get(i);
                HBox row = new HBox(12);
                row.setPadding(new Insets(8, 12, 8, 12));
                row.setStyle(
                    "-fx-background-color: #22222e; -fx-background-radius: 8; -fx-border-color: #2a2a38; -fx-border-radius: 8;"
                );
                row.setAlignment(Pos.CENTER_LEFT);

                org.kordamp.ikonli.Ikon outcomeIcon = switch (
                    round.getOutcome()
                ) {
                    case WIN -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.CHECK_CIRCLE;
                    case LOSS -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.TIMES_CIRCLE;
                    case DRAW -> org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.MINUS_CIRCLE;
                };
                String outcomeColor = switch (round.getOutcome()) {
                    case WIN -> "#61a5c2";
                    case LOSS -> "#e65c6c";
                    case DRAW -> "#c9a84c";
                };

                FontIcon iconView = new FontIcon(outcomeIcon);
                iconView.setIconSize(16);
                iconView.setIconColor(
                    javafx.scene.paint.Color.web(outcomeColor)
                );

                Label moveLabel = new Label(
                    "R" +
                        round.getRoundNumber() +
                        ": You: " +
                        round.getPlayerMove() +
                        " | Bot: " +
                        round.getBotMove()
                );
                moveLabel.setStyle(
                    "-fx-text-fill: #d4d4dc; -fx-font-size: 13px; -fx-font-weight: bold;"
                );
                row.getChildren().addAll(iconView, moveLabel);
                historyFeed.getChildren().add(row);
            }
        }
    }

    private VBox createRunSummary() {
        VBox summary = new VBox();
        summary.setAlignment(Pos.CENTER);
        summary.setStyle("-fx-background-color: rgba(15,15,20,0.85);");
        summary.setMaxWidth(Double.MAX_VALUE);
        summary.setMaxHeight(Double.MAX_VALUE);

        VBox card = new VBox(24);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        Label runOver = new Label("Run Over");
        runOver.setStyle(
            "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e8e8f0;"
        );

        VBox statsBox = new VBox(12);
        statsBox.setAlignment(Pos.CENTER);

        Label roundsLabel = new Label();
        roundsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label winsLabel = new Label();
        winsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label lossesLabel = new Label();
        lossesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        Label drawsLabel = new Label();
        drawsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0c8;");
        statsBox
            .getChildren()
            .addAll(roundsLabel, winsLabel, lossesLabel, drawsLabel);

        Button returnBtn = new Button("Return to Dashboard");
        returnBtn.getStyleClass().addAll("action-button", "button-primary");
        returnBtn.setOnAction(e -> {
            audioManager.play("click");
            navigateAway();
        });

        card.getChildren().addAll(runOver, statsBox, returnBtn);
        summary.getChildren().add(card);

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
            FadeTransition ft = new FadeTransition(
                Duration.millis(500),
                runSummary
            );
            ft.setToValue(1);
            ft.play();
        } else {
            runSummary.setOpacity(1);
        }
    }

    private void showError(String msg) {
        Toast.show(rootStack, msg, Toast.Type.ERROR);
    }

    private void navigateAway() {
        if (hasNavigated) return;
        hasNavigated = true;
        vm.shutdown();
        onNavigateToDashboard.run();
    }
}
