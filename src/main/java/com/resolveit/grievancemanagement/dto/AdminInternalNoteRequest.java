package com.resolveit.grievancemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for an internal admin-to-officer note on a complaint.
 * This note is stored as an internal status history entry and is not shown to citizens.
 */
public class AdminInternalNoteRequest {

    @NotBlank
    @Size(max = 500)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
