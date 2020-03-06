package com.wehotel.filter;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.function.client.WebClient;

@Component
@Order(2)
public class FizzGatewayFilter implements WebFilter {
	private DefaultDataBufferFactory defaultDataBufferFactory = new DefaultDataBufferFactory();
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request =  exchange.getRequest();
		String path = request.getURI().getPath();
		if (path.contains("/json")) {
			return chain.filter(exchange);
		}
//		https://gist.github.com/cer/04ce15ba46f54634312740135fcfdeea
		// 直接返回响应结果，跳过后面的filter和controller
		WebClient client = WebClient.create("http://localhost:8080");
		ServerHttpResponse serverHttpResponse =  exchange.getResponse();
		Mono<Void>userMono = client.get()
		            .uri("/json")
		            .accept(request.getHeaders().getAccept().toArray(new MediaType[0]))
		            .exchange()
		            .flatMap(clientResponse -> {
		              return serverHttpResponse.writeWith(clientResponse.bodyToFlux(String.class).map(s -> defaultDataBufferFactory.wrap(s.getBytes())));
		            });
		return userMono;
		 
//		
//		response.setStatusCode(HttpStatus.OK);
//   		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
//   		Map<String,Object> map = new HashMap<String,Object>();
//   		String jsonString = JSON.toJSONString(map);
   			
//   		return response.writeWith(Mono.just(response.bufferFactory().wrap(jsonString.getBytes())));
//		exchange.getResponse().writeWith(
//		Flux.just(exchange.getResponse().bufferFactory().wrap(JsonUtil.toJson(new BaseErrorVO().fail(e.getMessageCode(),e.getSystemMessage())).getBytes())));
	}
 
}