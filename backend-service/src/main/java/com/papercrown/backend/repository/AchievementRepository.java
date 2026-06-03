package com.papercrown.backend.repository;

import com.papercrown.backend.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, Long> {

    List<AchievementEntity> findAllByOrderByName();

    List<AchievementEntity> findByUnlockedTrue();
}
