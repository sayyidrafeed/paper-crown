package com.papercrown.backend.service;

import com.papercrown.backend.entity.RoundEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.repository.RoundRepository;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.StatsDTO;
import com.papercrown.shared.enums.RoundOutcome;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final RunRepository runRepository;
    private final RoundRepository roundRepository;

    public StatsService(RunRepository runRepository, RoundRepository roundRepository) {
        this.runRepository = runRepository;
        this.roundRepository = roundRepository;
    }

    public StatsDTO getStats() {
        List<RunEntity> completedRuns = runRepository.findByStatus(RunStatus.COMPLETED);

        int totalRuns = completedRuns.size();
        int totalWins = completedRuns.stream().mapToInt(RunEntity::getTotalWins).sum();
        int totalLosses = completedRuns.stream().mapToInt(RunEntity::getTotalLosses).sum();
        int totalDraws = completedRuns.stream().mapToInt(RunEntity::getTotalDraws).sum();
        int totalRounds = totalWins + totalLosses + totalDraws;
        double winRate = totalRounds > 0 ? (double) totalWins / totalRounds * 100 : 0.0;

        List<RoundEntity> allRounds = roundRepository
                .findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED);

        int bestStreak = 0;
        int currentStreakVal = 0;
        Long lastRunId = null;

        for (RoundEntity round : allRounds) {
            if (lastRunId == null || !lastRunId.equals(round.getRun().getId())) {
                currentStreakVal = 0;
                lastRunId = round.getRun().getId();
            }

            if (round.getOutcome() == RoundOutcome.WIN) {
                currentStreakVal++;
                bestStreak = Math.max(bestStreak, currentStreakVal);
            } else {
                currentStreakVal = 0;
            }
        }

        int currentStreak = computeCurrentStreak(completedRuns);

        Map<String, Integer> moveUsage = new HashMap<>();
        moveUsage.put("ROCK", 0);
        moveUsage.put("PAPER", 0);
        moveUsage.put("SCISSORS", 0);

        return new StatsDTO(totalRuns, totalWins, totalLosses, totalDraws, totalRounds,
                winRate, currentStreak, bestStreak, moveUsage);
    }

    public void updateStats(RunEntity run) {
        // Stats are computed on read, but this hook exists for future caching
    }

    private int computeCurrentStreak(List<RunEntity> completedRuns) {
        if (completedRuns.isEmpty()) return 0;
        RunEntity last = completedRuns.get(completedRuns.size() - 1);
        List<RoundEntity> rounds = roundRepository.findByRunIdOrderByRoundNumberDesc(last.getId());

        int streak = 0;
        for (RoundEntity round : rounds) {
            if (round.getOutcome() == RoundOutcome.WIN) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
