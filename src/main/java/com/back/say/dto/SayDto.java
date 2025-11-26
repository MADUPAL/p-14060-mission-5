package com.back.say.dto;

public class SayDto {
    private final String author;
    private final String content;

    public SayDto(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
