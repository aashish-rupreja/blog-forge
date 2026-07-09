package com.blogforge.pagination;

import java.util.Collection;

public class PagedResponse<T> {
    private Collection<T> content;
    private Integer pageNo;
    private Integer pageSize;
    private Integer totalPages;
    private boolean isEmpty;
    private boolean hasNext;

    public PagedResponse() {}

    public PagedResponse(Collection<T> content, Integer pageNo, Integer pageSize, Integer totalPages, boolean isEmpty, boolean hasNext) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.isEmpty = isEmpty;
        this.hasNext = hasNext;
    }

    public Collection<T> getContent() {
        return content;
    }

    public void setContent(Collection<T> content) {
        this.content = content;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
