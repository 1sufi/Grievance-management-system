package com.resolveit.grievancemanagement.controller;

import com.resolveit.grievancemanagement.dto.AdminComplaintResponse;
import com.resolveit.grievancemanagement.dto.OfficerComplaintUpdateRequest;
import com.resolveit.grievancemanagement.service.OfficerComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer/complaints")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('OFFICER') or hasRole('ADMIN')")
public class OfficerComplaintController {

    private final OfficerComplaintService officerComplaintService;

    public OfficerComplaintController(OfficerComplaintService officerComplaintService) {
        this.officerComplaintService = officerComplaintService;
    }

    /**
     * List all complaints assigned to the currently authenticated officer.
     */
    @GetMapping
    public ResponseEntity<List<AdminComplaintResponse>> listAssignedComplaints() {
        return ResponseEntity.ok(officerComplaintService.listAssignedComplaints());
    }

    /**
     * Get full details (including history) of a specific complaint assigned
     * to the current officer.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintResponse> getComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(officerComplaintService.getAssignedComplaint(id));
    }

    /**
     * Allow officers to update the status of a complaint that is assigned to
     * them and optionally add an internal note.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminComplaintResponse> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody OfficerComplaintUpdateRequest request
    ) {
        return ResponseEntity.ok(officerComplaintService.updateAssignedComplaint(id, request));
    }
}