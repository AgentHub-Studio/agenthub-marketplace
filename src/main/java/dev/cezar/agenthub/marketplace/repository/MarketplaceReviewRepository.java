package dev.cezar.agenthub.marketplace.repository;

import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link MarketplaceReview}.
 *
 * @since 1.0.0
 */
public interface MarketplaceReviewRepository extends ReactiveCrudRepository<MarketplaceReview, UUID> {

    Flux<MarketplaceReview> findByListingId(UUID listingId);

    Mono<MarketplaceReview> findByListingIdAndReviewerId(UUID listingId, UUID reviewerId);

    Mono<Long> countByListingId(UUID listingId);
}
