package dev.cezar.agenthub.marketplace.service;

import dev.cezar.agenthub.marketplace.api.InstallListingRequest;
import dev.cezar.agenthub.marketplace.api.PublishListingRequest;
import dev.cezar.agenthub.marketplace.api.SubmitReviewRequest;
import dev.cezar.agenthub.marketplace.domain.MarketplaceInstallation;
import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
import dev.cezar.agenthub.marketplace.repository.MarketplaceInstallationRepository;
import dev.cezar.agenthub.marketplace.repository.MarketplaceListingRepository;
import dev.cezar.agenthub.marketplace.repository.MarketplaceReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock private MarketplaceListingRepository listingRepository;
    @Mock private MarketplaceReviewRepository reviewRepository;
    @Mock private MarketplaceInstallationRepository installationRepository;

    private MarketplaceService service;

    @BeforeEach
    void setUp() {
        service = new MarketplaceService(listingRepository, reviewRepository, installationRepository);
    }

    @Test
    void shouldPublishListingToGlobalCatalog() {
        UUID tenantId = UUID.randomUUID();
        PublishListingRequest request = new PublishListingRequest(
                UUID.randomUUID(), "My Agent", "my-agent", "AGENT",
                "A useful agent", "1.0.0", "dev.cezar", null, "productivity"
        );

        when(listingRepository.existsByTenantIdAndPackageSlug(tenantId, "my-agent"))
                .thenReturn(Mono.just(false));
        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.publishListing(tenantId, request))
                .assertNext(r -> {
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.packageSlug()).isEqualTo("my-agent");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                    assertThat(r.visibility()).isEqualTo("PUBLIC");
                    assertThat(r.downloadCount()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicatePublishForSameTenantAndSlug() {
        UUID tenantId = UUID.randomUUID();
        PublishListingRequest request = new PublishListingRequest(
                UUID.randomUUID(), "My Agent", "my-agent", "AGENT",
                null, "1.0.0", null, null, null
        );

        when(listingRepository.existsByTenantIdAndPackageSlug(tenantId, "my-agent"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.publishListing(tenantId, request))
                .expectErrorMatches(e -> e.getMessage().contains("my-agent"))
                .verify();
    }

    @Test
    void shouldFindAllActiveListings() {
        MarketplaceListing listing = buildListing("my-agent");
        when(listingRepository.findByStatus("ACTIVE")).thenReturn(Flux.just(listing));

        StepVerifier.create(service.findAllListings())
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("my-agent"))
                .verifyComplete();
    }

    @Test
    void shouldInstallListingIntoTenantSchema() {
        UUID listingId = UUID.randomUUID();
        MarketplaceListing listing = buildListing("my-agent");
        listing.setId(listingId);

        InstallListingRequest request = new InstallListingRequest(listingId, "1.0.0");

        when(installationRepository.existsByListingId(listingId)).thenReturn(Mono.just(false));
        when(listingRepository.findById(listingId)).thenReturn(Mono.just(listing));
        when(installationRepository.save(any(MarketplaceInstallation.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.installListing(request))
                .assertNext(r -> {
                    assertThat(r.listingId()).isEqualTo(listingId);
                    assertThat(r.installedVersion()).isEqualTo("1.0.0");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                    assertThat(r.packageSlug()).isEqualTo("my-agent");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectInstallWhenAlreadyInstalled() {
        UUID listingId = UUID.randomUUID();
        InstallListingRequest request = new InstallListingRequest(listingId, "1.0.0");

        when(installationRepository.existsByListingId(listingId)).thenReturn(Mono.just(true));

        StepVerifier.create(service.installListing(request))
                .expectErrorMatches(e -> e.getMessage().contains("already installed"))
                .verify();
    }

    @Test
    void shouldSubmitReviewWithTenantAndUserId() {
        UUID listingId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        SubmitReviewRequest request = new SubmitReviewRequest((short) 5, "Excellent!", "Works perfectly.");

        MarketplaceListing listing = buildListing("my-agent");
        listing.setId(listingId);

        when(reviewRepository.findByListingIdAndTenantIdAndReviewerId(listingId, tenantId, reviewerId))
                .thenReturn(Mono.empty());
        when(reviewRepository.save(any(MarketplaceReview.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(reviewRepository.findByListingId(listingId)).thenReturn(Flux.empty());
        when(listingRepository.findById(listingId)).thenReturn(Mono.just(listing));
        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.submitReview(listingId, tenantId, reviewerId, request))
                .assertNext(r -> {
                    assertThat(r.rating()).isEqualTo((short) 5);
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.reviewerId()).isEqualTo(reviewerId);
                    assertThat(r.listingId()).isEqualTo(listingId);
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateReviewFromSameTenantAndUser() {
        UUID listingId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        SubmitReviewRequest request = new SubmitReviewRequest((short) 3, "OK", null);

        MarketplaceReview existing = MarketplaceReview.builder()
                .id(UUID.randomUUID())
                .listingId(listingId)
                .tenantId(tenantId)
                .reviewerId(reviewerId)
                .rating((short) 3)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(reviewRepository.findByListingIdAndTenantIdAndReviewerId(listingId, tenantId, reviewerId))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(service.submitReview(listingId, tenantId, reviewerId, request))
                .expectErrorMatches(e -> e.getMessage().contains("already submitted"))
                .verify();
    }

    // Helpers

    private MarketplaceListing buildListing(String slug) {
        return MarketplaceListing.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .packageId(UUID.randomUUID())
                .packageName(slug)
                .packageSlug(slug)
                .packageType("AGENT")
                .version("1.0.0")
                .visibility("PUBLIC")
                .status("ACTIVE")
                .downloadCount(0L)
                .avgRating(BigDecimal.ZERO)
                .reviewCount(0)
                .publishedAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
