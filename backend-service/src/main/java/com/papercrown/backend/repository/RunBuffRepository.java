package com.papercrown.backend.repository;

import com.papercrown.backend.entity.RunBuffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunBuffRepository extends JpaRepository<RunBuffEntity, Long> {

    List<RunBuffEntity> findByRunIdAndConsumedFalse(Long runId);
}
