package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

import com.wehotel.fizz.StepResponse;

public class InputContext {
	private Map<String, StepResponse> stepContext;
	private StepResponse lastStepResponse = null;
	public InputContext(Map<String, StepResponse> stepContext2, StepResponse lastStepResponse2) {
		this.stepContext = stepContext2;
		this.lastStepResponse = lastStepResponse2;
	}
	public Map<String, StepResponse> getStepContext() {
		return stepContext;
	}
	public void setStepContext(Map<String, StepResponse> stepContext) {
		this.stepContext = stepContext;
	}
	public StepResponse getLastStepResponse() {
		return lastStepResponse;
	}
	public void setLastStepResponse(StepResponse lastStepResponse) {
		this.lastStepResponse = lastStepResponse;
	}
	public Map<String, Object> getResponses() {
		 //TODO:
		if (stepContext  != null) {
			Map<String, Object> responses = new HashMap<String, Object>();
			for( String key :stepContext.keySet()) {
				StepResponse stepResponse = stepContext.get(key);
				responses.put(key, stepResponse.getResponse());
			}
			return responses;
		} else {
			return null;
		}
		
		
		
	}
	
}
