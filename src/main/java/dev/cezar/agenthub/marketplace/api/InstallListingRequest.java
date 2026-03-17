package dev.cezar.agenthub.marketplace.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to install a marketplace listing into the current tenant.
 *
 * @since 1.0.0
 */
public record InstallListingRequest(
        @NotNull UUID listingId,
        @NotBlank String installedVersion
) {}
