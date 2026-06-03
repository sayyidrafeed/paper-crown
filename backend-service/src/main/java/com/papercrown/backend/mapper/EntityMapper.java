package com.papercrown.backend.mapper;

import com.papercrown.backend.entity.*;
import com.papercrown.shared.dto.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public RunDTO toRunDTO(RunEntity entity) {
        if (entity == null) return null;
        RunDTO dto = new RunDTO(
                entity.getId(),
                entity.getStatus(),
                entity.getCurrentHp(),
                entity.getMaxHp(),
                entity.getRoundNumber(),
                entity.getTotalWins(),
                entity.getTotalLosses(),
                entity.getTotalDraws(),
                entity.getShield(),
                entity.getCreatedAt(),
                entity.getEndedAt()
        );
        if (entity.getRounds() != null) {
            dto.setRounds(entity.getRounds().stream().map(this::toRoundDTO).collect(Collectors.toList()));
        }
        if (entity.getRunBuffs() != null) {
            dto.setActiveBuffs(entity.getRunBuffs().stream()
                    .filter(rb -> !rb.isConsumed())
                    .map(rb -> toBuffDTO(rb.getBuff()))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public RunDTO toRunSummaryDTO(RunEntity entity) {
        if (entity == null) return null;
        return new RunDTO(
                entity.getId(),
                entity.getStatus(),
                entity.getCurrentHp(),
                entity.getMaxHp(),
                entity.getRoundNumber(),
                entity.getTotalWins(),
                entity.getTotalLosses(),
                entity.getTotalDraws(),
                entity.getShield(),
                entity.getCreatedAt(),
                entity.getEndedAt()
        );
    }

    public RoundDTO toRoundDTO(RoundEntity entity) {
        if (entity == null) return null;
        return new RoundDTO(
                entity.getId(),
                entity.getRoundNumber(),
                entity.getPlayerMove(),
                entity.getBotMove(),
                entity.getOutcome(),
                entity.getCreatedAt()
        );
    }

    public BuffDTO toBuffDTO(BuffEntity entity) {
        if (entity == null) return null;
        return new BuffDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getBuffType(),
                entity.getEffectKey(),
                entity.getIcon()
        );
    }

    public AchievementDTO toAchievementDTO(AchievementEntity entity) {
        if (entity == null) return null;
        return new AchievementDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIcon(),
                entity.getCriteriaType(),
                entity.getCriteriaValue(),
                entity.isUnlocked(),
                entity.getUnlockedAt(),
                entity.getProgress()
        );
    }

    public List<AchievementDTO> toAchievementDTOList(List<AchievementEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toAchievementDTO).collect(Collectors.toList());
    }

    public SettingDTO toSettingDTO(SettingEntity entity) {
        if (entity == null) return null;
        return new SettingDTO(entity.getSettingKey(), entity.getSettingValue());
    }
}
