package com.resolveit.grievancemanagement.controller;

import com.resolveit.grievancemanagement.dto.OfficerListItem;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/officers")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfficerController {

    private final UserRepository userRepository;

    public AdminOfficerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<OfficerListItem>> listOfficers(
            @RequestParam(value = "level", required = false) User.OfficerLevel level
    ) {
        List<User> officers;
        if (level != null) {
            officers = userRepository.findByRoleAndOfficerLevel(User.Role.OFFICER, level);
        } else {
            officers = userRepository.findByRole(User.Role.OFFICER);
        }

        List<OfficerListItem> response = officers.stream()
                .map(OfficerListItem::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
