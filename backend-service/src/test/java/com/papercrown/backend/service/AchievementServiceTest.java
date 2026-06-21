package com.papercrown.backend.service;

import com.papercrown.backend.entity.AchievementEntity;
import com.papercrown.backend.entity.RoundEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.AchievementRepository;
import com.papercrown.backend.repository.RoundRepository;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import com.papercrown.shared.enums.RunStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private RunRepository runRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private EntityMapper mapper;

    private AchievementService achievementService;

    @BeforeEach
    void setUp() {
        achievementService = new AchievementService(achievementRepository, runRepository, roundRepository, mapper);
    }

    @Test
    void bestWinStreakCalculatesConsecutiveWins() {
        // Arrange
        AchievementEntity winStreakAchievement = new AchievementEntity(
                "Win Streak", "Win 5 rounds in a row", "icon", "WIN_STREAK", 5
        );
        when(achievementRepository.findAll()).thenReturn(List.of(winStreakAchievement));
        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of());

        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);

        // WIN, WIN, LOSS, WIN, WIN, WIN
        RoundEntity round1 = new RoundEntity(run, 1, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round2 = new RoundEntity(run, 2, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round3 = new RoundEntity(run, 3, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);
        RoundEntity round4 = new RoundEntity(run, 4, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round5 = new RoundEntity(run, 5, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round6 = new RoundEntity(run, 6, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);

        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of(round1, round2, round3, round4, round5, round6));

        // Act
        achievementService.checkAchievements();

        // Assert
        assertEquals(3, winStreakAchievement.getProgress());
        assertFalse(winStreakAchievement.isUnlocked());
        verify(achievementRepository, times(1)).save(winStreakAchievement);
    }

    @Test
    void bestWinStreakResetsAcrossRuns() {
        // Arrange
        AchievementEntity winStreakAchievement = new AchievementEntity(
                "Win Streak", "Win 3 rounds in a row", "icon", "WIN_STREAK", 3
        );
        when(achievementRepository.findAll()).thenReturn(List.of(winStreakAchievement));
        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of());

        RunEntity runA = new RunEntity();
        runA.setId(1L);
        runA.setStatus(RunStatus.COMPLETED);

        RunEntity runB = new RunEntity();
        runB.setId(2L);
        runB.setStatus(RunStatus.COMPLETED);

        // Run A: WIN, WIN
        RoundEntity round1 = new RoundEntity(runA, 1, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round2 = new RoundEntity(runA, 2, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        // Run B: WIN
        RoundEntity round3 = new RoundEntity(runB, 1, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);

        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of(round1, round2, round3));

        // Act
        achievementService.checkAchievements();

        // Assert
        assertEquals(2, winStreakAchievement.getProgress());
        assertFalse(winStreakAchievement.isUnlocked());
    }

    @Test
    void bestWinStreakZeroForNoWins() {
        // Arrange
        AchievementEntity winStreakAchievement = new AchievementEntity(
                "Win Streak", "Win 3 rounds in a row", "icon", "WIN_STREAK", 3
        );
        when(achievementRepository.findAll()).thenReturn(List.of(winStreakAchievement));
        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of());

        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);

        // LOSS, DRAW, LOSS
        RoundEntity round1 = new RoundEntity(run, 1, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);
        RoundEntity round2 = new RoundEntity(run, 2, Move.ROCK, Move.ROCK, RoundOutcome.DRAW);
        RoundEntity round3 = new RoundEntity(run, 3, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);

        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of(round1, round2, round3));

        // Act
        achievementService.checkAchievements();

        // Assert
        assertEquals(0, winStreakAchievement.getProgress());
        assertFalse(winStreakAchievement.isUnlocked());
    }

    @Test
    void roundsSurvivedAndTotalRoundsCalculatedCorrectly() {
        // Arrange
        AchievementEntity totalRoundsAchievement = new AchievementEntity(
                "Total Rounds", "Play 10 rounds", "icon", "TOTAL_ROUNDS", 10
        );
        AchievementEntity roundsSurvivedAchievement = new AchievementEntity(
                "Rounds Survived", "Survive 5 rounds in a single run", "icon", "ROUNDS_SURVIVED", 5
        );
        when(achievementRepository.findAll()).thenReturn(List.of(totalRoundsAchievement, roundsSurvivedAchievement));

        RunEntity run1 = new RunEntity();
        run1.setId(1L);
        run1.setRoundNumber(5);
        run1.setStatus(RunStatus.COMPLETED);

        RunEntity run2 = new RunEntity();
        run2.setId(2L);
        run2.setRoundNumber(3);
        run2.setStatus(RunStatus.COMPLETED);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run1, run2));

        // Act
        achievementService.checkAchievements();

        // Assert
        assertEquals(8, totalRoundsAchievement.getProgress());
        assertEquals(5, roundsSurvivedAchievement.getProgress());
        assertTrue(roundsSurvivedAchievement.isUnlocked());
        assertFalse(totalRoundsAchievement.isUnlocked());
    }
}
