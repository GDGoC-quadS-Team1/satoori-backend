package com.satoori.domain.dialect.repository;

import com.satoori.domain.dialect.entity.Dialect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DialectRepository extends JpaRepository<Dialect, Long> {
    Page<Dialect> findByVerifiedTrue(Pageable pageable); // 방언 전체 목록 조회
    Page<Dialect> findByRegionAndVerifiedTrue(String region, Pageable pageable); // 지역별 방언 조회

    // 키워드 검색 (방언, 표준어, 의미, 예문 포함)
    @Query("SELECT d FROM Dialect d WHERE d.verified = true AND " +
            "(d.dialect LIKE %:keyword% OR " +
            "d.standard LIKE %:keyword% OR " +
            "d.meaning LIKE %:keyword% OR " +
            "d.example LIKE %:keyword%)")
    Page<Dialect> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Dialect> findByDialectAndVerifiedTrue(String dialect); // 특정 방언 단어로 조회

    @Query("SELECT d.region, COUNT(d) FROM Dialect d WHERE d.verified = true GROUP BY d.region") // 지역별 방언 개수 조회 (지도용)
    List<Object[]> countByRegion();

    List<Dialect> findTop5ByRegionAndVerifiedTrueOrderByCreatedAtDesc(String region); // 지역별 방언 샘플 조회 (지도용)

    Page<Dialect> findByVerifiedFalse(Pageable pageable); // 검수되지 않은 방언 조회 (관리자용)

    boolean existsByDialectAndRegion(String dialect, String region); // DB에 같은 방언(dialect + region)이 이미 있는지 확인
}