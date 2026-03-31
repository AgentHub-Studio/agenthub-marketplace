package dev.cezar.agenthub.marketplace.controller;

import dev.cezar.agenthub.marketplace.api.InstallListingRequest;
import dev.cezar.agenthub.marketplace.api.InstallationResponse;
import dev.cezar.agenthub.marketplace.service.MarketplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallationControllerTest {

    @Mock
    private MarketplaceService service;

    private InstallationController controller;

    @BeforeEach
    void setUp() {
        controller = new InstallationController(service);
    }

    @Test
    void shouldListActiveInstallations() {
        InstallationResponse inst1 = buildInstallation("my-agent", "1.0.0");
        InstallationResponse inst2 = buildInstallation("my-skill", "2.0.0");

        when(service.findInstallations()).thenReturn(Flux.just(inst1, inst2));

        StepVerifier.create(controller.listInstallations())
                .assertNext(r -> {
                    assertThat(r.packageSlug()).isEqualTo("my-agent");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .assertNext(r -> assertThat(r.packageSlug()).isEqualTo("my-skill"))
                .verifyComplete();

        verify(service).findInstallations();
    }

    @Test
    void shouldReturnEmptyWhenNoInstallations() {
        when(service.findInstallations()).thenReturn(Flux.empty());

        StepVerifier.create(controller.listInstallations())
                .verifyComplete();

        verify(service).findInstallations();
    }

    @Test
    void shouldInstallListingAndReturnCreatedInstallation() {
        UUID listingId = UUID.randomUUID();
        InstallListingRequest request = new InstallListingRequest(listingId, "1.0.0");
        InstallationResponse response = buildInstallation("some-pkg", "1.0.0");
        response = new InstallationResponse(
                response.id(), listingId, "some-pkg", "AGENT", "1.0.0", "ACTIVE",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(service.installListing(any(InstallListingRequest.class))).thenReturn(Mono.just(response));

        StepVerifier.create(controller.install(request))
                .assertNext(r -> {
                    assertThat(r.listingId()).isEqualTo(listingId);
                    assertThat(r.installedVersion()).isEqualTo("1.0.0");
                    assertThat(r.status()).isEqualTo("ACTIVE");
                })
                .verifyComplete();

        verify(service).installListing(request);
    }

    @Test
    void shouldUninstallListingSuccessfully() {
        UUID installationId = UUID.randomUUID();
        when(service.uninstallListing(installationId)).thenReturn(Mono.empty());

        StepVerifier.create(controller.uninstall(installationId))
                .verifyComplete();

        verify(service).uninstallListing(installationId);
    }

    @Test
    void shouldPropagateErrorWhenInstallFails() {
        UUID listingId = UUID.randomUUID();
        InstallListingRequest request = new InstallListingRequest(listingId, "1.0.0");

        when(service.installListing(request))
                .thenReturn(Mono.error(new IllegalArgumentException("Listing already installed: " + listingId)));

        StepVerifier.create(controller.install(request))
                .expectErrorMatches(e -> e.getMessage().contains("already installed"))
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenUninstallFails() {
        UUID installationId = UUID.randomUUID();
        when(service.uninstallListing(installationId))
                .thenReturn(Mono.error(new IllegalArgumentException("Installation not found: " + installationId)));

        StepVerifier.create(controller.uninstall(installationId))
                .expectErrorMatches(e -> e.getMessage().contains("Installation not found"))
                .verify();
    }

    // Helpers

    private InstallationResponse buildInstallation(String slug, String version) {
        return new InstallationResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                slug,
                "AGENT",
                version,
                "ACTIVE",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }
}
