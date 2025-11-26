package com.back.say.utils;

public class Pageable {
    private final int pageNo;
    private final int pageSize;

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Pageable(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }

}
