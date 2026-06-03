package com.papercrown.backend.repository;

import com.papercrown.backend.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Long> {

    List<RoundEntity> findByRunIdOrderByRoundNumberAsc(Long runId);

    int countByRunId(Long runId);
}
