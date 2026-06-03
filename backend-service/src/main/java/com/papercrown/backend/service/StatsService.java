package com.papercrown.backend.service;

import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.StatsDTO;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final RunRepository runRepository;

    public StatsService(RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    public StatsDTO getStats() {
        List<RunEntity> completedRuns = runRepository.findAll().stream()
                .filter(r -> r.getStatus() == RunStatus.COMPLETED)
                .toList();

        int totalRuns = completedRuns.size();
        int totalWins = completedRuns.stream().mapToInt(RunEntity::getTotalWins).sum();
        int totalLosses = completedRuns.stream().mapToInt(RunEntity::getTotalLosses).sum();
        int totalDraws = completedRuns.stream().mapToInt(RunEntity::getTotalDraws).sum();
        int totalRounds = totalWins + totalLosses + totalDraws;
        double winRate = totalRounds > 0 ? (double) totalWins / totalRounds * 100 : 0.0;

        int bestStreak = completedRuns.stream()
                .mapToInt(RunEntity::getTotalWins)
                .max()
                .orElse(0);

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
        return last.getTotalWins();
    }
}
