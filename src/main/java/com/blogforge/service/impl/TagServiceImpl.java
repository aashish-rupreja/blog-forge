package com.blogforge.service.impl;

import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.entity.Tag;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.TagMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.TagRepository;
import com.blogforge.service.TagService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;

import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {

    private final Logger LOG = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final MessageResolver messageResolver;

    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper, MessageResolver messageResolver) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    public PagedResponse<TagResponse> getAll(PaginationRequestParams reqParams, String tagName) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Page<Tag> tags = null;
        if(tagName != null) {
            tags = tagRepository.findByNameContaining(jpaPageable, tagName);
        } else {
            tags = tagRepository.findAll(jpaPageable);
        }

        return new PagedResponse<>(
                tags.stream().map(tagMapper::fromEntityToResponse).toList(),
                tags.getNumber()+1,
                tags.getSize(),
                (int) tags.getTotalElements(),
                tags.getTotalPages(),
                tags.isEmpty(),
                tags.hasNext()
        );
    }

    @Override
    public TagResponse getByName(String tagName) {
        Tag t = tagRepository.findByNameIgnoreCase(tagName)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageResolver.getMessage("entity.not-found", "Tag", tagName)
                ));
        return tagMapper.fromEntityToResponse(t);
    }

    @Override
    @Transactional
    public TagResponse create(CreateTagRequest tagRequest) {
        LOG.debug("Attempting to create Tag \"{}\"", tagRequest.name());
        Optional<Tag> t = tagRepository.findByNameIgnoreCase(tagRequest.name());
        if(t.isPresent()) {
            LOG.debug("Tag \"{}\" already exists", tagRequest.name());
            throw new EntityExistsException(messageResolver.getMessage(
                    "entity.already-exists",
                    "Tag", tagRequest.name()));
        }
        Tag saved = tagRepository.save(tagMapper.fromCreateRequestToEntity(tagRequest));
        LOG.debug("Tag \"{}\" created", tagRequest.name());
        return tagMapper.fromEntityToResponse(saved);
    }
}
