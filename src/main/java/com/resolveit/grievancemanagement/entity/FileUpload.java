package com.resolveit.grievancemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_uploads")
public class FileUpload {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name")
    private String fileName;
    
    @NotBlank
    @Size(max = 500)
    @Column(name = "file_path")
    private String filePath;
    
    @Size(max = 100)
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnore
    private User uploadedBy;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
    
    // Constructors
    public FileUpload() {}
    
    public FileUpload(String fileName, String filePath, String fileType, Long fileSize, 
                     Complaint complaint, User uploadedBy) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.complaint = complaint;
        this.uploadedBy = uploadedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Complaint getComplaint() { return complaint; }
    public void setComplaint(Complaint complaint) { this.complaint = complaint; }
    
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
