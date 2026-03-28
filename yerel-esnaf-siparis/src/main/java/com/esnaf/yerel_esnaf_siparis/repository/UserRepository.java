package com.esnaf.yerel_esnaf_siparis.repository;

import com.esnaf.yerel_esnaf_siparis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
