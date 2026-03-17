package dev.cezar.agenthub.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a package published to the marketplace by a tenant.
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("marketplace_listing")
public class MarketplaceListing {

    @Id
    private UUID id;

    @Column("package_id")
    private UUID packageId;

    @Column("package_name")
    private String packageName;

    @Column("package_slug")
    private String packageSlug;

    @Column("package_type")
    private String packageType;

    private String description;
    private String version;

    @Column("author_name")
    private String authorName;

    private String[] tags;
    private String category;
    private String visibility;
    private String status;

    @Column("download_count")
    private Long downloadCount;

    @Column("avg_rating")
    private BigDecimal avgRating;

    @Column("review_count")
    private Integer reviewCount;

    @Column("published_at")
    private OffsetDateTime publishedAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
