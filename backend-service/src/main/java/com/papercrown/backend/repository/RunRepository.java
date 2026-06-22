package com.papercrown.backend.repository;

import com.papercrown.backend.entity.RunEntity;
import com.papercrown.shared.enums.RunStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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

    @Modifying
    @Query("UPDATE RunEntity r SET r.status = :newStatus, r.endedAt = :now WHERE r.id = :runId AND r.status = :currentStatus")
    int updateStatus(@Param("runId") Long runId,
                     @Param("currentStatus") RunStatus currentStatus,
                     @Param("newStatus") RunStatus newStatus,
                     @Param("now") LocalDateTime now);
}
