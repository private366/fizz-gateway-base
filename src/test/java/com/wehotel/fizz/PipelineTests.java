package com.wehotel.fizz;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import com.wehotel.fizz.input.ClientInputConfig;
import com.wehotel.fizz.input.Input;
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
		step1.setStop(true);
		Map<String, Object> step1DataMapping = new HashMap<>();
		Map<String, Map<String, String>> step1dmResponse = new HashMap<>(); 
		step1DataMapping.put("response", step1dmResponse);
		Map <String ,String> step1RespBodyMapping = new HashMap<>(); 
		step1RespBodyMapping.put("rates", "step1.requests.request1.response.body.hello");
		step1RespBodyMapping.put("roomTypes", "step1.requests.request1.response.body.user");
		step1dmResponse.put("body", step1RespBodyMapping);
		step1.setDataMapping(step1DataMapping);
		
		
		Map<String, Object> config2= new HashMap<String, Object>();
	 
		Map<String, Map> requests2 = new HashMap<String, Map>();
		Map <String ,Object>requestConfig2 = new HashMap<String, Object>(); 
		Map <String ,Object>innerConfig1 =  new HashMap<String, Object>();
		innerConfig1.put("url","http://localhost:8080/json");
		
		Map<String, Object> dataMapping = new HashMap<>();
		Map<String, Map<String, String>> requestMapping = new HashMap<>(); 
		dataMapping.put("request", requestMapping);
		Map <String ,String> bodyMapping = new HashMap<>(); 
		bodyMapping.put("abc.requestBody22", "step1.requests.request1.response.body");
		requestMapping.put("body", bodyMapping);
		innerConfig1.put("dataMapping", dataMapping);
		
//		innerConfig1.put("pathMapping", "{\n" + 
//				"  \"pathMappings\": [{\n" + 
//				"    \"source\": \"$.step1.requests.request1.response.body\",\n" + 
//				"    \"target\": \"$.requestBody2\"\n" + 
//				"  }]\n" + 
//				"}");
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
		
		Map<String,Object> clientInput = new HashMap<>();
		clientInput.put("url", "/hotel/rates");
		clientInput.put("method", "POST");
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("traceId", "afdfdsfdsfdsfdsfs");
		clientInput.put("headers", headers);
		Map<String,Object> requestBody = new HashMap<String,Object>();
		requestBody.put("userId", 123);
		clientInput.put("body", requestBody);
		
		Input input = new Input();
		input.setName("aggr-api-test");
		
		ClientInputConfig clientInputConfig = new ClientInputConfig();
		clientInputConfig.setType(InputType.REQUEST);
		clientInputConfig.setMethod("POST");
		clientInputConfig.setPath("/hotel/rates");
		clientInputConfig.setHeaders(null);
		
		Map<String, Object> inputDataMapping = new HashMap<>();
		Map<String, Map<String, String>> dmRequest = new HashMap<>();
		inputDataMapping.put("request", dmRequest);
		Map <String ,String> inputReqScript = new HashMap<>(); 
		inputReqScript.put("source", "todo...");
		dmRequest.put("script", inputReqScript);
		
		Map<String, Map<String, String>> dmResponse = new HashMap<>();
		inputDataMapping.put("response", dmResponse); 
		Map <String ,String> inputRespBodyMapping = new HashMap<>(); 
		inputRespBodyMapping.put("aaaa", "step1.result");
		inputRespBodyMapping.put("bbb", "step1.requests.request1.response.body.hello");
		inputRespBodyMapping.put("ccc", "step2.requests.request3.response.body.hello");
		dmResponse.put("body", inputRespBodyMapping);
		
		
		clientInputConfig.setDataMapping(inputDataMapping);
		input.setConfig(clientInputConfig);
		
		Mono<?>result = pipeline.run(input, clientInput);
		result.block();
	}
	 



}
