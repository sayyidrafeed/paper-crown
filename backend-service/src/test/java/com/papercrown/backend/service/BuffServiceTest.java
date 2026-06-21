package com.papercrown.backend.service;

import com.papercrown.backend.entity.BuffEntity;
import com.papercrown.backend.entity.RunEntity;
import com.papercrown.backend.mapper.EntityMapper;
import com.papercrown.backend.repository.BuffRepository;
import com.papercrown.backend.repository.RunBuffRepository;
import com.papercrown.shared.dto.BuffDTO;
import com.papercrown.shared.enums.BuffType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuffServiceTest {

    @Mock
    private BuffRepository buffRepository;
    @Mock
    private RunBuffRepository runBuffRepository;

    private EntityMapper mapper;
    private BuffService buffService;

    @BeforeEach
    void setUp() {
        mapper = new EntityMapper();
        buffService = new BuffService(buffRepository, runBuffRepository, mapper);
        buffService.setRandomForTesting(new java.util.Random(42));
    }

    @Test
    void getRandomBuffChoiceReturnsThreeBuffs() {
        List<BuffEntity> allBuffs = List.of(
                new BuffEntity("Max HP Up", "+1 max HP", BuffType.SURVIVAL, "MAX_HP_UP", "mdi2h:heart-plus"),
                new BuffEntity("Heal", "Heal 1 HP", BuffType.SURVIVAL, "HEAL", "mdi2h:heart"),
                new BuffEntity("Shield", "Shield", BuffType.SURVIVAL, "SHIELD", "mdi2s:shield"),
                new BuffEntity("Double Reward", "Double reward", BuffType.SCORING, "DOUBLE_REWARD", "mdi2s:star"),
                new BuffEntity("Reroll", "Reroll", BuffType.UTILITY, "REROLL", "mdi2r:refresh")
        );
        when(buffRepository.findAll()).thenReturn(allBuffs);

        List<BuffDTO> choice = buffService.getRandomBuffChoice();

        assertEquals(3, choice.size());
    }

    @Test
    void applyMaxHpUpBuffIncreasesMaxAndCurrentHp() {
        BuffEntity buff = new BuffEntity("Max HP Up", "+1 max HP", BuffType.SURVIVAL, "MAX_HP_UP", "mdi2h:heart-plus");
        buff.setId(1L);
        RunEntity run = new RunEntity(3);

        when(runBuffRepository.save(any())).thenReturn(null);

        buffService.applyBuff(run, buff);

        assertEquals(4, run.getMaxHp());
        assertEquals(4, run.getCurrentHp());
    }

    @Test
    void applyHealBuffRestoresHp() {
        BuffEntity buff = new BuffEntity("Heal", "Heal 1 HP", BuffType.SURVIVAL, "HEAL", "mdi2h:heart");
        buff.setId(2L);
        RunEntity run = new RunEntity(3);
        run.setCurrentHp(1);

        when(runBuffRepository.save(any())).thenReturn(null);

        buffService.applyBuff(run, buff);

        assertEquals(2, run.getCurrentHp());
    }

    @Test
    void applyHealBuffDoesNotExceedMaxHp() {
        BuffEntity buff = new BuffEntity("Heal", "Heal 1 HP", BuffType.SURVIVAL, "HEAL", "mdi2h:heart");
        buff.setId(2L);
        RunEntity run = new RunEntity(3);
        run.setCurrentHp(3);

        when(runBuffRepository.save(any())).thenReturn(null);

        buffService.applyBuff(run, buff);

        assertEquals(3, run.getCurrentHp());
    }

    @Test
    void applyShieldBuffGivesShield() {
        BuffEntity buff = new BuffEntity("Shield", "Shield", BuffType.SURVIVAL, "SHIELD", "mdi2s:shield");
        buff.setId(3L);
        RunEntity run = new RunEntity(3);

        when(runBuffRepository.save(any())).thenReturn(null);

        buffService.applyBuff(run, buff);

        assertEquals(1, run.getShield());
    }
}
