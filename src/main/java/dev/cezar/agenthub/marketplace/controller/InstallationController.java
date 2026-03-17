package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.InstallListingRequest;
import dev.cezar.agenthub.marketplace.api.InstallationResponse;
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
 * REST controller for per-tenant marketplace installations.
 * All endpoints require authentication — operations are scoped to the current tenant.
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/marketplace/installations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Marketplace Installations", description = "Per-tenant package installations")
public class InstallationController {

    private final MarketplaceService service;

    @GetMapping
    @Operation(summary = "List all active installations for the current tenant")
    public Flux<InstallationResponse> listInstallations() {
        return service.findInstallations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Install a marketplace listing into the current tenant")
    public Mono<InstallationResponse> install(@Valid @RequestBody InstallListingRequest request) {
        return service.installListing(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Uninstall a package from the current tenant")
    public Mono<Void> uninstall(@PathVariable UUID id) {
        return service.uninstallListing(id);
    }
}
