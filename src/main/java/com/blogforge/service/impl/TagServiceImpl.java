package com.blogforge.service.impl;

import com.blogforge.dto.tag.TagResponse;
import com.blogforge.entity.Tag;
import com.blogforge.exception.MessageResolver;
import com.blogforge.exception.EntityNotFoundException;
import com.blogforge.mapper.TagMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.TagRepository;
import com.blogforge.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl implements TagService {

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
}
