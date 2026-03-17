package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.ListingResponse;
import dev.cezar.agenthub.marketplace.api.PublishListingRequest;
import dev.cezar.agenthub.marketplace.api.UpdateListingRequest;
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
 * REST controller for marketplace listing operations.
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/marketplace/listings")
@RequiredArgsConstructor
@Tag(name = "Marketplace Listings", description = "Package discovery and publishing")
public class MarketplaceController {

    private final MarketplaceService service;

    @GetMapping
    @Operation(summary = "List all active marketplace listings")
    public Flux<ListingResponse> listAll() {
        return service.findAllListings();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "List listings by package type")
    public Flux<ListingResponse> listByType(@PathVariable String type) {
        return service.findByType(type);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "List listings by category")
    public Flux<ListingResponse> listByCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get listing by package slug")
    public Mono<ListingResponse> getBySlug(@PathVariable String slug) {
        return service.findBySlug(slug)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + slug)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Publish a package to the marketplace")
    public Mono<ListingResponse> publish(@Valid @RequestBody PublishListingRequest request) {
        return service.publishListing(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a marketplace listing")
    public Mono<ListingResponse> update(@PathVariable UUID id, @RequestBody UpdateListingRequest request) {
        return service.updateListing(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a listing from the marketplace")
    public Mono<Void> remove(@PathVariable UUID id) {
        return service.removeListing(id);
    }

    @PostMapping("/{id}/install")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Record a package install (increments download count)")
    public Mono<Void> install(@PathVariable UUID id) {
        return service.incrementDownload(id);
    }
}
