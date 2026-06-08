package de.angebot.main.config;

import de.angebot.main.common.ERole;
import de.angebot.main.enities.user.Role;
import de.angebot.main.enities.user.User;
import de.angebot.main.repositories.users.RoleRepository;
import de.angebot.main.repositories.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapUsers implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.email:admin@example.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        Role userRole = ensureRole(ERole.ROLE_USER);
        Role adminRole = ensureRole(ERole.ROLE_ADMIN);
        ensureRole(ERole.ROLE_MODERATOR);

        if (!adminBootstrapEnabled) {
            return;
        }

        if (adminPassword.isBlank()) {
            log.warn("Admin bootstrap is enabled, but app.bootstrap.admin.password is empty. Admin user was not created.");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword)));
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRoles(Set.of(userRole, adminRole));
        userRepository.save(admin);
        log.info("Bootstrap admin user '{}' is available.", adminUsername);
    }

    private Role ensureRole(ERole roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }
}
