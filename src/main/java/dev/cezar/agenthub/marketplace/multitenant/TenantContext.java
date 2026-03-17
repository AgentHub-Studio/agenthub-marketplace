package dev.cezar.agenthub.marketplace.multitenant;

import lombok.Getter;

import java.util.Objects;

/**
 * Holds tenant identity and derived schema name for the current request.
 *
 * @since 1.0.0
 */
@Getter
public class TenantContext {

    private final String tenantId;
    private final String userId;
    private String schemaName;

    public TenantContext(String tenantId) {
        this(tenantId, null);
    }

    public TenantContext(String tenantId, String userId) {
        this.tenantId = tenantId;
        this.userId = userId;
    }

    /**
     * Returns the PostgreSQL schema name for this tenant.
     *
     * @return schema name (e.g. {@code ah_<tenantId>})
     */
    public String getSchemaName() {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        if (schemaName == null) {
            schemaName = MultiTenant.DEFAULT_SCHEMA.equalsIgnoreCase(tenantId)
                    ? tenantId
                    : MultiTenant.SCHEMA_PREFIX + tenantId;
        }
        return schemaName;
    }
}
