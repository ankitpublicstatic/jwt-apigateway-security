package com.ankit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ankit.entity.UserCredential;
import java.util.Optional;

public interface UserCredentialRepository  extends JpaRepository<UserCredential,Integer> {
    Optional<UserCredential> findByName(String username);
}
