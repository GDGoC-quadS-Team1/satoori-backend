package com.satoori.domain.dialect.controller;

import com.satoori.domain.dialect.dto.DialectResponse;
import com.satoori.domain.dialect.dto.DialectMapPoint;
import com.satoori.domain.dialect.dto.RegionMapResponse;
import com.satoori.domain.dialect.dto.DialectRequest;
import com.satoori.domain.dialect.service.DialectService;
import com.satoori.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
public class DialectController {

    private final DialectService dialectService;

    // 방언 등록
    @PostMapping
    public ResponseEntity<Void> createDialect(
            @RequestBody DialectRequest dto,
            @AuthenticationPrincipal User user
    ) {
        dialectService.createDialect(dto, user);
        return ResponseEntity.ok().build();
    }

    // 방언 전체 목록 조회
    @GetMapping
    public ResponseEntity<Page<DialectResponse>> getAllDialects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DialectResponse> dialects = dialectService.getAllDialects(pageable, user);
        return ResponseEntity.ok(dialects);
    }

    // 지역별 방언 조회
    @GetMapping("/region/{region}")
    public ResponseEntity<Page<DialectResponse>> getDialectsByRegion(
            @PathVariable("region") String region,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DialectResponse> dialects = dialectService.getDialectsByRegion(region, pageable, user);
        return ResponseEntity.ok(dialects);
    }

    // 키워드 검색
    @GetMapping("/search")
    public ResponseEntity<Page<DialectResponse>> searchDialects(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DialectResponse> dialects = dialectService.searchDialects(keyword, pageable, user);
        return ResponseEntity.ok(dialects);
    }

    // 특정 방언 상세 조회
    @GetMapping("/{dialectId}")
    public ResponseEntity<DialectResponse> getDialect(
            @PathVariable("dialectId") Long dialectId,
            @AuthenticationPrincipal User user) {

        DialectResponse dialect = dialectService.getDialectById(dialectId, user);
        return ResponseEntity.ok(dialect);
    }

    // 지도용 지역별 데이터
    @GetMapping("/map")
    public ResponseEntity<List<RegionMapResponse>> getMapData() {
        List<RegionMapResponse> mapData = dialectService.getRegionMapData();
        return ResponseEntity.ok(mapData);
    }

    // 지도용 마커 포인트 (전체 또는 지역별)
    @GetMapping("/map/points")
    public ResponseEntity<List<DialectMapPoint>> getMapPoints(
            @RequestParam(name = "region", required = false) String region) {
        System.out.println(">>> region param: [" + region + "]");  // 임시 로그

        List<DialectMapPoint> points = dialectService.getDialectMapPoints(region);
        return ResponseEntity.ok(points);
    }

    // 북마크 추가
    @PostMapping("/{dialectId}/bookmark")
    public ResponseEntity<String> addBookmark(
            @PathVariable("dialectId") Long dialectId,
            @AuthenticationPrincipal User user) {

        dialectService.addBookmark(dialectId, user);
        return ResponseEntity.ok("Bookmark added");
    }

    // 북마크 삭제
    @DeleteMapping("/{dialectId}/bookmark")
    public ResponseEntity<String> removeBookmark(
            @PathVariable("dialectId") Long dialectId,
            @AuthenticationPrincipal User user) {

        dialectService.removeBookmark(dialectId, user);
        return ResponseEntity.ok("Bookmark removed");
    }

    // 내 북마크 목록
    @GetMapping("/bookmarks")
    public ResponseEntity<Page<DialectResponse>> getMyBookmarks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DialectResponse> bookmarks = dialectService.getMyBookmarks(user, pageable);
        return ResponseEntity.ok(bookmarks);
    }

    // 방언 검증 (관리자용)
    @PatchMapping("/{dialectId}/verify")
    public ResponseEntity<String> verifyDialect(
            @PathVariable("dialectId") Long dialectId,
            @AuthenticationPrincipal User user
    ) {
        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body("Forbidden: 관리자 권한 필요");
        }

        dialectService.verifyDialect(dialectId);
        return ResponseEntity.ok("Dialect verified");
    }
}
