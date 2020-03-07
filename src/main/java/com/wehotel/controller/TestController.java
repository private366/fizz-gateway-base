package com.wehotel.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class TestController {
    @GetMapping("/json")
    public Mono<Map<String,Object>> sayJson() {
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("say", "hi");
    	System.out.println("say hi");
        return Mono.just(map);
    }
}