package com.resolveit.grievancemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "complaints")
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    @Size(max = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NEW;
    
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;
    
    @Column(name = "anonymous_email")
    private String anonymousEmail;
    
    @Column(name = "anonymous_phone")
    private String anonymousPhone;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    @JsonIgnore
    private User assignedOfficer;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "officer_rating")
    private Integer officerRating;

    @Column(name = "officer_feedback", length = 1000)
    private String officerFeedback;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "escalation_threshold_hours")
    private Integer escalationThresholdHours = 24; // Default 24 hours for escalation
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ComplaintComment> comments;
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FileUpload> attachments;
    
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ComplaintStatusHistory> statusHistory;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Complaint() {}
    
    public Complaint(String title, String description, Category category, Priority priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    
    public String getAnonymousEmail() { return anonymousEmail; }
    public void setAnonymousEmail(String anonymousEmail) { this.anonymousEmail = anonymousEmail; }
    
    public String getAnonymousPhone() { return anonymousPhone; }
    public void setAnonymousPhone(String anonymousPhone) { this.anonymousPhone = anonymousPhone; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public User getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(User assignedOfficer) { this.assignedOfficer = assignedOfficer; }
    
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public Integer getOfficerRating() { return officerRating; }
    public void setOfficerRating(Integer officerRating) { this.officerRating = officerRating; }
    
    public String getOfficerFeedback() { return officerFeedback; }
    public void setOfficerFeedback(String officerFeedback) { this.officerFeedback = officerFeedback; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<ComplaintComment> getComments() { return comments; }
    public void setComments(List<ComplaintComment> comments) { this.comments = comments; }
    
    public List<FileUpload> getAttachments() { return attachments; }
    public void setAttachments(List<FileUpload> attachments) { this.attachments = attachments; }
    
    public List<ComplaintStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<ComplaintStatusHistory> statusHistory) { this.statusHistory = statusHistory; }
    
    public Integer getEscalationThresholdHours() { return escalationThresholdHours; }
    public void setEscalationThresholdHours(Integer escalationThresholdHours) { this.escalationThresholdHours = escalationThresholdHours; }
    
    public enum Category {
        SMART_CITY, MUNICIPAL_CORPORATION, GOVERNMENT_SERVICES, IT_HELPDESK, 
        UNIVERSITY_COLLEGE, CORPORATE_SUPPORT, HOUSING_SOCIETY, CITIZEN_GRIEVANCE
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum Status {
        NEW, UNDER_REVIEW, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED
    }
}
