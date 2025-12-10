package com.resolveit.grievancemanagement.service;

import com.resolveit.grievancemanagement.dto.AdminComplaintResponse;
import com.resolveit.grievancemanagement.dto.AdminComplaintUpdateRequest;
import com.resolveit.grievancemanagement.entity.Complaint;
import com.resolveit.grievancemanagement.entity.ComplaintStatusHistory;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.ComplaintRepository;
import com.resolveit.grievancemanagement.repository.ComplaintStatusHistoryRepository;
import com.resolveit.grievancemanagement.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AdminComplaintService(ComplaintRepository complaintRepository,
                                 ComplaintStatusHistoryRepository statusHistoryRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AdminComplaintResponse> listAllComplaints() {
        return complaintRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminComplaintResponse getComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        return mapToResponse(complaint);
    }

    @Transactional
    public AdminComplaintResponse addInternalNote(Long complaintId, String message) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        if (complaint.getAssignedOfficer() == null) {
            throw new IllegalStateException("Cannot send internal note: no officer assigned to this complaint");
        }

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                complaint.getStatus(),
                message,
                complaint,
                getCurrentUser()
        );
        history.setInternalNote(true);
        statusHistoryRepository.save(history);

        // Reload to include the new history entry in the response mapping
        return mapToResponse(complaint);
    }

    @Transactional
    public AdminComplaintResponse updateComplaint(Long complaintId, AdminComplaintUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        Complaint.Status oldStatus = complaint.getStatus();

        boolean statusChanged = false;
        boolean priorityChanged = false;
        if (request.getStatus() != null && request.getStatus() != complaint.getStatus()) {
            complaint.setStatus(request.getStatus());
            statusChanged = true;
        }

        if (request.getPriority() != null) {
            complaint.setPriority(request.getPriority());
            priorityChanged = true;
        }

        // If priority was provided, set a due date based on the SLA for that priority.
        // If no due date exists yet but a priority is present, set it once based on that priority.
        if (priorityChanged) {
            complaint.setDueDate(calculateDueDateFromPriority(request.getPriority()));
        } else if (complaint.getDueDate() == null && complaint.getPriority() != null) {
            complaint.setDueDate(calculateDueDateFromPriority(complaint.getPriority()));
        }

        if (request.getDueDate() != null) {
            complaint.setDueDate(request.getDueDate());
        }

        if (request.getAssignedOfficerId() != null) {
            User officer = userRepository.findById(request.getAssignedOfficerId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned officer not found"));

            if (officer.getRole() != User.Role.OFFICER && officer.getRole() != User.Role.ADMIN) {
                throw new IllegalArgumentException("Assigned user must be an officer or admin");
            }

            complaint.setAssignedOfficer(officer);
        } else if (Boolean.TRUE.equals(request.getUnassignOfficer())) {
            complaint.setAssignedOfficer(null);
        }

        if (request.getResolvedAt() != null) {
            complaint.setResolvedAt(request.getResolvedAt());
        } else if (statusChanged) {
            if (complaint.getStatus() == Complaint.Status.RESOLVED
                    || complaint.getStatus() == Complaint.Status.CLOSED) {
                complaint.setResolvedAt(LocalDateTime.now());
            } else {
                complaint.setResolvedAt(null);
            }
        }

        Complaint saved = complaintRepository.save(complaint);

        if (statusChanged || StringUtils.hasText(request.getAdminComment())) {
            ComplaintStatusHistory history = new ComplaintStatusHistory(
                    saved.getStatus(),
                    request.getAdminComment(),
                    saved,
                    getCurrentUser()
            );
            statusHistoryRepository.save(history);
        }

        if (statusChanged) {
            notificationService.sendComplaintStatusChangeNotification(saved, oldStatus, request.getAdminComment());
        }

        return mapToResponse(saved);
    }

    private LocalDateTime calculateDueDateFromPriority(Complaint.Priority priority) {
        if (priority == null) {
            return null;
        }
        long hours;
        switch (priority) {
            case URGENT:
                hours = 8;
                break;
            case HIGH:
                hours = 12;
                break;
            case MEDIUM:
                hours = 24;
                break;
            case LOW:
            default:
                hours = 48;
                break;
        }
        return LocalDateTime.now().plusHours(hours);
    }

    public AdminComplaintResponse mapToResponse(Complaint complaint) {
        AdminComplaintResponse response = new AdminComplaintResponse();
        response.setId(complaint.getId());
        response.setTicketNumber(complaint.getId() != null ? "CMP-" + String.format("%06d", complaint.getId()) : null);
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setCategory(complaint.getCategory());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setAnonymous(Boolean.TRUE.equals(complaint.getIsAnonymous()));
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        response.setDueDate(complaint.getDueDate());
        response.setResolvedAt(complaint.getResolvedAt());
        response.setOfficerRating(complaint.getOfficerRating());
        response.setOfficerFeedback(complaint.getOfficerFeedback());

        if (complaint.getUser() != null) {
            AdminComplaintResponse.CitizenSummary citizen = new AdminComplaintResponse.CitizenSummary();
            citizen.setId(complaint.getUser().getId());
            citizen.setUsername(complaint.getUser().getUsername());
            citizen.setFirstName(complaint.getUser().getFirstName());
            citizen.setLastName(complaint.getUser().getLastName());
            citizen.setEmail(complaint.getUser().getEmail());
            response.setCitizen(citizen);
        }

        if (complaint.getAssignedOfficer() != null) {
            AdminComplaintResponse.OfficerSummary officer = new AdminComplaintResponse.OfficerSummary();
            officer.setId(complaint.getAssignedOfficer().getId());
            officer.setUsername(complaint.getAssignedOfficer().getUsername());
            officer.setFirstName(complaint.getAssignedOfficer().getFirstName());
            officer.setLastName(complaint.getAssignedOfficer().getLastName());
            officer.setEmail(complaint.getAssignedOfficer().getEmail());
            officer.setOfficerLevel(complaint.getAssignedOfficer().getOfficerLevel());
            response.setAssignedOfficer(officer);
        }

        if (complaint.getStatusHistory() != null) {
            List<AdminComplaintResponse.StatusHistoryEntry> historyEntries = complaint.getStatusHistory()
                    .stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ComplaintStatusHistory::getChangedAt))
                    .map(history -> {
                        AdminComplaintResponse.StatusHistoryEntry entry = new AdminComplaintResponse.StatusHistoryEntry();
                        entry.setStatus(history.getStatus());
                        entry.setComment(history.getComment());
                        entry.setChangedAt(history.getChangedAt());
                        entry.setInternalNote(Boolean.TRUE.equals(history.getInternalNote()));
                        if (history.getChangedBy() != null) {
                            entry.setChangedById(history.getChangedBy().getId());
                            entry.setChangedByUsername(history.getChangedBy().getUsername());
                        }
                        return entry;
                    })
                    .collect(Collectors.toList());
            response.setStatusHistory(historyEntries);
        }

        return response;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        String principalName = authentication.getName();
        return userRepository.findByUsernameOrEmail(principalName, principalName)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}


