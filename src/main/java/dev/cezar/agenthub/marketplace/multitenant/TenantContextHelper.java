package dev.cezar.agenthub.marketplace.multitenant;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive helpers for extracting tenant and user IDs from Reactor context.
 *
 * @since 1.0.0
 */
public class TenantContextHelper {

    private TenantContextHelper() {}

    /**
     * Returns the current tenant ID from Reactor context.
     *
     * @return {@code Mono<UUID>} with tenant ID, or error if not present
     */
    public static Mono<UUID> getTenantId() {
        return Mono.deferContextual(ctx -> {
            String tenantId = ctx.getOrDefault("tenantId", null);
            if (tenantId == null) {
                return Mono.error(new IllegalStateException("TenantId not available in context"));
            }
            try {
                return Mono.just(UUID.fromString(tenantId));
            } catch (IllegalArgumentException e) {
                return Mono.error(new IllegalStateException("Invalid tenantId: " + tenantId));
            }
        });
    }

    /**
     * Returns the current user ID from Reactor context.
     *
     * @return {@code Mono<UUID>} with user ID, or error if not present
     */
    public static Mono<UUID> getUserId() {
        return Mono.deferContextual(ctx -> {
            String userId = ctx.getOrDefault("userId", null);
            if (userId == null) {
                return Mono.error(new IllegalStateException("UserId not available in context"));
            }
            try {
                return Mono.just(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                return Mono.error(new IllegalStateException("Invalid userId: " + userId));
            }
        });
    }
}
