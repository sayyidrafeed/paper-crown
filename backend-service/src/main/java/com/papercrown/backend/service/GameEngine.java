package com.papercrown.backend.service;

import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class GameEngine {

    private static final Map<Move, Move> WIN_MAP = Map.of(
            Move.ROCK, Move.SCISSORS,
            Move.SCISSORS, Move.PAPER,
            Move.PAPER, Move.ROCK
    );

    private Random random = new java.security.SecureRandom();

    public RoundOutcome resolve(Move playerMove, Move botMove) {
        if (playerMove == botMove) {
            return RoundOutcome.DRAW;
        }
        return WIN_MAP.get(playerMove) == botMove ? RoundOutcome.WIN : RoundOutcome.LOSS;
    }

    public Move randomBotMove() {
        Move[] moves = Move.values();
        return moves[random.nextInt(moves.length)];
    }

    void setRandomForTesting(Random random) {
        this.random = random;
    }
}

