package com.ctxh.volunteer.module.auth.repository;

import com.ctxh.volunteer.module.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshTokenUuid(String refreshTokenUuid);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.userId = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
}
