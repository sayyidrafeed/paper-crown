package com.papercrown.shared.dto;

import com.papercrown.shared.enums.RoundOutcome;
import java.util.List;

public class MoveResponse {
    private RoundDTO round;
    private RoundOutcome outcome;
    private int currentHp;
    private List<BuffDTO> buffChoice;
    private boolean runEnded;
    private RunDTO finalRun;

    public MoveResponse() {}

    public MoveResponse(RoundDTO round, RoundOutcome outcome, int currentHp) {
        this.round = round;
        this.outcome = outcome;
        this.currentHp = currentHp;
    }

    public RoundDTO getRound() { return round; }
    public void setRound(RoundDTO round) { this.round = round; }
    public RoundOutcome getOutcome() { return outcome; }
    public void setOutcome(RoundOutcome outcome) { this.outcome = outcome; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public List<BuffDTO> getBuffChoice() { return buffChoice; }
    public void setBuffChoice(List<BuffDTO> buffChoice) { this.buffChoice = buffChoice; }
    public boolean isRunEnded() { return runEnded; }
    public void setRunEnded(boolean runEnded) { this.runEnded = runEnded; }
    public RunDTO getFinalRun() { return finalRun; }
    public void setFinalRun(RunDTO finalRun) { this.finalRun = finalRun; }
}
