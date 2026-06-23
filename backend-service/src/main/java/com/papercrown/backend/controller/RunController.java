package com.papercrown.backend.controller;

import com.papercrown.shared.dto.MoveRequest;
import com.papercrown.shared.dto.MoveResponse;
import com.papercrown.shared.dto.RunDTO;
import com.papercrown.backend.service.RunService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @PostMapping
    public ResponseEntity<RunDTO> startRun() {
        RunDTO run = runService.startRun();
        return ResponseEntity.status(HttpStatus.CREATED).body(run);
    }

    @GetMapping("/unfinished")
    public ResponseEntity<RunDTO> getUnfinishedRun() {
        RunDTO run = runService.getUnfinishedRun();
        if (run == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(run);
    }

    @PostMapping("/abandon")
    public ResponseEntity<Void> abandonUnfinishedRun() {
        runService.abandonUnfinishedRun();
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RunDTO>> getAllRuns() {
        return ResponseEntity.ok(runService.getAllRuns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RunDTO> getRun(@PathVariable Long id) {
        return ResponseEntity.ok(runService.getRunById(id));
    }

    @PostMapping("/{id}/round")
    public ResponseEntity<MoveResponse> submitMove(@PathVariable Long id,
                                                   @Valid @RequestBody MoveRequest request) {
        MoveResponse response = runService.submitMove(id, request.getMove());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/buff")
    public ResponseEntity<MoveResponse> selectBuff(@PathVariable Long id,
                                                   @RequestParam Long buffId) {
        MoveResponse response = runService.selectBuff(id, buffId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/abandon")
    public ResponseEntity<Void> abandonRun(@PathVariable Long id) {
        runService.abandonRun(id);
        return ResponseEntity.ok().build();
    }
}
