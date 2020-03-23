package com.wehotel.fizz.route;

import com.wehotel.util.Constants;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lancer Hong
 */
/*
use like:
curl -X POST -H 'Content-type:application/json;charset=UTF-8' -d '{ "cityCode":"AR00252", "keyword":"", "loLat":23.080194, "loLng":113.293113, "distance":10, "languageCode":"1" }' http://127.0.0.1:8080/fizz/service0/api0
to test
 */
// 基于 filter 实现？

@Component
@Order(3) // TODO
public class RouteFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RouteFilter.class);

    private boolean inSomeCase = false; // TODO

    @Resource
    private ServiceConfigHolder serviceConfigHolder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest clientReq = exchange.getRequest();
        ServerHttpResponse clientResp = exchange.getResponse();
        ServiceConfig serviceConfig = getServiceConfig(clientReq);

        // 4 test
        serviceConfig = new ServiceConfig();
        List<HandlerConfig> handlerConfigs = new ArrayList<>();
        serviceConfig.setBeforeForward(handlerConfigs); // TODO think about the scene that apis config override service config, serviceConfig.json need refactor
        HandlerConfig handlerConfig = new HandlerConfig("auth", "{'midStoreInHeader':'xid'}");
        handlerConfigs.add(handlerConfig);

        // to be handler
        // if (!canAccessService(serviceConfig)) {
        //     HttpHeaders hs = new HttpHeaders();
        //     hs.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        //     return resp(clientResp, HttpStatus.FORBIDDEN, hs, "can't access " + serviceConfig.getId());
        //     // TODO design client response model
        // }

        RoutingContext rc = new RoutingContext(exchange);
        Mono<DataBuffer> clientReqBody = clientReq.getBody().single();
        final DataBuffer[] retainBody = new DataBuffer[1];

        Mono<HandleResult> beforeForwardRes = clientReqBody.flatMap(
                body -> {
                    String bodyStr = body.toString(StandardCharsets.UTF_8);
                    log.info("client req body: " + bodyStr);
                    retainBody[0] = DataBufferUtils.retain(body); // !
                    return executeHandlers(rc, handlerConfigs);
                }
        );

        Mono<ClientResponse> remoteRespMono = beforeForwardRes.flatMap(
                hr -> {
                    log.info("______" + rc.get(AuthHandler.AUTH_HANDLER).data.get(AuthHandler.AUTH_RES));

                    // TODO use reactor netty http client instead
                    HttpClient hc = HttpClient.create()
                            .tcpConfiguration(client -> client
                                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
                                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10))
                                            .addHandlerLast(new WriteTimeoutHandler(10))));
                    WebClient wc = WebClient.builder().clientConnector(new ReactorClientHttpConnector(hc)).build();

                    // TODO
                    // 从注册中心找个转过去
                    // 负载均衡等...
                    String remoteUrl = "http://172.25.63.186:9999/inn/suggest";
                    WebClient.RequestBodySpec remoteReq = wc.method(clientReq.getMethod()).uri(remoteUrl);

                    clientReq.getHeaders().entrySet().forEach(entry -> {
                        List<String> hvs = entry.getValue();
                        remoteReq.header(entry.getKey(), hvs.toArray(new String[hvs.size()]));
                    });
                    return remoteReq.bodyValue(retainBody[0]).exchange();
                }
        );

        if (inSomeCase) { // TODO
            // do something then
            return chain.filter(exchange);
        } else {
            return remoteRespMono.flatMap(
                    remoteResp -> {
                        HttpHeaders clientRespHeaders = clientResp.getHeaders();
                        remoteResp.headers().asHttpHeaders().entrySet().forEach(
                                h -> {
                                    clientRespHeaders.addAll(h.getKey(), h.getValue());
                                }
                        );
                        Mono<DataBuffer> remoteRespBody = remoteResp.bodyToMono(DataBuffer.class);

                        // serviceConfig.getAfterForward(); // then do something

                        Mono<DataBuffer> m = remoteRespBody.flatMap(b -> {
                            String bs = b.toString(StandardCharsets.UTF_8);
                            log.info("remote resp body: " + bs);
                            return Mono.just(b);
                        });
                        return clientResp.writeWith(m);
                    }
            );
        }
    }

    private Mono<HandleResult> executeHandlers(RoutingContext rc, List<HandlerConfig> handlerConfigs) {

        log.info("start execute handlers ---------------");
        ApplicationContext appCtx = rc.getServerWebExchange().getApplicationContext();
        Mono<HandleResult> prevMhr = null;
        String[] prevHid = {null};

        for (byte i = 0; i < handlerConfigs.size(); i++) {
            HandlerConfig hc = handlerConfigs.get(i);
            Handler h = appCtx.getBean(hc.id, Handler.class);
            if (i == 0) {
                prevMhr = h.handle(rc, hc.json);
            } else {
                prevMhr = prevMhr.flatMap(
                        hr -> {
                            rc.put(prevHid[0], hr);
                            return h.handle(rc, hc.json);
                        }
                );
            }
            prevHid[0] = hc.id;
        }
        Mono<HandleResult> mhr = prevMhr.flatMap( // flat map is every where ...
                hr -> {
                    rc.put(prevHid[0], hr);
                    return Mono.just(hr);
                }
        );
        log.info("end execute handlers ---------------");
        return mhr;
    }

    private Mono<Void> resp(ServerHttpResponse clientResp, HttpStatus status, HttpHeaders headers, String bodyContent) {
        clientResp.setStatusCode(status);
        headers.forEach(
                (h, vs) -> {
                    clientResp.getHeaders().addAll(h, vs);
                }
        );
        return clientResp
                .writeWith(Mono.just(clientResp.bufferFactory().wrap(bodyContent.getBytes())));
    }

    private boolean canAccessService(ServiceConfig sc) {
        // List<String> blacklist = sc.getBlacklist();
        // List<String> whitelist = sc.getWhitelist();
        // handle blacklist or whitelist, then return result
        return true;
    }

    private static final String serviceHeader = "svc";

    private static final String fizz = "fizz";

    private String getServiceId(ServerHttpRequest clientReq) {
        List<String> vs = clientReq.getHeaders().get(serviceHeader);
        if (vs == null || vs.isEmpty()) {
            String p = clientReq.getPath().value();
            String svc = null;
            for (byte i = 9; i < p.length(); i++) {
                if (p.charAt(i) == Constants.Symbol.FORWARD_SLASH) {
                    svc = p.substring(fizz.length() + 2, i);
                    break;
                }
            }
            return svc;
        } else {
            return vs.get(0);
        }
    }

    private ServiceConfig getServiceConfig(ServerHttpRequest clientReq) {
        String id = getServiceId(clientReq);
        return serviceConfigHolder.getServiceConfig(id);
    }

    // @Override
    // public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    //
    //     ServerHttpRequest clientReq = exchange.getRequest();
    //     Flux<DataBuffer> clientReqBody = clientReq.getBody();
    //
    //     Mono<String> remoteRespBodyMono = clientReqBody.single().flatMap(
    //             body -> {
    //                 String bodyStr = body.toString(StandardCharsets.UTF_8);
    //                 log.info("client req body: " + bodyStr);
    //
    //                 HttpClient httpClient = HttpClient.create()
    //                         .tcpConfiguration(client -> client
    //                                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
    //                                 .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10))
    //                                         .addHandlerLast(new WriteTimeoutHandler(10))));
    //                 WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    //
    //                 WebClient.RequestBodySpec remoteReq = webClient.method(HttpMethod.POST).uri("http://172.25.63.186:9999/inn/suggest");
    //
    //                 // remoteReq.header("Content-Type", "application/json;charset=UTF-8");
    //                 // return remoteReq.bodyValue("{\"cityCode\":\"AR00252\", \"keyword\":\"\", \"loLat\":23.080194, \"loLng\":113.293113, \"distance\":10, \"languageCode\":\"1\"}")
    //                 //         .retrieve().bodyToMono(String.class);
    //
    //                 clientReq.getHeaders().entrySet().forEach(h -> {
    //                     List<String> hvs = h.getValue();
    //                     remoteReq.header(h.getKey(), hvs.toArray(new String[hvs.size()]));
    //                 });
    //
    //                 return remoteReq.bodyValue(body)
    //                         .retrieve().bodyToMono(String.class);
    //             }
    //     );
    //
    //     if (inSomeCase) {
    //         // do something then
    //         return chain.filter(exchange);
    //     } else {
    //         return remoteRespBodyMono.flatMap(
    //                 body -> {
    //                     log.info("remote resp body str: " + body);
    //                     ServerHttpResponse resp = exchange.getResponse();
    //                     resp.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
    //                     return resp
    //                             .writeWith(Mono.just(resp.bufferFactory().wrap("proxy done.".getBytes())));
    //                 }
    //         );
    //     }
    // }
}