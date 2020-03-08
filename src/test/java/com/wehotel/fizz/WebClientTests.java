package com.wehotel.fizz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSON;
 

@SpringBootTest
class WebClientTests {
	@Test
	void contextLoads() {
	 
		
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
		Assertions.assertNotEquals(result, null, "no response");
	}


}
