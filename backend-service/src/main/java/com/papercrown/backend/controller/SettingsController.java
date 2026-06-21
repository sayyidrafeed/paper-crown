package com.papercrown.backend.controller;

import com.papercrown.backend.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> getSetting(@PathVariable String key) {
        String value = settingsService.getSetting(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    @PutMapping
    public ResponseEntity<Void> updateSettings(@RequestBody Map<String, String> settings) {
        settingsService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> updateSetting(@PathVariable String key, @RequestBody String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        settingsService.updateSetting(key, value);
        return ResponseEntity.ok().build();
    }
}
