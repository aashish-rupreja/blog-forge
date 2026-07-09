package com.blogforge.service.impl;

import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.entity.Category;
import com.blogforge.mapper.CategoryMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.CategoryRepository;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecification;
import com.blogforge.specification.category.CategorySpecificationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PagedResponse<CategoryResponse> getAll(PaginationRequestParams reqParams, CategorySpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Category> spec = CategorySpecification.handleSpecs(specParams);
        Page<Category> categories = categoryRepository.findAll(spec, jpaPageable);
        return new PagedResponse<>(
                categories.stream().map(categoryMapper::fromEntityToResponse).toList(),
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalPages(),
                categories.getNumberOfElements(),
                categories.isEmpty(),
                categories.hasNext()
        );
    }
}
