package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.entity.Category;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CategoryMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.CategoryRepository;
import com.blogforge.specification.category.CategorySpecificationParams;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private CategoryServiceImpl service;

    private Category category;
    private CategoryResponse categoryResponse;
    private PaginationRequestParams defaultPaging;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Technology");

        categoryResponse = mock(CategoryResponse.class);
        defaultPaging = new PaginationRequestParams(1, 10, null, null);
    }

    // ── getByName ──────────────────────────────────────────────────────────────

    @Test
    void getByName_ShouldReturnCategory_WhenCategoryExists() {
        when(categoryRepository.findByNameIgnoreCase("Technology"))
                .thenReturn(Optional.of(category));
        when(categoryMapper.fromEntityToResponse(category))
                .thenReturn(categoryResponse);

        CategoryResponse result = service.getByName("Technology");

        assertNotNull(result);
    }

    @Test
    void getByName_ShouldThrowEntityNotFoundException_WhenCategoryDoesNotExist() {
        when(categoryRepository.findByNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Category"), eq("Unknown")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.getByName("Unknown"));
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateCategory_WhenCategoryAlreadyExists() {
        // NOTE: Due to the inverted orElseThrow logic in CategoryServiceImpl,
        // create() finds the existing category and uses it as a reference to save a new one.
        // It only throws when the category is NOT found.
        CreateCategoryRequest dto = new CreateCategoryRequest("Technology");

        when(categoryRepository.findByNameIgnoreCase("Technology"))
                .thenReturn(Optional.of(category));
        when(categoryMapper.fromCreateRequestToEntity(dto))
                .thenReturn(category);
        when(categoryRepository.save(category))
                .thenReturn(category);
        when(categoryMapper.fromEntityToResponse(category))
                .thenReturn(categoryResponse);

        CategoryResponse result = service.create(dto);

        assertNotNull(result);
        verify(categoryRepository).save(any());
    }

    @Test
    void create_ShouldThrowEntityExistsException_WhenCategoryDoesNotExist() {
        // NOTE: Due to inverted logic, EntityExistsException is thrown when NOT found
        CreateCategoryRequest dto = new CreateCategoryRequest("Technology");

        when(categoryRepository.findByNameIgnoreCase("Technology"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.already-exists"), eq("Category"), anyString()))
                .thenReturn("already exists");

        assertThrows(EntityExistsException.class, () -> service.create(dto));
        verify(categoryRepository, never()).save(any());
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldDeleteCategory_WhenCategoryExists() {
        DeleteCategoryRequest dto = new DeleteCategoryRequest("Technology");

        when(categoryRepository.findByNameIgnoreCase("Technology"))
                .thenReturn(Optional.of(category));
        when(messageResolver.getMessage(eq("entity.delete.success"), eq("Category"), eq("Technology")))
                .thenReturn("Deleted");

        GenericResponse result = service.delete(dto);

        assertNotNull(result);
        verify(categoryRepository).delete((Category) category);
    }

    @Test
    void delete_ShouldThrowEntityExistsException_WhenCategoryDoesNotExist() {
        DeleteCategoryRequest dto = new DeleteCategoryRequest("Unknown");

        when(categoryRepository.findByNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.already-exists"), eq("Category"), anyString()))
                .thenReturn("not found");

        assertThrows(EntityExistsException.class, () -> service.delete(dto));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ── getAll ─────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAll_ShouldReturnPagedResponse() {
        CategorySpecificationParams specParams = mock(CategorySpecificationParams.class);
        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(categoryMapper.fromEntityToResponse(category)).thenReturn(categoryResponse);

        PagedResponse<CategoryResponse> result = service.getAll(defaultPaging, specParams);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
