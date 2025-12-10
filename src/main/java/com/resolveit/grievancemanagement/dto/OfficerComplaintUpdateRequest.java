package com.resolveit.grievancemanagement.dto;

import com.resolveit.grievancemanagement.entity.Complaint;

import java.time.LocalDateTime;

/**
 * Request payload used by officers to update the status of complaints
 * that are assigned to them. All fields are optional; only provided
 * values will be applied.
 */
public class OfficerComplaintUpdateRequest {

    private Complaint.Status status;

    private LocalDateTime dueDate;

    private LocalDateTime resolvedAt;

    /**
     * Optional internal note added by the officer when changing status.
     */
    private String officerComment;

    public Complaint.Status getStatus() {
        return status;
    }

    public void setStatus(Complaint.Status status) {
        this.status = status;
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

    public String getOfficerComment() {
        return officerComment;
    }

    public void setOfficerComment(String officerComment) {
        this.officerComment = officerComment;
    }
}