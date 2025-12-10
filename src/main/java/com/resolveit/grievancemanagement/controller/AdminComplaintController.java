package com.resolveit.grievancemanagement.controller;

import com.resolveit.grievancemanagement.dto.AdminComplaintResponse;
import com.resolveit.grievancemanagement.dto.AdminComplaintUpdateRequest;
import com.resolveit.grievancemanagement.dto.AdminInternalNoteRequest;
import com.resolveit.grievancemanagement.service.AdminComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    public AdminComplaintController(AdminComplaintService adminComplaintService) {
        this.adminComplaintService = adminComplaintService;
    }

    @GetMapping
    public ResponseEntity<List<AdminComplaintResponse>> listComplaints() {
        return ResponseEntity.ok(adminComplaintService.listAllComplaints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintResponse> getComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(adminComplaintService.getComplaint(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminComplaintResponse> updateComplaint(@PathVariable Long id,
                                                                  @Valid @RequestBody AdminComplaintUpdateRequest request) {
        return ResponseEntity.ok(adminComplaintService.updateComplaint(id, request));
    }

    @PostMapping("/{id}/internal-note")
    public ResponseEntity<AdminComplaintResponse> addInternalNote(@PathVariable Long id,
                                                                  @Valid @RequestBody AdminInternalNoteRequest request) {
        return ResponseEntity.ok(adminComplaintService.addInternalNote(id, request.getMessage()));
    }
}


