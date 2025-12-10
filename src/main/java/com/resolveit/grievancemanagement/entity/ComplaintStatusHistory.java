package com.resolveit.grievancemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_status_history")
public class ComplaintStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Complaint.Status status;
    
    @Size(max = 500)
    private String comment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    @JsonIgnore
    private User changedBy;
    
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "is_internal_note")
    private Boolean internalNote = false;
    
    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
    
    // Constructors
    public ComplaintStatusHistory() {}
    
    public ComplaintStatusHistory(Complaint.Status status, Complaint complaint, User changedBy) {
        this.status = status;
        this.complaint = complaint;
        this.changedBy = changedBy;
    }
    
    public ComplaintStatusHistory(Complaint.Status status, String comment, Complaint complaint, User changedBy) {
        this.status = status;
        this.comment = comment;
        this.complaint = complaint;
        this.changedBy = changedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Complaint.Status getStatus() { return status; }
    public void setStatus(Complaint.Status status) { this.status = status; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Boolean getInternalNote() { return internalNote; }
    public void setInternalNote(Boolean internalNote) { this.internalNote = internalNote; }
}
