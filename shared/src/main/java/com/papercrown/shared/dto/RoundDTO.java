package com.papercrown.shared.dto;

import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import java.time.LocalDateTime;

public class RoundDTO {
    private Long id;
    private int roundNumber;
    private Move playerMove;
    private Move botMove;
    private RoundOutcome outcome;
    private LocalDateTime createdAt;

    public RoundDTO() {}

    public RoundDTO(Long id, int roundNumber, Move playerMove, Move botMove,
                    RoundOutcome outcome, LocalDateTime createdAt) {
        this.id = id;
        this.roundNumber = roundNumber;
        this.playerMove = playerMove;
        this.botMove = botMove;
        this.outcome = outcome;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public Move getPlayerMove() { return playerMove; }
    public void setPlayerMove(Move playerMove) { this.playerMove = playerMove; }
    public Move getBotMove() { return botMove; }
    public void setBotMove(Move botMove) { this.botMove = botMove; }
    public RoundOutcome getOutcome() { return outcome; }
    public void setOutcome(RoundOutcome outcome) { this.outcome = outcome; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
