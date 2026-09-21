CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    user_id UUID NOT NULL REFERENCES users (id),
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_memberships_user ON memberships (user_id);

CREATE TABLE vendors (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    name VARCHAR(200) NOT NULL,
    tax_number VARCHAR(20),
    email VARCHAR(180),
    phone VARCHAR(40),
    iban VARCHAR(34),
    address VARCHAR(400),
    party_type VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_vendors_org ON vendors (organization_id);
CREATE INDEX idx_vendors_org_name ON vendors (organization_id, name);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    name VARCHAR(80) NOT NULL,
    kind VARCHAR(20) NOT NULL,
    color VARCHAR(16) NOT NULL,
    icon VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, name, kind)
);

CREATE TABLE recurring_rules (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    vendor_id UUID REFERENCES vendors (id),
    category_id UUID REFERENCES categories (id),
    direction VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'TRY',
    interval VARCHAR(20) NOT NULL,
    next_due_date DATE NOT NULL,
    day_of_month INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated_at TIMESTAMPTZ,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    vendor_id UUID REFERENCES vendors (id),
    category_id UUID REFERENCES categories (id),
    recurring_rule_id UUID REFERENCES recurring_rules (id),
    created_by_id UUID REFERENCES users (id),
    direction VARCHAR(20) NOT NULL,
    invoice_number VARCHAR(80),
    issue_date DATE NOT NULL,
    due_date DATE,
    paid_date DATE,
    currency CHAR(3) NOT NULL DEFAULT 'TRY',
    subtotal NUMERIC(14, 2) NOT NULL DEFAULT 0,
    vat_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(40),
    notes VARCHAR(2000),
    ubl_xml TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_invoices_org_issue ON invoices (organization_id, issue_date DESC);
CREATE INDEX idx_invoices_org_status ON invoices (organization_id, status);
CREATE INDEX idx_invoices_org_due ON invoices (organization_id, due_date);
CREATE INDEX idx_invoices_org_direction ON invoices (organization_id, direction);

CREATE TABLE invoice_lines (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    description VARCHAR(300) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL DEFAULT 1,
    unit_price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    vat_rate NUMERIC(5, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE invoice_files (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    storage_key VARCHAR(400) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    invoice_id UUID REFERENCES invoices (id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_alerts_org ON alerts (organization_id, created_at DESC);

CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    category_id UUID REFERENCES categories (id),
    year INT NOT NULL,
    month INT,
    amount NUMERIC(14, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'TRY',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX idx_budgets_unique
    ON budgets (organization_id, year, COALESCE(month, 0), COALESCE(category_id, '00000000-0000-0000-0000-000000000000'));

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    user_id UUID REFERENCES users (id),
    action VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID,
    details VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_org ON audit_logs (organization_id, created_at DESC);
