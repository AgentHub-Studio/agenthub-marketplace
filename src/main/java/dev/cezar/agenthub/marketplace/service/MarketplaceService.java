package dev.cezar.agenthub.marketplace.service;

import dev.cezar.agenthub.marketplace.api.*;
import dev.cezar.agenthub.marketplace.domain.MarketplaceInstallation;
import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
import dev.cezar.agenthub.marketplace.repository.MarketplaceInstallationRepository;
import dev.cezar.agenthub.marketplace.repository.MarketplaceListingRepository;
import dev.cezar.agenthub.marketplace.repository.MarketplaceReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Business logic for the global marketplace catalog and per-tenant installations.
 *
 * <p>Listings and reviews live in the shared {@code agenthub} schema (with {@code tenant_id}).
 * Installations live in the per-tenant schema {@code ah_{tenantId}} (no {@code tenant_id}).</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketplaceListingRepository listingRepository;
    private final MarketplaceReviewRepository reviewRepository;
    private final MarketplaceInstallationRepository installationRepository;

    // ── Global Catalog ────────────────────────────────────────────────────────

    /**
     * Publishes a package to the global marketplace catalog.
     * Each tenant can publish at most one listing per package slug.
     *
     * @param tenantId publisher tenant ID
     * @param request  publish request
     * @return created listing
     */
    public Mono<ListingResponse> publishListing(UUID tenantId, PublishListingRequest request) {
        return listingRepository.existsByTenantIdAndPackageSlug(tenantId, request.packageSlug())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Listing already published for slug: " + request.packageSlug()));
                    }
                    MarketplaceListing listing = MarketplaceListing.builder()
                            .tenantId(tenantId)
                            .packageId(request.packageId())
                            .packageName(request.packageName())
                            .packageSlug(request.packageSlug())
                            .packageType(request.packageType())
                            .description(request.description())
                            .version(request.version())
                            .authorName(request.authorName())
                            .tags(request.tags())
                            .category(request.category())
                            .visibility("PUBLIC")
                            .status("ACTIVE")
                            .downloadCount(0L)
                            .avgRating(BigDecimal.ZERO)
                            .reviewCount(0)
                            .publishedAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return listingRepository.save(listing);
                })
                .map(ListingResponse::from);
    }

    /**
     * Returns all active listings in the global catalog (any tenant can browse).
     *
     * @return flux of active listings
     */
    public Flux<ListingResponse> findAllListings() {
        return listingRepository.findByStatus("ACTIVE")
                .map(ListingResponse::from);
    }

    /**
     * Returns active listings filtered by package type.
     *
     * @param type package type (AGENT, SKILL, TOOL, etc.)
     * @return matching listings
     */
    public Flux<ListingResponse> findByType(String type) {
        return listingRepository.findActiveByPackageType(type)
                .map(ListingResponse::from);
    }

    /**
     * Returns active listings filtered by category.
     *
     * @param category category name
     * @return matching listings
     */
    public Flux<ListingResponse> findByCategory(String category) {
        return listingRepository.findActiveByCategory(category)
                .map(ListingResponse::from);
    }

    /**
     * Returns a listing by its package slug.
     *
     * @param slug package slug
     * @return matching listing or empty
     */
    public Mono<ListingResponse> findBySlug(String slug) {
        return listingRepository.findByPackageSlug(slug)
                .map(ListingResponse::from);
    }

    /**
     * Returns all listings published by a specific tenant.
     *
     * @param tenantId publisher tenant ID
     * @return tenant's published listings
     */
    public Flux<ListingResponse> findByTenant(UUID tenantId) {
        return listingRepository.findByTenantId(tenantId)
                .map(ListingResponse::from);
    }

    /**
     * Updates mutable fields of a listing owned by the given tenant.
     *
     * @param id       listing ID
     * @param tenantId publisher tenant (ownership check)
     * @param request  update request
     * @return updated listing
     */
    public Mono<ListingResponse> updateListing(UUID id, UUID tenantId, UpdateListingRequest request) {
        return listingRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + id)))
                .flatMap(listing -> {
                    if (!tenantId.equals(listing.getTenantId())) {
                        return Mono.error(new IllegalArgumentException("Listing does not belong to this tenant"));
                    }
                    if (request.description() != null) listing.setDescription(request.description());
                    if (request.authorName() != null) listing.setAuthorName(request.authorName());
                    if (request.tags() != null) listing.setTags(request.tags());
                    if (request.category() != null) listing.setCategory(request.category());
                    if (request.status() != null) listing.setStatus(request.status());
                    listing.setUpdatedAt(OffsetDateTime.now());
                    return listingRepository.save(listing);
                })
                .map(ListingResponse::from);
    }

    /**
     * Marks a listing as REMOVED (soft delete). Only the publisher tenant can remove it.
     *
     * @param id       listing ID
     * @param tenantId publisher tenant (ownership check)
     * @return empty on completion
     */
    public Mono<Void> removeListing(UUID id, UUID tenantId) {
        return listingRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + id)))
                .flatMap(listing -> {
                    if (!tenantId.equals(listing.getTenantId())) {
                        return Mono.error(new IllegalArgumentException("Listing does not belong to this tenant"));
                    }
                    listing.setStatus("REMOVED");
                    listing.setUpdatedAt(OffsetDateTime.now());
                    return listingRepository.save(listing);
                })
                .then();
    }

    // ── Tenant Installations ──────────────────────────────────────────────────

    /**
     * Installs a marketplace listing into the current tenant's schema.
     * Each listing can only be installed once per tenant.
     *
     * @param request install request with listing ID and version
     * @return installation record
     */
    public Mono<InstallationResponse> installListing(InstallListingRequest request) {
        return installationRepository.existsByListingId(request.listingId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                                "Listing already installed: " + request.listingId()));
                    }
                    return listingRepository.findById(request.listingId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "Listing not found: " + request.listingId())))
                            .flatMap(listing -> {
                                MarketplaceInstallation installation = MarketplaceInstallation.builder()
                                        .listingId(request.listingId())
                                        .packageSlug(listing.getPackageSlug())
                                        .packageType(listing.getPackageType())
                                        .installedVersion(request.installedVersion())
                                        .status("ACTIVE")
                                        .installedAt(OffsetDateTime.now())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();
                                return installationRepository.save(installation)
                                        .flatMap(saved -> incrementDownload(listing).thenReturn(saved));
                            });
                })
                .map(InstallationResponse::from);
    }

    /**
     * Returns all active installations for the current tenant.
     *
     * @return flux of active installations
     */
    public Flux<InstallationResponse> findInstallations() {
        return installationRepository.findByStatus("ACTIVE")
                .map(InstallationResponse::from);
    }

    /**
     * Removes an installed package from the current tenant (soft delete).
     *
     * @param installationId installation ID
     * @return empty on completion
     */
    public Mono<Void> uninstallListing(UUID installationId) {
        return installationRepository.findById(installationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Installation not found: " + installationId)))
                .flatMap(installation -> {
                    installation.setStatus("REMOVED");
                    installation.setUpdatedAt(OffsetDateTime.now());
                    return installationRepository.save(installation);
                })
                .then();
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    /**
     * Submits a review for a listing. Each tenant+user pair can only review once.
     *
     * @param listingId  listing ID
     * @param tenantId   reviewer tenant ID
     * @param reviewerId reviewer user ID
     * @param request    review request
     * @return created review
     */
    public Mono<ReviewResponse> submitReview(UUID listingId, UUID tenantId, UUID reviewerId, SubmitReviewRequest request) {
        return reviewRepository.findByListingIdAndTenantIdAndReviewerId(listingId, tenantId, reviewerId)
                .flatMap(existing -> Mono.<MarketplaceReview>error(
                        new IllegalArgumentException("Reviewer has already submitted a review for this listing")))
                .switchIfEmpty(Mono.defer(() -> {
                    MarketplaceReview review = MarketplaceReview.builder()
                            .listingId(listingId)
                            .tenantId(tenantId)
                            .reviewerId(reviewerId)
                            .rating(request.rating())
                            .title(request.title())
                            .body(request.body())
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return reviewRepository.save(review)
                            .flatMap(saved -> updateAverageRating(listingId).thenReturn(saved));
                }))
                .map(ReviewResponse::from);
    }

    /**
     * Returns all reviews for a listing.
     *
     * @param listingId listing ID
     * @return flux of reviews
     */
    public Flux<ReviewResponse> getReviews(UUID listingId) {
        return reviewRepository.findByListingId(listingId)
                .map(ReviewResponse::from);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Mono<Void> incrementDownload(MarketplaceListing listing) {
        listing.setDownloadCount(listing.getDownloadCount() + 1);
        listing.setUpdatedAt(OffsetDateTime.now());
        return listingRepository.save(listing).then();
    }

    private Mono<Void> updateAverageRating(UUID listingId) {
        return reviewRepository.findByListingId(listingId)
                .collectList()
                .flatMap(reviews -> listingRepository.findById(listingId)
                        .flatMap(listing -> {
                            if (reviews.isEmpty()) {
                                listing.setAvgRating(BigDecimal.ZERO);
                                listing.setReviewCount(0);
                            } else {
                                double avg = reviews.stream()
                                        .mapToInt(r -> r.getRating())
                                        .average()
                                        .orElse(0.0);
                                listing.setAvgRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
                                listing.setReviewCount(reviews.size());
                            }
                            listing.setUpdatedAt(OffsetDateTime.now());
                            return listingRepository.save(listing);
                        }))
                .then();
    }
}
