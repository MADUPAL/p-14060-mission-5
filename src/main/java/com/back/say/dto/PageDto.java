package com.back.say.dto;

import java.util.List;

public class PageDto<T> {
    private final List<T> content;
    private final int pageNo;
    private final int pageSize;
    private final int totalCount;

    public PageDto(List<T> content, int pageNo, int pageSize, int totalCount) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        if (totalCount == 0)
            return 0;
        return (totalCount + pageSize - 1) / pageSize;
    }
}
