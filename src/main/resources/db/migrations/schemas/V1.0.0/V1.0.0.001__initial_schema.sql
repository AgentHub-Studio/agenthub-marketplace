-- AgentHub Marketplace — Tenant Schema (ah_{tenantId})
-- Tracks what packages this tenant has installed from the global marketplace.
-- No tenant_id — isolation is via schema.

-- =============================================================================
-- MARKETPLACE INSTALLATION
-- Records packages installed by this tenant from the global catalog.
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketplace_installation (
    id             UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    listing_id     UUID        NOT NULL,       -- ref to agenthub.marketplace_listing.id
    package_slug   VARCHAR(200) NOT NULL,
    package_type   VARCHAR(50)  NOT NULL,
    installed_version VARCHAR(50) NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, REMOVED
    installed_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id)
);

CREATE INDEX IF NOT EXISTS idx_installation_listing ON marketplace_installation (listing_id);
CREATE INDEX IF NOT EXISTS idx_installation_type    ON marketplace_installation (package_type);
CREATE INDEX IF NOT EXISTS idx_installation_status  ON marketplace_installation (status);
