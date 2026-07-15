package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.entity.Tag;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.TagMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.TagRepository;
import com.blogforge.service.TagService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl implements TagService {

    private static final Logger LOG = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final MessageResolver messageResolver;

    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper, MessageResolver messageResolver) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public PagedResponse<TagResponse> getAll(PaginationRequestParams reqParams, String tagName) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Page<Tag> tags = null;
        if (tagName != null) {
            tags = tagRepository.findByNameContaining(jpaPageable, tagName);
        } else {
            tags = tagRepository.findAll(jpaPageable);
        }

        return new PagedResponse<>(
                tags.stream().map(tagMapper::fromEntityToResponse).toList(),
                tags.getNumber() + 1,
                tags.getSize(),
                (int) tags.getTotalElements(),
                tags.getTotalPages(),
                tags.isEmpty(),
                tags.hasNext()
        );
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public TagResponse getByName(String tagName) {
        Tag t = tagRepository.findByNameIgnoreCase(tagName)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageResolver.getMessage("entity.not-found", "Tag", tagName)
                ));
        return tagMapper.fromEntityToResponse(t);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public TagResponse create(CreateTagRequest tagRequest) {
        LOG.info("Attempting to create Tag \"{}\"", tagRequest.name());

        Tag tag = tagRepository.findByNameIgnoreCase(tagRequest.name())
                .orElseThrow(() -> {
                    String alreadyExists = messageResolver.getMessage("entity.already-exists",
                            "Tag", tagRequest.name());
                    LOG.debug(alreadyExists);
                    return new EntityExistsException(alreadyExists);
                });

        Tag saved = tagRepository.save(tagMapper.fromCreateRequestToEntity(tagRequest));
        return tagMapper.fromEntityToResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse delete(DeleteTagRequest tagRequest) {
        LOG.info("Attempting to delete Tag \"{}\"", tagRequest.name());

        Tag tag = tagRepository.findByNameIgnoreCase(tagRequest.name())
                .orElseThrow(() -> {
                    String notExistsMessage = messageResolver.getMessage("entity.not-found", "Tag", tagRequest.name());
                    LOG.debug(notExistsMessage);
                    return new EntityNotFoundException(notExistsMessage);
                });

        tagRepository.delete(tag);
        String deleteMessage = "Tag \"##\" deleted".replace("##", tagRequest.name());
        LOG.info(deleteMessage);
        return new GenericResponse(deleteMessage);
    }
}
