package com.MoreTale.global.exception;

// 사용자가 빈 문장을 입력했을 때 발생하는 예외
// -> GlobalExceptionHandler에서 HTTP 400 Bad Request로 응답
public class EmptyInputException extends RuntimeException {

    public EmptyInputException(String message) {
        super(message);
    }
}
