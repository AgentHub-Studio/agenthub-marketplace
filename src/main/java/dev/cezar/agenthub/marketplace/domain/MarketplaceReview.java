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
 * A user review for a marketplace listing.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("marketplace_review")
public class MarketplaceReview {

    @Id
    private UUID id;

    @Column("listing_id")
    private UUID listingId;

    @Column("reviewer_id")
    private UUID reviewerId;

    private Short rating;
    private String title;
    private String body;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
