package com.papercrown.desktop.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AudioManager {

    private static final Logger LOG = Logger.getLogger(AudioManager.class.getName());

    private final Map<String, MediaPlayer> players = new HashMap<>();
    private boolean soundEnabled = true;
    private double masterVolume = 0.5;

    public AudioManager() {
        loadSound("click", "/sounds/click.wav");
        loadSound("win", "/sounds/win.wav");
        loadSound("lose", "/sounds/lose.wav");
        loadSound("achievement", "/sounds/achievement.wav");
    }

    private void loadSound(String name, String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                LOG.warning("Sound file not found: " + path);
                return;
            }
            Media media = new Media(resource.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(masterVolume);
            players.put(name, player);
        } catch (Exception e) {
            LOG.warning("Failed to load sound " + name + ": " + e.getMessage());
        }
    }

    public void play(String name) {
        if (!soundEnabled) return;
        MediaPlayer player = players.get(name);
        if (player == null) {
            LOG.warning("Sound not loaded: " + name);
            return;
        }
        player.stop();
        player.setVolume(masterVolume);
        player.play();
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0, Math.min(1, volume));
        for (MediaPlayer player : players.values()) {
            player.setVolume(this.masterVolume);
        }
    }
}
