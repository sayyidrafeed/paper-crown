package com.papercrown.backend.controller;

import com.papercrown.shared.dto.StatsDTO;
import com.papercrown.backend.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public ResponseEntity<StatsDTO> getStats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
