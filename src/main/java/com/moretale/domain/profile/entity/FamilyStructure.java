package com.moretale.domain.profile.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FamilyStructure {
    ONE_PARENT("한 분과 살아요"),
    TWO_PARENTS("두 분과 살아요"),
    EXTENDED_FAMILY("다른 가족과 살아요"),
    SECRET("비밀이에요"),
    CUSTOM("직접 작성해요");

    private final String description;
}
