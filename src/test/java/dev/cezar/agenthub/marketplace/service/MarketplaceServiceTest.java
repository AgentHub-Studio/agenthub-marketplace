package dev.cezar.agenthub.marketplace.service;

import dev.cezar.agenthub.marketplace.api.PublishListingRequest;
import dev.cezar.agenthub.marketplace.api.SubmitReviewRequest;
import dev.cezar.agenthub.marketplace.domain.MarketplaceListing;
import dev.cezar.agenthub.marketplace.domain.MarketplaceReview;
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

    private MarketplaceService service;

    @BeforeEach
    void setUp() {
        service = new MarketplaceService(listingRepository, reviewRepository);
    }

    @Test
    void shouldPublishListingSuccessfully() {
        PublishListingRequest request = new PublishListingRequest(
                UUID.randomUUID(), "My Agent", "my-agent", "AGENT",
                "A useful agent", "1.0.0", "dev.cezar", null, "productivity"
        );

        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.publishListing(request))
                .assertNext(r -> {
                    assertThat(r.packageName()).isEqualTo("My Agent");
                    assertThat(r.packageSlug()).isEqualTo("my-agent");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                    assertThat(r.visibility()).isEqualTo("PUBLIC");
                    assertThat(r.downloadCount()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllActiveListings() {
        MarketplaceListing listing = buildListing("my-agent", "ACTIVE");
        when(listingRepository.findByStatus("ACTIVE")).thenReturn(Flux.just(listing));

        StepVerifier.create(service.findAllListings())
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("my-agent"))
                .verifyComplete();
    }

    @Test
    void shouldFindListingsByType() {
        MarketplaceListing listing = buildListing("my-skill", "ACTIVE");
        listing.setPackageType("SKILL");
        when(listingRepository.findByPackageType("SKILL")).thenReturn(Flux.just(listing));

        StepVerifier.create(service.findByType("SKILL"))
                .assertNext(r -> assertThat(r.packageType()).isEqualTo("SKILL"))
                .verifyComplete();
    }

    @Test
    void shouldSubmitReviewSuccessfully() {
        UUID listingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        SubmitReviewRequest request = new SubmitReviewRequest((short) 5, "Great!", "Works perfectly.");

        MarketplaceListing listing = buildListing("my-agent", "ACTIVE");
        listing.setId(listingId);

        when(reviewRepository.findByListingIdAndReviewerId(listingId, reviewerId))
                .thenReturn(Mono.empty());
        when(reviewRepository.save(any(MarketplaceReview.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(reviewRepository.findByListingId(listingId)).thenReturn(Flux.empty());
        when(listingRepository.findById(listingId)).thenReturn(Mono.just(listing));
        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.submitReview(listingId, reviewerId, request))
                .assertNext(r -> {
                    assertThat(r.rating()).isEqualTo((short) 5);
                    assertThat(r.title()).isEqualTo("Great!");
                    assertThat(r.listingId()).isEqualTo(listingId);
                    assertThat(r.reviewerId()).isEqualTo(reviewerId);
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateReview() {
        UUID listingId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        SubmitReviewRequest request = new SubmitReviewRequest((short) 4, "Good", null);

        MarketplaceReview existing = MarketplaceReview.builder()
                .id(UUID.randomUUID())
                .listingId(listingId)
                .reviewerId(reviewerId)
                .rating((short) 4)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(reviewRepository.findByListingIdAndReviewerId(listingId, reviewerId))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(service.submitReview(listingId, reviewerId, request))
                .expectErrorMatches(e -> e.getMessage().contains("already submitted"))
                .verify();
    }

    @Test
    void shouldIncrementDownloadCount() {
        UUID id = UUID.randomUUID();
        MarketplaceListing listing = buildListing("my-agent", "ACTIVE");
        listing.setId(id);
        listing.setDownloadCount(10L);

        when(listingRepository.findById(id)).thenReturn(Mono.just(listing));
        when(listingRepository.save(any(MarketplaceListing.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.incrementDownload(id))
                .verifyComplete();

        assertThat(listing.getDownloadCount()).isEqualTo(11L);
    }

    // Helpers

    private MarketplaceListing buildListing(String slug, String status) {
        return MarketplaceListing.builder()
                .id(UUID.randomUUID())
                .packageId(UUID.randomUUID())
                .packageName(slug)
                .packageSlug(slug)
                .packageType("AGENT")
                .version("1.0.0")
                .visibility("PUBLIC")
                .status(status)
                .downloadCount(0L)
                .avgRating(BigDecimal.ZERO)
                .reviewCount(0)
                .publishedAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }
}
