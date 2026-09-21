package com.billbox.auth;

import com.billbox.auth.AuthDtos.AuthResponse;
import com.billbox.auth.AuthDtos.LoginRequest;
import com.billbox.auth.AuthDtos.RegisterRequest;
import com.billbox.category.CategorySeeder;
import com.billbox.common.enums.MembershipRole;
import com.billbox.common.exception.ApiException;
import com.billbox.organization.Membership;
import com.billbox.organization.MembershipRepository;
import com.billbox.organization.Organization;
import com.billbox.organization.OrganizationRepository;
import com.billbox.security.AuthPrincipal;
import com.billbox.security.JwtService;
import com.billbox.user.UserAccount;
import com.billbox.user.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CategorySeeder categorySeeder;

    public AuthService(
            UserAccountRepository userAccountRepository,
            OrganizationRepository organizationRepository,
            MembershipRepository membershipRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CategorySeeder categorySeeder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.categorySeeder = categorySeeder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("Bu e-posta adresi zaten kayıtlı.");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userAccountRepository.save(user);

        Organization organization = new Organization();
        String orgName = request.organizationName() == null || request.organizationName().isBlank()
                ? request.fullName().trim() + " Kasası"
                : request.organizationName().trim();
        organization.setName(orgName);
        organizationRepository.save(organization);
        categorySeeder.seedFor(organization);

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setOrganization(organization);
        membership.setRole(MembershipRole.OWNER);
        membershipRepository.save(membership);

        return toResponse(principal(user, organization, membership.getRole()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("invalid"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("invalid");
        }
        Membership membership = membershipRepository.findFirstByUserIdOrderByCreatedAtAsc(user.getId())
                .orElseThrow(() -> ApiException.unauthorized("Kullanıcının organizasyonu bulunamadı."));
        return toResponse(principal(user, membership.getOrganization(), membership.getRole()));
    }

    @Transactional(readOnly = true)
    public AuthResponse me(AuthPrincipal principal) {
        return toResponse(principal);
    }

    private AuthPrincipal principal(UserAccount user, Organization organization, MembershipRole role) {
        return new AuthPrincipal(
                user.getId(),
                organization.getId(),
                user.getEmail(),
                user.getFullName(),
                organization.getName(),
                role
        );
    }

    private AuthResponse toResponse(AuthPrincipal principal) {
        return new AuthResponse(
                jwtService.createToken(principal),
                principal.userId(),
                principal.organizationId(),
                principal.fullName(),
                principal.email(),
                principal.organizationName(),
                principal.role()
        );
    }
}
