package com.ctxh.volunteer.module.organization.service.impl;

import com.ctxh.volunteer.common.exception.BusinessException;
import com.ctxh.volunteer.common.exception.ErrorCode;
import com.ctxh.volunteer.module.auth.RoleEnum;
import com.ctxh.volunteer.module.auth.entity.Role;
import com.ctxh.volunteer.module.auth.entity.User;
import com.ctxh.volunteer.module.auth.repository.RoleRepository;
import com.ctxh.volunteer.module.auth.repository.UserRepository;
import com.ctxh.volunteer.module.organization.dto.request.CreateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.request.UpdateOrganizationRequestDto;
import com.ctxh.volunteer.module.organization.dto.response.OrganizationResponseDto;
import com.ctxh.volunteer.module.organization.entity.Organization;
import com.ctxh.volunteer.module.organization.enums.OrganizationType;
import com.ctxh.volunteer.module.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationService Unit Tests")
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private Role organizationRole;
    private User testUser;
    private Organization testOrganization;
    private CreateOrganizationRequestDto createRequest;
    private UpdateOrganizationRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        // Create test role
        organizationRole = Role.builder()
                .roleId(2L)
                .roleName(RoleEnum.ORGANIZATION.name())
                .build();

        // Create test user
        testUser = User.builder()
                .userId(1L)
                .email("org@example.com")
                .password("$2a$10$hashedPassword")
                .avatarUrl("default-avatar.png")
                .roles(List.of(organizationRole))
                .build();

        // Create test organization
        testOrganization = Organization.builder()
                .organizationId(1L)
                .user(testUser)
                .organizationName("Test Organization")
                .type(OrganizationType.NGO)
                .representativeName("John Doe")
                .representativeEmail("john@test.com")
                .representativePhone("0123456789")
                .build();

        testUser.setOrganization(testOrganization);

        // Create request DTOs
        createRequest = new CreateOrganizationRequestDto();
        createRequest.setEmail("neworg@example.com");
        createRequest.setPassword("password123");
        createRequest.setOrganizationName("New Organization");
        createRequest.setOrganizationType("NGO");

        updateRequest = new UpdateOrganizationRequestDto();
        updateRequest.setRepresentativeName("Jane Smith");
        updateRequest.setRepresentativeEmail("jane@test.com");
        updateRequest.setRepresentativePhoneNumber("0987654321");
        updateRequest.setBio("Updated bio");
    }

    // ==================== REGISTER ORGANIZATION TESTS ====================

    @Test
    @DisplayName("Register Organization - Success creates new organization")
    void registerOrganization_Success_CreatesNewOrganization() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(createRequest.getOrganizationName()))
                .thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.of(organizationRole));
        when(passwordEncoder.encode(createRequest.getPassword()))
                .thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    user.getOrganization().setOrganizationId(1L);
                    return user;
                });

        // Act
        OrganizationResponseDto result = organizationService.registerOrganization(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrganizationId()).isEqualTo(1L);
        assertThat(result.getOrganizationName()).isEqualTo("New Organization");
        assertThat(result.getEmail()).isEqualTo("neworg@example.com");
        assertThat(result.getType()).isEqualTo(OrganizationType.NGO);

        verify(organizationRepository).existsByOrganizationName(createRequest.getOrganizationName());
        verify(roleRepository).findByRoleName(RoleEnum.ORGANIZATION.name());
        verify(passwordEncoder).encode(createRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register Organization - Fails when organization name already exists")
    void registerOrganization_ThrowsException_WhenNameExists() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(createRequest.getOrganizationName()))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> organizationService.registerOrganization(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_NAME_ALREADY_EXISTS);

        verify(organizationRepository).existsByOrganizationName(createRequest.getOrganizationName());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register Organization - Fails when organization role not found")
    void registerOrganization_ThrowsException_WhenRoleNotFound() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(createRequest.getOrganizationName()))
                .thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> organizationService.registerOrganization(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROLE_NOT_FOUND);

        verify(roleRepository).findByRoleName(RoleEnum.ORGANIZATION.name());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register Organization - Encodes password")
    void registerOrganization_EncodesPassword() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.of(organizationRole));
        when(passwordEncoder.encode(createRequest.getPassword()))
                .thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    user.getOrganization().setOrganizationId(1L);
                    return user;
                });

        // Act
        organizationService.registerOrganization(createRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Register Organization - Sets default avatar URL")
    void registerOrganization_SetsDefaultAvatar() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.of(organizationRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    user.getOrganization().setOrganizationId(1L);
                    return user;
                });

        // Act
        OrganizationResponseDto result = organizationService.registerOrganization(createRequest);

        // Assert
        assertThat(result.getAvatarUrl()).isNotNull();
    }

    @Test
    @DisplayName("Register Organization - Assigns organization role")
    void registerOrganization_AssignsOrganizationRole() {
        // Arrange
        when(organizationRepository.existsByOrganizationName(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.of(organizationRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    user.getOrganization().setOrganizationId(1L);
                    return user;
                });

        // Act
        organizationService.registerOrganization(createRequest);

        // Assert
        verify(roleRepository).findByRoleName(RoleEnum.ORGANIZATION.name());
    }

    @Test
    @DisplayName("Register Organization - Sets correct organization type")
    void registerOrganization_SetsCorrectType() {
        // Arrange
        createRequest.setOrganizationType("GOVERNMENT");

        when(organizationRepository.existsByOrganizationName(anyString())).thenReturn(false);
        when(roleRepository.findByRoleName(RoleEnum.ORGANIZATION.name()))
                .thenReturn(Optional.of(organizationRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    user.getOrganization().setOrganizationId(1L);
                    return user;
                });

        // Act
        OrganizationResponseDto result = organizationService.registerOrganization(createRequest);

        // Assert
        assertThat(result.getType()).isEqualTo(OrganizationType.GOVERNMENT);
    }

    // ==================== UPDATE ORGANIZATION TESTS ====================

    @Test
    @DisplayName("Update Organization - Success updates organization info")
    void updateOrganization_Success_UpdatesOrganizationInfo() {
        // Arrange
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        OrganizationResponseDto result = organizationService.updateOrganization(1L, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRepresentativeName()).isEqualTo("Jane Smith");
        assertThat(result.getRepresentativeEmail()).isEqualTo("jane@test.com");
        assertThat(result.getRepresentativePhoneNumber()).isEqualTo("0987654321");
        assertThat(result.getBio()).isEqualTo("Updated bio");

        verify(organizationRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Update Organization - Fails when organization not found")
    void updateOrganization_ThrowsException_WhenOrganizationNotFound() {
        // Arrange
        when(organizationRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> organizationService.updateOrganization(999L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_NOT_FOUND);

        verify(organizationRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Organization - Updates only provided fields")
    void updateOrganization_UpdatesOnlyProvidedFields() {
        // Arrange
        UpdateOrganizationRequestDto partialUpdate = new UpdateOrganizationRequestDto();
        partialUpdate.setRepresentativeName("New Name");
        // Other fields are null

        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        organizationService.updateOrganization(1L, partialUpdate);

        // Assert
        assertThat(testOrganization.getRepresentativeName()).isEqualTo("New Name");
        // Original values should be preserved
        assertThat(testOrganization.getRepresentativeEmail()).isEqualTo("john@test.com");

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Update Organization - Updates bio in user entity")
    void updateOrganization_UpdatesBioInUser() {
        // Arrange
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        organizationService.updateOrganization(1L, updateRequest);

        // Assert
        assertThat(testUser.getBio()).isEqualTo("Updated bio");
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Update Organization - Handles null fields gracefully")
    void updateOrganization_HandlesNullFields() {
        // Arrange
        UpdateOrganizationRequestDto emptyUpdate = new UpdateOrganizationRequestDto();

        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        // Act
        OrganizationResponseDto result = organizationService.updateOrganization(1L, emptyUpdate);

        // Assert
        assertThat(result).isNotNull();
        // Original values should be preserved
        assertThat(result.getRepresentativeName()).isEqualTo("John Doe");
        assertThat(result.getRepresentativeEmail()).isEqualTo("john@test.com");
    }

    // ==================== GET ORGANIZATION BY ID TESTS ====================

    @Test
    @DisplayName("Get Organization By ID - Success returns organization")
    void getOrganizationById_Success_ReturnsOrganization() {
        // Arrange
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));

        // Act
        OrganizationResponseDto result = organizationService.getOrganizationById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrganizationId()).isEqualTo(1L);
        assertThat(result.getOrganizationName()).isEqualTo("Test Organization");
        assertThat(result.getEmail()).isEqualTo("org@example.com");
        assertThat(result.getType()).isEqualTo(OrganizationType.NGO);

        verify(organizationRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Organization By ID - Fails when organization not found")
    void getOrganizationById_ThrowsException_WhenOrganizationNotFound() {
        // Arrange
        when(organizationRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> organizationService.getOrganizationById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_NOT_FOUND);

        verify(organizationRepository).findById(999L);
    }

    @Test
    @DisplayName("Get Organization By ID - Returns all required fields")
    void getOrganizationById_ReturnsAllRequiredFields() {
        // Arrange
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));

        // Act
        OrganizationResponseDto result = organizationService.getOrganizationById(1L);

        // Assert
        assertThat(result.getOrganizationId()).isNotNull();
        assertThat(result.getOrganizationName()).isNotNull();
        assertThat(result.getType()).isNotNull();
        assertThat(result.getEmail()).isNotNull();
        assertThat(result.getAvatarUrl()).isNotNull();
        // Note: createdAt may be null in unit tests without database
    }

    @Test
    @DisplayName("Get Organization By ID - Maps representative info correctly")
    void getOrganizationById_MapsRepresentativeInfo() {
        // Arrange
        when(organizationRepository.findById(1L))
                .thenReturn(Optional.of(testOrganization));

        // Act
        OrganizationResponseDto result = organizationService.getOrganizationById(1L);

        // Assert
        assertThat(result.getRepresentativeName()).isEqualTo("John Doe");
        assertThat(result.getRepresentativeEmail()).isEqualTo("john@test.com");
        assertThat(result.getRepresentativePhoneNumber()).isEqualTo("0123456789");
    }
}
