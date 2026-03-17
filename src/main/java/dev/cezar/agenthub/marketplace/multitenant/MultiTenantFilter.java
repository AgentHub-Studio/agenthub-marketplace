package dev.cezar.agenthub.marketplace.multitenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFilter that extracts the tenant ID from the JWT {@code iss} claim and
 * populates the Reactor context with tenant metadata for downstream use.
 *
 * @since 1.0.0
 */
@Slf4j
@Component
public class MultiTenantFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tenantId = TokenExtractorUtils.getTenantIdFromToken(token);
            String userId = TokenExtractorUtils.getUserIdFromToken(token);

            if (tenantId != null) {
                TenantContext tenantContext = new TenantContext(tenantId, userId);
                TenantContextHolder.setContext(tenantContext);

                return chain.filter(exchange)
                        .contextWrite(TenantContextHolder.withTenantContext(tenantContext))
                        .contextWrite(ctx -> ctx
                                .put("tenantId", tenantId)
                                .put("schema", tenantContext.getSchemaName())
                                .put("userId", userId != null ? userId : ""))
                        .doFinally(signal -> TenantContextHolder.clear());
            }
        }

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx
                        .put("tenantId", MultiTenant.DEFAULT_SCHEMA)
                        .put("schema", MultiTenant.DEFAULT_SCHEMA));
    }
}
