package com.moretale.domain.story.repository;

import com.moretale.domain.story.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlideRepository extends JpaRepository<Slide, Long> {

    // 특정 동화의 슬라이드 목록 조회 (순서대로)
    List<Slide> findByStoryStoryIdOrderByOrderAsc(Long storyId);

    // 특정 동화의 슬라이드 삭제
    void deleteByStoryStoryId(Long storyId);
}
