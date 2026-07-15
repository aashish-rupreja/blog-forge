package com.blogforge.service.impl;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.UpdateAuthorApplicationRequest;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.entity.AuthorApplicationStatus;
import com.blogforge.entity.User;
import com.blogforge.exception.AuthorApplicationAlreadyExistsException;
import com.blogforge.exception.AuthorApplicationTransitionException;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.AuthorApplicationMapper;
import com.blogforge.repository.AuthorApplicationRepository;
import com.blogforge.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorApplicationServiceImplTest {

    @Mock
    private AuthorApplicationRepository authorApplicationRepository;

    @Mock
    private AuthorApplicationMapper authorApplicationMapper;

    @Mock
    private UserService userService;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private AuthorApplicationServiceImpl service;

    private User applicant;
    private AuthorApplication pendingApplication;
    private AuthorApplicationResponse mockResponse;

    @BeforeEach
    void setUp() {
        applicant = new User();
        applicant.setUsername("testuser");

        pendingApplication = new AuthorApplication();
        pendingApplication.setApplicant(applicant);
        pendingApplication.setStatus(AuthorApplicationStatus.PENDING);

        mockResponse = mock(AuthorApplicationResponse.class);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateApplication_WhenNoPendingApplicationExists() {
        CreateAuthorApplicationRequest dto = mock(CreateAuthorApplicationRequest.class);

        when(authorApplicationRepository.findByApplicant_Username("testuser"))
                .thenReturn(Optional.empty());
        when(authorApplicationMapper.fromCreateRequestToEntity(dto))
                .thenReturn(pendingApplication);
        when(authorApplicationRepository.save(pendingApplication))
                .thenReturn(pendingApplication);
        when(authorApplicationMapper.fromEntityToResponse(pendingApplication))
                .thenReturn(mockResponse);

        AuthorApplicationResponse result = service.create(dto, "testuser");

        assertNotNull(result);
        verify(authorApplicationRepository).save(pendingApplication);
    }

    @Test
    void create_ShouldCreateApplication_WhenExistingApplicationIsNotPending() {
        AuthorApplication rejectedApp = new AuthorApplication();
        rejectedApp.setApplicant(applicant);
        rejectedApp.setStatus(AuthorApplicationStatus.REJECTED);

        CreateAuthorApplicationRequest dto = mock(CreateAuthorApplicationRequest.class);

        when(authorApplicationRepository.findByApplicant_Username("testuser"))
                .thenReturn(Optional.of(rejectedApp));
        when(authorApplicationMapper.fromCreateRequestToEntity(dto))
                .thenReturn(pendingApplication);
        when(authorApplicationRepository.save(pendingApplication))
                .thenReturn(pendingApplication);
        when(authorApplicationMapper.fromEntityToResponse(pendingApplication))
                .thenReturn(mockResponse);

        AuthorApplicationResponse result = service.create(dto, "testuser");

        assertNotNull(result);
        verify(authorApplicationRepository).save(pendingApplication);
    }

    @Test
    void create_ShouldThrowAuthorApplicationAlreadyExistsException_WhenPendingApplicationExists() {
        when(authorApplicationRepository.findByApplicant_Username("testuser"))
                .thenReturn(Optional.of(pendingApplication));
        when(messageResolver.getMessage("author-application.already-pending"))
                .thenReturn("already pending");

        assertThrows(
                AuthorApplicationAlreadyExistsException.class,
                () -> service.create(mock(CreateAuthorApplicationRequest.class), "testuser")
        );

        verify(authorApplicationRepository, never()).save(any());
    }

    // ── getSingleApplication ──────────────────────────────────────────────────

    @Test
    void getSingleApplication_ShouldReturnResponse_WhenApplicationExists() {
        UUID id = UUID.randomUUID();

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.of(pendingApplication));
        when(authorApplicationMapper.fromEntityToResponse(pendingApplication))
                .thenReturn(mockResponse);

        AuthorApplicationResponse result = service.getSingleApplication(id);

        assertNotNull(result);
    }

    @Test
    void getSingleApplication_ShouldThrowEntityNotFoundException_WhenApplicationDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author Application"), eq(id.toString())))
                .thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.getSingleApplication(id)
        );
    }

    // ── approveApplication ────────────────────────────────────────────────────

    @Test
    void approveApplication_ShouldApprove_WhenApplicationIsPending() {
        UUID id = UUID.randomUUID();
        UpdateAuthorApplicationRequest dto = new UpdateAuthorApplicationRequest(AuthorApplicationStatus.APPROVED, "Looks good");

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.of(pendingApplication));
        when(authorApplicationRepository.save(pendingApplication))
                .thenReturn(pendingApplication);
        when(authorApplicationMapper.fromEntityToResponse(pendingApplication))
                .thenReturn(mockResponse);

        AuthorApplicationResponse result = service.approveApplication(id, dto);

        assertNotNull(result);
        assertEquals(AuthorApplicationStatus.APPROVED, pendingApplication.getStatus());
        assertEquals("Looks good", pendingApplication.getReviewerRemarks());
        verify(userService).assignAuthorRole(any());
        verify(authorApplicationRepository).save(pendingApplication);
    }

    @Test
    void approveApplication_ShouldThrowEntityNotFoundException_WhenApplicationDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author Application"), eq(id.toString())))
                .thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.approveApplication(id, mock(UpdateAuthorApplicationRequest.class))
        );
    }

    @Test
    void approveApplication_ShouldThrowAuthorApplicationTransitionException_WhenAlreadyApproved() {
        UUID id = UUID.randomUUID();
        AuthorApplication approvedApp = new AuthorApplication();
        approvedApp.setApplicant(applicant);
        approvedApp.setStatus(AuthorApplicationStatus.APPROVED);

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.of(approvedApp));
        when(messageResolver.getMessage(eq("author.application.illegal-transition"), anyString(), anyString()))
                .thenReturn("illegal transition");

        assertThrows(
                AuthorApplicationTransitionException.class,
                () -> service.approveApplication(id, mock(UpdateAuthorApplicationRequest.class))
        );

        verify(authorApplicationRepository, never()).save(any());
    }

    // ── rejectApplication ─────────────────────────────────────────────────────

    @Test
    void rejectApplication_ShouldReject_WhenApplicationIsPending() {
        UUID id = UUID.randomUUID();
        UpdateAuthorApplicationRequest dto = new UpdateAuthorApplicationRequest(AuthorApplicationStatus.REJECTED, "Not qualified");

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.of(pendingApplication));
        when(authorApplicationRepository.save(pendingApplication))
                .thenReturn(pendingApplication);
        when(authorApplicationMapper.fromEntityToResponse(pendingApplication))
                .thenReturn(mockResponse);

        AuthorApplicationResponse result = service.rejectApplication(id, dto);

        assertNotNull(result);
        assertEquals(AuthorApplicationStatus.REJECTED, pendingApplication.getStatus());
        assertEquals("Not qualified", pendingApplication.getReviewerRemarks());
        verify(authorApplicationRepository).save(pendingApplication);
    }

    @Test
    void rejectApplication_ShouldThrowEntityNotFoundException_WhenApplicationDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author Application"), eq(id.toString())))
                .thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.rejectApplication(id, mock(UpdateAuthorApplicationRequest.class))
        );
    }

    @Test
    void rejectApplication_ShouldThrowAuthorApplicationTransitionException_WhenAlreadyRejected() {
        UUID id = UUID.randomUUID();
        AuthorApplication rejectedApp = new AuthorApplication();
        rejectedApp.setApplicant(applicant);
        rejectedApp.setStatus(AuthorApplicationStatus.REJECTED);

        when(authorApplicationRepository.findById(id))
                .thenReturn(Optional.of(rejectedApp));
        when(messageResolver.getMessage(eq("author.application.illegal-transition"), anyString(), anyString()))
                .thenReturn("illegal transition");

        assertThrows(
                AuthorApplicationTransitionException.class,
                () -> service.rejectApplication(id, mock(UpdateAuthorApplicationRequest.class))
        );

        verify(authorApplicationRepository, never()).save(any());
    }
}
