package com.papercrown.shared.dto;

import com.papercrown.shared.enums.Move;
import jakarta.validation.constraints.NotNull;

public class MoveRequest {
    @NotNull
    private Move move;

    public MoveRequest() {}

    public MoveRequest(Move move) {
        this.move = move;
    }

    public Move getMove() { return move; }
    public void setMove(Move move) { this.move = move; }
}
