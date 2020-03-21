package com.wehotel.fizz.route;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Lancer Hong
 */
/*
use:
curl -X POST -H 'Content-type:application/json;charset=UTF-8' -d '{ "cityCode":"AR00252", "keyword":"", "loLat":23.080194, "loLng":113.293113, "distance":10, "languageCode":"1" }' http://127.0.0.1:8080/service0/api0
to test
 */
// 基于 filter 实现？

@Component
@Order(3) // TODO
public class RouteFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RouteFilter.class);

    private boolean inSomeCase = false; // TODO

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest clientReq = exchange.getRequest();
        Flux<DataBuffer> clientReqBody = clientReq.getBody();

        Mono<ClientResponse> remoteRespMono = clientReqBody.single().flatMap(
                body -> {
                    String bodyStr = body.toString(StandardCharsets.UTF_8);
                    log.info("client req body: " + bodyStr);

                    // TODO improve and use reactor http client instead webclient
                    HttpClient hc = HttpClient.create()
                            .tcpConfiguration(client -> client
                                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
                                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10))
                                            .addHandlerLast(new WriteTimeoutHandler(10))));
                    WebClient wc = WebClient.builder().clientConnector(new ReactorClientHttpConnector(hc)).build();

                    String remoteUrl = "http://172.25.63.186:9999/inn/suggest"; // TODO
                    WebClient.RequestBodySpec remoteReq = wc.method(clientReq.getMethod()).uri(remoteUrl);

                    clientReq.getHeaders().entrySet().forEach(entry -> {
                        List<String> hvs = entry.getValue();
                        remoteReq.header(entry.getKey(), hvs.toArray(new String[hvs.size()]));
                    });
                    return remoteReq.bodyValue(body).exchange();
                }
        );

        if (inSomeCase) { // TODO
            // do something then
            return chain.filter(exchange);
        } else {
            return remoteRespMono.flatMap(
                    remoteResp -> {
                        ServerHttpResponse clientResp = exchange.getResponse();
                        HttpHeaders clientRespHeaders = clientResp.getHeaders();
                        remoteResp.headers().asHttpHeaders().entrySet().forEach(
                                h -> {
                                    clientRespHeaders.addAll(h.getKey(), h.getValue());
                                }
                        );
                        Mono<DataBuffer> remoteRespBody = remoteResp.bodyToMono(DataBuffer.class);
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
