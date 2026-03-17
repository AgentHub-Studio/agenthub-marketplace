-- AgentHub Marketplace — Public Schema (shared agenthub schema)
-- The marketplace uses per-tenant schemas for listings and reviews.
-- This migration only enables the uuid-ossp extension in the public schema.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
