package com.papercrown.backend.repository;

import com.papercrown.backend.entity.RunEntity;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunRepository extends JpaRepository<RunEntity, Long> {

    @EntityGraph(attributePaths = {"rounds", "runBuffs", "runBuffs.buff"})
    Optional<RunEntity> findById(Long id);

    Optional<RunEntity> findTopByStatusOrderByCreatedAtDesc(RunStatus status);

    List<RunEntity> findTop10ByStatusOrderByCreatedAtDesc(RunStatus status);

    List<RunEntity> findAllByOrderByCreatedAtDesc();

    int countByStatus(RunStatus status);

    List<RunEntity> findByStatus(RunStatus status);
}
