package com.papercrown.shared.dto;

import java.util.Map;

public class StatsDTO {
    private int totalRuns;
    private int totalWins;
    private int totalLosses;
    private int totalDraws;
    private int totalRounds;
    private double winRate;
    private int currentStreak;
    private int bestStreak;
    private Map<String, Integer> moveUsage;

    public StatsDTO() {}

    public StatsDTO(int totalRuns, int totalWins, int totalLosses, int totalDraws,
                    int totalRounds, double winRate, int currentStreak, int bestStreak,
                    Map<String, Integer> moveUsage) {
        this.totalRuns = totalRuns;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
        this.totalDraws = totalDraws;
        this.totalRounds = totalRounds;
        this.winRate = winRate;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.moveUsage = moveUsage;
    }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }
    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }
    public int getTotalLosses() { return totalLosses; }
    public void setTotalLosses(int totalLosses) { this.totalLosses = totalLosses; }
    public int getTotalDraws() { return totalDraws; }
    public void setTotalDraws(int totalDraws) { this.totalDraws = totalDraws; }
    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }
    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }
    public Map<String, Integer> getMoveUsage() { return moveUsage; }
    public void setMoveUsage(Map<String, Integer> moveUsage) { this.moveUsage = moveUsage; }
}
