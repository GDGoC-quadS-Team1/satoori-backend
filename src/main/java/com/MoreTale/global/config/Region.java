package com.MoreTale.global.config;

// 지원 지역 코드 Enum
public enum Region {

    GYEONGSANG_BUKDO("경상북도"),
    GYEONGSANG_NAMDO("경상남도"),
    CHUNGCHEONGDO("충청도"),
    JEONRA("전라도"),
    JEJU("제주도");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 프론트에서 넘어오는 문자열을 Enum으로 변환
    // 매칭되지 않으면 null 반환 -> Service에서 InvalidRegionException 발생
    public static Region fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (Region region : values()) {
            if (region.name().equalsIgnoreCase(value.trim())) {
                return region;
            }
        }
        return null;
    }
}
