package com.billbox.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    @Query("""
            select u from UserAccount u
            where lower(u.email) = lower(:email)
            """)
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @Query("""
            select count(u) > 0 from UserAccount u
            where lower(u.email) = lower(:email)
            """)
    boolean existsByEmailIgnoreCase(String email);
}
