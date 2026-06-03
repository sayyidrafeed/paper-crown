package com.papercrown.shared.dto;

import com.papercrown.shared.enums.BuffType;

public class BuffDTO {
    private Long id;
    private String name;
    private String description;
    private BuffType buffType;
    private String effectKey;
    private String icon;

    public BuffDTO() {}

    public BuffDTO(Long id, String name, String description, BuffType buffType,
                   String effectKey, String icon) {
        this.id = id;
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
