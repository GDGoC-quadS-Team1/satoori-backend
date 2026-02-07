package com.MoreTale.domain.dialect.service;

import com.MoreTale.domain.dialect.dto.DialectResponse;
import com.MoreTale.domain.dialect.dto.DialectMapPoint;
import com.MoreTale.domain.dialect.dto.RegionMapResponse;
import com.MoreTale.domain.dialect.dto.DialectRequest;
import com.MoreTale.domain.dialect.entity.Dialect;
import com.MoreTale.domain.dialect.entity.DialectBookmark;
import com.MoreTale.domain.dialect.repository.DialectBookmarkRepository;
import com.MoreTale.domain.dialect.repository.DialectRepository;
import com.MoreTale.domain.user.entity.User;
import com.MoreTale.global.exception.DuplicateDialectException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DialectService {

    private final DialectRepository dialectRepository;
    private final DialectBookmarkRepository bookmarkRepository;

    // 방언 전체 목록 조회
    @Transactional(readOnly = true)
    public Page<DialectResponse> getAllDialects(Pageable pageable, User user) {
        Page<Dialect> dialects = dialectRepository.findByVerifiedTrue(pageable);
        return dialects.map(dialect -> {
            boolean bookmarked = user != null &&
                    bookmarkRepository.existsByUserAndDialect_DialectId(user, dialect.getDialectId());
            return DialectResponse.from(dialect, bookmarked);
        });
    }

    // 지역별 방언 조회
    @Transactional(readOnly = true)
    public Page<DialectResponse> getDialectsByRegion(String region, Pageable pageable, User user) {
        Page<Dialect> dialects = dialectRepository.findByRegionAndVerifiedTrue(region, pageable);
        return dialects.map(dialect -> {
            boolean bookmarked = user != null &&
                    bookmarkRepository.existsByUserAndDialect_DialectId(user, dialect.getDialectId());
            return DialectResponse.from(dialect, bookmarked);
        });
    }

    // 방언 등록
    @Transactional
    public void createDialect(DialectRequest dto, User user) {
        if (dialectRepository.existsByDialectAndRegion(dto.getDialect(), dto.getRegion())) {
            throw new DuplicateDialectException("이미 등록된 방언입니다.");
        }

        Dialect dialect = Dialect.builder()
                .dialect(dto.getDialect())
                .standard(dto.getStandard())
                .meaning(dto.getMeaning())
                .origin(dto.getOrigin())
                .region(dto.getRegion())
                .example(dto.getExample())
                .source(dto.getSource())
                .uploadedBy(user)
                .verified(false)
                .build();

        dialectRepository.save(dialect);
    }

    // 키워드 검색
    @Transactional(readOnly = true)
    public Page<DialectResponse> searchDialects(String keyword, Pageable pageable, User user) {
        Page<Dialect> dialects = dialectRepository.searchByKeyword(keyword, pageable);
        return dialects.map(dialect -> {
            boolean bookmarked = user != null &&
                    bookmarkRepository.existsByUserAndDialect_DialectId(user, dialect.getDialectId());
            return DialectResponse.from(dialect, bookmarked);
        });
    }

    // 특정 방언 상세 조회
    @Transactional(readOnly = true)
    public DialectResponse getDialectById(Long dialectId, User user) {
        Dialect dialect = dialectRepository.findById(dialectId)
                .orElseThrow(() -> new RuntimeException("Dialect not found"));

        boolean bookmarked = user != null &&
                bookmarkRepository.existsByUserAndDialect_DialectId(user, dialectId);

        return DialectResponse.from(dialect, bookmarked);
    }

    // 지도용 지역별 데이터 조회
    @Transactional(readOnly = true)
    public List<RegionMapResponse> getRegionMapData() {
        List<Object[]> regionCounts = dialectRepository.countByRegion();

        return regionCounts.stream().map(row -> {
            String region = (String) row[0];
            Long count = (Long) row[1];

            // 해당 지역 샘플 방언 5개 조회
            List<Dialect> samples = dialectRepository
                    .findTop5ByRegionAndVerifiedTrueOrderByCreatedAtDesc(region);

            List<DialectResponse> sampleResponses = samples.stream()
                    .map(DialectResponse::from)
                    .collect(Collectors.toList());

            // 첫 번째 샘플의 좌표 사용 (또는 null)
            Double lat = samples.isEmpty() ? null : samples.get(0).getLatitude();
            Double lng = samples.isEmpty() ? null : samples.get(0).getLongitude();

            return RegionMapResponse.builder()
                    .region(region)
                    .count(count)
                    .latitude(lat)
                    .longitude(lng)
                    .samples(sampleResponses)
                    .build();
        }).collect(Collectors.toList());
    }

    // 특정 지역의 모든 방언 좌표 조회 (지도 마커용)
    @Transactional(readOnly = true)
    public List<DialectMapPoint> getDialectMapPoints(String region) {
        List<Dialect> dialects;

        if (region == null || region.isEmpty()) {
            dialects = dialectRepository.findByVerifiedTrue(Pageable.unpaged()).getContent();
        } else {
            dialects = dialectRepository.findByRegionAndVerifiedTrue(region, Pageable.unpaged()).getContent();
        }

        return dialects.stream()
                .filter(d -> d.getLatitude() != null && d.getLongitude() != null)
                .map(d -> DialectMapPoint.builder()
                        .dialectId(d.getDialectId())
                        .dialect(d.getDialect())
                        .standard(d.getStandard())
                        .region(d.getRegion())
                        .latitude(d.getLatitude())
                        .longitude(d.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }

    // 북마크 추가
    @Transactional
    public void addBookmark(Long dialectId, User user) {
        Dialect dialect = dialectRepository.findById(dialectId)
                .orElseThrow(() -> new RuntimeException("Dialect not found"));

        if (bookmarkRepository.existsByUserAndDialect_DialectId(user, dialectId)) {
            throw new RuntimeException("Already bookmarked");
        }

        DialectBookmark bookmark = DialectBookmark.builder()
                .user(user)
                .dialect(dialect)
                .build();

        bookmarkRepository.save(bookmark);
    }

    // 북마크 삭제
    @Transactional
    public void removeBookmark(Long dialectId, User user) {
        bookmarkRepository.deleteByUserAndDialect_DialectId(user, dialectId);
    }

    // 내 북마크 목록 조회
    @Transactional(readOnly = true)
    public Page<DialectResponse> getMyBookmarks(User user, Pageable pageable) {
        Page<DialectBookmark> bookmarks = bookmarkRepository.findByUser(user, pageable);
        return bookmarks.map(bookmark -> DialectResponse.from(bookmark.getDialect(), true));
    }

    // 관리자 승인 처리
    @Transactional
    public void verifyDialect(Long dialectId) {
        Dialect dialect = dialectRepository.findById(dialectId)
                .orElseThrow(() -> new RuntimeException("해당 방언이 존재하지 않습니다."));
        dialect.setVerified(true);
    }

    // 방언 삭제
    @Transactional
    public void deleteDialect(Long dialectId) {
        // 해당 방언이 존재하는지 확인
        Dialect dialect = dialectRepository.findById(dialectId)
                .orElseThrow(() -> new RuntimeException("해당 방언이 존재하지 않습니다."));

        // 삭제 실행
        dialectRepository.delete(dialect);
    }
}
