package com.papercrown.backend.service;

import com.papercrown.backend.entity.BuffEntity;
import com.papercrown.backend.entity.RoundEntity;
import com.papercrown.backend.entity.RunBuffEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.*;
import com.papercrown.shared.dto.MoveResponse;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.shared.enums.BuffType;
import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
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
        gameEngine.setRandomForTesting(new java.util.Random(42));
        mapper = new EntityMapper();
        buffService = new BuffService(buffRepository, runBuffRepository, mapper);
        buffService.setRandomForTesting(new java.util.Random(42));
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

    @Test
    void winDoesNotConsumeDrawAsWinBuff() {
        RunEntity run = new RunEntity(3);
        run.setId(1L);

        BuffEntity drawAsWinBuff = new BuffEntity("Draw As Win", "Converts draw to win", BuffType.UTILITY, "DRAW_AS_WIN", "🔄");
        drawAsWinBuff.setId(1L);
        RunBuffEntity runBuff = new RunBuffEntity(run, drawAsWinBuff);
        run.getRunBuffs().add(runBuff);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runRepository.save(any(RunEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(roundRepository.save(any(RoundEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Force bot to play SCISSORS so player ROCK wins
        gameEngine.setRandomForTesting(new java.util.Random(0));
        // Find a seed where bot plays SCISSORS
        for (long seed = 0; seed < 1000; seed++) {
            gameEngine.setRandomForTesting(new java.util.Random(seed));
            Move botMove = gameEngine.randomBotMove();
            if (botMove == Move.SCISSORS) {
                gameEngine.setRandomForTesting(new java.util.Random(seed));
                break;
            }
        }

        MoveResponse response = runService.submitMove(1L, Move.ROCK);

        assertEquals(RoundOutcome.WIN, response.getOutcome());
        assertFalse(runBuff.isConsumed(), "DRAW_AS_WIN buff should NOT be consumed on a WIN");
    }

    @Test
    void drawWithDrawAsWinBuffCountsAsWin() {
        RunEntity run = new RunEntity(3);
        run.setId(1L);

        BuffEntity drawAsWinBuff = new BuffEntity("Draw As Win", "Converts draw to win", BuffType.UTILITY, "DRAW_AS_WIN", "🔄");
        drawAsWinBuff.setId(1L);
        RunBuffEntity runBuff = new RunBuffEntity(run, drawAsWinBuff);
        run.getRunBuffs().add(runBuff);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runRepository.save(any(RunEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(roundRepository.save(any(RoundEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Force bot to play ROCK so player ROCK draws
        for (long seed = 0; seed < 1000; seed++) {
            gameEngine.setRandomForTesting(new java.util.Random(seed));
            Move botMove = gameEngine.randomBotMove();
            if (botMove == Move.ROCK) {
                gameEngine.setRandomForTesting(new java.util.Random(seed));
                break;
            }
        }

        MoveResponse response = runService.submitMove(1L, Move.ROCK);

        assertEquals(RoundOutcome.WIN, response.getOutcome(), "DRAW should be converted to WIN in response");
        assertEquals(1, run.getTotalWins(), "DRAW with DRAW_AS_WIN buff should count as a win");
        assertTrue(runBuff.isConsumed(), "DRAW_AS_WIN buff should be consumed after use");

        // Verify RoundEntity stores the ORIGINAL outcome (DRAW), not the effective one
        RoundEntity savedRound = run.getRounds().get(0);
        assertEquals(RoundOutcome.DRAW, savedRound.getOutcome(),
                "RoundEntity should store original DRAW outcome for accurate history");
    }

    @Test
    void roundEntityStoresCorrectOutcomeOnNormalWin() {
        RunEntity run = new RunEntity(3);
        run.setId(1L);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runRepository.save(any(RunEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(roundRepository.save(any(RoundEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Force bot to play SCISSORS so player ROCK wins
        for (long seed = 0; seed < 1000; seed++) {
            gameEngine.setRandomForTesting(new java.util.Random(seed));
            Move botMove = gameEngine.randomBotMove();
            if (botMove == Move.SCISSORS) {
                gameEngine.setRandomForTesting(new java.util.Random(seed));
                break;
            }
        }

        MoveResponse response = runService.submitMove(1L, Move.ROCK);

        assertEquals(RoundOutcome.WIN, response.getOutcome());
        RoundEntity savedRound = run.getRounds().get(0);
        assertEquals(RoundOutcome.WIN, savedRound.getOutcome(),
                "RoundEntity outcome should match the actual RPS result");
        assertEquals(Move.ROCK, savedRound.getPlayerMove());
        assertEquals(Move.SCISSORS, savedRound.getBotMove());
    }

    @Test
    void roundEntityStoresCorrectOutcomeOnLoss() {
        RunEntity run = new RunEntity(3);
        run.setId(1L);

        when(runRepository.findById(1L)).thenReturn(Optional.of(run));
        when(runRepository.save(any(RunEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(roundRepository.save(any(RoundEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Force bot to play PAPER so player ROCK loses
        for (long seed = 0; seed < 1000; seed++) {
            gameEngine.setRandomForTesting(new java.util.Random(seed));
            Move botMove = gameEngine.randomBotMove();
            if (botMove == Move.PAPER) {
                gameEngine.setRandomForTesting(new java.util.Random(seed));
                break;
            }
        }

        MoveResponse response = runService.submitMove(1L, Move.ROCK);

        assertEquals(RoundOutcome.LOSS, response.getOutcome());
        RoundEntity savedRound = run.getRounds().get(0);
        assertEquals(RoundOutcome.LOSS, savedRound.getOutcome(),
                "RoundEntity outcome should match: ROCK vs PAPER = LOSS");
        assertEquals(Move.ROCK, savedRound.getPlayerMove());
        assertEquals(Move.PAPER, savedRound.getBotMove());
    }
}
