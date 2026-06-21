package com.papercrown.backend.controller;

import com.papercrown.backend.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class SettingsControllerTest {

    private SettingsService settingsService;
    private SettingsController settingsController;

    @BeforeEach
    void setUp() {
        settingsService = Mockito.mock(SettingsService.class);
        settingsController = new SettingsController(settingsService);
    }

    @Test
    void updateSettingStripsDoubleQuotes() {
        // Arrange
        String key = "theme";
        String rawValue = "\"dark\"";

        // Act
        ResponseEntity<Void> response = settingsController.updateSetting(key, rawValue);

        // Assert
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(settingsService).updateSetting(Mockito.eq("theme"), valueCaptor.capture());
        assertEquals("dark", valueCaptor.getValue());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateSettingDoesNotStripIfNoQuotes() {
        // Arrange
        String key = "volume";
        String rawValue = "80";

        // Act
        ResponseEntity<Void> response = settingsController.updateSetting(key, rawValue);

        // Assert
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(settingsService).updateSetting(Mockito.eq("volume"), valueCaptor.capture());
        assertEquals("80", valueCaptor.getValue());
        assertEquals(200, response.getStatusCode().value());
    }
}
