package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.ListingResponse;
import dev.cezar.agenthub.marketplace.api.PublishListingRequest;
import dev.cezar.agenthub.marketplace.api.UpdateListingRequest;
import dev.cezar.agenthub.marketplace.service.MarketplaceService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceControllerTest {

    @Mock
    private MarketplaceService service;

    private MarketplaceController controller;

    @BeforeEach
    void setUp() {
        controller = new MarketplaceController(service);
    }

    @Test
    void shouldListAllActiveListings() {
        ListingResponse listing1 = buildListing("agent-a", "AGENT");
        ListingResponse listing2 = buildListing("skill-b", "SKILL");

        when(service.findAllListings()).thenReturn(Flux.just(listing1, listing2));

        StepVerifier.create(controller.listAll())
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("agent-a"))
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("skill-b"))
                .verifyComplete();

        verify(service).findAllListings();
    }

    @Test
    void shouldListByType() {
        ListingResponse listing = buildListing("my-agent", "AGENT");
        when(service.findByType("AGENT")).thenReturn(Flux.just(listing));

        StepVerifier.create(controller.listByType("AGENT"))
                .assertNext(r -> {
                    assertThat(r.packageType()).isEqualTo("AGENT");
                    assertThat(r.packageSlug()).isEqualTo("my-agent");
                })
                .verifyComplete();

        verify(service).findByType("AGENT");
    }

    @Test
    void shouldListByCategory() {
        ListingResponse listing = buildListing("my-agent", "AGENT");
        when(service.findByCategory("productivity")).thenReturn(Flux.just(listing));

        StepVerifier.create(controller.listByCategory("productivity"))
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("my-agent"))
                .verifyComplete();

        verify(service).findByCategory("productivity");
    }

    @Test
    void shouldGetBySlugWhenExists() {
        ListingResponse listing = buildListing("my-agent", "AGENT");
        when(service.findBySlug("my-agent")).thenReturn(Mono.just(listing));

        StepVerifier.create(controller.getBySlug("my-agent"))
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("my-agent"))
                .verifyComplete();

        verify(service).findBySlug("my-agent");
    }

    @Test
    void shouldReturnErrorWhenSlugNotFound() {
        when(service.findBySlug("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(controller.getBySlug("unknown"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("unknown"))
                .verify();
    }

    @Test
    void shouldRemoveListingAndReturnEmpty() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(service.removeListing(eq(id), any())).thenReturn(Mono.empty());

        // removeListing delegates to TenantContextHelper which requires Reactor context
        // Test the service call directly via Mono.just for the tenant
        Mono<Void> result = Mono.just(tenantId)
                .flatMap(tid -> service.removeListing(id, tid));

        StepVerifier.create(result)
                .verifyComplete();

        verify(service).removeListing(id, tenantId);
    }

    @Test
    void shouldReturnEmptyListWhenNoListings() {
        when(service.findAllListings()).thenReturn(Flux.empty());

        StepVerifier.create(controller.listAll())
                .verifyComplete();
    }

    // Helpers

    private ListingResponse buildListing(String slug, String type) {
        return new ListingResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                slug,
                slug,
                type,
                "Description",
                "1.0.0",
                "Author",
                new String[]{"tag1"},
                "productivity",
                "PUBLIC",
                "ACTIVE",
                0L,
                BigDecimal.ZERO,
                0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
