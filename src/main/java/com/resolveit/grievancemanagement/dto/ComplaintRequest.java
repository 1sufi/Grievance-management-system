package com.resolveit.grievancemanagement.dto;

import com.resolveit.grievancemanagement.entity.Complaint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class ComplaintRequest {
    
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    @Size(max = 2000)
    private String description;
    
    @NotNull
    private Complaint.Category category;
    
    @NotNull
    private Complaint.Priority priority;
    
    private Boolean isAnonymous = false;
    
    private String anonymousEmail;
    
    private String anonymousPhone;
    
    private LocalDateTime dueDate;
    
    private List<String> attachmentUrls;
    
    public ComplaintRequest() {}
    
    public ComplaintRequest(String title, String description, Complaint.Category category, 
                          Complaint.Priority priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
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
    
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }
    
    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }
    
    public String getAnonymousEmail() {
        return anonymousEmail;
    }
    
    public void setAnonymousEmail(String anonymousEmail) {
        this.anonymousEmail = anonymousEmail;
    }
    
    public String getAnonymousPhone() {
        return anonymousPhone;
    }
    
    public void setAnonymousPhone(String anonymousPhone) {
        this.anonymousPhone = anonymousPhone;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public List<String> getAttachmentUrls() {
        return attachmentUrls;
    }
    
    public void setAttachmentUrls(List<String> attachmentUrls) {
        this.attachmentUrls = attachmentUrls;
    }
}
