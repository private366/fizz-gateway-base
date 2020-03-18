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

	/**
	 * condition.variables中需要从StepContext获取变量值的前缀
	 */
	private static final String STEP_CONTEXT_PARAM_PREFIX = "input ";

	@Override
	@SuppressWarnings("unchecked")
    public boolean needRun(Map<String, Object> stepContext) {
		Map<String, Object> condition = ((RequestInputConfig) config).getCondition();
		if (CollectionUtils.isEmpty(condition)) {
			// 没有配置condition，直接运行
			return Boolean.TRUE;
		}

		Script script = new Script();
		script.setType((String) condition.get("type"));
		script.setSource((String) condition.get("source"));
		Map<String, Object> variables = (Map<String, Object>) condition.get("variables");
		if (!CollectionUtils.isEmpty(variables)) {
			Map<String, Object> transformedVariables = new HashMap<>(variables.size());
			Map<String, String> rules = new HashMap<>(variables.size());
			for (Entry<String, Object> entry : variables.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof String && ((String) value).startsWith(STEP_CONTEXT_PARAM_PREFIX)) {
					// 需要从Step上下文中获取参数
					rules.put(key, ((String) value).substring(STEP_CONTEXT_PARAM_PREFIX.length()));
				} else {
					transformedVariables.put(key, value);
				}
			}
			if (rules.size() > 0) {
				Map<String, Object> result = PathMapping.transform(stepContext, rules);
				if (!CollectionUtils.isEmpty(result)) {
					// 从Step上下文拿到了值
					transformedVariables.putAll(result);
				}
			}

			variables = transformedVariables;
		}
		Map<String, Object> ctx = new HashMap<>(4);
		ctx.put("variables", variables);
		ctx.put("context", stepContext);

		try {
			return (boolean)ScriptUtils.execute(script, ctx);
		} catch (ScriptException e) {
			LOGGER.warn("execute script failed", e);
			throw new RuntimeException("execute script failed");
		}
    }

    @Override
	public Mono<Map> run() {
		this.doRequestMapping(config, inputContext);
		Mono<ClientResponse> clientResponse = this.getClientSpecFromContext(config, inputContext);
		return clientResponse.doOnSuccess(cr -> {
			HttpHeaders httpHeaders = cr.headers().asHttpHeaders();
			this.response.put("headers", httpHeaders);
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
