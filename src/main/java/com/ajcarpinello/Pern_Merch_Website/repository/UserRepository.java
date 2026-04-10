package com.ajcarpinello.Pern_Merch_Website.repository;

import com.ajcarpinello.Pern_Merch_Website.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    // checks if already exists
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
