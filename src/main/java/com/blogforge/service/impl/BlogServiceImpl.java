package com.blogforge.service.impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private final String AUTHOR_ROLE_NAME = "ROLE_AUTHOR";

    private final Logger LOG = LoggerFactory.getLogger(BlogServiceImpl.class);

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
    public BlogDetailsResponse getBlogDetails(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        return blogMapper.fromEntityToDetailsResponse(b);
    }

    @Override
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

    public BlogDetailsResponse partialUpdate(String slug, UpdateBlogRequest updateBlogRequest) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        if (updateBlogRequest.title() != null) {
            b.setTitle(updateBlogRequest.title());
            b.setSlug(generateSlug(updateBlogRequest.title(), b.getAuthor().getUuid()));
        }
        if (updateBlogRequest.content() != null) {
            b.setContent(updateBlogRequest.content());
        }
        if (updateBlogRequest.enableComments() != null) {
            b.setEnableComments(updateBlogRequest.enableComments());
        }
        if (updateBlogRequest.blogStatus() != null) {
            if (!b.getStatus().canTransitionTo(updateBlogRequest.blogStatus().toString())) {
                throw new IllegalBlogTransitionException(messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), updateBlogRequest.blogStatus().toString()));
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
    public GenericResponse delete(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        if (!b.getStatus().canTransitionTo(BlogStatus.DELETED.toString())) {
            throw new IllegalBlogTransitionException(messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), BlogStatus.DELETED.toString()));
        }
        b.setStatus(BlogStatus.DELETED);
        blogRepository.save(b);
        String deleteMessage = messageResolver.getMessage("blog.blogStatus.deleted", b.getTitle());
        return new GenericResponse(deleteMessage);
    }

    @Override
    public PagedResponse<BlogSummaryResponse> getMyBlogs(PaginationRequestParams reqParams) {
        String currentAuthenticatedUser = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<Blog> myBlogs = blogRepository.findAllByAuthor_UsernameIgnoreCase(currentAuthenticatedUser, jpaPageable);

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
    public BlogDetailsResponse create(CreateBlogRequest dto) {
        Blog b = blogMapper.fromCreateRequestToEntity(dto);

//        String currentAuthenticatedUsername = SecurityContextHolder.getContext()
//                        .getAuthentication()
//                        .getName();

        String currentAuthenticatedUsername = "bruce.banner";

        Optional<User> check = userRepository.findByUsernameIgnoreCase(currentAuthenticatedUsername);
        if (check.isEmpty()) {
            String userNotFound = messageResolver.getMessage("entity.not-found", "User", currentAuthenticatedUsername);
            throw new EntityNotFoundException(userNotFound);
        }

        Optional<Role> checkAuthor = check.get().getRoles()
                .stream()
                .filter(roleName -> roleName.getName().equals(AUTHOR_ROLE_NAME))
                .findFirst();
        if (checkAuthor.isEmpty()) {
            String userNotAuthor = messageResolver.getMessage("user.not-author");
            throw new IllegalStateException(userNotAuthor);
        }

        User author = check.get();
        b.setAuthor(author);

        // handle categories
        addCategoriesIfValid(b, dto.categories());

        // handle tags
        Set<Tag> tags = handleTags(b, dto.tags());
        b.setTags(tags);

        // add slug
        String slug = generateSlug(b.getTitle(), author.getUuid());
        b.setSlug(slug);

        if(b.getStatus() == BlogStatus.PUBLISHED) b.setPublishedAt(Instant.now());

        Blog saved = blogRepository.save(b);
        return blogMapper.fromEntityToDetailsResponse(saved);
    }

    @Override
    public GenericResponse hardDelete(List<UUID> uuids) {
        blogRepository.deleteAllById(uuids);
        return new GenericResponse("Blogs Deleted!");
    }

    @Override
    public GenericResponse like(String slug, AddReactionRequest dto, CustomUserDetails principal) {
        Blog blog = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> {
                    String notFound = messageResolver.getMessage("entity.not-found", "Blog", slug);
                    return new EntityNotFoundException(notFound);
                });

        Optional<Reaction> check = reactionRepository.findByReactor_UsernameAndBlog_Slug(principal.getUsername(), slug);

        // if blog already has Like/Dislike and user reacts Like/Dislike again remove reaction
        if(check.isPresent()) {

            if(check.get().getReactionType() == dto.reactionType()) {
                reactionRepository.delete(check.get());
            } else {
                check.get().setReactionType(dto.reactionType());
                reactionRepository.save(check.get());
            }
        } else {
            Reaction reaction = new Reaction();
            reaction.setBlog(blog);
            reaction.setReactor(principal.getUser());
            reaction.setReactionType(dto.reactionType());
            reactionRepository.save(reaction);
        }

        return new GenericResponse("reaction updated");

    }

    private void addCategoriesIfValid(Blog b, Set<String> incomingCategories) {
        Collection<Category> knownCategories = categoryRepository.findByNameIn(incomingCategories);
        if(knownCategories.size() != incomingCategories.size()) {
            Set<String> knownCategoryNames = knownCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet());

            // removing known categories from incoming ones leave us with unknown categories
            incomingCategories.removeAll(knownCategoryNames);

            String unknownCategoriesNotAllowedMsg =
                    messageResolver.getMessage("blog.categories.unknown-exist", incomingCategories);
            throw new IllegalArgumentException(unknownCategoriesNotAllowedMsg);
        }
        b.setCategories(new HashSet<>(knownCategories));
    }

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

    private String generateSlug(String title, UUID authorId) {
        String titleSlug = title
                            .trim()
                            .toLowerCase()
                            .replaceAll("[^a-z0-9\\s-]", "")
                            .replaceAll("\\s+", "-")
                            .replaceAll("-+", "-")
                            .replaceAll("^-|-$", "");

        List<String> similarSlugs = blogRepository.findSimilarSlugs(titleSlug);

        if(similarSlugs.isEmpty()) return titleSlug;

        int latestNo = 0;
        for(String similarSlug : similarSlugs) {
            if(similarSlug.equals(titleSlug)) continue;

            int slugNo = Integer.parseInt(similarSlug.substring(titleSlug.length()+1));
            latestNo = Math.max(latestNo, slugNo);
        }
        return titleSlug+"-"+(latestNo+1);
    }
}
