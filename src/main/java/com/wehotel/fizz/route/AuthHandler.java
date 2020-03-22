package com.wehotel.fizz.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wehotel.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 */

@Component(AuthHandler.AUTH_HANDLER)
public class AuthHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(AuthHandler.class);

    public static final String AUTH_HANDLER = "auth"; // it's serviceConfig.json.服务id.beforeForward[1].id

    public static final String AUTH_RES = "authRes"; // test

    private static Map<String, Config> configCache = new HashMap<>(2048); // to public ?

    public static class Config {
        public String midStoreInHeader; // test cfg
    }

    @Override
    public Mono<? extends HandleResult> handle(RoutingContext routingContext, @Nullable String jsonConfig) {
        ServerWebExchange exchange = routingContext.getServerWebExchange();
        ServerHttpRequest clientReq = exchange.getRequest();
        String path = clientReq.getPath().value(); // path is heavy
        Config config = configCache.get(path);
        try {
            if (config == null) {
                config = JacksonUtils.getObjectMapper().readValue(jsonConfig, Config.class);
                configCache.put(path, config);
            }
            String memberId = "mid:13710242676"; // come from codis of member system
            // clientReq.getHeaders().add(config.midStoreInHeader, memberId); // can't do this
            log.info(memberId);
            Map<String, Object> authRes = new HashMap<>();
            authRes.put(AUTH_RES, "this is auth result");
            return Mono.just(HandleResult.SUCCESS_WITH(authRes));
        } catch (JsonProcessingException e) {
            return Mono.just(HandleResult.FAIL_WITH(e));
        }
    }
}
