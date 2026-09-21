package com.billbox.settings;

import com.billbox.audit.AuditLog;
import com.billbox.audit.AuditService;
import com.billbox.organization.Membership;
import com.billbox.organization.MembershipRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final MembershipRepository membershipRepository;
    private final AuditService auditService;

    public SettingsController(MembershipRepository membershipRepository, AuditService auditService) {
        this.membershipRepository = membershipRepository;
        this.auditService = auditService;
    }

    @GetMapping
    public SettingsResponse load() {
        AuthPrincipal principal = SecurityUtils.current();
        List<MemberResponse> members = membershipRepository.findByOrganizationId(principal.organizationId())
                .stream()
                .map(this::toMember)
                .toList();
        List<AuditResponse> audit = auditService.recent(principal.organizationId()).stream()
                .map(this::toAudit)
                .toList();
        return new SettingsResponse(principal.organizationName(), members, audit);
    }

    private MemberResponse toMember(Membership membership) {
        return new MemberResponse(
                membership.getUser().getId(),
                membership.getUser().getFullName(),
                membership.getUser().getEmail(),
                membership.getRole().name()
        );
    }

    private AuditResponse toAudit(AuditLog log) {
        return new AuditResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }

    public record SettingsResponse(String organizationName, List<MemberResponse> members, List<AuditResponse> auditLogs) {
    }

    public record MemberResponse(UUID userId, String fullName, String email, String role) {
    }

    public record AuditResponse(UUID id, String action, String entityType, String details, Instant createdAt) {
    }
}
