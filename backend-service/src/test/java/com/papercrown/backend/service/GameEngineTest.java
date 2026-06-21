package com.papercrown.backend.service;

import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.setRandomForTesting(new java.util.Random(42));
    }

    @Test
    void rockBeatsScissors() {
        assertEquals(RoundOutcome.WIN, engine.resolve(Move.ROCK, Move.SCISSORS));
    }

    @Test
    void rockLosesToPaper() {
        assertEquals(RoundOutcome.LOSS, engine.resolve(Move.ROCK, Move.PAPER));
    }

    @Test
    void rockDrawsWithRock() {
        assertEquals(RoundOutcome.DRAW, engine.resolve(Move.ROCK, Move.ROCK));
    }

    @Test
    void paperBeatsRock() {
        assertEquals(RoundOutcome.WIN, engine.resolve(Move.PAPER, Move.ROCK));
    }

    @Test
    void paperLosesToScissors() {
        assertEquals(RoundOutcome.LOSS, engine.resolve(Move.PAPER, Move.SCISSORS));
    }

    @Test
    void paperDrawsWithPaper() {
        assertEquals(RoundOutcome.DRAW, engine.resolve(Move.PAPER, Move.PAPER));
    }

    @Test
    void scissorsBeatsPaper() {
        assertEquals(RoundOutcome.WIN, engine.resolve(Move.SCISSORS, Move.PAPER));
    }

    @Test
    void scissorsLosesToRock() {
        assertEquals(RoundOutcome.LOSS, engine.resolve(Move.SCISSORS, Move.ROCK));
    }

    @Test
    void scissorsDrawsWithScissors() {
        assertEquals(RoundOutcome.DRAW, engine.resolve(Move.SCISSORS, Move.SCISSORS));
    }

    @Test
    void randomBotMoveReturnsValidMove() {
        for (int i = 0; i < 100; i++) {
            Move move = engine.randomBotMove();
            assertNotNull(move);
            assertTrue(move == Move.ROCK || move == Move.PAPER || move == Move.SCISSORS);
        }
    }

    @Test
    void resolveWithEveryCombination() {
        for (Move player : Move.values()) {
            for (Move bot : Move.values()) {
                RoundOutcome outcome = engine.resolve(player, bot);
                assertNotNull(outcome);
                if (player == bot) {
                    assertEquals(RoundOutcome.DRAW, outcome);
                }
            }
        }
    }

    @Test
    void seededBotMoveIsDeterministic() {
        engine.setRandomForTesting(new java.util.Random(123));
        Move first = engine.randomBotMove();
        engine.setRandomForTesting(new java.util.Random(123));
        Move second = engine.randomBotMove();
        assertEquals(first, second);
    }
}
