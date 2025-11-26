package com.back.say.utils;

public class SaySearchCondition {
    private final String authorContains;
    private final String contentContains;

    public SaySearchCondition(String authorContains, String contentContains) {
        this.authorContains = authorContains;
        this.contentContains = contentContains;
    }

    public String getAuthorContains() {
        return authorContains;
    }

    public String getContentContains() {
        return contentContains;
    }

    public boolean hasAuthorCondition() {
        return authorContains != null && !authorContains.isBlank();
    }
    public boolean hasContentCondition() {
        return contentContains != null && !contentContains.isBlank();
    }
}
