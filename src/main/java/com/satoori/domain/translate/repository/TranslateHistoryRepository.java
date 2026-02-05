package com.satoori.domain.translate.repository;

import com.satoori.domain.translate.entity.TranslateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 사투리 변환 이력 조회용 JPA Repository
@Repository
public interface TranslateHistoryRepository extends JpaRepository<TranslateHistory, Long> {
    @Override
    Optional<TranslateHistory> findById(Long id);
}
