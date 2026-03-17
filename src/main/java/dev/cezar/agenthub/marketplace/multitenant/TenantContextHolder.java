package dev.cezar.agenthub.marketplace.multitenant;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Objects;

/**
 * Stores and retrieves {@link TenantContext} from both ThreadLocal and Reactor context.
 *
 * @since 1.0.0
 */
public class TenantContextHolder {

    public static final String TENANT_CONTEXT_KEY = "TENANT_CONTEXT_KEY";

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setContext(TenantContext tenantContext) {
        CONTEXT.set(tenantContext);
    }

    public static TenantContext getContext() {
        TenantContext tc = CONTEXT.get();
        if (tc == null) {
            try {
                return Mono.deferContextual(ctx ->
                        Mono.justOrEmpty(ctx.<TenantContext>getOrEmpty(TENANT_CONTEXT_KEY)))
                        .block();
            } catch (Exception e) {
                return null;
            }
        }
        return tc;
    }

    public static Mono<TenantContext> getContextFromReactor() {
        return Mono.deferContextual(ctx ->
                Mono.justOrEmpty(ctx.<TenantContext>getOrEmpty(TENANT_CONTEXT_KEY)));
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Context withTenantContext(TenantContext tenantContext) {
        return Context.of(TENANT_CONTEXT_KEY, tenantContext);
    }

    public static Context withTenantContext() {
        return Context.of(TENANT_CONTEXT_KEY, Objects.requireNonNull(getContext()));
    }
}
