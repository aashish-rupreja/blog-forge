package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.entity.Category;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CategoryMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.CategoryRepository;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecification;
import com.blogforge.specification.category.CategorySpecificationParams;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final MessageResolver messageResolver;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            MessageResolver messageResolver
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.messageResolver = messageResolver;
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

    @Override
    public CategoryResponse getByName(String categoryName) {
        Category c = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new EntityNotFoundException(
                    messageResolver.getMessage(
                            "entity.not-found",
                            "Category", categoryName
                    )
                ));
        return categoryMapper.fromEntityToResponse(c);
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest dto) {
        String categoryName = dto.name().trim();
        LOG.info("Attempting to create Category \"{}\"", categoryName);
        Category c = categoryRepository.findByNameIgnoreCase(dto.name())
                .orElseThrow(() -> {
                    String existsMessage = messageResolver.getMessage("entity.already-exists", "Category", categoryName);
                    LOG.warn(existsMessage);
                    return new EntityExistsException(existsMessage);
                });

        Category saved = categoryRepository.save(categoryMapper.fromCreateRequestToEntity(dto));
        return categoryMapper.fromEntityToResponse(saved);
    }

    @Override
    @Transactional
    public GenericResponse delete(DeleteCategoryRequest dto) {
        LOG.info("Attempting to delete Category \"{}\"", dto.name());
        Category c = categoryRepository.findByNameIgnoreCase(dto.name())
                .orElseThrow(() -> {
                    String existsMessage = messageResolver.getMessage("entity.already-exists", "Category", dto.name());
                    LOG.warn(existsMessage);
                    return new EntityExistsException(existsMessage);
                });

        categoryRepository.delete(c);
        String deleteMessage = messageResolver.getMessage("entity.delete.success", "Category", c.getName());
        LOG.debug(deleteMessage);
        return new GenericResponse(deleteMessage);
    }
}
