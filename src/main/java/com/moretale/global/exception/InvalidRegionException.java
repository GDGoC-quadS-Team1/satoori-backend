package com.moretale.global.exception;

// 프론트에서 넘어온 region 코드가 Region Enum에 존재하지 않을 때 발생하는 예외
// -> GlobalExceptionHandler에서 HTTP 400 Bad Request로 응답
public class InvalidRegionException extends RuntimeException {

    public InvalidRegionException(String message) {
        super(message);
    }
}
