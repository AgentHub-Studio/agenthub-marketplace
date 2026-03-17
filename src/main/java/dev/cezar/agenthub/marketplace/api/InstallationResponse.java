package dev.cezar.agenthub.marketplace.api;

import dev.cezar.agenthub.marketplace.domain.MarketplaceInstallation;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for a marketplace installation record.
 *
 * @since 1.0.0
 */
public record InstallationResponse(
        UUID id,
        UUID listingId,
        String packageSlug,
        String packageType,
        String installedVersion,
        String status,
        OffsetDateTime installedAt,
        OffsetDateTime updatedAt
) {

    /**
     * Converts a {@link MarketplaceInstallation} entity to a response DTO.
     *
     * @param installation the entity
     * @return response DTO
     */
    public static InstallationResponse from(MarketplaceInstallation installation) {
        return new InstallationResponse(
                installation.getId(),
                installation.getListingId(),
                installation.getPackageSlug(),
                installation.getPackageType(),
                installation.getInstalledVersion(),
                installation.getStatus(),
                installation.getInstalledAt(),
                installation.getUpdatedAt()
        );
    }
}
