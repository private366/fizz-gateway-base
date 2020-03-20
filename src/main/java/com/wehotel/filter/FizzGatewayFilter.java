package com.wehotel.filter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wehotel.fizz.input.ClientInputConfig;
import com.wehotel.fizz.input.InputConfig;
import com.wehotel.util.JsonSchemaUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wehotel.fizz.AggregateResource;
import com.wehotel.fizz.AggregateResult;
import com.wehotel.fizz.ConfigLoader;
import com.wehotel.fizz.Pipeline;
import com.wehotel.fizz.input.Input;
import com.wehotel.util.MapUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(2)
public class FizzGatewayFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		ServerHttpResponse serverHttpResponse = exchange.getResponse();

		String path = request.getURI().getPath();
		String method = request.getMethodValue();
		AggregateResource aggregateResource = ConfigLoader.matchAggregateResource(method, path);
		if (aggregateResource == null) {
			return chain.filter(exchange);
		}

		Pipeline pipeline = aggregateResource.getPipeline();
		Input input = aggregateResource.getInput();

		// 客户端提交上来的信息
		Map<String, Object> clientInput = new HashMap<>();
		clientInput.put("url", path);
		clientInput.put("method", method);
		clientInput.put("headers", MapUtil.toHashMap(request.getHeaders()));
		clientInput.put("params", MapUtil.toHashMap(request.getQueryParams()));

		long len = request.getHeaders().getContentLength();
		Mono<AggregateResult> result = null;
		if (len > 0 && HttpMethod.POST.name().equalsIgnoreCase(method)) {
			result = DataBufferUtils.join(request.getBody()).flatMap(buf -> {
				String requestBody = buf.toString(StandardCharsets.UTF_8);
				InputConfig config = input.getConfig();
				if (config instanceof ClientInputConfig) {
					Map<String, Object> requestBodySchema = ((ClientInputConfig) config).getRequestBodySchema();
					if (!CollectionUtils.isEmpty(requestBodySchema)) {
						// 验证入参是否符合要求
						List<String> valiateResult =
								JsonSchemaUtils.validate(JSON.toJSONString(requestBodySchema), requestBody);
						if (valiateResult != null) {
							// 验证不通过
							AggregateResult aggregateResult = new AggregateResult();
							Map<String, Object> rsp = new HashMap<>(4);
							// TODO 规范化错误响应
							rsp.put("code", -1);
							rsp.put("msg", valiateResult);
							aggregateResult.setBody(rsp);
							return Mono.just(aggregateResult);
						}
					}
				}

				clientInput.put("body", JSON.parse(requestBody));
				return pipeline.run(input, clientInput);
			});
		} else {
			result = pipeline.run(input, clientInput);
		}
		return result.flatMap(aggResult -> {
			String jsonString = JSON.toJSONString(aggResult.getBody());
			if(aggResult.getHeaders() != null && !aggResult.getHeaders().isEmpty()) {
				serverHttpResponse.getHeaders().addAll(aggResult.getHeaders());
			}
			if (!serverHttpResponse.getHeaders().containsKey("Content-Type")) {
				//defalut content-type
				serverHttpResponse.getHeaders().add("Content-Type", "application/json; charset=UTF-8");
			}
			return serverHttpResponse
					.writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(jsonString.getBytes())));
		});

	}

}