package com.wehotel.fizz.input;

import java.util.Map;

import com.wehotel.fizz.StepResponse;

import reactor.core.publisher.Mono;

public class Input {
	protected String name;
	protected InputConfig config;
	protected InputContext inputContext;
	protected StepResponse lastStepResponse = null;
	
	public void setConfig(InputConfig inputConfig) {
		config = inputConfig;
	}
	protected InputConfig getConfig() {
		return config;
	}

	public void beforeRun(InputContext context) {
		this.inputContext = context;
	}

	public String getName() {
		if (name == null) {
			return name = "input" + (int)(Math.random()*100);
		}
		return name;
	}

	public Mono<Map> run() {
		return null;
	}
	public void setName(String configName) {
		this.name = configName;
		
	}
	
	 
	
}
