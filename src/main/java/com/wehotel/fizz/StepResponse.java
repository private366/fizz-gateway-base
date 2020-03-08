package com.wehotel.fizz;

import java.util.HashMap;
import java.util.Map;

public class StepResponse {
	private Step step;
	private Map result;
	public StepResponse(Step aStep, HashMap item) {
		setStep(aStep);
		setResponse(item);
	}
	public Step getStep() {
		return step;
	}
	public void setStep(Step step) {
		this.step = step;
	}
	public Map getResponse() {
		return result;
	}
	public void setResponse(Map result) {
		this.result = result;
	}

}
