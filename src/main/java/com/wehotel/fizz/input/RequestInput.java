package com.wehotel.fizz.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.transformer.TransformationSpec;
import com.wehotel.fizz.StepResponse;

import reactor.core.publisher.Mono;

public class RequestInput extends Input {
	
	private ResponseSpec getClientSpecFromContext(InputConfig aConfig, InputContext inputContext) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		WebClient client = WebClient.create(config.getBaseUrl());
		HttpMethod method = HttpMethod.valueOf(config.getMethod()); 
		String url = config.getPath()+ (StringUtils.isEmpty(config.getQueryStr())  ? "" : ("?" + config.getQueryParams()));
		WebClient.RequestBodySpec uriSpec = client
			  .method(method).uri(url);
		String jsonStr = JSON.toJSONString(new Object());
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			String pathMapping = this.getConfig().getPathMapping();
			if (pathMapping != null && !StringUtils.isEmpty(pathMapping)) {
				ObjectMapper mapper = new ObjectMapper();
				String data;
				try {
					data = mapper.writeValueAsString(stepContext);
					InputStream sourceStream = new ByteArrayInputStream(data.getBytes());
					
			         InputStream transformSpec = new ByteArrayInputStream(pathMapping.getBytes());;
			         TransformationSpec spec;
			         Object sourceJson;

			         //assuming sourceStream, and transformSpec have been initialized
			         Configuration configuration = Configuration.builder()               
			        		 .options(Option.CREATE_MISSING_PROPERTIES_ON_DEFINITE_PATH).build();
			         sourceJson = configuration.jsonProvider().parse(sourceStream,Charset.defaultCharset().name());
			         spec = configuration.transformationProvider().spec(transformSpec, configuration);
			         Object transformed = configuration.transformationProvider().transform(sourceJson,spec, configuration);
			         Map<String, Object> transformedResult = (Map<String, Object>) transformed;
			         jsonStr = JSON.toJSONString(transformedResult);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");
		config.getVariables();
		engine.put("config", config);
		if (!config.getHeaders().containsKey("Content-Type")) {
			//defalut content-type
			uriSpec.header("Content-Type", "application/json; charset=UTF-8");
		}
		for(String key:config.getHeaders().keySet()) {
			String value = (String)config.getHeaders().get(key);
			if (value instanceof String) {
				String maybeEvalStr = (String)value;
				if (maybeEvalStr.startsWith("groovy")) {
					String script = maybeEvalStr.substring("groovy ".length());
					try {
						String scriptResult = (String) engine.eval(script);
						uriSpec.header(key, value);
					} catch (ScriptException e) {
						// Todo:do something when failed
						e.printStackTrace();
					}	
				} else {
					uriSpec.header(key, value);		
				}
			} else {	
				uriSpec.header(key, value);
			}
		}
		
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters.fromObject(jsonStr);
		
		// put request info into stepContext
		request = new HashMap<>();
		this.stepResponse.getRequests().put(name, request);
		request.put("url", url);
		request.put("method", method);
		request.put("requestBody", JSON.parse(jsonStr));
		uriSpec.headers(httpHeaders->{
			request.put("headers", httpHeaders);
		});
		
		return  uriSpec.body(body).retrieve();
	}
	

	private Map<String, Object> getResponses(Map<String, StepResponse> stepContext2) {
		// TODO Auto-generated method stub
		return null;
	}


	public Mono<Map> run() {
		WebClient.ResponseSpec client = this.getClientSpecFromContext(config, inputContext);
		return client.bodyToMono(String.class).flatMap(item->{
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", item);
			result.put("request", this);
			
			this.request.put("responseBody", JSON.parse(item));
			// TODO: change to Class
			return Mono.just(result);
		});
	}

}
