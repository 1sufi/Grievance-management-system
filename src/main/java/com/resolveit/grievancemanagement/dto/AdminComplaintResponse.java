package com.resolveit.grievancemanagement.dto;

import com.resolveit.grievancemanagement.entity.Complaint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO returned to administrators when viewing complaints.
 */
public class AdminComplaintResponse {

    private Long id;

    private String ticketNumber;

    private String title;

    private String description;

    private Complaint.Category category;

    private Complaint.Priority priority;

    private Complaint.Status status;

    private Boolean anonymous;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime dueDate;

    private LocalDateTime resolvedAt;

    private Integer officerRating;

    private String officerFeedback;

    private CitizenSummary citizen;

    private OfficerSummary assignedOfficer;

    private List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Complaint.Category getCategory() {
        return category;
    }

    public void setCategory(Complaint.Category category) {
        this.category = category;
    }

    public Complaint.Priority getPriority() {
        return priority;
    }

    public void setPriority(Complaint.Priority priority) {
        this.priority = priority;
    }

    public Complaint.Status getStatus() {
        return status;
    }

    public void setStatus(Complaint.Status status) {
        this.status = status;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    public Integer getOfficerRating() {
        return officerRating;
    }

    public void setOfficerRating(Integer officerRating) {
        this.officerRating = officerRating;
    }

    public String getOfficerFeedback() {
        return officerFeedback;
    }

    public void setOfficerFeedback(String officerFeedback) {
        this.officerFeedback = officerFeedback;
    }

    public CitizenSummary getCitizen() {
        return citizen;
    }

    public void setCitizen(CitizenSummary citizen) {
        this.citizen = citizen;
    }

    public OfficerSummary getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(OfficerSummary assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public List<StatusHistoryEntry> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<StatusHistoryEntry> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public static class CitizenSummary {

        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class OfficerSummary {

        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
        private com.resolveit.grievancemanagement.entity.User.OfficerLevel officerLevel;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public com.resolveit.grievancemanagement.entity.User.OfficerLevel getOfficerLevel() {
            return officerLevel;
        }

        public void setOfficerLevel(com.resolveit.grievancemanagement.entity.User.OfficerLevel officerLevel) {
            this.officerLevel = officerLevel;
        }
    }

    public static class StatusHistoryEntry {

        private Complaint.Status status;
        private String comment;
        private LocalDateTime changedAt;
        private Long changedById;
        private String changedByUsername;
        private Boolean internalNote;

        public Complaint.Status getStatus() {
            return status;
        }

        public void setStatus(Complaint.Status status) {
            this.status = status;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public LocalDateTime getChangedAt() {
            return changedAt;
        }

        public void setChangedAt(LocalDateTime changedAt) {
            this.changedAt = changedAt;
        }

        public Long getChangedById() {
            return changedById;
        }

        public void setChangedById(Long changedById) {
            this.changedById = changedById;
        }

        public String getChangedByUsername() {
            return changedByUsername;
        }

        public void setChangedByUsername(String changedByUsername) {
            this.changedByUsername = changedByUsername;
        }

        public Boolean getInternalNote() {
            return internalNote;
        }

        public void setInternalNote(Boolean internalNote) {
            this.internalNote = internalNote;
        }
    }
}


