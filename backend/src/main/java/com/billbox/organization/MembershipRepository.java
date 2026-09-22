package com.billbox.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    @Query("""
            select m from Membership m
            join fetch m.organization
            join fetch m.user
            where m.user.id = :userId
            order by m.createdAt
            """)
    List<Membership> findByUserId(UUID userId);

    @Query("""
            select m from Membership m
            where m.user.id = :userId
            order by m.createdAt asc
            fetch first 1 row only
            """)
    Optional<Membership> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);

    @Query("""
            select m from Membership m
            join fetch m.organization
            join fetch m.user
            where m.organization.id = :organizationId
            order by m.createdAt
            """)
    List<Membership> findByOrganizationId(UUID organizationId);
}
