package dev.cezar.agenthub.marketplace.api;

import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for a marketplace review.
 *
 * @since 1.0.0
 */
public record ReviewResponse(
        UUID id,
        UUID listingId,
        UUID tenantId,
        UUID reviewerId,
        Short rating,
        String title,
        String body,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Converts a {@link MarketplaceReview} entity to a response DTO.
     *
     * @param review the entity
     * @return response DTO
     */
    public static ReviewResponse from(MarketplaceReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getListingId(),
                review.getTenantId(),
                review.getReviewerId(),
                review.getRating(),
                review.getTitle(),
                review.getBody(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
