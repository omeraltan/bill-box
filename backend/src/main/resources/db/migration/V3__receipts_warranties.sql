CREATE TABLE receipts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    vendor_id UUID REFERENCES vendors (id),
    category_id UUID REFERENCES categories (id),
    created_by_id UUID REFERENCES users (id),
    merchant_name VARCHAR(200) NOT NULL,
    purchased_on DATE NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'TRY',
    total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(40),
    return_until DATE,
    notes VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_receipts_org_date ON receipts (organization_id, purchased_on DESC);

CREATE TABLE receipt_items (
    id UUID PRIMARY KEY,
    receipt_id UUID NOT NULL REFERENCES receipts (id) ON DELETE CASCADE,
    description VARCHAR(300) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL DEFAULT 1,
    unit_price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    line_total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    warranty_months INT,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE receipt_files (
    id UUID PRIMARY KEY,
    receipt_id UUID NOT NULL REFERENCES receipts (id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    storage_key VARCHAR(400) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE warranties (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    receipt_id UUID REFERENCES receipts (id) ON DELETE SET NULL,
    vendor_id UUID REFERENCES vendors (id),
    created_by_id UUID REFERENCES users (id),
    product_name VARCHAR(200) NOT NULL,
    brand VARCHAR(120),
    serial_number VARCHAR(120),
    merchant_name VARCHAR(200),
    purchased_on DATE NOT NULL,
    warranty_ends_on DATE NOT NULL,
    return_until DATE,
    notes VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_warranties_org_end ON warranties (organization_id, warranty_ends_on);

CREATE TABLE warranty_files (
    id UUID PRIMARY KEY,
    warranty_id UUID NOT NULL REFERENCES warranties (id) ON DELETE CASCADE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    storage_key VARCHAR(400) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE alerts ADD COLUMN warranty_id UUID REFERENCES warranties (id) ON DELETE CASCADE;
ALTER TABLE alerts ADD COLUMN receipt_id UUID REFERENCES receipts (id) ON DELETE CASCADE;

INSERT INTO categories (id, organization_id, name, kind, color, icon, created_at, updated_at)
SELECT gen_random_uuid(), o.id, seed.name, 'EXPENSE', seed.color, seed.icon, now(), now()
FROM organizations o
CROSS JOIN (
    VALUES
        ('Elektronik', '#6366f1', 'mobile'),
        ('Beyaz Eşya', '#0ea5e9', 'box'),
        ('Ev Eşyası', '#14b8a6', 'home'),
        ('Giyim', '#e11d48', 'tag')
) AS seed(name, color, icon)
WHERE NOT EXISTS (
    SELECT 1 FROM categories c
    WHERE c.organization_id = o.id AND c.name = seed.name AND c.kind = 'EXPENSE'
);
