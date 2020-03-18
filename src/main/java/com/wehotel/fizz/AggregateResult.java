package com.wehotel.fizz;

import java.util.Map;

import org.springframework.util.MultiValueMap;

public class AggregateResult {

	private MultiValueMap<String, String> headers;
	
	private Map<String, Object> body;

	public MultiValueMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(MultiValueMap<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

}
