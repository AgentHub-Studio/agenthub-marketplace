-- AgentHub Marketplace — Tenant Schema (ah_{tenantId})
-- Tables in this file do NOT carry tenant_id — isolation is via schema.

-- =============================================================================
-- MARKETPLACE LISTING
-- Represents a package published to the marketplace by a tenant.
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketplace_listing (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    package_id      UUID          NOT NULL,
    package_name    VARCHAR(200)  NOT NULL,
    package_slug    VARCHAR(200)  NOT NULL UNIQUE,
    package_type    VARCHAR(50)   NOT NULL,
    description     TEXT,
    version         VARCHAR(50)   NOT NULL,
    author_name     VARCHAR(200),
    tags            TEXT[],
    category        VARCHAR(100),
    visibility      VARCHAR(30)   NOT NULL DEFAULT 'PUBLIC',
    status          VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE',
    download_count  BIGINT        NOT NULL DEFAULT 0,
    avg_rating      DECIMAL(3,2)  DEFAULT 0.00,
    review_count    INTEGER       NOT NULL DEFAULT 0,
    published_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_listing_status       ON marketplace_listing (status);
CREATE INDEX IF NOT EXISTS idx_listing_package_type ON marketplace_listing (package_type);
CREATE INDEX IF NOT EXISTS idx_listing_category     ON marketplace_listing (category);
CREATE INDEX IF NOT EXISTS idx_listing_slug         ON marketplace_listing (package_slug);
CREATE INDEX IF NOT EXISTS idx_listing_package_id   ON marketplace_listing (package_id);
CREATE INDEX IF NOT EXISTS idx_listing_published    ON marketplace_listing (published_at DESC);

-- =============================================================================
-- MARKETPLACE REVIEW
-- A user review for a marketplace listing (one per reviewer per listing).
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketplace_review (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id   UUID        NOT NULL REFERENCES marketplace_listing (id) ON DELETE CASCADE,
    reviewer_id  UUID        NOT NULL,
    rating       SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title        VARCHAR(200),
    body         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id, reviewer_id)
);

CREATE INDEX IF NOT EXISTS idx_review_listing  ON marketplace_review (listing_id);
CREATE INDEX IF NOT EXISTS idx_review_reviewer ON marketplace_review (reviewer_id);
