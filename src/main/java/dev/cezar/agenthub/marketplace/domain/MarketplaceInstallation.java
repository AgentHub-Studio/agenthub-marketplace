package dev.cezar.agenthub.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Records a package installed by a tenant from the global marketplace catalog.
 * Lives in the per-tenant schema {@code ah_{tenantId}} — no {@code tenant_id} column needed.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("marketplace_installation")
public class MarketplaceInstallation {

    @Id
    private UUID id;

    /** Reference to {@code agenthub.marketplace_listing.id}. */
    @Column("listing_id")
    private UUID listingId;

    @Column("package_slug")
    private String packageSlug;

    @Column("package_type")
    private String packageType;

    @Column("installed_version")
    private String installedVersion;

    private String status;

    @Column("installed_at")
    private OffsetDateTime installedAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
