package dev.cezar.agenthub.marketplace.repository;

import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link MarketplaceListing}.
 *
 * @since 1.0.0
 */
public interface MarketplaceListingRepository extends ReactiveCrudRepository<MarketplaceListing, UUID> {

    Flux<MarketplaceListing> findByStatus(String status);

    Flux<MarketplaceListing> findByPackageType(String packageType);

    Flux<MarketplaceListing> findByCategory(String category);

    Mono<MarketplaceListing> findByPackageSlug(String packageSlug);

    Mono<MarketplaceListing> findByPackageId(UUID packageId);
}
