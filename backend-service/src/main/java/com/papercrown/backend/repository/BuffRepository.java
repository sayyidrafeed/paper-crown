package com.papercrown.backend.repository;

import com.papercrown.backend.entity.BuffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuffRepository extends JpaRepository<BuffEntity, Long> {
}
