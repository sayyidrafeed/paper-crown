package com.papercrown.backend.service;

import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.repository.RunRepository;
import com.papercrown.shared.dto.StatsDTO;
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

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(runRepository);
    }

    @Test
    void getStatsReturnsZeroForNoRuns() {
        when(runRepository.findAll()).thenReturn(List.of());

        StatsDTO stats = statsService.getStats();

        assertEquals(0, stats.getTotalRuns());
        assertEquals(0, stats.getTotalWins());
        assertEquals(0, stats.getTotalLosses());
        assertEquals(0, stats.getTotalDraws());
        assertEquals(0.0, stats.getWinRate(), 0.001);
    }

    @Test
    void getStatsComputesCorrectAggregates() {
        RunEntity run1 = new RunEntity(3);
        run1.setStatus(RunStatus.COMPLETED);
        run1.setTotalWins(5);
        run1.setTotalLosses(3);
        run1.setTotalDraws(2);
        run1.setRoundNumber(10);

        RunEntity run2 = new RunEntity(3);
        run2.setStatus(RunStatus.COMPLETED);
        run2.setTotalWins(3);
        run2.setTotalLosses(4);
        run2.setTotalDraws(1);
        run2.setRoundNumber(8);

        when(runRepository.findAll()).thenReturn(List.of(run1, run2));

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
        run.setStatus(RunStatus.COMPLETED);
        run.setTotalWins(7);
        run.setTotalLosses(2);
        run.setTotalDraws(1);
        run.setRoundNumber(10);

        when(runRepository.findAll()).thenReturn(List.of(run));

        StatsDTO stats = statsService.getStats();

        assertEquals(70.0, stats.getWinRate(), 0.001);
    }
}
