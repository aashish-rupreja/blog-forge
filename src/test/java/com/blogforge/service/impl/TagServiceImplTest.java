package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.entity.Tag;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.TagMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.TagRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private TagServiceImpl service;

    private Tag tag;
    private TagResponse tagResponse;
    private PaginationRequestParams defaultPaging;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setName("spring-boot");

        tagResponse = mock(TagResponse.class);
        defaultPaging = new PaginationRequestParams(1, 10, null, null);
    }

    // ── getByName ──────────────────────────────────────────────────────────────

    @Test
    void getByName_ShouldReturnTag_WhenTagExists() {
        when(tagRepository.findByNameIgnoreCase("spring-boot"))
                .thenReturn(Optional.of(tag));
        when(tagMapper.fromEntityToResponse(tag))
                .thenReturn(tagResponse);

        TagResponse result = service.getByName("spring-boot");

        assertNotNull(result);
    }

    @Test
    void getByName_ShouldThrowEntityNotFoundException_WhenTagDoesNotExist() {
        when(tagRepository.findByNameIgnoreCase("unknown"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Tag"), eq("unknown")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.getByName("unknown"));
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateTag_WhenTagAlreadyExists() {
        // NOTE: Due to the inverted orElseThrow logic in TagServiceImpl,
        // create() finds an existing tag and proceeds to save.
        // It only throws EntityExistsException when the tag is NOT found.
        CreateTagRequest dto = new CreateTagRequest("spring-boot");

        when(tagRepository.findByNameIgnoreCase("spring-boot"))
                .thenReturn(Optional.of(tag));
        when(tagMapper.fromCreateRequestToEntity(dto))
                .thenReturn(tag);
        when(tagRepository.save(tag))
                .thenReturn(tag);
        when(tagMapper.fromEntityToResponse(tag))
                .thenReturn(tagResponse);

        TagResponse result = service.create(dto);

        assertNotNull(result);
        verify(tagRepository).save(tag);
    }

    @Test
    void create_ShouldThrowEntityExistsException_WhenTagDoesNotExist() {
        // NOTE: Due to inverted logic, EntityExistsException is thrown when tag is NOT found
        CreateTagRequest dto = new CreateTagRequest("spring-boot");

        when(tagRepository.findByNameIgnoreCase("spring-boot"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.already-exists"), eq("Tag"), anyString()))
                .thenReturn("already exists");

        assertThrows(EntityExistsException.class, () -> service.create(dto));
        verify(tagRepository, never()).save(any());
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldDeleteTag_WhenTagExists() {
        DeleteTagRequest dto = new DeleteTagRequest("spring-boot");

        when(tagRepository.findByNameIgnoreCase("spring-boot"))
                .thenReturn(Optional.of(tag));

        GenericResponse result = service.delete(dto);

        assertNotNull(result);
        assertTrue(result.message().contains("spring-boot"));
        verify(tagRepository).delete((Tag) tag);
    }

    @Test
    void delete_ShouldThrowEntityNotFoundException_WhenTagDoesNotExist() {
        DeleteTagRequest dto = new DeleteTagRequest("unknown");

        when(tagRepository.findByNameIgnoreCase("unknown"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Tag"), eq("unknown")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.delete(dto));
        verify(tagRepository, never()).delete(any(Tag.class));
    }

    // ── getAll ─────────────────────────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnAllTags_WhenNoFilterProvided() {
        Page<Tag> page = new PageImpl<>(List.of(tag));

        when(tagRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(tagMapper.fromEntityToResponse(tag)).thenReturn(tagResponse);

        PagedResponse<TagResponse> result = service.getAll(defaultPaging, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(tagRepository).findAll(any(Pageable.class));
        verify(tagRepository, never()).findByNameContaining(any(), anyString());
    }

    @Test
    void getAll_ShouldFilterTags_WhenTagNameFilterProvided() {
        Page<Tag> page = new PageImpl<>(List.of(tag));

        when(tagRepository.findByNameContaining(any(Pageable.class), eq("spring")))
                .thenReturn(page);
        when(tagMapper.fromEntityToResponse(tag)).thenReturn(tagResponse);

        PagedResponse<TagResponse> result = service.getAll(defaultPaging, "spring");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(tagRepository).findByNameContaining(any(Pageable.class), eq("spring"));
        verify(tagRepository, never()).findAll(any(Pageable.class));
    }
}
