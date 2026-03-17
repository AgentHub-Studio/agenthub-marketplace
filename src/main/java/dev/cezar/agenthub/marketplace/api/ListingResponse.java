package dev.cezar.agenthub.marketplace.api;

import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for a global marketplace listing.
 *
 * @since 1.0.0
 */
public record ListingResponse(
        UUID id,
        UUID tenantId,
        UUID packageId,
        String packageName,
        String packageSlug,
        String packageType,
        String description,
        String version,
        String authorName,
        String[] tags,
        String category,
        String visibility,
        String status,
        Long downloadCount,
        BigDecimal avgRating,
        Integer reviewCount,
        OffsetDateTime publishedAt,
        OffsetDateTime updatedAt
) {

    /**
     * Converts a {@link MarketplaceListing} entity to a response DTO.
     *
     * @param listing the entity
     * @return response DTO
     */
    public static ListingResponse from(MarketplaceListing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getTenantId(),
                listing.getPackageId(),
                listing.getPackageName(),
                listing.getPackageSlug(),
                listing.getPackageType(),
                listing.getDescription(),
                listing.getVersion(),
                listing.getAuthorName(),
                listing.getTags(),
                listing.getCategory(),
                listing.getVisibility(),
                listing.getStatus(),
                listing.getDownloadCount(),
                listing.getAvgRating(),
                listing.getReviewCount(),
                listing.getPublishedAt(),
                listing.getUpdatedAt()
        );
    }
}
