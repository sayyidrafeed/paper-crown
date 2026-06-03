package com.papercrown.backend.repository;

import com.papercrown.backend.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<SettingEntity, Long> {

    Optional<SettingEntity> findBySettingKey(String key);
}
