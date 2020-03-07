package com.wehotel.fizz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSON;

import reactor.core.publisher.Mono;

public class Step {

	private String name; 
	
	private List<RequestConfig> requestConfigs = new ArrayList<RequestConfig>();

	public static class Builder {
		public Step read(Map<String, Object> config) {
			Step step = new Step();
			List<Map> requests= (List<Map>) config.get("requests");
			for(Map request: requests) {
				RequestConfig requestConfig = new RequestConfig();
				requestConfig.setUrl((String)request.get("url"));
				step.addRequestConfig(requestConfig);
			}
			return step;
		}
	}
	
	private List<StepResponse> stepContext = null;
	StepResponse lastStepResponse = null;
	public void loadContext(List<StepResponse> aStepContext, StepResponse response ) {
		stepContext = aStepContext;
		lastStepResponse = response;
	}

	public List<Mono> configureMonos() {
		List<RequestConfig> configs = this.getRequestConfigs();
		List<Mono> monos = new ArrayList<Mono>();  
		for(RequestConfig config :configs) {
			WebClient client = WebClient.create(config.getBaseUrl());
			WebClient.RequestBodySpec uriSpec = client
					  .method(HttpMethod.POST).uri(config.getPath()).contentType(MediaType.APPLICATION_JSON);
			Mono<String>singleMono = null;
			String jsonStr = null;
			if (lastStepResponse != null) {
				jsonStr = JSON.toJSONString(lastStepResponse.getResult());
			} else {
				jsonStr = JSON.toJSONString(new Object());
			}
			BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters.fromObject(jsonStr);
			singleMono = 
					uriSpec.body(body).retrieve().bodyToMono(String.class);
		
			monos.add(singleMono);
		}
		return monos;
		
	}
	
	public boolean addRequestConfig(RequestConfig requestConfig) {
		return requestConfigs.add(requestConfig);
	}
 

	public List<RequestConfig> getRequestConfigs() {
		return requestConfigs;
	}


	public String getName() {
		if (name == null) {
			return name = "" + (int)(Math.random()*100);
		}
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



}

