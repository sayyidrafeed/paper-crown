package com.papercrown.backend.controller;

import com.papercrown.shared.dto.AchievementDTO;
import com.papercrown.backend.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }
}
