package com.wehotel.fizz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.alibaba.fastjson.JSON;
import com.wehotel.filter.FizzLogFilter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Pipeline {
	private static final Logger LOGGER = LoggerFactory.getLogger(FizzLogFilter.class);
	private Stack<Step> steps = new Stack<Step>();
	private List<StepResponse> stepContext= new ArrayList<StepResponse>();
	public void addStep(Step step) {
		steps.push(step);
	}
	
	static void displayValue(String n) {
	    System.out.println("input : " + n);
	}
	
	public Mono<?> run() {
		Stack<Step> opSteps = (Stack<Step>) steps.clone();
		Step step1 = opSteps.pop();
		
		Mono<?> result = createStep(step1).expand(response -> {
			if (opSteps.isEmpty()) {
				return Mono.empty();
			}
			Step step = opSteps.pop();
			step.loadContext(stepContext, response);
			return createStep(step);
        }).flatMap(response -> Flux.just(response)).collectList();
		return result.map(clientResponse -> {
			String jsonString = JSON.toJSONString(clientResponse);
			LOGGER.info("jsonString '{}'", jsonString);
			return clientResponse;
		});
	}
	private Integer i = 0;
	@SuppressWarnings("unchecked")
	public Mono<StepResponse> createStep(Step step) {
		List<Mono> monos = step.configureMonos();
		Mono<String>[] monoArray = monos.stream().toArray(Mono[]::new);
		Mono<StepResponse>result = Flux.merge(monoArray).reduce(new HashMap(), (item1, item2)-> {
			item1.put(step.getName() , item2);
			return item1;
		}).flatMap(item -> {
			StepResponse response = new StepResponse(step, item);
			stepContext.add(response);
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
