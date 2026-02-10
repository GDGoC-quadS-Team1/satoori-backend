package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlideRepository extends JpaRepository<Slide, Long> {

    // 특정 동화의 슬라이드 목록 조회 (순서대로)
    List<Slide> findByStoryStoryIdOrderByOrderAsc(Long storyId);

    // 특정 동화의 슬라이드 목록 조회 (JPQL 명시 버전)
    @Query("SELECT s FROM Slide s WHERE s.story.storyId = :storyId ORDER BY s.order ASC")
    List<Slide> findByStoryIdOrderByOrder(@Param("storyId") Long storyId);

    // TTS가 생성되지 않은 슬라이드 조회 (KR 또는 Native 중 하나라도 없는 경우)
    @Query("""
        SELECT s FROM Slide s
        WHERE s.story.storyId = :storyId
          AND (s.audioUrlKr IS NULL OR s.audioUrlNative IS NULL)
    """)
    List<Slide> findSlidesWithoutTTS(@Param("storyId") Long storyId);

    // 특정 동화에 속한 모든 슬라이드 삭제
    void deleteByStoryStoryId(Long storyId);
}
