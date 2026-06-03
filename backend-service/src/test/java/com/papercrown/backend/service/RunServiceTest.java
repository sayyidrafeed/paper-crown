package com.papercrown.backend.service;

import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.*;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.enums.RunStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RunServiceTest {

    private RunRepository runRepository;
    private RoundRepository roundRepository;
    private BuffRepository buffRepository;
    private RunBuffRepository runBuffRepository;
    private StatsService statsService;
    private AchievementService achievementService;
    private GameEngine gameEngine;
    private BuffService buffService;
    private EntityMapper mapper;
    private RunService runService;

    @BeforeEach
    void setUp() {
        runRepository = mock(RunRepository.class);
        roundRepository = mock(RoundRepository.class);
        buffRepository = mock(BuffRepository.class);
        runBuffRepository = mock(RunBuffRepository.class);
        statsService = mock(StatsService.class);
        achievementService = mock(AchievementService.class);
        gameEngine = new GameEngine();
        gameEngine.setSeed(42);
        mapper = new EntityMapper();
        buffService = new BuffService(buffRepository, runBuffRepository, mapper);
        buffService.setSeed(42);
        runService = new RunService(runRepository, roundRepository, buffRepository, runBuffRepository,
                gameEngine, buffService, achievementService, statsService, mapper);
    }

    @Test
    void startRunCreatesNewRunWithFullHp() {
        when(runRepository.findTopByStatusOrderByCreatedAtDesc(RunStatus.IN_PROGRESS)).thenReturn(Optional.empty());
        when(runRepository.save(any(RunEntity.class))).thenAnswer(i -> {
            RunEntity e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        RunDTO run = runService.startRun();

        assertNotNull(run);
        assertEquals(3, run.getCurrentHp());
        assertEquals(3, run.getMaxHp());
        assertEquals(RunStatus.IN_PROGRESS, run.getStatus());
    }

    @Test
    void startRunThrowsIfUnfinishedRunExists() {
        RunEntity existing = new RunEntity(3);
        existing.setId(1L);
        when(runRepository.findTopByStatusOrderByCreatedAtDesc(RunStatus.IN_PROGRESS))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> runService.startRun());
    }
}
