package com.blogforge.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagedRequest {
    private Integer pageNo;
    private Integer pageSize;
    private Sort.Direction direction;
    private String sortBy;

    public PagedRequest() {
        pageNo = 0;
        pageSize = 5;
        direction = Sort.Direction.ASC;
        sortBy = "id";
    }

    public PagedRequest(Integer pageNo, Integer pageSize, Sort.Direction direction, String sortBy) {
        if (pageNo == null || pageNo < 0) { this.pageNo = 0; }
        else { this.pageNo = pageNo; }

        if (pageSize == null || pageSize < 0) { this.pageSize = 5; }
        else { this.pageSize = pageSize; }

        if (direction == null) { this.direction = Sort.Direction.ASC; }
        else if (direction != Sort.Direction.ASC && direction != Sort.Direction.DESC) { this.direction = Sort.Direction.ASC; }
        else { this.direction = Sort.Direction.ASC; }

        if (sortBy == null) { this.sortBy = "uuid"; }
        else { this.sortBy = sortBy; }
    }

    public static Pageable getJPAPageRequest(PagedRequest pr) {
        return PageRequest.of(
                pr.getPageNo(),
                pr.getPageSize(),
                pr.getDirection(),
                pr.getsortBy()
        );
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

    public Sort.Direction getDirection() {
        return direction;
    }

    public void setDirection(Sort.Direction direction) {
        this.direction = direction;
    }

    public String getsortBy() {
        return sortBy;
    }

    public void setsortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    @Override
    public String toString() {
        return "PagedRequest{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", direction=" + direction +
                ", sortBy='" + sortBy + '\'' +
                '}';
    }
}
