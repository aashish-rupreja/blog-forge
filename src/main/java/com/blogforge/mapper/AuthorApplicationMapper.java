package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.authorapplication.*;
import com.blogforge.entity.AuthorApplication;
import org.springframework.stereotype.Component;

@Component
public class AuthorApplicationMapper {
    public AuthorApplication fromCreateRequestToEntity(CreateAuthorApplicationRequest dto) {
        AuthorApplication aa = new AuthorApplication();
        aa.setApplicationReason(dto.applicationReason());
        return aa;
    }

    public AuthorApplication fromUpdateRequestToEntity(UpdateAuthorApplicationRequest dto, AuthorApplication aa) {
        aa.setReviewerRemarks(dto.reviewerRemarks());
        aa.setStatus(aa.getStatus());
        return aa;
    }

    public AuthorApplicationResponse fromEntityToResponse(AuthorApplication aa) {
        return new AuthorApplicationResponse(
                new BaseResponse(
                        aa.getUuid(),
                        aa.getCreatedAt(),
                        aa.getUpdatedAt()),
                aa.getApplicant().getUsername(),
                aa.getApplicationReviewer().getUsername(),
                aa.getStatus(),
                aa.getReviewerRemarks(),
                aa.getReviewedAt()
        );
    }


}
