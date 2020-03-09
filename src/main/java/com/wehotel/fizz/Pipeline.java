package com.wehotel.fizz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.wehotel.filter.FizzLogFilter;
import com.wehotel.fizz.input.Input;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Pipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(FizzLogFilter.class);
	private LinkedList<Step> steps = new LinkedList<Step>();
	private Map<String, StepResponse> stepContext= new HashMap<String, StepResponse>();
	public void addStep(Step step) {
		steps.add(step);
	}
	
	static void displayValue(String n) {
	    System.out.println("input : " + n);
	}
	
	public Mono<?> run(Map<String, Object> request) {
		LinkedList<Step> opSteps = (LinkedList<Step>) steps.clone();
		Step step1 = opSteps.removeFirst();
		step1.beforeRun(null, null);
		Mono<?> result = createStep(step1).expand(response -> {
			if (opSteps.isEmpty()) {
				return Mono.empty();
			}
			Step step = opSteps.pop();
			step.beforeRun(stepContext, response);
			return createStep(step);
        }).flatMap(response -> Flux.just(response)).collectList();
		return result.map(clientResponse -> {
			String jsonString = JSON.toJSONString(clientResponse);
			LOGGER.info("jsonString '{}'", jsonString);
			return clientResponse;
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
			StepResponse response = new StepResponse(step, item);
			stepContext.put(step.getName(), response);
			return Mono.just(response);
		});
		return result;
//		Mono<Map>result = Mono.zip().flatMap(tuple->{
//			String userInfo = tuple.getT1();
//			String userInfo1 = tuple.getT1();
//			Map<String, String> data = new HashMap<String, String>();
//			data.put("userInfo", userInfo);
//			data.put("userInfo1", userInfo1);
//			return Mono.just(data);
//		});
//		return result;
	}


}
