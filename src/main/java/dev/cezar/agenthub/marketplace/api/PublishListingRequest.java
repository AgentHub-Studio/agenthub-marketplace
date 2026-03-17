package dev.cezar.agenthub.marketplace.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to publish a package from the registry into the marketplace.
 *
 * @since 1.0.0
 */
public record PublishListingRequest(
        @NotNull UUID packageId,
        @NotBlank String packageName,
        @NotBlank String packageSlug,
        @NotBlank String packageType,
        String description,
        @NotBlank String version,
        String authorName,
        String[] tags,
        String category
) {}
