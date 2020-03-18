package com.wehotel.fizz.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestInputConfig extends InputConfig{
	private URL url ;
	private Map<String, Object> body = new HashMap<String, Object>();
	private Map<String, Object> params = new HashMap<String, Object>();
	private  Map<String, Object> headers =  new HashMap<String, Object>();
	private String method ;
	private int connectTimeout = 1;
	private int readTimeout = 3;
	private int writeTimeout = 3;
	private Map<String, Object> condition;

	public RequestInputConfig(Map configBody) {
		String url = (String) configBody.get("url");
		setUrl(url);
		if (configBody.get("body") != null) {
			setBody((Map)configBody.get("body"));	
		}
		if (configBody.get("params") != null) {
			setParams((Map)configBody.get("params"));	
		}
		if (configBody.get("headers") != null) {
			setHeaders((Map)configBody.get("headers"));
		}
		if (configBody.get("method") != null) {
			setMethod((String)configBody.get("method"));
		} else {
			setMethod("GET");
		}
		if (configBody.get("connectTimeout") != null) {
			connectTimeout = (int)configBody.get("connectTimeout");
		}
		if (configBody.get("readTimeout") != null) {
			readTimeout = (int)configBody.get("readTimeout");
		}
		if (configBody.get("writeTimeout") != null) {
			writeTimeout = (int)configBody.get("writeTimeout");
		}
		if (configBody.get("condition") != null) {
			setCondition((Map)configBody.get("condition"));
		}
	}
	
	public String getQueryStr(){
		return url.getQuery();
	}
	
	public MultiValueMap<String, String> getQueryParams(){
		MultiValueMap<String, String> parameters =
	            UriComponentsBuilder.fromUriString(url.toString()).build().getQueryParams();
		return parameters;
	}

	
	public String getBaseUrl() {
		return url.getProtocol()+ "://"+ url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort());
	}

	public String getPath() {
		return url.getPath();
	}

	public void setUrl(String string) {
		try {
			url = new URL(string);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
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

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public Map<String, Object> getCondition() {
		return condition;
	}

	public void setCondition(Map<String, Object> condition) {
		this.condition = condition;
	}
}
