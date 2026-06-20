package com.papercrown.desktop.component;

import com.papercrown.shared.dto.AchievementDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class AchievementCard extends VBox {

    public AchievementCard(AchievementDTO dto) {
        getStyleClass().add("achievement-card");
        setPadding(new Insets(16));
        setSpacing(8);
        setAlignment(Pos.CENTER);
        setPrefWidth(180);
        setMinWidth(140);

        String iconStr = dto.getIcon() != null ? dto.getIcon() : "fas-trophy";
        FontIcon icon = new FontIcon(resolveIcon(iconStr));
        icon.setIconSize(28);
        icon.getStyleClass().add("achievement-icon");

        Label nameLabel = new Label(dto.getName());
        nameLabel.getStyleClass().add("achievement-name");
        nameLabel.setWrapText(true);

        Label descLabel = new Label(dto.getDescription());
        descLabel.getStyleClass().add("achievement-desc");
        descLabel.setWrapText(true);

        getChildren().addAll(icon, nameLabel, descLabel);

        if (dto.isUnlocked()) {
            getStyleClass().add("achievement-card-unlocked");
            FontIcon check = new FontIcon(FontAwesomeSolid.CHECK_CIRCLE);
            check.setIconSize(20);
            check.getStyleClass().add("achievement-checkmark");
            getChildren().add(check);
        } else if (dto.getProgress() > 0) {
            getStyleClass().add("achievement-card-progress");
            ProgressBar bar = new ProgressBar(
                    (double) dto.getProgress() / Math.max(dto.getCriteriaValue(), 1));
            bar.setPrefWidth(120);
            bar.getStyleClass().add("achievement-progress-bar");
            Label progressLabel = new Label(dto.getProgress() + " / " + dto.getCriteriaValue());
            progressLabel.getStyleClass().add("achievement-progress-text");
            getChildren().addAll(bar, progressLabel);
        } else {
            getStyleClass().add("achievement-card-locked");
        }
    }

    private static Ikon resolveIcon(String iconStr) {
        return switch (iconStr) {
            case "fas-trophy" -> FontAwesomeSolid.TROPHY;
            case "fas-star" -> FontAwesomeSolid.STAR;
            case "fas-shield-alt" -> FontAwesomeSolid.SHIELD_ALT;
            case "fas-heart" -> FontAwesomeSolid.HEART;
            case "fas-heartbeat" -> FontAwesomeSolid.HEARTBEAT;
            case "fas-fire" -> FontAwesomeSolid.FIRE;
            case "fas-sync-alt" -> FontAwesomeSolid.SYNC_ALT;
            case "fas-skull" -> FontAwesomeSolid.SKULL;
            case "fas-gem" -> FontAwesomeSolid.GEM;
            case "fas-robot" -> FontAwesomeSolid.ROBOT;
            default -> FontAwesomeSolid.TROPHY;
        };
    }
}
