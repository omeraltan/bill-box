package com.billbox.audit;

import com.billbox.organization.Organization;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.user.UserAccount;
import com.billbox.user.UserAccountRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final OrganizationRepository organizationRepository;
    private final UserAccountRepository userAccountRepository;

    public AuditService(
            AuditLogRepository auditLogRepository,
            OrganizationRepository organizationRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.organizationRepository = organizationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public void record(AuthPrincipal principal, String action, String entityType, UUID entityId, String details) {
        Organization organization = organizationRepository.getReferenceById(principal.organizationId());
        UserAccount user = userAccountRepository.getReferenceById(principal.userId());
        AuditLog log = new AuditLog();
        log.setOrganization(organization);
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> recent(UUID organizationId) {
        return auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, PageRequest.of(0, 30));
    }
}
