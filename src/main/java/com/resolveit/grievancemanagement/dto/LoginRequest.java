package com.resolveit.grievancemanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    
    @NotBlank
    @Size(max = 50)
    private String username;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    public LoginRequest() {}
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
