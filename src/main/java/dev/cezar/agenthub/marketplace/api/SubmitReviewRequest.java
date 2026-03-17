package dev.cezar.agenthub.marketplace.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request to submit a rating and review for a marketplace listing.
 *
 * @since 1.0.0
 */
public record SubmitReviewRequest(
        @NotNull @Min(1) @Max(5) Short rating,
        String title,
        String body
) {}
