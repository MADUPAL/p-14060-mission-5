package com.back.say.dto;

public class ResponseSayDto {
    private final int id;
    private final String author;
    private final String content;

    public ResponseSayDto(int id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return id + " / " + author + " / " + content;
    }
}
