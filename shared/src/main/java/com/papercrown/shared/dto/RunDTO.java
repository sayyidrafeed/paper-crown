package com.papercrown.shared.dto;

import com.papercrown.shared.enums.RunStatus;
import java.time.LocalDateTime;
import java.util.List;

public class RunDTO {
    private Long id;
    private RunStatus status;
    private int currentHp;
    private int maxHp;
    private int roundNumber;
    private int totalWins;
    private int totalLosses;
    private int totalDraws;
    private int shield;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private List<RoundDTO> rounds;
    private List<BuffDTO> activeBuffs;

    public RunDTO() {}

    public RunDTO(Long id, RunStatus status, int currentHp, int maxHp, int roundNumber,
                  int totalWins, int totalLosses, int totalDraws, int shield,
                  LocalDateTime createdAt, LocalDateTime endedAt) {
        this.id = id;
        this.status = status;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.roundNumber = roundNumber;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
        this.totalDraws = totalDraws;
        this.shield = shield;
        this.createdAt = createdAt;
        this.endedAt = endedAt;
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
    public List<RoundDTO> getRounds() { return rounds; }
    public void setRounds(List<RoundDTO> rounds) { this.rounds = rounds; }
    public List<BuffDTO> getActiveBuffs() { return activeBuffs; }
    public void setActiveBuffs(List<BuffDTO> activeBuffs) { this.activeBuffs = activeBuffs; }
}
