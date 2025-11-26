package com.back.say.domain;

import java.util.Objects;

public class Say {

    private int id;
    private String author;
    private String content;

    public Say(int id, String author, String content) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Say say = (Say) o;
        return id == say.id && Objects.equals(author, say.author) && Objects.equals(content, say.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, content);
    }
}
