package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

public class DataMapping {

	private Map<String, Map<String, String>> request = new HashMap<>();
	private Map<String, Map<String, String>> response = new HashMap<>();
	
	public Map<String, Map<String, String>> getRequest() {
		return request;
	}

	public void setRequest(Map<String, Map<String, String>> request) {
		this.request = request;
	}

	public Map<String, Map<String, String>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, String>> response) {
		this.response = response;
	}

}
