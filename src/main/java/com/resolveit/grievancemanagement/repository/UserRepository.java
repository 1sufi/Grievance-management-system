package com.resolveit.grievancemanagement.repository;

import com.resolveit.grievancemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    List<User> findByRoleAndOfficerLevel(User.Role role, User.OfficerLevel officerLevel);
    
    List<User> findByRole(User.Role role);
}
