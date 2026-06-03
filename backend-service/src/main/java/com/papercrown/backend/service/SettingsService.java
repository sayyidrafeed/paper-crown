package com.papercrown.backend.service;

import com.papercrown.backend.entity.SettingEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.SettingRepository;
import com.papercrown.shared.dto.SettingDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SettingsService {

    private final SettingRepository settingRepository;
    private final EntityMapper mapper;

    public SettingsService(SettingRepository settingRepository, EntityMapper mapper) {
        this.settingRepository = settingRepository;
        this.mapper = mapper;
    }

    public Map<String, String> getAllSettings() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(SettingEntity::getSettingKey, SettingEntity::getSettingValue));
    }

    public String getSetting(String key) {
        return settingRepository.findBySettingKey(key)
                .map(SettingEntity::getSettingValue)
                .orElse(null);
    }

    public void updateSetting(String key, String value) {
        SettingEntity entity = settingRepository.findBySettingKey(key)
                .orElseGet(() -> new SettingEntity(key, value));
        entity.setSettingValue(value);
        settingRepository.save(entity);
    }

    public void updateSettings(Map<String, String> settings) {
        settings.forEach(this::updateSetting);
    }
}
