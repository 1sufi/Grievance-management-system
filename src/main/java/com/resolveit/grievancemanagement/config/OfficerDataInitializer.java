package com.resolveit.grievancemanagement.config;

import com.resolveit.grievancemanagement.entity.User;
import com.resolveit.grievancemanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with sample L1 and L2 officers so that
 * they are available in the admin dashboard for assignment
 * and for escalation logic.
 */
@Component
@Profile({"default", "dev"})
public class OfficerDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerDataInitializer(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // If there are already officers in the system, don't create duplicates
        if (!userRepository.findByRole(User.Role.OFFICER).isEmpty()) {
            return;
        }

        // Common password for all seeded officers
        String rawPassword = "Password@123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create 8 L1 officers
        for (int i = 1; i <= 8; i++) {
            String username = "officer_l1_" + i;
            if (userRepository.existsByUsername(username)) {
                continue;
            }
            User officer = new User();
            officer.setUsername(username);
            officer.setEmail("officer_l1_" + i + "@resolveit.local");
            officer.setPassword(encodedPassword);
            officer.setRole(User.Role.OFFICER);
            officer.setOfficerLevel(User.OfficerLevel.L1);
            officer.setFirstName("Officer L1-" + i);
            officer.setLastName("ResolveIT");
            officer.setPhoneNumber("99900010" + i);
            userRepository.save(officer);
        }

        // Create 2 L2 officers
        for (int i = 1; i <= 2; i++) {
            String username = "officer_l2_" + i;
            if (userRepository.existsByUsername(username)) {
                continue;
            }
            User officer = new User();
            officer.setUsername(username);
            officer.setEmail("officer_l2_" + i + "@resolveit.local");
            officer.setPassword(encodedPassword);
            officer.setRole(User.Role.OFFICER);
            officer.setOfficerLevel(User.OfficerLevel.L2);
            officer.setFirstName("Officer L2-" + i);
            officer.setLastName("ResolveIT");
            officer.setPhoneNumber("99900020" + i);
            userRepository.save(officer);
        }
    }
}
