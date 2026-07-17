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
    public PagedResponse<TagResponse> getAll(PaginationRequestParams reqParams, String tagName) {
        LOG.trace("Entering getAll with reqParams: {}, tagName: {}", reqParams, tagName);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Page<Tag> tags = null;
        if (tagName != null) {
            LOG.trace("Fetching tags containing: {} from repository", tagName);
            tags = tagRepository.findByNameContaining(jpaPageable, tagName);
        } else {
            LOG.trace("Fetching all tags from repository");
            tags = tagRepository.findAll(jpaPageable);
        }
        LOG.trace("Fetched {} tags", tags.getNumberOfElements());

        LOG.trace("Mapping Tag entities to response DTOs");
        PagedResponse<TagResponse> response = new PagedResponse<>(
                tags.stream().map(tagMapper::fromEntityToResponse).toList(),
                tags.getNumber() + 1,
                tags.getSize(),
                (int) tags.getTotalElements(),
                tags.getTotalPages(),
                tags.isEmpty(),
                tags.hasNext()
        );
        LOG.trace("Exiting getAll with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    public TagResponse getByName(String tagName) {
        LOG.trace("Entering getByName with tagName: {}", tagName);
        LOG.trace("Finding tag by name: {}", tagName);
        Tag t = tagRepository.findByNameIgnoreCase(tagName)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageResolver.getMessage("entity.not-found", "Tag", tagName)
                ));
        
        LOG.trace("Mapping Tag entity to response DTO");
        TagResponse response = tagMapper.fromEntityToResponse(t);
        LOG.trace("Exiting getByName with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public TagResponse create(CreateTagRequest tagRequest) {
        LOG.trace("Entering create with tagRequest: {}", tagRequest);
        LOG.info("Attempting to create Tag \"{}\"", tagRequest.name());

        LOG.trace("Checking if tag already exists in repository with name: {}", tagRequest.name());
        Tag tag = tagRepository.findByNameIgnoreCase(tagRequest.name())
                .orElseThrow(() -> {
                    String alreadyExists = messageResolver.getMessage("entity.already-exists",
                            "Tag", tagRequest.name());
                    LOG.debug(alreadyExists);
                    return new EntityExistsException(alreadyExists);
                });

        LOG.trace("Mapping CreateTagRequest DTO to Tag entity");
        Tag tagEntity = tagMapper.fromCreateRequestToEntity(tagRequest);
        
        LOG.trace("Saving tag to repository");
        Tag saved = tagRepository.save(tagEntity);
        
        LOG.trace("Mapping saved Tag entity to response DTO");
        TagResponse response = tagMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting create with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse delete(DeleteTagRequest tagRequest) {
        LOG.trace("Entering delete with tagRequest: {}", tagRequest);
        LOG.info("Attempting to delete Tag \"{}\"", tagRequest.name());

        LOG.trace("Finding tag for deletion by name: {}", tagRequest.name());
        Tag tag = tagRepository.findByNameIgnoreCase(tagRequest.name())
                .orElseThrow(() -> {
                    String notExistsMessage = messageResolver.getMessage("entity.not-found", "Tag", tagRequest.name());
                    LOG.debug(notExistsMessage);
                    return new EntityNotFoundException(notExistsMessage);
                });

        LOG.trace("Deleting tag from repository: {}", tag.getName());
        tagRepository.delete(tag);
        String deleteMessage = "Tag \"##\" deleted".replace("##", tagRequest.name());
        LOG.info(deleteMessage);
        LOG.trace("Exiting delete with message: {}", deleteMessage);
        return new GenericResponse(deleteMessage);
    }
}
