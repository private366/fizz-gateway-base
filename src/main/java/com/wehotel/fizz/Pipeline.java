package com.wehotel.fizz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.wehotel.fizz.input.ClientInputConfig;
import com.wehotel.fizz.input.InputConfig;
import com.wehotel.util.JsonSchemaUtils;
import com.wehotel.util.ScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.wehotel.fizz.input.Input;
import com.wehotel.fizz.input.PathMapping;
import com.wehotel.fizz.input.ScriptHelper;
import com.wehotel.util.MapUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Pipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);
	private LinkedList<Step> steps = new LinkedList<Step>();
	private Map<String, Object> stepContext= new HashMap<String, Object>();
	public void addStep(Step step) {
		steps.add(step);
	}
	
	static void displayValue(String n) {
	    System.out.println("input : " + n);
	}
	
	public Mono<AggregateResult> run(Input input, Map<String, Object> clientInput) {
		this.initialStepContext(clientInput);
		List<String> validateErrorList = inputVialdate(input, clientInput);
		if (!CollectionUtils.isEmpty(validateErrorList)) {
			// 入参验证失败
			// TODO by zhongjie 错误响应
			AggregateResult aggregateResult = new AggregateResult();
			Map<String, Object> body = new HashMap<>(2);
			body.put("msg", StringUtils.collectionToCommaDelimitedString(validateErrorList));
			aggregateResult.setBody(body);
			return Mono.just(aggregateResult);
		}

		LinkedList<Step> opSteps = (LinkedList<Step>) steps.clone();
		Step step1 = opSteps.removeFirst();
		step1.beforeRun(stepContext, null);
		Mono<List<StepResponse>> result = createStep(step1).expand(response -> {
			if (opSteps.isEmpty() || response.getStep().isStop()) {
				return Mono.empty();
			}
			Step step = opSteps.pop();
			step.beforeRun(stepContext, response);
			return createStep(step);
        }).flatMap(response -> Flux.just(response)).collectList();
		return result.map(clientResponse -> {
			// 数据转换
			AggregateResult aggResult = this.doInputDataMapping(input);
			String jsonString = JSON.toJSONString(aggResult);
			LOGGER.info("jsonString {}", jsonString);
			LOGGER.info("stepContext {}", JSON.toJSONString(stepContext));
			
			return aggResult;
		});
	}

	@SuppressWarnings("unchecked")
	public Mono<StepResponse> createStep(Step step) {
		List<Mono> monos = step.run();
		Mono<Map>[] monoArray = monos.stream().toArray(Mono[]::new);
		Mono<StepResponse>result = Flux.merge(monoArray).reduce(new HashMap(), (item1, item2)-> {
			Input input = (Input)item2.get("request");
			item1.put(input.getName() , item2.get("data"));
			return item1;
		}).flatMap(item -> {
			// stepResult 数据转换
			StepResponse stepResponse = this.doStepDataMapping(step);
			return Mono.just(stepResponse);
		});
		return result;
	}
	
	/**
	 * 初始化上下文
	 * @param clientInput 客户端提交上来的信息
	 */
	public void initialStepContext(Map<String,Object> clientInput) {
		Map<String,Object> input = new HashMap<>();
		Map<String,Object> inputRequest = new HashMap<>();
		Map<String,Object> inputResponse = new HashMap<>();
		input.put("request", inputRequest);
		input.put("response", inputResponse);
		if(clientInput != null) {
			inputRequest.put("url", clientInput.get("url"));
			inputRequest.put("method", clientInput.get("method"));
			inputRequest.put("headers", clientInput.get("headers"));
			inputRequest.put("params", clientInput.get("params"));
			inputRequest.put("body", clientInput.get("body"));
		}
		stepContext.put("input", input);
	}


	private StepResponse doStepDataMapping(Step step) {
		StepResponse stepResponse = (StepResponse)stepContext.get(step.getName());
		if (step.getDataMapping() != null) {
			Map<String, Object> responseMapping = (Map<String, Object>) step.getDataMapping().get("response");
			if(responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
				// body
				Map<String, Object> body = new HashMap<>();
				if(responseMapping.get("fixedBody") != null) {
					body.putAll((Map<String, Object>)responseMapping.get("fixedBody"));
				}
				if (responseMapping.get("body") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext,
							(Map<String, Object>) responseMapping.get("body"));
					if (result != null) {
						body.putAll(result);
					}
					Map<String, Object> scriptRules = PathMapping
							.getScriptRules((Map<String, Object>) responseMapping.get("body"));
					Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
					if (scriptResult != null && !scriptResult.isEmpty()) {
						body.putAll(scriptResult);
					}
				}
				stepResponse.setResult(body);
				
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
		return stepResponse;
	}
	
	private AggregateResult doInputDataMapping(Input input) {
		AggregateResult aggResult = new AggregateResult();
		Map<String, Map<String,Object>> group = (Map<String, Map<String, Object>>) stepContext.get("input");
		if(group == null) {
			group = new HashMap<String, Map<String,Object>>();
			stepContext.put("input", group);
		}
		Map<String,Object> response = null;
		if(group.get("response") == null) {
			response = new HashMap<>();
			group.put("response", response);
		}
		response = group.get("response");
		if (input != null && input.getConfig() != null && input.getConfig().getDataMapping() != null) {
			Map<String, Object> responseMapping = (Map<String, Object>) input.getConfig().getDataMapping()
					.get("response");
			if (responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
				// headers
				Map<String, Object> headers = new HashMap<>();
				if(responseMapping.get("fixedHeaders") != null) {
					headers.putAll((Map<String, Object>)responseMapping.get("fixedHeaders"));
				}
				if (responseMapping.get("headers") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext,
							(Map<String, Object>) responseMapping.get("headers"));
					if (result != null) {
						headers.putAll(result);
					}
					Map<String, Object> scriptRules = PathMapping
							.getScriptRules((Map<String, Object>) responseMapping.get("headers"));
					Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
					if (scriptResult != null && !scriptResult.isEmpty()) {
						headers.putAll(scriptResult);
					}
				}
				response.put("headers", headers);

				// body
				Map<String, Object> body = new HashMap<>();
				if (responseMapping.get("fixedBody") != null) {
					body.putAll((Map<String, Object>) responseMapping.get("fixedBody"));
				}
				if (responseMapping.get("body") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext,
							(Map<String, Object>) responseMapping.get("body"));
					if (result != null) {
						body.putAll(result);
					}
					Map<String, Object> scriptRules = PathMapping
							.getScriptRules((Map<String, Object>) responseMapping.get("body"));
					Map<String, Object> scriptResult = ScriptHelper.executeScripts(scriptRules, stepContext);
					if (scriptResult != null && !scriptResult.isEmpty()) {
						body.putAll(scriptResult);
					}
				}
				response.put("body", body);

				// script
				if (responseMapping.get("script") != null) {
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
		
		aggResult.setBody((Map<String, Object>) response.get("body"));
		aggResult.setHeaders(MapUtil.toMultiValueMap((Map<String, Object>) response.get("headers")));
		return aggResult;
	}

	@SuppressWarnings("unchecked")
	private List<String> inputVialdate(Input input, Map<String, Object> clientInput) {
		InputConfig config = input.getConfig();
		if (config instanceof ClientInputConfig) {
			Map<String, Object> headersDef = ((ClientInputConfig) config).getHeadersDef();
			if (!CollectionUtils.isEmpty(headersDef)) {
				// 验证headers入参是否符合要求
				List<String> errorList = JsonSchemaUtils.validateAllowNumberStr(JSON.toJSONString(headersDef), JSON.toJSONString(clientInput.get("headers")));
				if (!CollectionUtils.isEmpty(errorList)) {
					return errorList;
				}
			}

			Map<String, Object> paramsDef = ((ClientInputConfig) config).getParamsDef();
			if (!CollectionUtils.isEmpty(paramsDef)) {
				// 验证params入参是否符合要求
				List<String> errorList = JsonSchemaUtils.validateAllowNumberStr(JSON.toJSONString(paramsDef), JSON.toJSONString(clientInput.get("params")));
				if (!CollectionUtils.isEmpty(errorList)) {
					return errorList;
				}
			}

			Map<String, Object> bodyDef = ((ClientInputConfig) config).getBodyDef();
			if (!CollectionUtils.isEmpty(bodyDef)) {
				// 验证body入参是否符合要求
				List<String> errorList = JsonSchemaUtils.validate(JSON.toJSONString(bodyDef), JSON.toJSONString(clientInput.get("body")));
				if (!CollectionUtils.isEmpty(errorList)) {
					return errorList;
				}
			}

			Map<String, Object> scriptValidate = ((ClientInputConfig) config).getScriptValidate();
			if (!CollectionUtils.isEmpty(scriptValidate)) {
				// 验证入参是否符合脚本要求
				List<String> errorList = (List<String>) ScriptUtils.execute(scriptValidate, stepContext);
				if (!CollectionUtils.isEmpty(errorList)) {
					return errorList;
				}
			}
		}
		return null;
	}
}
