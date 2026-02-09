package com.moretale.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "slides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slide_id")
    private Long slideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "order_num", nullable = false)
    private Integer order;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "text_kr", columnDefinition = "TEXT")
    private String textKr;

    @Column(name = "text_native", columnDefinition = "TEXT")
    private String textNative;

    @Column(name = "audio_url_kr", columnDefinition = "TEXT")
    private String audioUrlKr;

    @Column(name = "audio_url_native", columnDefinition = "TEXT")
    private String audioUrlNative;
}
