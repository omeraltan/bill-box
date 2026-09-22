package com.billbox.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("""
            select t from PasswordResetToken t
            where t.tokenHash = :tokenHash
            """)
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Query("""
            select t from PasswordResetToken t
            where t.user.id = :userId
              and t.usedAt is null
            """)
    List<PasswordResetToken> findByUser_IdAndUsedAtIsNull(UUID userId);
}
