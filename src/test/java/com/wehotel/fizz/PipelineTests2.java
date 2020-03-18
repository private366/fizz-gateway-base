package com.wehotel.fizz;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import com.wehotel.fizz.input.Input;
 


class PipelineTests2 {
	private static final Logger LOGGER = LoggerFactory.getLogger(PipelineTests2.class);
	
	@Test
	void contextLoads() {
		 
		// 客户端提交上来的信息
		Map<String,Object> clientInput = new HashMap<>();
		clientInput.put("url", "/hotel/rates");
		clientInput.put("method", "POST");
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("traceId", "afdfdsfdsfdsfdsfs");
		clientInput.put("headers", headers);
		Map<String,Object> requestBody = new HashMap<String,Object>();
		requestBody.put("userId", 123);
		clientInput.put("body", requestBody);
		
		// 聚合接口配置
		File file = new File("json/aggr-demo.json");
		Input input = null;
		Pipeline pipeline = null;
		try {
			input = ConfigLoader.createInput(file);
			pipeline = ConfigLoader.createPipeline(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Mono<AggregateResult> result = pipeline.run(input, clientInput);
		result.onErrorResume((ex)->{
			// TODO handle exception 
			LOGGER.error("=================ERROR");
			
			return Mono.just(new AggregateResult());
		}).block();
	}
	 



}
