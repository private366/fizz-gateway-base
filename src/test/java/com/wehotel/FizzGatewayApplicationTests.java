package com.wehotel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.Step;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@SpringBootTest
class FizzGatewayApplicationTests {
 
	private static Tuple2<Integer, Flux<Integer>> nextPage(int index, int pageSize) {
	    System.out.println("prepared a request for page " + index);
	    return Tuples.of(index, Flux.range((pageSize * (index - 1)) + 1, pageSize));
	}
	private Integer i = 0;
	@SuppressWarnings("unchecked")
	@Test
	void contextLoads() {
	 
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
		Mono<?>result = process.run();
		result.block();
		
	}
	@Test
	void testWebClient() {
		WebClient client = WebClient.create("http://localhost:8080");
		String jsonStr = JSON.toJSONString(new Object());
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters.fromObject(jsonStr);
		WebClient.RequestBodySpec uriSpec = client
				  .method(HttpMethod.POST).uri("/json").contentType(MediaType.APPLICATION_JSON);
 
		String result = uriSpec.body(body).retrieve().bodyToMono(String.class).block();
		System.out.print(result);
	}
	 



}
