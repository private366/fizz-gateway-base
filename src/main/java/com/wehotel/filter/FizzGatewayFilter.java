package com.wehotel.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.Step;

import reactor.core.publisher.Flux;
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
		ServerHttpResponse serverHttpResponse =  exchange.getResponse();
//		https://gist.github.com/cer/04ce15ba46f54634312740135fcfdeea
		// 直接返回响应结果，跳过后面的filter和controller
		Map<String, Object> config1= new HashMap<String, Object>();
		List<Map> requests = new ArrayList<Map>();
		Map <String ,Object>requestConfig = new HashMap<String, Object>(); 
		requestConfig.put("url","http://localhost:8080/json");
		requests.add(requestConfig);
		requests.add(requestConfig);
		config1.put("requests", requests);
		Step step1 = new Step.Builder().read(config1);
		Map<String, Object> config2= new HashMap<String, Object>();
		config2.put("requests", requests);
		Step step2 = new Step.Builder().read(config2);
		Pipeline process = new Pipeline();
		process.addStep(step1);
		process.addStep(step2);
		Mono<?>result = process.run(null);
	
		return result.flatMap(clientResponse -> {
			String jsonString = JSON.toJSONString(clientResponse);
			return serverHttpResponse.writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(jsonString.getBytes())));
		});
//		            .accept(request.getHeaders().getAccept().toArray(new MediaType[0]))
//		            .exchange()
//		            .flatMap(clientResponse -> {
//		              return serverHttpResponse.writeWith(clientResponse.bodyToFlux(String.class).map(s -> defaultDataBufferFactory.wrap(s.getBytes())));
//		            });
//		return userMono;
		 
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