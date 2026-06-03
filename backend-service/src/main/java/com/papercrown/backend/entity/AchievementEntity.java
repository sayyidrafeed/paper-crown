package com.papercrown.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
public class AchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String icon;

    @Column(name = "criteria_type", nullable = false)
    private String criteriaType;

    @Column(name = "criteria_value", nullable = false)
    private int criteriaValue;

    @Column(nullable = false)
    private boolean unlocked;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private int progress;

    public AchievementEntity() {}

    public AchievementEntity(String name, String description, String icon,
                             String criteriaType, int criteriaValue) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.criteriaType = criteriaType;
        this.criteriaValue = criteriaValue;
        this.unlocked = false;
        this.progress = 0;
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
