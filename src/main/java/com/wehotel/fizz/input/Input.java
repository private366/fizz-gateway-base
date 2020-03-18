package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

import com.wehotel.fizz.StepResponse;

import reactor.core.publisher.Mono;

public class Input {
	protected String name;
	protected InputConfig config;
	protected InputContext inputContext;
	protected StepResponse lastStepResponse = null;
	protected StepResponse stepResponse;
	
	public void setConfig(InputConfig inputConfig) {
		config = inputConfig;
	}
	public InputConfig getConfig() {
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

	/**
	 * 检查该Input是否需要运行，默认都运行
	 * @stepContext Step上下文
	 * @return TRUE：运行
	 */
	public boolean needRun(Map<String, Object> stepContext) {
		return Boolean.TRUE;
	}

	public Mono<Map> run() {
		return null;
	}
	public void setName(String configName) {
		this.name = configName;
		
	}
	
	public StepResponse getStepResponse() {
		return stepResponse;
	}
	public void setStepResponse(StepResponse stepResponse) {
		this.stepResponse = stepResponse;
	}
	
	 
	
}
