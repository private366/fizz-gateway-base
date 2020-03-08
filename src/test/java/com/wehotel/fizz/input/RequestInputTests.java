package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Mono;

@SpringBootTest
class RequestInputTests {
	@Test
	void contextLoads() {
		Map <String ,Object>requestConfig = new HashMap<String, Object>(); 
		requestConfig.put("url","http://localhost:8080/json");
		RequestInputConfig inputConfig = new RequestInputConfig(requestConfig);
		RequestInput input = new RequestInput();
		input.setConfig(inputConfig);
		input.beforeRun(null);
		Mono<Map>mono = input.run();
		Map result = mono.block();
		System.out.print(result);
		Assertions.assertNotEquals(result, null, "no response");
	}

}