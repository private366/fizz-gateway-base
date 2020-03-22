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

@SuppressWarnings("unchecked")
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
		
		Map<String, Object> params = new HashMap<>();
		params.putAll(MapUtil.toHashMap(config.getQueryParams()));		
		request.put("params", params);
		
		// 数据转换
		if (inputContext != null && inputContext.getStepContext() != null) {
			Map<String, Object> stepContext = inputContext.getStepContext();
			Map<String, Object> dataMapping = this.getConfig().getDataMapping();
			if (dataMapping != null) {
				Map<String, Object> requestMapping = (Map<String, Object>) dataMapping.get("request");
				if(requestMapping != null && !StringUtils.isEmpty(requestMapping)) {
					// headers
					Map<String, Object> headers = new HashMap<>();
					if(requestMapping.get("fixedHeaders") != null) {
						headers.putAll((Map<String, Object>)requestMapping.get("fixedHeaders"));
					}
					if(requestMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, Object>) requestMapping.get("headers"));
						if(result != null) {
							headers.putAll(result);
						}
						Map<String, Object> scriptRules = PathMapping.getScriptRules((Map<String, Object>) requestMapping.get("headers"));
						Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
						if(scriptResult != null && !scriptResult.isEmpty()) {
							headers.putAll(scriptResult);
						}
					}
					request.put("headers", headers);
					
					// params
					if(requestMapping.get("fixedParams") != null) {
						params.putAll((Map<String, Object>)requestMapping.get("fixedParams"));
					}
					if(requestMapping.get("params") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, Object>) requestMapping.get("params"));
						if(result != null) {
							params.putAll(result);
						}
						Map<String, Object> scriptRules = PathMapping.getScriptRules((Map<String, Object>) requestMapping.get("params"));
						Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
						if(scriptResult != null && !scriptResult.isEmpty()) {
							params.putAll(scriptResult);
						}
					}
					request.put("params", params);
					
					// body
					Map<String, Object> body = new HashMap<>();
					if(requestMapping.get("fixedBody") != null) {
						body.putAll((Map<String, Object>)requestMapping.get("fixedBody"));
					}
					if(requestMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, Object>) requestMapping.get("body"));
						if(result != null) {
							body.putAll(result);
						}
						Map<String, Object> scriptRules = PathMapping.getScriptRules((Map<String, Object>) requestMapping.get("body"));
						Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
						if(scriptResult != null && !scriptResult.isEmpty()) {
							body.putAll(scriptResult);
						}
					}
					request.put("body", body);
					
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
			}
		}
		
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(config.getBaseUrl() + config.getPath())
				.queryParams(MapUtil.toMultiValueMap(params)).build();
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
					Map<String, Object> headers = (Map<String, Object>) response.get("headers");
					if(responseMapping.get("fixedHeaders") != null) {
						headers.putAll((Map<String, Object>)responseMapping.get("fixedHeaders"));
					}
					if(responseMapping.get("headers") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, Object>) responseMapping.get("headers"));
						if(result != null) {
							headers.putAll(result);
						}
						Map<String, Object> scriptRules = PathMapping.getScriptRules((Map<String, Object>) responseMapping.get("headers"));
						Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
						if(scriptResult != null && !scriptResult.isEmpty()) {
							headers.putAll(scriptResult);
						}
					}
					response.put("headers", headers);
					
					// body
					Map<String, Object> body = (Map<String, Object>) response.get("body");
					if(responseMapping.get("fixedBody") != null) {
						body.putAll((Map<String, Object>)responseMapping.get("fixedBody"));
					}
					if(responseMapping.get("body") != null) {
						Map<String, Object> result = PathMapping.transform(stepContext, (Map<String, Object>) responseMapping.get("body"));
						if(result != null) {
							body.putAll(result);
						}
						Map<String, Object> scriptRules = PathMapping.getScriptRules((Map<String, Object>) responseMapping.get("body"));
						Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
						if(scriptResult != null && !scriptResult.isEmpty()) {
							body.putAll(scriptResult);
						}
					}
					response.put("body", body);
					
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
		
		Map<String, Object> headers = (Map<String, Object>) request.get("headers");
		if(headers == null) {
			headers = new HashMap<>();
		}
		if (!headers.containsKey("Content-Type")) {
			//defalut content-type
			headers.put("Content-Type", "application/json; charset=UTF-8");
		}
		
		for(Entry<String, Object> entry : headers.entrySet()) {
			if (entry.getValue() != null) {
				if(entry.getValue() instanceof List) {
					List<Object> list = (List<Object>) entry.getValue();
					if(list.size() > 0) {
						uriSpec.header(entry.getKey(), list.toArray(new String[list.size()]));
					}
				}else if(entry.getValue() instanceof String) {
					uriSpec.header(entry.getKey(), (String) entry.getValue());
				}else {
					uriSpec.header(entry.getKey(), entry.getValue().toString());
				}
			}
		}
		
		BodyInserter<String, ReactiveHttpOutputMessage> body = BodyInserters
				.fromObject(JSON.toJSONString(request.get("body")));
		return uriSpec.body(body).exchange();
	}
	

	private Map<String, Object> getResponses(Map<String, StepResponse> stepContext2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
    public boolean needRun(Map<String, Object> stepContext) {
		Map<String, Object> condition = ((RequestInputConfig) config).getCondition();
		if (CollectionUtils.isEmpty(condition)) {
			// 没有配置condition，直接运行
			return Boolean.TRUE;
		}

		return (boolean) ScriptUtils.execute(condition, stepContext);
    }

    @Override
	public Mono<Map> run() {
		this.doRequestMapping(config, inputContext);
		Mono<ClientResponse> clientResponse = this.getClientSpecFromContext(config, inputContext);
		Mono<String> body = clientResponse.doOnSuccess(cr -> {
			HttpHeaders httpHeaders = cr.headers().asHttpHeaders();
			Map<String, Object> headers = new HashMap<>();
			httpHeaders.forEach((key,value)->{
				if(value.size() > 1) {
					headers.put(key, value);
				}else {
					headers.put(key, httpHeaders.getFirst(key));
				}
			});
			this.response.put("headers", headers);
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
