package dev.cezar.agenthub.marketplace.multitenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiTenantFilterTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private HttpHeaders headers;

    @Mock
    private WebFilterChain chain;

    private static String buildTestJwt(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".fakesig";
    }

    @Test
    void shouldPropagateTenantIdInReactorContextWhenTokenPresent() {
        String tenantId = "my-tenant-slug";
        String jwt = buildTestJwt(
                "{\"iss\":\"http://keycloak/realms/" + tenantId + "\",\"sub\":\"user-123\"}");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn("Bearer " + jwt);

        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    String extracted = ctx.getOrDefault("tenantId", null);
                    if (!tenantId.equals(extracted)) {
                        return Mono.error(new AssertionError(
                                "Expected tenantId=" + tenantId + " but got " + extracted));
                    }
                    return Mono.empty();
                })
        );

        MultiTenantFilter filter = new MultiTenantFilter();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldUseDefaultSchemaWhenNoAuthorizationHeader() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn(null);

        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    String schema = ctx.getOrDefault("schema", null);
                    if (!MultiTenant.DEFAULT_SCHEMA.equals(schema)) {
                        return Mono.error(new AssertionError(
                                "Expected schema=" + MultiTenant.DEFAULT_SCHEMA + " but got " + schema));
                    }
                    return Mono.empty();
                })
        );

        MultiTenantFilter filter = new MultiTenantFilter();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldPropagateUserIdInReactorContextWhenTokenPresent() {
        String tenantId = "my-tenant-slug";
        String userId = "sub-user-id-123";
        String jwt = buildTestJwt(
                "{\"iss\":\"http://keycloak/realms/" + tenantId + "\",\"sub\":\"" + userId + "\"}");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn("Bearer " + jwt);

        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    String extractedUserId = ctx.getOrDefault("userId", null);
                    if (!userId.equals(extractedUserId)) {
                        return Mono.error(new AssertionError(
                                "Expected userId=" + userId + " but got " + extractedUserId));
                    }
                    return Mono.empty();
                })
        );

        MultiTenantFilter filter = new MultiTenantFilter();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldSetCorrectSchemaNameFromTenantId() {
        String tenantId = "acme-corp";
        String jwt = buildTestJwt(
                "{\"iss\":\"http://keycloak/realms/" + tenantId + "\",\"sub\":\"user-1\"}");

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn("Bearer " + jwt);

        String expectedSchema = MultiTenant.SCHEMA_PREFIX + tenantId;

        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    String schema = ctx.getOrDefault("schema", null);
                    if (!expectedSchema.equals(schema)) {
                        return Mono.error(new AssertionError(
                                "Expected schema=" + expectedSchema + " but got " + schema));
                    }
                    return Mono.empty();
                })
        );

        MultiTenantFilter filter = new MultiTenantFilter();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void shouldFallbackToDefaultWhenBearerPrefixMissing() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.getFirst("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    String schema = ctx.getOrDefault("schema", null);
                    if (!MultiTenant.DEFAULT_SCHEMA.equals(schema)) {
                        return Mono.error(new AssertionError(
                                "Expected default schema but got " + schema));
                    }
                    return Mono.empty();
                })
        );

        MultiTenantFilter filter = new MultiTenantFilter();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }
}
