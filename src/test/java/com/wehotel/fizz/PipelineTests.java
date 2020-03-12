package com.wehotel.fizz;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import com.wehotel.fizz.input.InputType;
 


class PipelineTests {
	@Test
	void contextLoads() {
		 
//		https://gist.github.com/cer/04ce15ba46f54634312740135fcfdeea
		// 直接返回响应结果，跳过后面的filter和controller
		Map<String, Object> config1= new HashMap<String, Object>();
		Map<String, Map> requests1 = new HashMap<String, Map>();
		Map <String ,Object>requestConfig = new HashMap<String, Object>(); 
		Map <String ,Object>innerConfig =  new HashMap<String, Object>();
		innerConfig.put("url","http://localhost:8080/json");
		requestConfig.put("type", InputType.REQUEST.toString());
		requestConfig.put("config", innerConfig);
		requests1.put("request1", requestConfig);
		requests1.put("request2", requestConfig);
		config1.put("requests", requests1);
		
		Step step1 = new Step.Builder().read(config1);
		step1.setName( "step1");
		Map<String, Object> config2= new HashMap<String, Object>();
	 
		Map<String, Map> requests2 = new HashMap<String, Map>();
		Map <String ,Object>requestConfig2 = new HashMap<String, Object>(); 
		Map <String ,Object>innerConfig1 =  new HashMap<String, Object>();
		innerConfig1.put("url","http://localhost:8080/json");
		innerConfig1.put("pathMapping", "{\n" + 
				"  \"pathMappings\": [{\n" + 
				"    \"source\": \"$.step1.requests.request1.responseBody\",\n" + 
				"    \"target\": \"$.requestBody2\"\n" + 
				"  }]\n" + 
				"}");
		requestConfig2.put("type", InputType.REQUEST.toString());
		requestConfig2.put("config", innerConfig1);
		requests2.put("request3", requestConfig2);
		requests2.put("request4", requestConfig2);
		config2.put("requests", requests2);
 
		Step step2 = new Step.Builder().read(config2);
		step2.setName( "step2");
		Pipeline pipeline = new Pipeline();
		pipeline.addStep(step1);
		pipeline.addStep(step2);
		
		Map<String,Object> input = new HashMap<>();
		input.put("url", "/hello");
		input.put("method", "GET");
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("traceId", "afdfdsfdsfdsfdsfs");
		input.put("headers", headers);
		Map<String,Object> requestBody = new HashMap<String,Object>();
		requestBody.put("userId", 123);
		input.put("requestBody", requestBody);
		
		Mono<?>result = pipeline.run(input);
		result.block();
	}
	 



}
