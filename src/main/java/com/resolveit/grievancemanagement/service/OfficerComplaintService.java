package com.resolveit.grievancemanagement.service;

import com.resolveit.grievancemanagement.dto.AdminComplaintResponse;
import com.resolveit.grievancemanagement.dto.OfficerComplaintUpdateRequest;
import com.resolveit.grievancemanagement.entity.Complaint;
import com.resolveit.grievancemanagement.entity.ComplaintStatusHistory;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.ComplaintRepository;
import com.resolveit.grievancemanagement.repository.ComplaintStatusHistoryRepository;
import com.resolveit.grievancemanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the officer dashboard.
 *
 * Officers can:
 *  - See complaints that are assigned specifically to them
 *  - Update the status (and optionally due date) of those complaints
 *  - Add an internal note when changing status
 */
@Service
public class OfficerComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final AdminComplaintService adminComplaintService;
    private final NotificationService notificationService;

    public OfficerComplaintService(ComplaintRepository complaintRepository,
                                   ComplaintStatusHistoryRepository statusHistoryRepository,
                                   UserRepository userRepository,
                                   AdminComplaintService adminComplaintService,
                                   NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
        this.adminComplaintService = adminComplaintService;
        this.notificationService = notificationService;
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

    @Transactional(readOnly = true)
    public List<AdminComplaintResponse> listAssignedComplaints() {
        User officer = getCurrentUser();
        List<Complaint> complaints = complaintRepository.findByAssignedOfficer(officer);

        return complaints.stream()
                .sorted(Comparator.comparing(Complaint::getCreatedAt).reversed())
                .map(adminComplaintService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminComplaintResponse getAssignedComplaint(Long complaintId) {
        User current = getCurrentUser();
        Complaint complaint = complaintRepository.findByIdWithStatusHistory(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        ensureHasAccess(current, complaint);

        return adminComplaintService.mapToResponse(complaint);
    }

    @Transactional
    public AdminComplaintResponse updateAssignedComplaint(Long complaintId, OfficerComplaintUpdateRequest request) {
        User current = getCurrentUser();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        ensureHasAccess(current, complaint);

        Complaint.Status oldStatus = complaint.getStatus();

        boolean statusChanged = false;
        if (request.getStatus() != null && request.getStatus() != complaint.getStatus()) {
            complaint.setStatus(request.getStatus());
            statusChanged = true;
        }

        if (request.getDueDate() != null) {
            complaint.setDueDate(request.getDueDate());
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

        if (statusChanged || StringUtils.hasText(request.getOfficerComment())) {
            ComplaintStatusHistory history = new ComplaintStatusHistory(
                    saved.getStatus(),
                    request.getOfficerComment(),
                    saved,
                    current
            );
            statusHistoryRepository.save(history);
        }

        if (statusChanged) {
            notificationService.sendComplaintStatusChangeNotification(saved, oldStatus, request.getOfficerComment());
        }

        return adminComplaintService.mapToResponse(saved);
    }

    private void ensureHasAccess(User current, Complaint complaint) {
        // Admins can see/update via this endpoint as well (useful for debugging),
        // but officers are limited strictly to complaints assigned to them.
        if (current.getRole() == User.Role.ADMIN) {
            return;
        }

        if (complaint.getAssignedOfficer() == null
                || complaint.getAssignedOfficer().getId() == null
                || !complaint.getAssignedOfficer().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
    }
}