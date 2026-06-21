package com.papercrown.backend.service;

import com.papercrown.backend.entity.AchievementEntity;
import com.papercrown.backend.entity.RoundEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.AchievementRepository;
import com.papercrown.backend.repository.RoundRepository;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.shared.enums.RoundOutcome;
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
    private final RoundRepository roundRepository;
    private final EntityMapper mapper;

    public AchievementService(AchievementRepository achievementRepository,
                              RunRepository runRepository,
                              RoundRepository roundRepository,
                              EntityMapper mapper) {
        this.achievementRepository = achievementRepository;
        this.runRepository = runRepository;
        this.roundRepository = roundRepository;
        this.mapper = mapper;
    }

    public List<AchievementDTO> getAllAchievements() {
        return mapper.toAchievementDTOList(achievementRepository.findAllByOrderByName());
    }

    public void checkAchievements() {
        List<AchievementEntity> all = achievementRepository.findAll();
        List<RunEntity> completedRuns = runRepository.findByStatus(RunStatus.COMPLETED);

        int totalWins = completedRuns.stream()
                .mapToInt(RunEntity::getTotalWins)
                .sum();
        int totalRunsCompleted = completedRuns.size();
        int totalRounds = completedRuns.stream()
                .mapToInt(RunEntity::getRoundNumber)
                .sum();
        int maxRoundsSurvived = completedRuns.stream()
                .mapToInt(RunEntity::getRoundNumber)
                .max()
                .orElse(0);

        for (AchievementEntity ach : all) {
            if (ach.isUnlocked()) continue;

            int progress = switch (ach.getCriteriaType()) {
                case "TOTAL_WINS" -> totalWins;
                case "RUNS_COMPLETED" -> totalRunsCompleted;
                case "TOTAL_ROUNDS" -> totalRounds;
                case "ROUNDS_SURVIVED" -> maxRoundsSurvived;
                case "WIN_STREAK" -> getBestWinStreak();
                case "RUNS_WON" -> getRunsWon(completedRuns);
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
        List<RoundEntity> allRounds = roundRepository
                .findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED);

        int bestStreak = 0;
        int currentStreak = 0;
        Long lastRunId = null;

        for (RoundEntity round : allRounds) {
            // Reset streak ketika pindah ke run yang berbeda
            if (lastRunId == null || !lastRunId.equals(round.getRun().getId())) {
                currentStreak = 0;
                lastRunId = round.getRun().getId();
            }

            if (round.getOutcome() == RoundOutcome.WIN) {
                currentStreak++;
                bestStreak = Math.max(bestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return bestStreak;
    }

    private int getRunsWon(List<RunEntity> completedRuns) {
        return (int) completedRuns.stream()
                .filter(r -> r.getCurrentHp() > 0)
                .count();
    }
}
