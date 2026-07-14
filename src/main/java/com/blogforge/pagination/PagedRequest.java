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
        sortBy = "uuid";
    }

    public PagedRequest(Integer pageNo, Integer pageSize, Sort.Direction direction, String sortBy) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.direction = direction;
        this.sortBy = sortBy;
    }

    public static Pageable getJPAPageRequest(PagedRequest pr) {
        return PageRequest.of(
                pr.getPageNo(),
                pr.getPageSize(),
                pr.getDirection(),
                pr.getsortBy()
        );
    }

    public static PagedRequest initWithDefaultsIfAnyInvalid(PaginationRequestParams params) {
        PagedRequest pr = new PagedRequest();
        if (params.pageNo() == null || params.pageNo() <= 0) { pr.setPageNo(0); }
        else { pr.setPageNo(params.pageNo() - 1); }

        if (params.pageSize() == null || params.pageSize() < 5) { pr.setPageSize(5); }
        else { pr.setPageSize(params.pageSize()); }

        if (params.sortDirection() == null ||
                (params.sortDirection() != Sort.Direction.ASC && params.sortDirection() != Sort.Direction.DESC))
        { pr.setDirection(Sort.Direction.ASC); }
        else { pr.setDirection(params.sortDirection()); }

        if(params.sortBy() == null) { pr.setsortBy("uuid"); }
        else { pr.setsortBy(params.sortBy()); }

        return pr;
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
