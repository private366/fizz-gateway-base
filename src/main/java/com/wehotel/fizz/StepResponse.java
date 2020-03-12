package com.wehotel.fizz;

import java.util.HashMap;
import java.util.Map;

public class StepResponse {
	private Step step;
	private Map<String, Map<String, Object>> requests;
	private Map result;
	public StepResponse(Step aStep, HashMap item, Map<String, Map<String, Object>> requests) {
		setStep(aStep);
		setResult(item);
		setRequests(requests);
	}
	public StepResponse(Step aStep, HashMap item) {
		setStep(aStep);
		setResult(item);
	}
	public Step getStep() {
		return step;
	}
	public void setStep(Step step) {
		this.step = step;
	}
//	public Map getResponse() {
//		return result;
//	}
//	public void setResponse(Map result) {
//		this.result = result;
//	}
	
	public Map<String, Map<String, Object>> getRequests() {
		return requests;
	}
	public void setRequests(Map<String, Map<String, Object>> requests) {
		this.requests = requests;
	}
	public Map getResult() {
		return result;
	}
	public void setResult(Map result) {
		this.result = result;
	}

}
