package com.back.say.exception;

public class SayNotFoundException extends RuntimeException {
    public SayNotFoundException(int id) {
        super(id + "번 명언은 존재하지 않습니다.");
    }
}
