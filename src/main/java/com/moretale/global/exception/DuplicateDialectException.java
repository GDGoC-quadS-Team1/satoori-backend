package com.moretale.global.exception;


// 방언 중복 등록 시 발생하는 사용자 정의 예외
public class DuplicateDialectException extends RuntimeException {
    public DuplicateDialectException(String message) {
        super(message);
    }
}
