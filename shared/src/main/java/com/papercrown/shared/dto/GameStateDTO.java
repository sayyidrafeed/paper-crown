package com.papercrown.shared.dto;

import com.papercrown.shared.enums.RunStatus;

public class GameStateDTO {
    private RunDTO activeRun;
    private RunStatus status;

    public GameStateDTO() {}

    public GameStateDTO(RunDTO activeRun, RunStatus status) {
        this.activeRun = activeRun;
        this.status = status;
    }

    public RunDTO getActiveRun() { return activeRun; }
    public void setActiveRun(RunDTO activeRun) { this.activeRun = activeRun; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }
}
