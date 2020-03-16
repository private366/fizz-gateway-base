package com.wehotel.fizz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.wehotel.filter.FizzLogFilter;
import com.wehotel.fizz.input.Input;
import com.wehotel.fizz.input.InputConfig;
import com.wehotel.fizz.input.InputContext;
import com.wehotel.fizz.input.PathMapping;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Pipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(FizzLogFilter.class);
	private LinkedList<Step> steps = new LinkedList<Step>();
	private Map<String, Object> stepContext= new HashMap<String, Object>();
	public void addStep(Step step) {
		steps.add(step);
	}
	
	static void displayValue(String n) {
	    System.out.println("input : " + n);
	}
	
	public Mono<?> run(Input input, Map<String, Object> clientInput) {
		this.initialStepContext(clientInput);
		LinkedList<Step> opSteps = (LinkedList<Step>) steps.clone();
		Step step1 = opSteps.removeFirst();
		step1.beforeRun(stepContext, null);
		Mono<?> result = createStep(step1).expand(response -> {
			if (opSteps.isEmpty() || response.getStep().isStop()) {
				return Mono.empty();
			}
			Step step = opSteps.pop();
			step.beforeRun(stepContext, response);
			return createStep(step);
        }).flatMap(response -> Flux.just(response)).collectList();
		return result.map(clientResponse -> {
			// 数据转换
			Map<String,Object> clientResult = this.doInputDataMapping(input);
			String jsonString = JSON.toJSONString(clientResult);
			LOGGER.info("jsonString '{}'", jsonString);
			LOGGER.info("stepContext '{}'", JSON.toJSONString(stepContext));
			
			return clientResult;
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
			Map<String, Map<String, String>> responseMapping = (Map<String, Map<String, String>>) step.getDataMapping().get("response");
			if(responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
				// body
				if(responseMapping.get("body") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext, responseMapping.get("body"));
					stepResponse.setResult(result);
				}
				
				// script
				if(responseMapping.get("script") != null) {
					// TODO
				}
			}
		}
		return stepResponse;
	}
	
	private Map<String,Object> doInputDataMapping(Input input) {
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
			Map<String, Map<String, String>> responseMapping = (Map<String, Map<String, String>>) input.getConfig().getDataMapping().get("response");
			if(responseMapping != null && !StringUtils.isEmpty(responseMapping)) {
				// headers
				if(responseMapping.get("headers") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext, responseMapping.get("headers"));
					response.put("headers", result);
				}
				
				// body
				if(responseMapping.get("body") != null) {
					Map<String, Object> result = PathMapping.transform(stepContext, responseMapping.get("body"));
					response.put("body", result);
				}
				
				// script
				if(responseMapping.get("script") != null) {
					// TODO
				}
			}
		}
		return response;
	}
}
