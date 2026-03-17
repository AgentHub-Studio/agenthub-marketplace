package dev.cezar.agenthub.marketplace.repository;

import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link MarketplaceReview} (global {@code agenthub} schema).
 *
 * @since 1.0.0
 */
public interface MarketplaceReviewRepository extends ReactiveCrudRepository<MarketplaceReview, UUID> {

    @Query("SELECT * FROM agenthub.marketplace_review WHERE listing_id = :listingId ORDER BY created_at DESC")
    Flux<MarketplaceReview> findByListingId(UUID listingId);

    @Query("SELECT * FROM agenthub.marketplace_review WHERE listing_id = :listingId AND tenant_id = :tenantId AND reviewer_id = :reviewerId LIMIT 1")
    Mono<MarketplaceReview> findByListingIdAndTenantIdAndReviewerId(UUID listingId, UUID tenantId, UUID reviewerId);

    @Query("SELECT COUNT(*) FROM agenthub.marketplace_review WHERE listing_id = :listingId")
    Mono<Long> countByListingId(UUID listingId);
}
