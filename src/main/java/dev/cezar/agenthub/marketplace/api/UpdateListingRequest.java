package dev.cezar.agenthub.marketplace.api;

/**
 * Request to update an existing marketplace listing.
 *
 * @since 1.0.0
 */
public record UpdateListingRequest(
        String description,
        String authorName,
        String[] tags,
        String category,
        String status
) {}
