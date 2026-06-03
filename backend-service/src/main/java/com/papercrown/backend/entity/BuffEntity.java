package com.papercrown.backend.entity;

import com.papercrown.shared.enums.BuffType;
import jakarta.persistence.*;

@Entity
@Table(name = "buffs")
public class BuffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "buff_type", nullable = false)
    private BuffType buffType;

    @Column(name = "effect_key", nullable = false)
    private String effectKey;

    @Column(nullable = false)
    private String icon;

    public BuffEntity() {}

    public BuffEntity(String name, String description, BuffType buffType, String effectKey, String icon) {
        this.name = name;
        this.description = description;
        this.buffType = buffType;
        this.effectKey = effectKey;
        this.icon = icon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BuffType getBuffType() { return buffType; }
    public void setBuffType(BuffType buffType) { this.buffType = buffType; }
    public String getEffectKey() { return effectKey; }
    public void setEffectKey(String effectKey) { this.effectKey = effectKey; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
