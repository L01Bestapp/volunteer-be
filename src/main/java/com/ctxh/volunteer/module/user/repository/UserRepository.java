package com.ctxh.volunteer.module.user.repository;

import com.ctxh.volunteer.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
