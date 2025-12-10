package com.resolveit.grievancemanagement.controller;

import com.resolveit.grievancemanagement.dto.JwtResponse;
import com.resolveit.grievancemanagement.dto.LoginRequest;
import com.resolveit.grievancemanagement.dto.MessageResponse;
import com.resolveit.grievancemanagement.dto.SignupRequest;
import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.UserRepository;
import com.resolveit.grievancemanagement.security.JwtUtils;
import com.resolveit.grievancemanagement.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collections;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    JwtUtils jwtUtils;

    @Value("${google.oauth.client-id}")
    private String googleClientId;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        String principalName = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(principalName, principalName)
                .orElseGet(() -> userRepository.findByUsernameOrEmail(loginRequest.getUsername(), loginRequest.getUsername()).orElse(null));

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }

        return ResponseEntity.ok(new JwtResponse(jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()));
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                           signUpRequest.getEmail(),
                           encoder.encode(signUpRequest.getPassword()),
                           User.Role.CITIZEN);
        
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        
        if (signUpRequest.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(signUpRequest.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Invalid role!"));
            }
        }
        
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
    
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> body) throws Exception {
        String idTokenString = body.get("idToken");
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid Google ID token"));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User(email, email, encoder.encode("google-login"), User.Role.CITIZEN);
            u.setFirstName(name);
            return userRepository.save(u);
        });

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new JwtResponse(jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()));
    }
}
