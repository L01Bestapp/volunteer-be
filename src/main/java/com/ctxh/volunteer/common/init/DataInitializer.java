package com.ctxh.volunteer.common.init;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("-----Data initialization started-----------");
        createRoleIfNotExists("ADMIN", "Administrator role with full access");
        createRoleIfNotExists("STUDENT", "STUDENT role with limited access");
        createRoleIfNotExists("ORGANIZATION", "ORGANIZATION role with limited access");

        // create admin
        createUserIfNotExists("admin", "admin");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        log.info("Creating role: {}", roleName);
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            roleRepository.save(Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build());
            log.info("Role {} created successfully", roleName);
        } else {
            log.info("Role {} already exists, skipping creation", roleName);
        }
    }

    private void createUserIfNotExists(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ; // Admin user already exists, skip creation
        }

        log.info("Creating admin user...");
        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .isVerified(true)
                .build();

        Role adminRole = roleRepository.findByRoleName(RoleEnum.ADMIN.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );
        Role studentRole = roleRepository.findByRoleName(RoleEnum.STUDENT.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );
        Role organizationRole = roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()).orElseThrow(
                () -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
        );

        admin.addRole(adminRole);
        admin.addRole(studentRole);
        admin.addRole(organizationRole);

        log.info("Saving admin user with email: {}", admin.getEmail());
        userRepository.save(admin);
    }
}
