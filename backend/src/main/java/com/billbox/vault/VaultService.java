package com.billbox.vault;

import com.billbox.common.web.PageResponse;
import com.billbox.security.AuthPrincipal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class VaultService {

    private static final String UNION = """
            SELECT kind, id, title, subtitle, occurred_on, highlight_on, amount, currency, status, category_name
            FROM (
                SELECT 'BILL' AS kind,
                       i.id,
                       COALESCE(NULLIF(i.invoice_number, ''), 'Fatura') AS title,
                       COALESCE(v.name, '') AS subtitle,
                       i.issue_date AS occurred_on,
                       i.due_date AS highlight_on,
                       i.total AS amount,
                       i.currency,
                       i.status,
                       c.name AS category_name
                FROM invoices i
                LEFT JOIN vendors v ON v.id = i.vendor_id
                LEFT JOIN categories c ON c.id = i.category_id
                WHERE i.organization_id = :org
                UNION ALL
                SELECT 'RECEIPT',
                       r.id,
                       r.merchant_name,
                       COALESCE(c.name, ''),
                       r.purchased_on,
                       r.return_until,
                       r.total,
                       r.currency,
                       'RECORDED',
                       c.name
                FROM receipts r
                LEFT JOIN categories c ON c.id = r.category_id
                WHERE r.organization_id = :org
                UNION ALL
                SELECT 'WARRANTY',
                       w.id,
                       w.product_name,
                       COALESCE(w.brand, w.merchant_name, ''),
                       w.purchased_on,
                       w.warranty_ends_on,
                       0,
                       'TRY',
                       CASE
                           WHEN w.warranty_ends_on < CURRENT_DATE THEN 'EXPIRED'
                           WHEN w.warranty_ends_on <= CURRENT_DATE + 30 THEN 'EXPIRING'
                           ELSE 'ACTIVE'
                       END,
                       NULL
                FROM warranties w
                WHERE w.organization_id = :org
            ) vault
            WHERE (:kind = '' OR kind = :kind)
              AND (:q = '' OR lower(title) LIKE :q OR lower(subtitle) LIKE :q)
            """;

    private final JdbcClient jdbcClient;

    public VaultService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Transactional(readOnly = true)
    public PageResponse<VaultDtos> search(AuthPrincipal principal, String kind, String query, int page, int size) {
        String normalizedKind = kind == null ? "" : kind.trim().toUpperCase(Locale.ROOT);
        String like = query == null || query.isBlank() ? "" : "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        List<VaultDtos> content = jdbcClient.sql(UNION + " ORDER BY occurred_on DESC NULLS LAST LIMIT :limit OFFSET :offset")
                .param("org", principal.organizationId())
                .param("kind", normalizedKind)
                .param("q", like)
                .param("limit", safeSize)
                .param("offset", safePage * safeSize)
                .query(VaultDtos.class)
                .list();
        long total = jdbcClient.sql("SELECT count(*) FROM (" + UNION + ") counted")
                .param("org", principal.organizationId())
                .param("kind", normalizedKind)
                .param("q", like)
                .query(Long.class)
                .single();
        int totalPages = (int) Math.ceil(total / (double) safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, totalPages);
    }
}
