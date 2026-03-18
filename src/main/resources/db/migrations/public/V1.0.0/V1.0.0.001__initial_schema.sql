-- AgentHub Marketplace — Public Schema
-- Global catalog visible to all tenants.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- MARKETPLACE LISTING
-- Global package catalog. Each listing references a package in the registry.
-- Status flow: PENDING → APPROVED or REJECTED.
-- No tenant_id — listings are global (not cross-tenant, no publisher ownership here).
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketplace_listing (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id          UUID          NOT NULL,
    title               VARCHAR(255)  NOT NULL,
    short_description   VARCHAR(500),
    long_description    TEXT,
    tags                JSONB,
    category            VARCHAR(100),
    featured_image_url  TEXT,
    status              VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    published_at        TIMESTAMPTZ,
    published_by        UUID
);

CREATE INDEX IF NOT EXISTS idx_listing_package_id  ON marketplace_listing (package_id);
CREATE INDEX IF NOT EXISTS idx_listing_status      ON marketplace_listing (status);
CREATE INDEX IF NOT EXISTS idx_listing_category    ON marketplace_listing (category);

-- =============================================================================
-- MARKETPLACE RATING
-- Ratings/reviews submitted by tenants for a listing.
-- Carries tenant_id because each tenant can rate a listing at most once.
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketplace_rating (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id  UUID        NOT NULL REFERENCES marketplace_listing (id) ON DELETE CASCADE,
    tenant_id   VARCHAR(100) NOT NULL,
    rating      SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review      TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_rating_listing   ON marketplace_rating (listing_id);
CREATE INDEX IF NOT EXISTS idx_rating_tenant    ON marketplace_rating (tenant_id);
