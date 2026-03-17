package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.ReviewResponse;
import dev.cezar.agenthub.marketplace.api.SubmitReviewRequest;
import dev.cezar.agenthub.marketplace.multitenant.TenantContextHelper;
import dev.cezar.agenthub.marketplace.service.MarketplaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for marketplace review operations.
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/marketplace/listings/{listingId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Marketplace Reviews", description = "Package ratings and reviews")
public class ReviewController {

    private final MarketplaceService service;

    @GetMapping
    @Operation(summary = "List all reviews for a listing")
    public Flux<ReviewResponse> getReviews(@PathVariable UUID listingId) {
        return service.getReviews(listingId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a review for a listing")
    public Mono<ReviewResponse> submitReview(
            @PathVariable UUID listingId,
            @Valid @RequestBody SubmitReviewRequest request) {
        return TenantContextHelper.getUserId()
                .flatMap(reviewerId -> service.submitReview(listingId, reviewerId, request));
    }
}
