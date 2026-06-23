package com.papercrown.desktop.viewmodel;

import com.papercrown.desktop.service.BackendClient;
import com.papercrown.desktop.util.AudioManager;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class SettingsViewModel {

    private static final String KEY_FULLSCREEN = "fullscreen";
    private static final String KEY_VOLUME = "master_volume";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_ANIMATION = "animation_enabled";

    private final BackendClient client;
    private final AudioManager audioManager;
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "settings-vm");
        t.setDaemon(true);
        return t;
    });

    public final SimpleBooleanProperty fullscreen = new SimpleBooleanProperty(false);
    public final SimpleDoubleProperty masterVolume = new SimpleDoubleProperty(0.5);
    public final SimpleBooleanProperty soundEnabled = new SimpleBooleanProperty(true);
    public final SimpleBooleanProperty animationEnabled = new SimpleBooleanProperty(true);

    private boolean loaded = false;

    public SettingsViewModel(BackendClient client, AudioManager audioManager) {
        this.client = client;
        this.audioManager = audioManager;

        fullscreen.addListener((obs, old, val) -> { if (loaded) save(); });
        masterVolume.addListener((obs, old, val) -> { if (loaded) save(); });
        soundEnabled.addListener((obs, old, val) -> { if (loaded) save(); });
        animationEnabled.addListener((obs, old, val) -> { if (loaded) save(); });
    }

    public void load() {
        executor.execute(() -> {
            try {
                Map<String, String> settings = client.getSettings();
                Platform.runLater(() -> {
                    loaded = false;
                    fullscreen.set("true".equals(settings.getOrDefault(KEY_FULLSCREEN, "false")));
                    masterVolume.set(Double.parseDouble(settings.getOrDefault(KEY_VOLUME, "0.5")));
                    soundEnabled.set("true".equals(settings.getOrDefault(KEY_SOUND, "true")));
                    animationEnabled.set("true".equals(settings.getOrDefault(KEY_ANIMATION, "true")));
                    
                    // Apply loaded settings immediately without triggering save
                    audioManager.setSoundEnabled(soundEnabled.get());
                    audioManager.setMasterVolume(masterVolume.get());
                    
                    loaded = true;
                });
            } catch (Exception ignored) {}
        });
    }

    private void save() {
        audioManager.setSoundEnabled(soundEnabled.get());
        audioManager.setMasterVolume(masterVolume.get());
        Map<String, String> settings = new HashMap<>();
        settings.put(KEY_FULLSCREEN, String.valueOf(fullscreen.get()));
        settings.put(KEY_VOLUME, String.valueOf(masterVolume.get()));
        settings.put(KEY_SOUND, String.valueOf(soundEnabled.get()));
        settings.put(KEY_ANIMATION, String.valueOf(animationEnabled.get()));
        executor.execute(() -> {
            try {
                client.updateSettings(settings);
            } catch (Exception ignored) {}
        });
    }
}
