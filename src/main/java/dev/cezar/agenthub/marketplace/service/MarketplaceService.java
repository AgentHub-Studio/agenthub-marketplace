package dev.cezar.agenthub.marketplace.service;

import dev.cezar.agenthub.marketplace.api.*;
import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
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
 * Business logic for marketplace listings and reviews.
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceService {

    private final MarketplaceListingRepository listingRepository;
    private final MarketplaceReviewRepository reviewRepository;

    // ── Listings ──────────────────────────────────────────────────────────────

    /**
     * Publishes a package to the marketplace.
     *
     * @param request publish request with package details
     * @return created listing
     */
    public Mono<ListingResponse> publishListing(PublishListingRequest request) {
        MarketplaceListing listing = MarketplaceListing.builder()
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

        return listingRepository.save(listing)
                .map(ListingResponse::from);
    }

    /**
     * Returns all active marketplace listings.
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
     * @param type package type (e.g. AGENT, SKILL, TOOL)
     * @return flux of matching listings
     */
    public Flux<ListingResponse> findByType(String type) {
        return listingRepository.findByPackageType(type)
                .map(ListingResponse::from);
    }

    /**
     * Returns active listings filtered by category.
     *
     * @param category category name
     * @return flux of matching listings
     */
    public Flux<ListingResponse> findByCategory(String category) {
        return listingRepository.findByCategory(category)
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
     * Updates a listing's mutable fields.
     *
     * @param id      listing ID
     * @param request update request
     * @return updated listing
     */
    public Mono<ListingResponse> updateListing(UUID id, UpdateListingRequest request) {
        return listingRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + id)))
                .flatMap(listing -> {
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
     * Marks a listing as REMOVED (soft delete).
     *
     * @param id listing ID
     * @return empty on completion
     */
    public Mono<Void> removeListing(UUID id) {
        return listingRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + id)))
                .flatMap(listing -> {
                    listing.setStatus("REMOVED");
                    listing.setUpdatedAt(OffsetDateTime.now());
                    return listingRepository.save(listing);
                })
                .then();
    }

    /**
     * Increments the download counter for a listing.
     *
     * @param id listing ID
     * @return empty on completion
     */
    public Mono<Void> incrementDownload(UUID id) {
        return listingRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + id)))
                .flatMap(listing -> {
                    listing.setDownloadCount(listing.getDownloadCount() + 1);
                    listing.setUpdatedAt(OffsetDateTime.now());
                    return listingRepository.save(listing);
                })
                .then();
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    /**
     * Submits a review for a listing. Each reviewer can only review a listing once.
     *
     * @param listingId  listing ID
     * @param reviewerId reviewer user ID
     * @param request    review request
     * @return created review
     */
    public Mono<ReviewResponse> submitReview(UUID listingId, UUID reviewerId, SubmitReviewRequest request) {
        return reviewRepository.findByListingIdAndReviewerId(listingId, reviewerId)
                .flatMap(existing -> Mono.<MarketplaceReview>error(
                        new IllegalArgumentException("Reviewer has already submitted a review for this listing")))
                .switchIfEmpty(Mono.defer(() -> {
                    MarketplaceReview review = MarketplaceReview.builder()
                            .listingId(listingId)
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

    /**
     * Recalculates and persists the average rating for a listing
     * after a new review is submitted.
     *
     * @param listingId listing ID
     * @return empty on completion
     */
    public Mono<Void> updateAverageRating(UUID listingId) {
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
