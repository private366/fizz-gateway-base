package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.StepResponse;
import com.wehotel.util.MapUtil;
import com.wehotel.util.Script;
import com.wehotel.util.ScriptUtils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class RequestInput extends Input {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestInput.class);
	private InputType type;
	protected Map<String, Object> dataMapping;
	protected Map<String, Object> request = new HashMap<>();
	protected Map<String, Object> response = new HashMap<>();
	
	private static final String FALLBACK_MODE_STOP = "stop";
	private static final String FALLBACK_MODE_CONTINUE = "continue";
	
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.putAll(config.getQueryParams());
		params.putAll(MapUtil.toMultiValueMap(config.getParams()));
		
		// 数据转换
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			Map<String, Object> dataMapping = this.getConfig().getDataMapping();
			if (dataMapping != null) {
				Map<String, Object> requestMapping = (Map<String, Object>) dataMapping.get("request");
				if(requestMapping != null && !StringUtils.isEmpty(requestMapping)) {
					
					// headers
					if(requestMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, String>) requestMapping.get("headers"));
						MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
						headers.putAll(MapUtil.toMultiValueMap(config.getHeaders()));
						if(result != null) {
							headers.putAll(MapUtil.toMultiValueMap(result));
						}
						request.put("headers", headers);
					}
					
					// params
					if(requestMapping.get("params") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, String>) requestMapping.get("params"));
						if(result != null) {
							params.putAll(MapUtil.toMultiValueMap(result));
						}
						request.put("params", params);
					}
					
					// body
					if(requestMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, String>) requestMapping.get("body"));
						Map<String, Object> body = new HashMap<>();
						body.putAll(config.getBody());
						if(result != null) {
							body.putAll(result);
						}
						request.put("body", body);
					}
					
					// script
					if(requestMapping.get("script") != null) {
						Map<String, Object> scriptCfg = (Map<String, Object>)requestMapping.get("script");
						try {
							ScriptHelper.execute(scriptCfg, stepContext);
						} catch (ScriptException e) {
							LOGGER.warn("execute script failed, {}", e);
							throw new RuntimeException("execute script failed");
						}
					}
				}
			}else {
				request.put("headers", config.getHeaders());
				request.put("param", config.getParams());
				request.put("body", config.getBody());
			}
		}
		
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(config.getBaseUrl() + config.getPath())
				.queryParams(params).build();
		request.put("url", uriComponents.toUriString());
	}
	
	private void doResponseMapping(InputConfig aConfig, InputContext inputContext, String responseBody) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		this.response.put("body", JSON.parse(responseBody));	
		// 数据转换
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			Map<String, Object> dataMapping = this.getConfig().getDataMapping();
			if (dataMapping != null) {
				Map<String, Object> responseMapping = (Map<String, Object>) dataMapping.get("response");
				if(responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
					
					// headers
					if(responseMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, String>) responseMapping.get("headers"));
						MultiValueMap<String, String> headers = (MultiValueMap<String, String>) response.get("headers");
						if(result != null) {
							headers.putAll(MapUtil.toMultiValueMap(result));
						}
						response.put("headers", headers);
					}
					
					// body
					if(responseMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, String>) responseMapping.get("body"));
						Map<String, Object> body = (Map<String, Object>) response.get("body");
						if(result != null) {
							body.putAll(result);
						}
						response.put("body", body);
					}
					
					// script
					if(responseMapping.get("script") != null) {
						Map<String, Object> scriptCfg = (Map<String, Object>) responseMapping.get("script");
						try {
							ScriptHelper.execute(scriptCfg, stepContext);
						} catch (ScriptException e) {
							LOGGER.warn("execute script failed, {}", e);
							throw new RuntimeException("execute script failed");
						}
					}
				}
			}
		}
	}
	
	private Mono<ClientResponse> getClientSpecFromContext(InputConfig aConfig, InputContext inputContext) {
		RequestInputConfig config = (RequestInputConfig)aConfig;
		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(client -> client
						.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout() * 1000)
						.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(config.getReadTimeout()))
								.addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeout()))));
		WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();

		HttpMethod method = HttpMethod.valueOf(config.getMethod()); 
		String url = (String) request.get("url");
		WebClient.RequestBodySpec uriSpec = client.method(method).uri(url);
		
		MultiValueMap<String, String> headers = (MultiValueMap<String, String>) request.get("headers");
		if(headers == null) {
			headers = new LinkedMultiValueMap<>();
		}
		if (!headers.containsKey("Content-Type")) {
			//defalut content-type
			headers.add("Content-Type", "application/json; charset=UTF-8");
		}
		for(Entry<String, List<String>> entry : headers.entrySet()) {
			if(!CollectionUtils.isEmpty(entry.getValue())) {
				uriSpec.header(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
			}
		}
		
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters
				.fromObject(JSON.toJSONString(config.getBody()));
		return uriSpec.body(body).exchange();
	}
	

	private Map<String, Object> getResponses(Map<String, StepResponse> stepContext2) {
		// TODO Auto-generated method stub
		return null;
	}


	public Mono<Map> run() {
		this.doRequestMapping(config, inputContext);
		Mono<ClientResponse> clientResponse = this.getClientSpecFromContext(config, inputContext);
		Mono<String> body = clientResponse.doOnSuccess(cr -> {
			HttpHeaders httpHeaders = cr.headers().asHttpHeaders();
			this.response.put("headers", httpHeaders);
		}).doOnError(ex->{
			LOGGER.warn("failed to call {}", request.get("url"), ex);
		}).flatMap(cr -> cr.bodyToMono(String.class));

		// fallback handler
		RequestInputConfig reqConfig = (RequestInputConfig) config;
		if (reqConfig.getFallback() != null) {
			Map<String, String> fallback = reqConfig.getFallback();
			String mode = fallback.get("mode");
			if (FALLBACK_MODE_STOP.equals(mode)) {
				body = body.onErrorStop();
			} else if (FALLBACK_MODE_CONTINUE.equals(mode)) {
				body = body.onErrorResume(ex -> {
					return Mono.just(fallback.get("defaultResult"));
				});
			}else {
				body = body.onErrorStop();
			}
		}

		return body.flatMap(item -> {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("data", item);
			result.put("request", this);

			this.doResponseMapping(config, inputContext, item);

			// TODO: change to Class
			return Mono.just(result);
		});
	}

}
