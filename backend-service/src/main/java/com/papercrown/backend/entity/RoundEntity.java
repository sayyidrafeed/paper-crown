package com.papercrown.backend.entity;

import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
public class RoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private RunEntity run;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_move", nullable = false)
    private Move playerMove;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_move", nullable = false)
    private Move botMove;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundOutcome outcome;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RoundEntity() {}

    public RoundEntity(RunEntity run, int roundNumber, Move playerMove, Move botMove, RoundOutcome outcome) {
        this.run = run;
        this.roundNumber = roundNumber;
        this.playerMove = playerMove;
        this.botMove = botMove;
        this.outcome = outcome;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RunEntity getRun() { return run; }
    public void setRun(RunEntity run) { this.run = run; }
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
