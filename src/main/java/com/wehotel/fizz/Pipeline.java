package com.wehotel.fizz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
}
