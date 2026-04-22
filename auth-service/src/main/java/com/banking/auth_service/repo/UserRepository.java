package com.banking.auth_service.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.auth_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ? — login ke liye
    Optional<User> findByEmail(String email);

    // SELECT EXISTS(SELECT 1 FROM users WHERE email = ?) — duplicate check
    boolean existsByEmail(String email);
}
