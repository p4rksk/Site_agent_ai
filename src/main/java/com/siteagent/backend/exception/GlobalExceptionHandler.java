package com.siteagent.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body("서버 오류가 발생했습니다.");
    }
}