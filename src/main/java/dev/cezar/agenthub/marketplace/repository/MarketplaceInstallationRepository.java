package dev.cezar.agenthub.marketplace.repository;

import dev.cezar.agenthub.marketplace.domain.MarketplaceInstallation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link MarketplaceInstallation} (per-tenant schema).
 * Isolation is automatic via the tenant search_path set by {@code MultiTenantFilter}.
 *
 * @since 1.0.0
 */
public interface MarketplaceInstallationRepository extends ReactiveCrudRepository<MarketplaceInstallation, UUID> {

    Flux<MarketplaceInstallation> findByStatus(String status);

    Flux<MarketplaceInstallation> findByPackageType(String packageType);

    Mono<MarketplaceInstallation> findByListingId(UUID listingId);

    Mono<Boolean> existsByListingId(UUID listingId);
}
