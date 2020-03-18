package com.wehotel.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.AggregateResource;
import com.wehotel.fizz.ConfigLoader;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.input.Input;

import reactor.core.publisher.Mono;

@RestController
public class TestController {

	@PostMapping("/json")
	public Mono<Map<String, Object>> sayJson(@RequestBody Map body) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("requestBody", JSON.toJSONString(body));
		System.out.println("say hi");
		return Mono.just(map);
	}

	@GetMapping("/json")
	public Mono<Map<String, Object>> sayJson() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", "ABC");
		map.put("hello", "world");
		System.out.println("hello world");
		return Mono.just(map);
	}
	

	@GetMapping("/delay")
	public Mono<Map<String, Object>> sayJsonWithDelay(@RequestParam(name = "seconds", required = true)Long seconds) {
		try {
			Thread.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user", "ABC");
		map.put("hello", "world");
		System.out.println("hello world");
		return Mono.just(map);
	}
	
	@GetMapping("/reloadConfig")
	public Mono<String> reloadConfig(ServerWebExchange exchange){
		ConfigLoader.init();
		
		return Mono.just("ok");
	}
	
}