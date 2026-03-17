package dev.cezar.agenthub.marketplace.multitenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

/**
 * Executes reactive operations inside a tenant-scoped transaction,
 * setting the PostgreSQL {@code search_path} to the tenant schema before running.
 *
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantTransactionalHelper {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    /**
     * Wraps {@code source} in a transaction that first sets the PostgreSQL search_path
     * to the current tenant schema.
     *
     * @param source the reactive operation to execute
     * @param <T>    result type
     * @return transactional mono
     */
    public <T> Mono<T> inTenantTransaction(Mono<T> source) {
        return TenantContextHolder.getContextFromReactor()
                .flatMap(ctx -> databaseClient
                        .sql("SET search_path TO " + ctx.getSchemaName())
                        .fetch()
                        .rowsUpdated()
                        .then(source))
                .as(transactionalOperator::transactional);
    }
}
