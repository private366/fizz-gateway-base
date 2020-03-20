package com.wehotel.fizz.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class ClientInputConfig extends InputConfig {
	
	private String path;
	private String method;
	private Map<String, Object> headers = new HashMap<String, Object>();
	private Map<String, Object> requestBodySchema;

	@SuppressWarnings("unchecked")
	public ClientInputConfig(Map configBody) {
		this.path = (String) configBody.get("path");
		if (configBody.get("headers") != null) {
			setHeaders((Map) configBody.get("headers"));
		}
		if (configBody.get("method") != null) {
			setMethod((String) configBody.get("method"));
		} else {
			setMethod("GET");
		}

		if (configBody.get("requestBodySchema") != null) {
			requestBodySchema = ((Map) configBody.get("requestBodySchema"));
		}
	}
	
	public ClientInputConfig() {
		
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, Object> getRequestBodySchema() {
		return requestBodySchema;
	}

	public void setRequestBodySchema(Map<String, Object> requestBodySchema) {
		this.requestBodySchema = requestBodySchema;
	}
}
