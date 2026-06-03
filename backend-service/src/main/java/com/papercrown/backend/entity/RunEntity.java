package com.papercrown.backend.entity;

import com.papercrown.shared.enums.RunStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "runs")
public class RunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "total_wins", nullable = false)
    private int totalWins;

    @Column(name = "total_losses", nullable = false)
    private int totalLosses;

    @Column(name = "total_draws", nullable = false)
    private int totalDraws;

    @Column(nullable = false)
    private int shield;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundEntity> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "run", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RunBuffEntity> runBuffs = new ArrayList<>();

    public RunEntity() {}

    public RunEntity(int maxHp) {
        this.status = RunStatus.IN_PROGRESS;
        this.currentHp = maxHp;
        this.maxHp = maxHp;
        this.roundNumber = 0;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.totalDraws = 0;
        this.shield = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }
    public int getTotalLosses() { return totalLosses; }
    public void setTotalLosses(int totalLosses) { this.totalLosses = totalLosses; }
    public int getTotalDraws() { return totalDraws; }
    public void setTotalDraws(int totalDraws) { this.totalDraws = totalDraws; }
    public int getShield() { return shield; }
    public void setShield(int shield) { this.shield = shield; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public List<RoundEntity> getRounds() { return rounds; }
    public void setRounds(List<RoundEntity> rounds) { this.rounds = rounds; }
    public List<RunBuffEntity> getRunBuffs() { return runBuffs; }
    public void setRunBuffs(List<RunBuffEntity> runBuffs) { this.runBuffs = runBuffs; }
}
