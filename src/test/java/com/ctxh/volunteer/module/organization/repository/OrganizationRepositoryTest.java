package com.ctxh.volunteer.module.organization.repository;

import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrganizationRepository Integration Tests")
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role organizationRole;
    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        // Create role
        organizationRole = Role.builder()
                .roleName(RoleEnum.ORGANIZATION.name())
                .build();
        organizationRole = roleRepository.save(organizationRole);

        // Create user and organization
        User user = User.builder()
                .email("org@example.com")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(organizationRole))
                .build();

        testOrganization = Organization.builder()
                .user(user)
                .organizationName("Test Organization")
                .type(OrganizationType.NGO)
                .representativeName("John Doe")
                .representativeEmail("john@test.com")
                .representativePhone("0123456789")
                .build();

        user.setOrganization(testOrganization);
        userRepository.save(user);
        testOrganization = organizationRepository.save(testOrganization);
    }

    // ==================== EXISTS BY ORGANIZATION NAME TESTS ====================

    @Test
    @DisplayName("Exists By Organization Name - Returns true when exists")
    void existsByOrganizationName_ReturnsTrue_WhenExists() {
        // Act
        boolean exists = organizationRepository.existsByOrganizationName("Test Organization");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists By Organization Name - Returns false when not exists")
    void existsByOrganizationName_ReturnsFalse_WhenNotExists() {
        // Act
        boolean exists = organizationRepository.existsByOrganizationName("Nonexistent Organization");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Exists By Organization Name - Is case sensitive")
    void existsByOrganizationName_IsCaseSensitive() {
        // Act
        boolean exists = organizationRepository.existsByOrganizationName("test organization");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Exists By Organization Name - Handles special characters")
    void existsByOrganizationName_HandlesSpecialCharacters() {
        // Arrange
        User user2 = User.builder()
                .email("special@example.com")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(organizationRole))
                .build();

        Organization specialOrg = Organization.builder()
                .user(user2)
                .organizationName("Org & Company (HCM)")
                .type(OrganizationType.COMPANY)
                .build();

        user2.setOrganization(specialOrg);
        userRepository.save(user2);
        organizationRepository.save(specialOrg);

        // Act
        boolean exists = organizationRepository.existsByOrganizationName("Org & Company (HCM)");

        // Assert
        assertThat(exists).isTrue();
    }

    // ==================== FIND BY ID TESTS ====================

    @Test
    @DisplayName("Find By ID - Returns organization")
    void findById_ReturnsOrganization() {
        // Act
        Optional<Organization> result = organizationRepository.findById(
                testOrganization.getOrganizationId()
        );

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getOrganizationName()).isEqualTo("Test Organization");
        assertThat(result.get().getType()).isEqualTo(OrganizationType.NGO);
    }

    @Test
    @DisplayName("Find By ID - Returns empty when not found")
    void findById_ReturnsEmpty_WhenNotFound() {
        // Act
        Optional<Organization> result = organizationRepository.findById(999999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== FIND ALL TESTS ====================

    @Test
    @DisplayName("Find All - Returns all organizations")
    void findAll_ReturnsAllOrganizations() {
        // Arrange
        User user2 = User.builder()
                .email("org2@example.com")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(organizationRole))
                .build();

        Organization org2 = Organization.builder()
                .user(user2)
                .organizationName("Second Organization")
                .type(OrganizationType.GOVERNMENT)
                .build();

        user2.setOrganization(org2);
        userRepository.save(user2);
        organizationRepository.save(org2);

        // Act
        List<Organization> result = organizationRepository.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Organization::getOrganizationName)
                .containsExactlyInAnyOrder("Test Organization", "Second Organization");
    }

    @Test
    @DisplayName("Find All - Returns empty list when no organizations")
    void findAll_ReturnsEmpty_WhenNoOrganizations() {
        // Arrange
        organizationRepository.deleteAll();

        // Act
        List<Organization> result = organizationRepository.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== SAVE TESTS ====================

    @Test
    @DisplayName("Save - Creates new organization")
    void save_CreatesNewOrganization() {
        // Arrange
        User newUser = User.builder()
                .email("neworg@example.com")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(organizationRole))
                .build();

        Organization newOrg = Organization.builder()
                .user(newUser)
                .organizationName("New Organization")
                .type(OrganizationType.COMMUNITY_GROUP)
                .representativeName("Jane Smith")
                .build();

        newUser.setOrganization(newOrg);
        userRepository.save(newUser);

        // Act
        Organization saved = organizationRepository.save(newOrg);

        // Assert
        assertThat(saved.getOrganizationId()).isNotNull();
        assertThat(saved.getOrganizationName()).isEqualTo("New Organization");
        assertThat(saved.getType()).isEqualTo(OrganizationType.COMMUNITY_GROUP);
    }

    @Test
    @DisplayName("Save - Updates existing organization")
    void save_UpdatesExistingOrganization() {
        // Arrange
        testOrganization.setRepresentativeName("Updated Name");
        testOrganization.setRepresentativeEmail("updated@test.com");

        // Act
        Organization updated = organizationRepository.save(testOrganization);

        // Assert
        assertThat(updated.getOrganizationId()).isEqualTo(testOrganization.getOrganizationId());
        assertThat(updated.getRepresentativeName()).isEqualTo("Updated Name");
        assertThat(updated.getRepresentativeEmail()).isEqualTo("updated@test.com");
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Delete - Removes organization")
    void delete_RemovesOrganization() {
        // Arrange
        Long orgId = testOrganization.getOrganizationId();

        // Act
        organizationRepository.delete(testOrganization);

        // Assert
        Optional<Organization> result = organizationRepository.findById(orgId);
        assertThat(result).isEmpty();
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("Count - Returns correct count")
    void count_ReturnsCorrectCount() {
        // Arrange
        User user2 = User.builder()
                .email("org2@example.com")
                .password("password")
                .avatarUrl("avatar.png")
                .roles(List.of(organizationRole))
                .build();

        Organization org2 = Organization.builder()
                .user(user2)
                .organizationName("Second Organization")
                .type(OrganizationType.GOVERNMENT)
                .build();

        user2.setOrganization(org2);
        userRepository.save(user2);
        organizationRepository.save(org2);

        // Act
        long count = organizationRepository.count();

        // Assert
        assertThat(count).isEqualTo(2);
    }

    // ==================== ORGANIZATION TYPE TESTS ====================

    @Test
    @DisplayName("Organization Types - Saves and retrieves different types")
    void organizationTypes_SavesAndRetrievesDifferentTypes() {
        // Arrange
        OrganizationType[] types = {
                OrganizationType.NGO,
                OrganizationType.GOVERNMENT,
                OrganizationType.COMMUNITY_GROUP,
                OrganizationType.COMPANY
        };

        for (int i = 0; i < types.length; i++) {
            User user = User.builder()
                    .email("org" + i + "@example.com")
                    .password("password")
                    .avatarUrl("avatar.png")
                    .roles(List.of(organizationRole))
                    .build();

            Organization org = Organization.builder()
                    .user(user)
                    .organizationName("Organization " + i)
                    .type(types[i])
                    .build();

            user.setOrganization(org);
            userRepository.save(user);
            organizationRepository.save(org);
        }

        // Act
        List<Organization> all = organizationRepository.findAll();

        // Assert
        assertThat(all).hasSizeGreaterThanOrEqualTo(4);
        assertThat(all).extracting(Organization::getType)
                .contains(types);
    }

    // ==================== REPRESENTATIVE INFO TESTS ====================

    @Test
    @DisplayName("Representative Info - Saves and retrieves correctly")
    void representativeInfo_SavesAndRetrievesCorrectly() {
        // Assert
        assertThat(testOrganization.getRepresentativeName()).isEqualTo("John Doe");
        assertThat(testOrganization.getRepresentativeEmail()).isEqualTo("john@test.com");
        assertThat(testOrganization.getRepresentativePhone()).isEqualTo("0123456789");
    }

    @Test
    @DisplayName("Representative Info - Can be updated")
    void representativeInfo_CanBeUpdated() {
        // Arrange
        testOrganization.setRepresentativeName("Jane Smith");
        testOrganization.setRepresentativeEmail("jane@test.com");
        testOrganization.setRepresentativePhone("0987654321");

        // Act
        Organization updated = organizationRepository.save(testOrganization);

        // Assert
        assertThat(updated.getRepresentativeName()).isEqualTo("Jane Smith");
        assertThat(updated.getRepresentativeEmail()).isEqualTo("jane@test.com");
        assertThat(updated.getRepresentativePhone()).isEqualTo("0987654321");
    }
}
