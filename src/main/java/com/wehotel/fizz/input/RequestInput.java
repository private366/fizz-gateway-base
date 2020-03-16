package com.wehotel.fizz.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.transformer.TransformationSpec;
import com.wehotel.fizz.StepResponse;
import com.wehotel.util.ParamUtil;

import reactor.core.publisher.Mono;

public class RequestInput extends Input {
	private InputType type;
	protected Map<String, Object> dataMapping;
	public InputType getType() {
		return type;
	}
	public void setType(InputType typeEnum) {
		this.type = typeEnum;
	}
	
	public Map<String, Object> getDataMapping() {
		return dataMapping;
	}
	public void setDataMapping(Map<String, Object> dataMapping) {
		this.dataMapping = dataMapping;
	}
	
	private void doRequestMapping(InputConfig aConfig, InputContext inputContext) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		
		// 把请求信息放入stepContext
		Map<String,Object> group = new HashMap<>();
		group.put("request", request);
		group.put("response", response);
		this.stepResponse.getRequests().put(name, group);
	
		HttpMethod method = HttpMethod.valueOf(config.getMethod()); 
		request.put("method", method);
		
		Map<String, Object> params = new HashMap<>();
		params.putAll(config.getQueryParams());
		params.putAll(config.getParams());
		
		// 数据转换
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			Map<String, Object> dataMapping = this.getConfig().getDataMapping();
			if (dataMapping != null) {
				Map<String, Map<String, String>> requestMapping = (Map<String, Map<String, String>>) dataMapping.get("request");
				if(requestMapping != null && !StringUtils.isEmpty(requestMapping)) {
					
					// headers
					if(requestMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, requestMapping.get("headers"));
						Map<String, Object> headers = new HashMap<>();
						headers.putAll(config.getHeaders());
						if(result != null) {
							headers.putAll(result);
						}
						request.put("headers", headers);
					}
					
					// params
					if(requestMapping.get("params") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, requestMapping.get("params"));
						if(result != null) {
							params.putAll(result);
						}
						request.put("params", params);
					}
					
					// body
					if(requestMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, requestMapping.get("body"));
						Map<String, Object> body = new HashMap<>();
						body.putAll(config.getBody());
						if(result != null) {
							body.putAll(result);
						}
						request.put("body", body);
					}
					
					// script
					if(requestMapping.get("script") != null) {
						// TODO execute script
					}
				}
			}else {
				request.put("headers", config.getHeaders());
				request.put("param", config.getParams());
				request.put("body", config.getBody());
			}
		}
		
		String url = config.getBaseUrl() + config.getPath()+ (params.isEmpty() ? "" : ("?" + ParamUtil.toQueryString(params)));
		request.put("url", url);
	}
	
	private void doResponseMapping(InputConfig aConfig, InputContext inputContext, String responseBody) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		this.response.put("body", JSON.parse(responseBody));	
		// 数据转换
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			Map<String, Object> dataMapping = this.getConfig().getDataMapping();
			if (dataMapping != null) {
				Map<String, Map<String, String>> responseMapping = (Map<String, Map<String, String>>) dataMapping.get("response");
				if(responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
					
					// headers
					if(responseMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, responseMapping.get("headers"));
						Map<String, Object> headers = (Map<String, Object>) response.get("headers");
						if(result != null) {
							headers.putAll(result);
						}
						response.put("headers", headers);
					}
					
					// body
					if(responseMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, responseMapping.get("body"));
						Map<String, Object> body = (Map<String, Object>) response.get("body");
						if(result != null) {
							body.putAll(result);
						}
						response.put("body", body);
					}
					
					// script
					if(responseMapping.get("script") != null) {
						// TODO
					}
				}
			}
		}
	}
	
	private Mono<ClientResponse> getClientSpecFromContext(InputConfig aConfig, InputContext inputContext) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		WebClient client = WebClient.create();
		HttpMethod method = HttpMethod.valueOf(config.getMethod()); 
		String url = (String) request.get("url");
		WebClient.RequestBodySpec uriSpec = client
			  .method(method).uri(url);
		
		
//		ScriptEngineManager factory = new ScriptEngineManager();
//		ScriptEngine engine = factory.getEngineByName("groovy");
//		engine.put("config", config.getVariables());
		if (!config.getHeaders().containsKey("Content-Type")) {
			//defalut content-type
			uriSpec.header("Content-Type", "application/json; charset=UTF-8");
		}
		for(String key:config.getHeaders().keySet()) {
			String value = (String)config.getHeaders().get(key);
			if (value instanceof String) {
				String maybeEvalStr = (String)value;
				if (maybeEvalStr.startsWith("groovy")) {
//					String script = maybeEvalStr.substring("groovy ".length());
//					try {
//						String scriptResult = (String) engine.eval(script);
//						uriSpec.header(key, value);
//					} catch (ScriptException e) {
//						// Todo:do something when failed
//						e.printStackTrace();
//					}	
				} else {
					uriSpec.header(key, value);		
				}
			} else {	
				uriSpec.header(key, value);
			}
		}
		
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters.fromObject(JSON.toJSONString(config.getBody()));	
		return  uriSpec.body(body).exchange();
	}
	

	private Map<String, Object> getResponses(Map<String, StepResponse> stepContext2) {
		// TODO Auto-generated method stub
		return null;
	}


	public Mono<Map> run() {
		this.doRequestMapping(config, inputContext);
		Mono<ClientResponse> clientResponse = this.getClientSpecFromContext(config, inputContext);
		return clientResponse.doOnSuccess(cr -> {
			Map<String, Object> h = new HashMap<>();
			HttpHeaders httpHeaders = cr.headers().asHttpHeaders();
			httpHeaders.forEach((k,v)->{
				h.put(k, httpHeaders.getFirst(k));
			});
			this.response.put("headers", h);
		}).flatMap(cr -> cr.bodyToMono(String.class)).flatMap(item -> {
					Map<String, Object> result = new HashMap<String, Object>();
					result.put("data", item);
					result.put("request", this);

					this.doResponseMapping(config, inputContext, item);

					// TODO: change to Class
					return Mono.just(result);
				});
	}

}
