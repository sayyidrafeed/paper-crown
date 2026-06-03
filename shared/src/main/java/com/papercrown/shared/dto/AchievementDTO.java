package com.papercrown.shared.dto;

import java.time.LocalDateTime;

public class AchievementDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String criteriaType;
    private int criteriaValue;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private int progress;

    public AchievementDTO() {}

    public AchievementDTO(Long id, String name, String description, String icon,
                          String criteriaType, int criteriaValue, boolean unlocked,
                          LocalDateTime unlockedAt, int progress) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.criteriaType = criteriaType;
        this.criteriaValue = criteriaValue;
        this.unlocked = unlocked;
        this.unlockedAt = unlockedAt;
        this.progress = progress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCriteriaType() { return criteriaType; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public int getCriteriaValue() { return criteriaValue; }
    public void setCriteriaValue(int criteriaValue) { this.criteriaValue = criteriaValue; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
