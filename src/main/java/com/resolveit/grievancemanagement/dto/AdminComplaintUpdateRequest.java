package com.resolveit.grievancemanagement.dto;

import com.resolveit.grievancemanagement.entity.Complaint;

import java.time.LocalDateTime;

/**
 * Request payload used by administrators to update complaint details.
 * All fields are optional; only provided values will be applied.
 */
public class AdminComplaintUpdateRequest {

    private Complaint.Status status;

    private Complaint.Priority priority;

    private Long assignedOfficerId;

    private Boolean unassignOfficer;

    private LocalDateTime dueDate;

    private LocalDateTime resolvedAt;

    private String adminComment;

    public Complaint.Status getStatus() {
        return status;
    }

    public void setStatus(Complaint.Status status) {
        this.status = status;
    }

    public Complaint.Priority getPriority() {
        return priority;
    }

    public void setPriority(Complaint.Priority priority) {
        this.priority = priority;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(Long assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public Boolean getUnassignOfficer() {
        return unassignOfficer;
    }

    public void setUnassignOfficer(Boolean unassignOfficer) {
        this.unassignOfficer = unassignOfficer;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}


