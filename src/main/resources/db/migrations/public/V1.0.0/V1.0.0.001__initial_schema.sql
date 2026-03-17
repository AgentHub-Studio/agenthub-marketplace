-- AgentHub Marketplace — Public Schema (agenthub)
-- Global catalog visible to all tenants.
-- Tables here carry tenant_id (the publisher/reviewer).

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- MARKETPLACE LISTING
-- Global package catalog. Each listing is owned by the tenant that published it.
-- Visible to all tenants for discovery/installation.
-- =============================================================================

CREATE TABLE IF NOT EXISTS agenthub.marketplace_listing (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id       UUID          NOT NULL,        -- publisher tenant
    package_id      UUID          NOT NULL,        -- ref to package_registry in publisher's schema
    package_name    VARCHAR(200)  NOT NULL,
    package_slug    VARCHAR(200)  NOT NULL,
    package_type    VARCHAR(50)   NOT NULL,        -- AGENT, SKILL, TOOL, etc.
    description     TEXT,
    version         VARCHAR(50)   NOT NULL,
    author_name     VARCHAR(200),
    tags            TEXT[],
    category        VARCHAR(100),
    visibility      VARCHAR(30)   NOT NULL DEFAULT 'PUBLIC',
    status          VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, SUSPENDED, REMOVED
    download_count  BIGINT        NOT NULL DEFAULT 0,
    avg_rating      DECIMAL(3,2)  DEFAULT 0.00,
    review_count    INTEGER       NOT NULL DEFAULT 0,
    published_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, package_slug)
);

CREATE INDEX IF NOT EXISTS idx_listing_tenant      ON agenthub.marketplace_listing (tenant_id);
CREATE INDEX IF NOT EXISTS idx_listing_status      ON agenthub.marketplace_listing (status);
CREATE INDEX IF NOT EXISTS idx_listing_type        ON agenthub.marketplace_listing (package_type);
CREATE INDEX IF NOT EXISTS idx_listing_category    ON agenthub.marketplace_listing (category);
CREATE INDEX IF NOT EXISTS idx_listing_slug        ON agenthub.marketplace_listing (package_slug);
CREATE INDEX IF NOT EXISTS idx_listing_published   ON agenthub.marketplace_listing (published_at DESC);

-- =============================================================================
-- MARKETPLACE REVIEW
-- Reviews/ratings on listings. One review per (listing, reviewer_tenant).
-- =============================================================================

CREATE TABLE IF NOT EXISTS agenthub.marketplace_review (
    id           UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id   UUID        NOT NULL REFERENCES agenthub.marketplace_listing (id) ON DELETE CASCADE,
    tenant_id    UUID        NOT NULL,   -- reviewer tenant
    reviewer_id  UUID        NOT NULL,   -- user within that tenant
    rating       SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title        VARCHAR(200),
    body         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id, tenant_id, reviewer_id)
);

CREATE INDEX IF NOT EXISTS idx_review_listing   ON agenthub.marketplace_review (listing_id);
CREATE INDEX IF NOT EXISTS idx_review_tenant    ON agenthub.marketplace_review (tenant_id);
CREATE INDEX IF NOT EXISTS idx_review_reviewer  ON agenthub.marketplace_review (reviewer_id);
