package com.resolveit.grievancemanagement.dto;

import com.resolveit.grievancemanagement.entity.User;

/**
 * Lightweight DTO for listing officers (e.g. in admin dropdowns).
 */
public class OfficerListItem {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private User.OfficerLevel officerLevel;

    public OfficerListItem() {
    }

    public OfficerListItem(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.officerLevel = user.getOfficerLevel();
    }

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

    public User.OfficerLevel getOfficerLevel() {
        return officerLevel;
    }

    public void setOfficerLevel(User.OfficerLevel officerLevel) {
        this.officerLevel = officerLevel;
    }
}
