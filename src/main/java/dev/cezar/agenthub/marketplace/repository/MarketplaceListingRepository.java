package dev.cezar.agenthub.marketplace.repository;

import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link MarketplaceListing} (global {@code agenthub} schema).
 *
 * @since 1.0.0
 */
public interface MarketplaceListingRepository extends ReactiveCrudRepository<MarketplaceListing, UUID> {

    @Query("SELECT * FROM agenthub.marketplace_listing WHERE status = :status ORDER BY published_at DESC")
    Flux<MarketplaceListing> findByStatus(String status);

    @Query("SELECT * FROM agenthub.marketplace_listing WHERE status = 'ACTIVE' AND package_type = :packageType ORDER BY published_at DESC")
    Flux<MarketplaceListing> findActiveByPackageType(String packageType);

    @Query("SELECT * FROM agenthub.marketplace_listing WHERE status = 'ACTIVE' AND category = :category ORDER BY published_at DESC")
    Flux<MarketplaceListing> findActiveByCategory(String category);

    @Query("SELECT * FROM agenthub.marketplace_listing WHERE package_slug = :packageSlug LIMIT 1")
    Mono<MarketplaceListing> findByPackageSlug(String packageSlug);

    @Query("SELECT * FROM agenthub.marketplace_listing WHERE tenant_id = :tenantId ORDER BY published_at DESC")
    Flux<MarketplaceListing> findByTenantId(UUID tenantId);

    @Query("SELECT COUNT(*) > 0 FROM agenthub.marketplace_listing WHERE tenant_id = :tenantId AND package_slug = :packageSlug")
    Mono<Boolean> existsByTenantIdAndPackageSlug(UUID tenantId, String packageSlug);
}
