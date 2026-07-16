package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.CreateBlogRequest;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.reaction.AddReactionRequest;
import com.blogforge.entity.*;
import com.blogforge.exception.IllegalBlogTransitionException;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.BlogMapper;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.*;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.BlogService;
import com.blogforge.specification.blog.BlogSpecification;
import com.blogforge.specification.blog.BlogSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private static final Logger LOG = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final BlogMapper blogMapper;
    private final CommentMapper commentMapper;
    private final MessageResolver messageResolver;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            CommentRepository commentRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            ReactionRepository reactionRepository,
            BlogMapper blogMapper,
            CommentMapper commentMapper,
            MessageResolver messageResolver) {
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.reactionRepository = reactionRepository;
        this.blogMapper = blogMapper;
        this.commentMapper = commentMapper;
        this.messageResolver = messageResolver;
    }

    @PreAuthorize("permitAll()")
    public PagedResponse<BlogSummaryResponse> getAllSummary(PaginationRequestParams requestParams, BlogSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(requestParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Blog> spec = BlogSpecification.handleSpecs(specParams);

        Page<Blog> blogSummaries = blogRepository.findAll(spec, jpaPageable);
        return new PagedResponse<>(
                blogSummaries.stream().map(blogMapper::fromEntityToSummaryResponse).toList(),
                blogSummaries.getNumber() + 1,
                blogSummaries.getSize(),
                blogSummaries.getTotalPages(),
                blogSummaries.getTotalElements(),
                blogSummaries.isEmpty(),
                blogSummaries.hasNext()
        );
    }

    @Override
    @PreAuthorize("permitAll()")
    public BlogDetailsResponse getBlogDetails(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        return blogMapper.fromEntityToDetailsResponse(b);
    }

    @Override
    @PreAuthorize("permitAll()")
    public PagedResponse<CommentResponse> getBlogComments(String slug, PaginationRequestParams requestParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(requestParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<Comment> comments = commentRepository.findByBlog_Slug(slug, jpaPageable);

        return new PagedResponse<>(
                comments.stream().map(commentMapper::fromEntityToResponse).toList(),
                comments.getNumber() + 1,
                comments.getSize(),
                comments.getTotalPages(),
                comments.getTotalElements(),
                comments.isEmpty(),
                comments.hasNext()
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public BlogDetailsResponse partialUpdate(
            String slug,
            UpdateBlogRequest updateBlogRequest,
            CustomUserDetails principal) {

        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> {
                    String blogNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "Blog", slug);
                    LOG.warn(blogNotFound);
                    return new EntityNotFoundException(blogNotFound);
                });

        boolean isBlogAuthor = b.getAuthor().getUsername().equals(principal.getUsername());

        if (!(isBlogAuthor)) {
            String accessDeniedMsg = messageResolver.getMessage("blog.delete.not-allowed");
            LOG.warn(accessDeniedMsg);
            throw new AccessDeniedException(accessDeniedMsg);
        }

        if (updateBlogRequest.title() != null) {
            b.setTitle(updateBlogRequest.title());
            b.setSlug(generateSlug(updateBlogRequest.title()));
        }
        if (updateBlogRequest.content() != null) {
            b.setContent(updateBlogRequest.content());
        }
        if (updateBlogRequest.enableComments() != null) {
            b.setEnableComments(updateBlogRequest.enableComments());
        }
        if (updateBlogRequest.blogStatus() != null) {
            if (!b.getStatus().canTransitionTo(updateBlogRequest.blogStatus().toString())) {
                String illegalTransitionMsg = messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), updateBlogRequest.blogStatus().toString());
                LOG.warn(illegalTransitionMsg);
                throw new IllegalBlogTransitionException(illegalTransitionMsg);
            }
            b.setStatus(updateBlogRequest.blogStatus());
        }
        if (updateBlogRequest.categories() != null) {
            addCategoriesIfValid(b, updateBlogRequest.categories());
        }
        if (updateBlogRequest.tags() != null) {
            Set<Tag> updatedTags = handleTags(b, updateBlogRequest.tags());
            b.setTags(updatedTags);
        }

        Blog saved = blogRepository.save(b);
        return blogMapper.fromEntityToDetailsResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_AUTHOR', 'ROLE_ADMIN')")
    public GenericResponse delete(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        if (!b.getStatus().canTransitionTo(BlogStatus.DELETED.toString())) {
            String illegalTransitionMsg = messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), BlogStatus.DELETED.toString());
            LOG.warn(illegalTransitionMsg);
            throw new IllegalBlogTransitionException(illegalTransitionMsg);
        }
        b.setStatus(BlogStatus.DELETED);
        blogRepository.save(b);
        String deleteMessage = messageResolver.getMessage("blog.status.deleted", b.getTitle());
        return new GenericResponse(deleteMessage);
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public PagedResponse<BlogSummaryResponse> getMyBlogs(
            PaginationRequestParams reqParams,
            String currentAuthenticatedUsername) {

        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<Blog> myBlogs = blogRepository.findAllByAuthor_UsernameIgnoreCase(currentAuthenticatedUsername, jpaPageable);

        return new PagedResponse<>(
                myBlogs.stream().map(blogMapper::fromEntityToSummaryResponse).toList(),
                myBlogs.getNumber() + 1,
                myBlogs.getSize(),
                myBlogs.getTotalPages(),
                myBlogs.getTotalElements(),
                myBlogs.isEmpty(),
                myBlogs.hasNext()
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public BlogDetailsResponse create(CreateBlogRequest dto, String currentAuthenticatedUsername) {
        LOG.info("User {} attempting to create a new blog", currentAuthenticatedUsername);
        Blog b = blogMapper.fromCreateRequestToEntity(dto);
        User author = userRepository.findByUsernameAndRoles_Name(currentAuthenticatedUsername, Constants.AUTHOR_ROLE_NAME)
                .orElseThrow(() -> {
                    String userNotFound = messageResolver.getMessage("entity.not-found", "Author", currentAuthenticatedUsername);
                    LOG.warn(userNotFound);
                    return new EntityNotFoundException(userNotFound);
                });

        b.setAuthor(author);

        // handle categories
        addCategoriesIfValid(b, dto.categories());

        // handle tags
        Set<Tag> tags = handleTags(b, dto.tags());
        b.setTags(tags);

        // add slug
        String slug = generateSlug(b.getTitle());
        b.setSlug(slug);

        if (b.getStatus() == BlogStatus.PUBLISHED) b.setPublishedAt(Instant.now());

        Blog saved = blogRepository.save(b);
        LOG.info("Blog {} successfully created", b.getSlug());
        return blogMapper.fromEntityToDetailsResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse hardDelete(List<UUID> uuids) {
        blogRepository.deleteAllById(uuids);
        return new GenericResponse("Blogs Deleted!");
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public GenericResponse like(String slug, AddReactionRequest dto, CustomUserDetails principal) {
        Blog blog = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> {
                    String notFound = messageResolver.getMessage("entity.not-found", "Blog", slug);
                    LOG.warn(notFound);
                    return new EntityNotFoundException(notFound);
                });

        Optional<Reaction> check = reactionRepository.findByReactor_UsernameAndBlog_Slug(principal.getUsername(), slug);
        String reactionMsg = null;
        // if blog already has Like/Dislike and user reacts Like/Dislike again remove reaction
        if (check.isPresent()) {
            if (check.get().getReactionType() == dto.reactionType()) {
                reactionRepository.delete(check.get());
                reactionMsg = messageResolver.getMessage("blog.reaction.remove", dto.reactionType().toString(), slug);
            } else {
                check.get().setReactionType(dto.reactionType());
                reactionRepository.save(check.get());
                reactionMsg = messageResolver.getMessage("blog.reaction.add", dto.reactionType().toString(), slug);
            }
        } else {
            Reaction reaction = new Reaction();
            reaction.setBlog(blog);
            User user = userRepository.findByUsernameIgnoreCase(principal.getUsername())
                    .orElseThrow(() -> {
                        String notFound = messageResolver.getMessage("entity.not-found", "User", principal.getUsername());
                        LOG.warn(notFound);
                        return new EntityNotFoundException(notFound);
                    });
            reaction.setReactor(user);
            reaction.setReactionType(dto.reactionType());
            reactionRepository.save(reaction);
            reactionMsg = messageResolver.getMessage("blog.reaction.add", dto.reactionType().toString(), slug);
        }
        return new GenericResponse(reactionMsg);
    }

    private void addCategoriesIfValid(Blog b, Set<String> incomingCategories) {
        Collection<Category> knownCategories = categoryRepository.findByNameIn(incomingCategories);
        if (knownCategories.size() != incomingCategories.size()) {
            Set<String> knownCategoryNames = knownCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());

            // removing known categories from incoming ones leave us with unknown categories
            incomingCategories.removeAll(knownCategoryNames);

            String unknownCategoriesNotAllowedMsg =
                    messageResolver.getMessage("blog.categories.not-found", incomingCategories);
            LOG.warn(unknownCategoriesNotAllowedMsg);
            throw new IllegalArgumentException(unknownCategoriesNotAllowedMsg);
        }
        b.setCategories(new HashSet<>(knownCategories));
    }

    @Transactional
    private Set<Tag> handleTags(Blog b, Set<String> incomingTags) {
        Set<String> currentTags = (b.getTags() != null && !b.getTags().isEmpty())
                ? b.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
                : new HashSet<>();

        Set<Tag> newTagsToAdd = new HashSet<>(b.getTags());

        for (String tagToAdd : incomingTags) {
            if (!currentTags.contains(tagToAdd)) {
                Optional<Tag> t = tagRepository.findByNameIgnoreCase(tagToAdd);

                if (t.isEmpty()) {
                    Tag newTag = new Tag();
                    newTag.setName(tagToAdd);
                    Tag saved = tagRepository.save(newTag);
                    newTagsToAdd.add(saved);
                } else {
                    newTagsToAdd.add(t.get());
                }
            }
        }
        return newTagsToAdd;
    }

    private String generateSlug(String title) {
        String titleSlug = title
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        List<String> similarSlugs = blogRepository.findSimilarSlugs(titleSlug);

        if (similarSlugs.isEmpty()) return titleSlug;

        int latestNo = 0;
        for (String similarSlug : similarSlugs) {
            if (similarSlug.equals(titleSlug)) continue;

            int slugNo = Integer.parseInt(similarSlug.substring(titleSlug.length() + 1));
            latestNo = Math.max(latestNo, slugNo);
        }
        return titleSlug + "-" + (latestNo + 1);
    }
}
