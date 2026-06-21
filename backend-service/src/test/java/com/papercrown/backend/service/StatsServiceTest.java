package com.papercrown.backend.service;

import com.papercrown.backend.entity.RoundEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.repository.RoundRepository;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.StatsDTO;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private RunRepository runRepository;

    @Mock
    private RoundRepository roundRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(runRepository, roundRepository);
    }

    @Test
    void getStatsReturnsZeroForNoRuns() {
        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of());
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());

        StatsDTO stats = statsService.getStats();

        assertEquals(0, stats.getTotalRuns());
        assertEquals(0, stats.getTotalWins());
        assertEquals(0, stats.getTotalLosses());
        assertEquals(0, stats.getTotalDraws());
        assertEquals(0.0, stats.getWinRate(), 0.001);
        assertEquals(0, stats.getBestStreak());
        assertEquals(0, stats.getCurrentStreak());
    }

    @Test
    void getStatsComputesCorrectAggregates() {
        RunEntity run1 = new RunEntity(3);
        run1.setId(1L);
        run1.setStatus(RunStatus.COMPLETED);
        run1.setTotalWins(5);
        run1.setTotalLosses(3);
        run1.setTotalDraws(2);
        run1.setRoundNumber(10);

        RunEntity run2 = new RunEntity(3);
        run2.setId(2L);
        run2.setStatus(RunStatus.COMPLETED);
        run2.setTotalWins(3);
        run2.setTotalLosses(4);
        run2.setTotalDraws(1);
        run2.setRoundNumber(8);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run1, run2));
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());
        when(roundRepository.findByRunIdOrderByRoundNumberDesc(2L))
                .thenReturn(List.of());

        StatsDTO stats = statsService.getStats();

        assertEquals(2, stats.getTotalRuns());
        assertEquals(8, stats.getTotalWins());
        assertEquals(7, stats.getTotalLosses());
        assertEquals(3, stats.getTotalDraws());
        assertEquals(18, stats.getTotalRounds());
    }

    @Test
    void getStatsComputesWinRate() {
        RunEntity run = new RunEntity(3);
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);
        run.setTotalWins(7);
        run.setTotalLosses(2);
        run.setTotalDraws(1);
        run.setRoundNumber(10);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run));
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());
        when(roundRepository.findByRunIdOrderByRoundNumberDesc(1L))
                .thenReturn(List.of());

        StatsDTO stats = statsService.getStats();

        assertEquals(70.0, stats.getWinRate(), 0.001);
    }

    @Test
    void bestStreakCalculatesConsecutiveWins() {
        // Arrange
        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run));

        // WIN, WIN, LOSS, WIN, WIN, WIN
        RoundEntity round1 = new RoundEntity(run, 1, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round2 = new RoundEntity(run, 2, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round3 = new RoundEntity(run, 3, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);
        RoundEntity round4 = new RoundEntity(run, 4, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round5 = new RoundEntity(run, 5, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round6 = new RoundEntity(run, 6, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);

        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of(round1, round2, round3, round4, round5, round6));
        when(roundRepository.findByRunIdOrderByRoundNumberDesc(1L))
                .thenReturn(List.of());

        // Act
        StatsDTO stats = statsService.getStats();

        // Assert
        assertEquals(3, stats.getBestStreak());
    }

    @Test
    void currentStreakCountsTrailingWins() {
        // Arrange
        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run));
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());

        // Trailing order (Desc): WIN, WIN, LOSS
        RoundEntity round3 = new RoundEntity(run, 3, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round2 = new RoundEntity(run, 2, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round1 = new RoundEntity(run, 1, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);

        when(roundRepository.findByRunIdOrderByRoundNumberDesc(1L))
                .thenReturn(List.of(round3, round2, round1));

        // Act
        StatsDTO stats = statsService.getStats();

        // Assert
        assertEquals(2, stats.getCurrentStreak());
    }

    @Test
    void currentStreakZeroWhenLastRoundIsLoss() {
        // Arrange
        RunEntity run = new RunEntity();
        run.setId(1L);
        run.setStatus(RunStatus.COMPLETED);

        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of(run));
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());

        // Trailing order (Desc): LOSS, WIN, WIN
        RoundEntity round3 = new RoundEntity(run, 3, Move.ROCK, Move.PAPER, RoundOutcome.LOSS);
        RoundEntity round2 = new RoundEntity(run, 2, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);
        RoundEntity round1 = new RoundEntity(run, 1, Move.ROCK, Move.SCISSORS, RoundOutcome.WIN);

        when(roundRepository.findByRunIdOrderByRoundNumberDesc(1L))
                .thenReturn(List.of(round3, round2, round1));

        // Act
        StatsDTO stats = statsService.getStats();

        // Assert
        assertEquals(0, stats.getCurrentStreak());
    }

    @Test
    void currentStreakZeroForNoRuns() {
        // Arrange
        when(runRepository.findByStatus(RunStatus.COMPLETED)).thenReturn(List.of());
        when(roundRepository.findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus.COMPLETED))
                .thenReturn(List.of());

        // Act
        StatsDTO stats = statsService.getStats();

        // Assert
        assertEquals(0, stats.getCurrentStreak());
    }
}
