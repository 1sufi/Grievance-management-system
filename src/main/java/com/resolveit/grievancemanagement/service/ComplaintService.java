package com.resolveit.grievancemanagement.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.resolveit.grievancemanagement.dto.ComplaintRequest;
import com.resolveit.grievancemanagement.dto.ComplaintRatingRequest;
import com.resolveit.grievancemanagement.dto.UserComplaintResponse;
import com.resolveit.grievancemanagement.entity.Complaint;
import com.resolveit.grievancemanagement.entity.ComplaintStatusHistory;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.ComplaintRepository;
import com.resolveit.grievancemanagement.repository.ComplaintStatusHistoryRepository;
import com.resolveit.grievancemanagement.repository.UserRepository;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintStatusHistoryRepository statusHistoryRepository,
                            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        return userRepository.findByUsernameOrEmail(name, name)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    @Transactional
    public Complaint createComplaint(ComplaintRequest request) {
        User current = getCurrentUser();
        Complaint complaint = new Complaint(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getPriority()
        );
        complaint.setIsAnonymous(Boolean.FALSE);
        complaint.setUser(current);
        complaint.setDueDate(request.getDueDate());
        Complaint saved = complaintRepository.save(complaint);

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                Complaint.Status.NEW,
                "Complaint created",
                saved,
                current
        );
        statusHistoryRepository.save(history);
        return saved;
    }

    @Transactional
    public Complaint createAnonymousComplaint(ComplaintRequest request) {
        Complaint complaint = new Complaint(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getPriority()
        );
        complaint.setIsAnonymous(Boolean.TRUE);
        complaint.setAnonymousEmail(request.getAnonymousEmail());
        complaint.setAnonymousPhone(request.getAnonymousPhone());
        complaint.setDueDate(request.getDueDate());
        Complaint saved = complaintRepository.save(complaint);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Complaint> listMyComplaints() {
        User current = getCurrentUser();
        return complaintRepository.findByUserWithOfficer(current);
    }

    @Transactional(readOnly = true)
    public UserComplaintResponse getMyComplaint(Long id) {
        User current = getCurrentUser();
        Complaint c = complaintRepository.findByIdWithStatusHistory(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        if (c.getIsAnonymous() != null && c.getIsAnonymous()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (c.getUser() == null || !c.getUser().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return mapToUserResponse(c);
    }
    
    private UserComplaintResponse mapToUserResponse(Complaint complaint) {
        UserComplaintResponse response = new UserComplaintResponse();
        response.setId(complaint.getId());
        response.setTitle(complaint.getTitle());
        response.setDescription(complaint.getDescription());
        response.setCategory(complaint.getCategory());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        response.setDueDate(complaint.getDueDate());
        response.setResolvedAt(complaint.getResolvedAt());
        response.setOfficerRating(complaint.getOfficerRating());
        response.setOfficerFeedback(complaint.getOfficerFeedback());
        
        if (complaint.getAssignedOfficer() != null) {
            UserComplaintResponse.OfficerSummary officer = new UserComplaintResponse.OfficerSummary();
            officer.setId(complaint.getAssignedOfficer().getId());
            officer.setUsername(complaint.getAssignedOfficer().getUsername());
            officer.setFirstName(complaint.getAssignedOfficer().getFirstName());
            officer.setLastName(complaint.getAssignedOfficer().getLastName());
            officer.setEmail(complaint.getAssignedOfficer().getEmail());
            response.setAssignedOfficer(officer);
        }
        
        if (complaint.getStatusHistory() != null) {
            List<UserComplaintResponse.StatusHistoryEntry> historyEntries = complaint.getStatusHistory()
                    .stream()
                    // Hide internal admin/officer notes from citizens
                    .filter(history -> !Boolean.TRUE.equals(history.getInternalNote()))
                    .sorted(Comparator.comparing(ComplaintStatusHistory::getChangedAt).reversed())
                    .map(history -> {
                        UserComplaintResponse.StatusHistoryEntry entry = new UserComplaintResponse.StatusHistoryEntry();
                        entry.setStatus(history.getStatus());
                        entry.setComment(history.getComment());
                        entry.setChangedAt(history.getChangedAt());
                        if (history.getChangedBy() != null) {
                            entry.setChangedByUsername(history.getChangedBy().getUsername());
                        }
                        return entry;
                    })
                    .collect(Collectors.toList());
            response.setStatusHistory(historyEntries);
        }
        
        return response;
    }

    @Transactional
    public Complaint updateMyComplaint(Long id, ComplaintRequest request) {
        User current = getCurrentUser();
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        if (c.getIsAnonymous() != null && c.getIsAnonymous()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (c.getUser() == null || !c.getUser().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (c.getStatus() != Complaint.Status.NEW && c.getStatus() != Complaint.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Cannot edit after processing started");
        }
        c.setTitle(request.getTitle());
        c.setDescription(request.getDescription());
        c.setCategory(request.getCategory());
        c.setPriority(request.getPriority());
        c.setDueDate(request.getDueDate());
        return complaintRepository.save(c);
    }

    @Transactional
    public UserComplaintResponse rateOfficer(Long id, ComplaintRatingRequest request) {
        User current = getCurrentUser();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        if (complaint.getIsAnonymous() != null && complaint.getIsAnonymous()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (complaint.getUser() == null || !complaint.getUser().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (complaint.getAssignedOfficer() == null) {
            throw new IllegalStateException("No officer assigned to this complaint");
        }
        if (complaint.getStatus() != Complaint.Status.RESOLVED && complaint.getStatus() != Complaint.Status.CLOSED) {
            throw new IllegalStateException("You can rate only after the complaint is resolved or closed");
        }

        complaint.setOfficerRating(request.getRating());
        complaint.setOfficerFeedback(request.getFeedback());

        Complaint saved = complaintRepository.save(complaint);
        return mapToUserResponse(saved);
    }

    @Transactional
    public void withdrawMyComplaint(Long id) {
        User current = getCurrentUser();
        Complaint c = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        if (c.getIsAnonymous() != null && c.getIsAnonymous()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (c.getUser() == null || !c.getUser().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        if (c.getStatus() != Complaint.Status.NEW && c.getStatus() != Complaint.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Cannot withdraw after processing started");
        }
        c.setStatus(Complaint.Status.CLOSED);
        complaintRepository.save(c);
    }
}

