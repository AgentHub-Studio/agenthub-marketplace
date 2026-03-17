package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.*;
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
 * REST controller for the global marketplace catalog.
 * GET endpoints are public; mutations require authentication.
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/marketplace/listings")
@RequiredArgsConstructor
@Tag(name = "Marketplace Catalog", description = "Global package catalog — discovery and publishing")
public class MarketplaceController {

    private final MarketplaceService service;

    // ── Browse (public) ───────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all active listings in the global catalog")
    public Flux<ListingResponse> listAll() {
        return service.findAllListings();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "List active listings by package type (AGENT, SKILL, TOOL...)")
    public Flux<ListingResponse> listByType(@PathVariable String type) {
        return service.findByType(type);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "List active listings by category")
    public Flux<ListingResponse> listByCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a listing by package slug")
    public Mono<ListingResponse> getBySlug(@PathVariable String slug) {
        return service.findBySlug(slug)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Listing not found: " + slug)));
    }

    // ── Publish / Manage (authenticated — publisher tenant only) ──────────────

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List listings published by the current tenant")
    public Flux<ListingResponse> listMine() {
        return TenantContextHelper.getTenantId()
                .flatMapMany(service::findByTenant);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Publish a package to the global marketplace")
    public Mono<ListingResponse> publish(@Valid @RequestBody PublishListingRequest request) {
        return TenantContextHelper.getTenantId()
                .flatMap(tenantId -> service.publishListing(tenantId, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a listing (publisher tenant only)")
    public Mono<ListingResponse> update(@PathVariable UUID id, @RequestBody UpdateListingRequest request) {
        return TenantContextHelper.getTenantId()
                .flatMap(tenantId -> service.updateListing(id, tenantId, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a listing from the marketplace (publisher tenant only)")
    public Mono<Void> remove(@PathVariable UUID id) {
        return TenantContextHelper.getTenantId()
                .flatMap(tenantId -> service.removeListing(id, tenantId));
    }
}
