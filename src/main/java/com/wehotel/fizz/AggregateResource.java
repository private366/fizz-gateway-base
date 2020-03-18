package com.wehotel.fizz;

import com.wehotel.fizz.input.Input;

public class AggregateResource {

	private Pipeline pipeline;
	private Input input;

	public AggregateResource(Pipeline pipeline, Input input) {
		super();
		this.pipeline = pipeline;
		this.input = input;
	}
	
	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public Input getInput() {
		return input;
	}

	public void setInput(Input input) {
		this.input = input;
	}

}
