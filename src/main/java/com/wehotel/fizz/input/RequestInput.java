package com.wehotel.fizz.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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
		WebClient.RequestBodySpec uriSpec = client
			  .method(HttpMethod.POST).uri(config.getPath()).contentType(MediaType.APPLICATION_JSON);
		String jsonStr = JSON.toJSONString(new Object());
		if (inputContext != null && inputContext.getResponses() != null) {
			Map<String, Object> responses = inputContext.getResponses();
			
			String pathMapping = this.getConfig().getPathMapping();
			
			if (pathMapping != null && !StringUtils.isEmpty(pathMapping)) {
				ObjectMapper mapper = new ObjectMapper();
				String data;
				try {
					data = mapper.writeValueAsString(responses);
					InputStream sourceStream = new ByteArrayInputStream(data.getBytes());
					
			         InputStream transformSpec = new ByteArrayInputStream(pathMapping.getBytes());;
			         TransformationSpec spec;
			         Object sourceJson;

			         //assuming sourceStream, and transformSpec have been initialized
			         Configuration configuration = Configuration.builder()               .options(Option.CREATE_MISSING_PROPERTIES_ON_DEFINITE_PATH).build();
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
		
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters.fromObject(jsonStr);
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
			// TODO: change to Class
			return Mono.just(result);
		});
	}


	 
	

}
