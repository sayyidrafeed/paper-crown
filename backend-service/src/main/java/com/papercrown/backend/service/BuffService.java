package com.papercrown.backend.service;

import com.papercrown.backend.entity.BuffEntity;
import com.papercrown.backend.entity.RunBuffEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.BuffRepository;
import com.papercrown.backend.repository.RunBuffRepository;
import com.papercrown.shared.dto.BuffDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BuffService {

    private static final int BUFF_CHOICE_SIZE = 3;

    private final BuffRepository buffRepository;
    private final RunBuffRepository runBuffRepository;
    private final EntityMapper mapper;
    private Random random = new java.security.SecureRandom();

    public BuffService(BuffRepository buffRepository, RunBuffRepository runBuffRepository,
                       EntityMapper mapper) {
        this.buffRepository = buffRepository;
        this.runBuffRepository = runBuffRepository;
        this.mapper = mapper;
    }

    public List<BuffDTO> getRandomBuffChoice() {
        List<BuffEntity> allBuffs = new java.util.ArrayList<>(buffRepository.findAll());
        if (allBuffs.size() <= BUFF_CHOICE_SIZE) {
            return allBuffs.stream().map(mapper::toBuffDTO).collect(Collectors.toList());
        }
        Collections.shuffle(allBuffs, random);
        return allBuffs.subList(0, BUFF_CHOICE_SIZE).stream()
                .map(mapper::toBuffDTO)
                .collect(Collectors.toList());
    }

    public void applyBuff(RunEntity run, BuffEntity buff) {
        String effectKey = buff.getEffectKey();
        switch (effectKey) {
            case "MAX_HP_UP" -> {
                run.setMaxHp(run.getMaxHp() + 1);
                run.setCurrentHp(run.getCurrentHp() + 1);
            }
            case "HEAL" -> {
                int newHp = Math.min(run.getCurrentHp() + 1, run.getMaxHp());
                run.setCurrentHp(newHp);
            }
            case "SHIELD" -> run.setShield(run.getShield() + 1);
            case "DOUBLE_REWARD" -> {}
            case "STREAK_BONUS" -> {}
            case "REROLL" -> {}
            case "DRAW_AS_WIN" -> {}
            case "IGNORE_LOSS" -> {}
            default -> throw new IllegalArgumentException("Unknown buff effect: " + effectKey);
        }

        RunBuffEntity runBuff = new RunBuffEntity(run, buff);
        runBuffRepository.save(runBuff);
        run.getRunBuffs().add(runBuff);
    }

    void setRandomForTesting(Random random) {
        this.random = random;
    }
}
