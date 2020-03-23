package com.wehotel.fizz.route;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

/**
 * @author Lancer Hong
 * @see serviceConfig.json
 * inspired by vert.x
 */

@FunctionalInterface
public interface Handler<E extends RoutingContext> {

    Mono<? extends HandleResult> handle(E routingContext, @Nullable String jsonConfig);
}
