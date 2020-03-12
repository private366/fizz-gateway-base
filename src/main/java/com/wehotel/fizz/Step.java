package com.wehotel.fizz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.input.Input;
import com.wehotel.fizz.input.InputConfig;
import com.wehotel.fizz.input.InputContext;
import com.wehotel.fizz.input.InputFactory;
import com.wehotel.fizz.input.InputType;

import reactor.core.publisher.Mono;

public class Step {

	private String name; 
	
	private Map<String, InputConfig> requestConfigs = new HashMap<String, InputConfig>();

	public static class Builder {
		public Step read(Map<String, Object> config) {
			Step step = new Step();
			Map<String, Map> requests= (Map<String, Map>) config.get("requests");
			for(String name: requests.keySet()) {
				Map requestConfig = requests.get(name);
				InputConfig inputConfig = InputFactory.createInputConfig(requestConfig);
				step.addRequestConfig(name, inputConfig);
			}
			return step;
		}
	}
	
	private Map<String, Object> stepContext;
	private StepResponse lastStepResponse = null;
	private Map<String, Input> inputs = new HashMap<String, Input>();
	public void beforeRun(Map<String, Object> stepContext2, StepResponse response ) {
		stepContext = stepContext2;
		lastStepResponse = response;
		StepResponse stepResponse = new StepResponse(this, null, new HashMap<String, Map<String, Object>>());
		stepContext.put(name, stepResponse);
		Map<String, InputConfig> configs = this.getRequestConfigs();
		for(String configName :configs.keySet()) {
			InputConfig inputConfig = configs.get(configName);
			InputType type = inputConfig.getType();
			Input input = InputFactory.createInput(type.toString());
			input.setConfig(inputConfig);
			input.setName(configName);
			input.setStepResponse(stepResponse);
			InputContext context = new InputContext(stepContext, lastStepResponse);
			input.beforeRun(context); 
			inputs.put(input.getName(), input);
		}
	}

	public List<Mono> run() {
		List<Mono> monos = new ArrayList<Mono>();  
		for(String name :inputs.keySet()) {
			Input input = inputs.get(name);
			Mono<Map>singleMono = input.run(); 
			monos.add(singleMono);
		}
		return monos;	
	}

	public void afeterRun() {
		
	}
	
	public InputConfig addRequestConfig(String name,  InputConfig requestConfig) {
		return requestConfigs.put(name, requestConfig);
	}
 

	public Map<String, InputConfig> getRequestConfigs() {
		return requestConfigs;
	}


	public String getName() {
		if (name == null) {
			return name = "step" + (int)(Math.random()*100);
		}
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}



}

