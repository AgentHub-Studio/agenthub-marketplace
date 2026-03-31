package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.ReviewResponse;
import dev.cezar.agenthub.marketplace.service.MarketplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private MarketplaceService service;

    private ReviewController controller;

    @BeforeEach
    void setUp() {
        controller = new ReviewController(service);
    }

    @Test
    void shouldReturnReviewsForListing() {
        UUID listingId = UUID.randomUUID();
        ReviewResponse review1 = buildReview(listingId, (short) 5, "Excellent");
        ReviewResponse review2 = buildReview(listingId, (short) 4, "Good");

        when(service.getReviews(listingId)).thenReturn(Flux.just(review1, review2));

        StepVerifier.create(controller.getReviews(listingId))
                .assertNext(r -> {
                    assertThat(r.listingId()).isEqualTo(listingId);
                    assertThat(r.rating()).isEqualTo((short) 5);
                })
                .assertNext(r -> assertThat(r.rating()).isEqualTo((short) 4))
                .verifyComplete();

        verify(service).getReviews(listingId);
    }

    @Test
    void shouldReturnEmptyFluxWhenNoReviews() {
        UUID listingId = UUID.randomUUID();
        when(service.getReviews(listingId)).thenReturn(Flux.empty());

        StepVerifier.create(controller.getReviews(listingId))
                .verifyComplete();

        verify(service).getReviews(listingId);
    }

    @Test
    void shouldIncludeAllFieldsInReviewResponse() {
        UUID listingId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();
        ReviewResponse review = new ReviewResponse(
                UUID.randomUUID(), listingId, tenantId, reviewerId,
                (short) 3, "Average", "Body text", OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(service.getReviews(listingId)).thenReturn(Flux.just(review));

        StepVerifier.create(controller.getReviews(listingId))
                .assertNext(r -> {
                    assertThat(r.listingId()).isEqualTo(listingId);
                    assertThat(r.tenantId()).isEqualTo(tenantId);
                    assertThat(r.reviewerId()).isEqualTo(reviewerId);
                    assertThat(r.title()).isEqualTo("Average");
                    assertThat(r.body()).isEqualTo("Body text");
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateServiceErrorOnGetReviews() {
        UUID listingId = UUID.randomUUID();
        when(service.getReviews(listingId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(controller.getReviews(listingId))
                .expectErrorMessage("DB error")
                .verify();
    }

    // Helpers

    private ReviewResponse buildReview(UUID listingId, short rating, String title) {
        return new ReviewResponse(
                UUID.randomUUID(),
                listingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                rating,
                title,
                "Review body",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
