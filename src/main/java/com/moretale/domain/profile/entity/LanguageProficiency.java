package com.moretale.domain.profile.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LanguageProficiency {
    EGG("알", 0),           // 기초
    LARVA("애벌레", 1),      // 초급
    PUPA("번데기", 2),       // 중급
    BEE("꿀벌", 3);          // 고급

    private final String description;
    private final int level;

    // 숙련도를 숫자로 변환 (0~3)
    public int getLevel() {
        return level;
    }

    // 하위 호환성: 기존 Enum 값 매핑
    public static LanguageProficiency fromLegacy(String legacy) {
        return switch (legacy.toUpperCase()) {
            case "CATERPILLAR" -> LARVA;
            case "CHRYSALIS" -> PUPA;
            case "BUTTERFLY" -> BEE;
            default -> LARVA;
        };
    }
}
