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
import org.springframework.security.access.prepost.PreAuthorize;
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
        LOG.trace("Entering getAll with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Category> spec = CategorySpecification.handleSpecs(specParams);
        
        LOG.trace("Fetching categories from repository with spec: {}", specParams);
        Page<Category> categories = categoryRepository.findAll(spec, jpaPageable);
        LOG.trace("Fetched {} categories", categories.getNumberOfElements());

        LOG.trace("Mapping Category entities to response DTOs");
        PagedResponse<CategoryResponse> response = new PagedResponse<>(
                categories.stream().map(categoryMapper::fromEntityToResponse).toList(),
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalPages(),
                categories.getNumberOfElements(),
                categories.isEmpty(),
                categories.hasNext()
        );
        LOG.trace("Exiting getAll with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    public CategoryResponse getByName(String categoryName) {
        LOG.trace("Entering getByName with categoryName: {}", categoryName);
        LOG.trace("Finding category by name: {}", categoryName);
        Category c = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new EntityNotFoundException(
                    messageResolver.getMessage(
                            "entity.not-found",
                            "Category", categoryName
                    )
                ));

        LOG.trace("Mapping Category entity to response DTO");
        CategoryResponse response = categoryMapper.fromEntityToResponse(c);
        LOG.trace("Exiting getByName with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CategoryResponse create(CreateCategoryRequest dto) {
        LOG.trace("Entering create with dto: {}", dto);
        String categoryName = dto.name().trim();
        LOG.info("Attempting to create Category \"{}\"", categoryName);
        
        LOG.trace("Checking if category already exists with name: {}", dto.name());
        Category c = categoryRepository.findByNameIgnoreCase(dto.name())
                .orElseThrow(() -> {
                    String existsMessage = messageResolver.getMessage("entity.already-exists", "Category", categoryName);
                    LOG.warn(existsMessage);
                    return new EntityExistsException(existsMessage);
                });

        LOG.trace("Mapping create request DTO to Category entity");
        Category categoryEntity = categoryMapper.fromCreateRequestToEntity(dto);
        
        LOG.trace("Saving new category to repository");
        Category saved = categoryRepository.save(categoryEntity);
        
        LOG.trace("Mapping saved Category entity to response DTO");
        CategoryResponse response = categoryMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting create with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse delete(DeleteCategoryRequest dto) {
        LOG.trace("Entering delete with dto: {}", dto);
        LOG.info("Attempting to delete Category \"{}\"", dto.name());
        
        LOG.trace("Finding category for deletion by name: {}", dto.name());
        Category c = categoryRepository.findByNameIgnoreCase(dto.name())
                .orElseThrow(() -> {
                    String existsMessage = messageResolver.getMessage("entity.already-exists", "Category", dto.name());
                    LOG.warn(existsMessage);
                    return new EntityExistsException(existsMessage);
                });

        LOG.trace("Deleting category from repository: {}", c.getName());
        categoryRepository.delete(c);
        String deleteMessage = messageResolver.getMessage("entity.delete.success", "Category", c.getName());
        LOG.debug(deleteMessage);
        LOG.trace("Exiting delete with message: {}", deleteMessage);
        return new GenericResponse(deleteMessage);
    }
}
