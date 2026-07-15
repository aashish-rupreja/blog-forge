package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.CreateBlogRequest;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.reaction.AddReactionRequest;
import com.blogforge.entity.*;
import com.blogforge.exception.IllegalBlogTransitionException;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.BlogMapper;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.*;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.specification.blog.BlogSpecificationParams;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceImplTest {

    @Mock private BlogRepository blogRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReactionRepository reactionRepository;
    @Mock private BlogMapper blogMapper;
    @Mock private CommentMapper commentMapper;
    @Mock private MessageResolver messageResolver;

    @InjectMocks
    private BlogServiceImpl service;

    private User author;
    private Blog blog;
    private BlogDetailsResponse detailsResponse;
    private PaginationRequestParams defaultPaging;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("author1");

        blog = new Blog();
        blog.setTitle("Test Blog");
        blog.setSlug("test-blog");
        blog.setAuthor(author);
        blog.setStatus(BlogStatus.DRAFT);
        blog.setTags(new HashSet<>());
        blog.setCategories(new HashSet<>());

        detailsResponse = mock(BlogDetailsResponse.class);
        defaultPaging = new PaginationRequestParams(1, 10, null, null);
    }

    // ── getBlogDetails ─────────────────────────────────────────────────────────

    @Test
    void getBlogDetails_ShouldReturnDetails_WhenBlogExists() {
        when(blogRepository.findBySlugIgnoreCase("test-blog"))
                .thenReturn(Optional.of(blog));
        when(blogMapper.fromEntityToDetailsResponse(blog))
                .thenReturn(detailsResponse);

        BlogDetailsResponse result = service.getBlogDetails("test-blog");

        assertNotNull(result);
    }

    @Test
    void getBlogDetails_ShouldThrowEntityNotFoundException_WhenBlogDoesNotExist() {
        when(blogRepository.findBySlugIgnoreCase("missing"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Blog"), eq("missing")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.getBlogDetails("missing"));
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateBlog_WhenAuthorExists() {
        CreateBlogRequest dto = mock(CreateBlogRequest.class);
        when(dto.categories()).thenReturn(Set.of());
        when(dto.tags()).thenReturn(Set.of());

        when(blogMapper.fromCreateRequestToEntity(dto)).thenReturn(blog);
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(categoryRepository.findByNameIn(anySet())).thenReturn(List.of());
        when(blogRepository.findSimilarSlugs(anyString())).thenReturn(List.of());
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.fromEntityToDetailsResponse(blog)).thenReturn(detailsResponse);

        BlogDetailsResponse result = service.create(dto, "author1");

        assertNotNull(result);
        verify(blogRepository).save(blog);
    }

    @Test
    void create_ShouldThrowEntityNotFoundException_WhenAuthorDoesNotExist() {
        CreateBlogRequest dto = mock(CreateBlogRequest.class);

        when(blogMapper.fromCreateRequestToEntity(dto)).thenReturn(blog);
        when(userRepository.findByUsernameAndRoles_Name("unknown", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author"), eq("unknown")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.create(dto, "unknown"));
        verify(blogRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenUnknownCategoryProvided() {
        CreateBlogRequest dto = mock(CreateBlogRequest.class);
        Set<String> categories = new HashSet<>(Set.of("UnknownCategory"));
        when(dto.categories()).thenReturn(categories);

        when(blogMapper.fromCreateRequestToEntity(dto)).thenReturn(blog);
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        // Only 0 known categories returned vs 1 requested
        when(categoryRepository.findByNameIn(categories)).thenReturn(List.of());
        when(messageResolver.getMessage(eq("blog.categories.not-found"), any()))
                .thenReturn("unknown category");

        assertThrows(IllegalArgumentException.class, () -> service.create(dto, "author1"));
        verify(blogRepository, never()).save(any());
    }

    // ── partialUpdate ──────────────────────────────────────────────────────────

    @Test
    void partialUpdate_ShouldUpdateBlog_WhenUserIsBlogAuthor() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("author1");

        UpdateBlogRequest dto = mock(UpdateBlogRequest.class);
        when(dto.title()).thenReturn(null);
        when(dto.content()).thenReturn("Updated content");
        when(dto.enableComments()).thenReturn(null);
        when(dto.blogStatus()).thenReturn(null);
        when(dto.categories()).thenReturn(null);
        when(dto.tags()).thenReturn(null);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(blogMapper.fromEntityToDetailsResponse(blog)).thenReturn(detailsResponse);

        BlogDetailsResponse result = service.partialUpdate("test-blog", dto, principal);

        assertNotNull(result);
        assertEquals("Updated content", blog.getContent());
        verify(blogRepository).save(blog);
    }

    @Test
    void partialUpdate_ShouldThrowAccessDeniedException_WhenUserIsNotBlogAuthor() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("someoneelse");

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(messageResolver.getMessage("blog.delete.not-allowed")).thenReturn("access denied");

        assertThrows(AccessDeniedException.class,
                () -> service.partialUpdate("test-blog", mock(UpdateBlogRequest.class), principal));
        verify(blogRepository, never()).save(any());
    }

    @Test
    void partialUpdate_ShouldThrowIllegalBlogTransitionException_WhenTransitionIsInvalid() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("author1");

        // DRAFT can't go to ARCHIVED directly
        UpdateBlogRequest dto = mock(UpdateBlogRequest.class);
        when(dto.title()).thenReturn(null);
        when(dto.content()).thenReturn(null);
        when(dto.enableComments()).thenReturn(null);
        when(dto.blogStatus()).thenReturn(BlogStatus.ARCHIVED);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(messageResolver.getMessage(eq("blog.transition.illegal"), anyString(), anyString()))
                .thenReturn("illegal transition");

        assertThrows(IllegalBlogTransitionException.class,
                () -> service.partialUpdate("test-blog", dto, principal));
        verify(blogRepository, never()).save(any());
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldSoftDeleteBlog_WhenBlogCanTransitionToDeleted() {
        // DRAFT -> DELETED is allowed
        blog.setStatus(BlogStatus.DRAFT);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(blogRepository.save(blog)).thenReturn(blog);
        when(messageResolver.getMessage(eq("blog.status.deleted"), eq(blog.getTitle())))
                .thenReturn("Blog deleted");

        GenericResponse result = service.delete("test-blog");

        assertNotNull(result);
        assertEquals(BlogStatus.DELETED, blog.getStatus());
    }

    @Test
    void delete_ShouldThrowEntityNotFoundException_WhenBlogDoesNotExist() {
        when(blogRepository.findBySlugIgnoreCase("missing")).thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Blog"), eq("missing")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.delete("missing"));
    }

    // ── like ───────────────────────────────────────────────────────────────────

    @Test
    void like_ShouldAddReaction_WhenNoPreviousReactionExists() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("user1");
        when(principal.getUser()).thenReturn(author);

        AddReactionRequest dto = mock(AddReactionRequest.class);
        when(dto.reactionType()).thenReturn(ReactionType.LIKE);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(reactionRepository.findByReactor_UsernameAndBlog_Slug("user1", "test-blog"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("blog.reaction.add"), anyString(), anyString()))
                .thenReturn("Liked");

        GenericResponse result = service.like("test-blog", dto, principal);

        assertNotNull(result);
        verify(reactionRepository).save(any(Reaction.class));
    }

    @Test
    void like_ShouldRemoveReaction_WhenSameReactionTypeAlreadyExists() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("user1");

        Reaction existingReaction = new Reaction();
        existingReaction.setReactionType(ReactionType.LIKE);

        AddReactionRequest dto = mock(AddReactionRequest.class);
        when(dto.reactionType()).thenReturn(ReactionType.LIKE);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(reactionRepository.findByReactor_UsernameAndBlog_Slug("user1", "test-blog"))
                .thenReturn(Optional.of(existingReaction));
        when(messageResolver.getMessage(eq("blog.reaction.remove"), anyString(), anyString()))
                .thenReturn("Like removed");

        GenericResponse result = service.like("test-blog", dto, principal);

        assertNotNull(result);
        verify(reactionRepository).delete((Reaction) existingReaction);
    }

    @Test
    void like_ShouldSwitchReaction_WhenDifferentReactionTypeAlreadyExists() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getUsername()).thenReturn("user1");

        Reaction existingReaction = new Reaction();
        existingReaction.setReactionType(ReactionType.LIKE);

        AddReactionRequest dto = mock(AddReactionRequest.class);
        when(dto.reactionType()).thenReturn(ReactionType.DISLIKE);

        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(reactionRepository.findByReactor_UsernameAndBlog_Slug("user1", "test-blog"))
                .thenReturn(Optional.of(existingReaction));
        when(messageResolver.getMessage(eq("blog.reaction.add"), anyString(), anyString()))
                .thenReturn("Disliked");

        GenericResponse result = service.like("test-blog", dto, principal);

        assertNotNull(result);
        assertEquals(ReactionType.DISLIKE, existingReaction.getReactionType());
        verify(reactionRepository).save(existingReaction);
    }

    @Test
    void like_ShouldThrowEntityNotFoundException_WhenBlogDoesNotExist() {
        when(blogRepository.findBySlugIgnoreCase("missing")).thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Blog"), eq("missing")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.like("missing", mock(AddReactionRequest.class), mock(CustomUserDetails.class)));
    }

    // ── getMyBlogs ─────────────────────────────────────────────────────────────

    @Test
    void getMyBlogs_ShouldReturnPagedResponse() {
        BlogSummaryResponse summaryResponse = mock(BlogSummaryResponse.class);
        Page<Blog> page = new PageImpl<>(List.of(blog));

        when(blogRepository.findAllByAuthor_UsernameIgnoreCase(eq("author1"), any(Pageable.class)))
                .thenReturn(page);
        when(blogMapper.fromEntityToSummaryResponse(blog)).thenReturn(summaryResponse);

        PagedResponse<BlogSummaryResponse> result = service.getMyBlogs(defaultPaging, "author1");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ── getAllSummary ──────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAllSummary_ShouldReturnPagedResponse() {
        BlogSummaryResponse summaryResponse = mock(BlogSummaryResponse.class);
        Page<Blog> page = new PageImpl<>(List.of(blog));
        BlogSpecificationParams specParams = mock(BlogSpecificationParams.class);

        when(blogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(blogMapper.fromEntityToSummaryResponse(blog)).thenReturn(summaryResponse);

        PagedResponse<BlogSummaryResponse> result = service.getAllSummary(defaultPaging, specParams);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    // ── hardDelete ────────────────────────────────────────────────────────────

    @Test
    void hardDelete_ShouldDeleteAllBlogsById() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        GenericResponse result = service.hardDelete(ids);

        assertNotNull(result);
        verify(blogRepository).deleteAllById(ids);
    }
}
