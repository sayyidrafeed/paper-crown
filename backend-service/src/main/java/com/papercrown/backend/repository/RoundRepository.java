package com.papercrown.backend.repository;

import com.papercrown.backend.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.papercrown.shared.enums.RunStatus;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

    List<RoundEntity> findByRunIdOrderByRoundNumberAsc(Long runId);

    List<RoundEntity> findByRunIdOrderByRoundNumberDesc(Long runId);

    int countByRunId(Long runId);

    List<RoundEntity> findByRun_StatusOrderByRunIdAscRoundNumberAsc(RunStatus status);
}
