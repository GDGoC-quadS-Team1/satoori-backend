package com.satoori.domain.dialect.repository;

import com.satoori.domain.dialect.entity.DialectBookmark;
import com.satoori.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DialectBookmarkRepository extends JpaRepository<DialectBookmark, Long> {
    Page<DialectBookmark> findByUser(User user, Pageable pageable); // 사용자의 북마크 목록 조회
    boolean existsByUserAndDialect_DialectId(User user, Long dialectId); // 특정 북마크 존재 여부 확인
    Optional<DialectBookmark> findByUserAndDialect_DialectId(User user, Long dialectId); // 특정 북마크 조회
    void deleteByUserAndDialect_DialectId(User user, Long dialectId); // 특정 북마크 삭제
}
