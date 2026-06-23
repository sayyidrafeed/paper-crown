package com.papercrown.backend.service;

import com.papercrown.backend.entity.*;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.*;
import com.papercrown.shared.dto.*;
import com.papercrown.shared.enums.Move;
import com.papercrown.shared.enums.RoundOutcome;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RunService {

    private static final int BASE_HP = 3;
    private static final int BUFF_INTERVAL = 3;

    private final RunRepository runRepository;
    private final RoundRepository roundRepository;
    private final BuffRepository buffRepository;
    private final RunBuffRepository runBuffRepository;
    private final GameEngine gameEngine;
    private final BuffService buffService;
    private final AchievementService achievementService;
    private final StatsService statsService;
    private final EntityMapper mapper;

    public RunService(RunRepository runRepository, RoundRepository roundRepository,
                      BuffRepository buffRepository, RunBuffRepository runBuffRepository,
                      GameEngine gameEngine, BuffService buffService,
                      AchievementService achievementService, StatsService statsService,
                      EntityMapper mapper) {
        this.runRepository = runRepository;
        this.roundRepository = roundRepository;
        this.buffRepository = buffRepository;
        this.runBuffRepository = runBuffRepository;
        this.gameEngine = gameEngine;
        this.buffService = buffService;
        this.achievementService = achievementService;
        this.statsService = statsService;
        this.mapper = mapper;
    }

    public RunDTO startRun() {
        if (getUnfinishedRun() != null) {
            throw new IllegalStateException("An unfinished run already exists");
        }
        RunEntity run = new RunEntity(BASE_HP);
        run = runRepository.save(run);
        return mapper.toRunDTO(run);
    }

    @Transactional(readOnly = true)
    public RunDTO getUnfinishedRun() {
        return runRepository.findTopByStatusOrderByCreatedAtDesc(RunStatus.IN_PROGRESS)
                .map(mapper::toRunDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public RunDTO getRunById(Long id) {
        return runRepository.findById(id)
                .map(mapper::toRunDTO)
                .orElseThrow(() -> new NoSuchElementException("Run not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<RunDTO> getAllRuns() {
        return runRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toRunSummaryDTO)
                .collect(Collectors.toList());
    }

    public MoveResponse submitMove(Long runId, Move playerMove) {
        RunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Run not found: " + runId));

        if (run.getStatus() != RunStatus.IN_PROGRESS) {
            throw new IllegalStateException("Run is already completed");
        }

        if (playerMove == null) {
            throw new IllegalArgumentException("Player move cannot be null");
        }

        Move botMove = gameEngine.randomBotMove();
        RoundOutcome originalOutcome = gameEngine.resolve(playerMove, botMove);
        RoundOutcome effectiveOutcome = originalOutcome;

        run.setRoundNumber(run.getRoundNumber() + 1);

        if (effectiveOutcome == RoundOutcome.DRAW && hasDrawAsWin(run)) {
            consumeDrawAsWin(run);
            effectiveOutcome = RoundOutcome.WIN;
        }

        switch (effectiveOutcome) {
            case WIN -> {
                run.setTotalWins(run.getTotalWins() + 1);
            }
            case LOSS -> {
                if (run.getShield() > 0) {
                    run.setShield(run.getShield() - 1);
                } else if (hasIgnoreLoss(run)) {
                    consumeIgnoreLoss(run);
                } else {
                    run.setCurrentHp(run.getCurrentHp() - 1);
                }
                run.setTotalLosses(run.getTotalLosses() + 1);
            }
            case DRAW -> {
                run.setTotalDraws(run.getTotalDraws() + 1);
            }
        }

        // Store original outcome in DB so round history shows the true RPS result
        RoundEntity round = new RoundEntity(run, run.getRoundNumber(), playerMove, botMove, originalOutcome);
        roundRepository.save(round);
        run.getRounds().add(round);

        runRepository.save(run);

        // Send effective outcome to client for UI display
        MoveResponse response = new MoveResponse(
                mapper.toRoundDTO(round), effectiveOutcome, run.getCurrentHp()
        );

        if (run.getCurrentHp() <= 0) {
            return endRun(run, response);
        }

        if (run.getRoundNumber() % BUFF_INTERVAL == 0) {
            List<BuffDTO> buffChoice = buffService.getRandomBuffChoice();
            response.setBuffChoice(buffChoice);
        }

        statsService.updateStats(run);
        achievementService.checkAchievements();

        return response;
    }

    public MoveResponse selectBuff(Long runId, Long buffId) {
        RunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Run not found: " + runId));

        BuffEntity buff = buffRepository.findById(buffId)
                .orElseThrow(() -> new NoSuchElementException("Buff not found: " + buffId));

        buffService.applyBuff(run, buff);
        runRepository.save(run);

        MoveResponse response = new MoveResponse(null, null, run.getCurrentHp());
        return response;
    }

    private MoveResponse endRun(RunEntity run, MoveResponse response) {
        run.setStatus(RunStatus.COMPLETED);
        run.setEndedAt(LocalDateTime.now());
        runRepository.save(run);

        statsService.updateStats(run);
        achievementService.checkAchievements();

        response.setRunEnded(true);
        response.setFinalRun(mapper.toRunDTO(run));
        return response;
    }

    public void abandonRun(Long runId) {
        RunEntity run = runRepository.findById(runId)
                .orElseThrow(() -> new NoSuchElementException("Run not found: " + runId));
        if (run.getStatus() == RunStatus.IN_PROGRESS) {
            run.setStatus(RunStatus.COMPLETED);
            run.setCurrentHp(0);
            run.setEndedAt(LocalDateTime.now());
            runRepository.save(run);
            statsService.updateStats(run);
            achievementService.checkAchievements();
        }
    }

    private boolean hasDrawAsWin(RunEntity run) {
        return run.getRunBuffs().stream()
                .anyMatch(rb -> !rb.isConsumed() && "DRAW_AS_WIN".equals(rb.getBuff().getEffectKey()));
    }

    private void consumeDrawAsWin(RunEntity run) {
        run.getRunBuffs().stream()
                .filter(rb -> !rb.isConsumed() && "DRAW_AS_WIN".equals(rb.getBuff().getEffectKey()))
                .findFirst()
                .ifPresent(rb -> {
                    rb.setConsumed(true);
                    rb.setUsedAt(LocalDateTime.now());
                });
    }

    private boolean hasIgnoreLoss(RunEntity run) {
        return run.getRunBuffs().stream()
                .anyMatch(rb -> !rb.isConsumed() && "IGNORE_LOSS".equals(rb.getBuff().getEffectKey()));
    }

    private void consumeIgnoreLoss(RunEntity run) {
        run.getRunBuffs().stream()
                .filter(rb -> !rb.isConsumed() && "IGNORE_LOSS".equals(rb.getBuff().getEffectKey()))
                .findFirst()
                .ifPresent(rb -> {
                    rb.setConsumed(true);
                    rb.setUsedAt(LocalDateTime.now());
                });
    }


}
