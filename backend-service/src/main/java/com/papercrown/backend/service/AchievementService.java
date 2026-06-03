package com.papercrown.backend.service;

import com.papercrown.backend.entity.AchievementEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.AchievementRepository;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final RunRepository runRepository;
    private final EntityMapper mapper;

    public AchievementService(AchievementRepository achievementRepository,
                              RunRepository runRepository, EntityMapper mapper) {
        this.achievementRepository = achievementRepository;
        this.runRepository = runRepository;
        this.mapper = mapper;
    }

    public List<AchievementDTO> getAllAchievements() {
        return mapper.toAchievementDTOList(achievementRepository.findAllByOrderByName());
    }

    public void checkAchievements() {
        List<AchievementEntity> all = achievementRepository.findAll();

        int totalWins = runRepository.findAll().stream()
                .mapToInt(r -> r.getStatus() == RunStatus.COMPLETED ? r.getTotalWins() : 0)
                .sum();
        int totalRunsCompleted = runRepository.countByStatus(RunStatus.COMPLETED);
        int totalRounds = runRepository.findAll().stream()
                .mapToInt(r -> r.getStatus() == RunStatus.COMPLETED ? r.getRoundNumber() : 0)
                .sum();

        for (AchievementEntity ach : all) {
            if (ach.isUnlocked()) continue;

            int progress = switch (ach.getCriteriaType()) {
                case "TOTAL_WINS" -> totalWins;
                case "RUNS_COMPLETED" -> totalRunsCompleted;
                case "TOTAL_ROUNDS" -> totalRounds;
                case "ROUNDS_SURVIVED" -> totalRounds;
                case "WIN_STREAK" -> getBestWinStreak();
                case "RUNS_WON" -> getRunsWon();
                default -> 0;
            };

            ach.setProgress(Math.min(progress, ach.getCriteriaValue()));

            if (progress >= ach.getCriteriaValue()) {
                ach.setUnlocked(true);
                ach.setUnlockedAt(LocalDateTime.now());
            }

            achievementRepository.save(ach);
        }
    }

    private int getBestWinStreak() {
        return runRepository.findAll().stream()
                .mapToInt(r -> r.getStatus() == RunStatus.COMPLETED ? r.getTotalWins() : 0)
                .max()
                .orElse(0);
    }

    private int getRunsWon() {
        return (int) runRepository.findAll().stream()
                .filter(r -> r.getStatus() == RunStatus.COMPLETED && r.getCurrentHp() > 0)
                .count();
    }
}
