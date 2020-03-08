package com.wehotel.fizz.input;


public class InputConfig {
	
	private InputType type;
	protected String pathMapping;
	public InputType getType() {
		return type;
	}
	public void setType(InputType typeEnum) {
		this.type = typeEnum;
	}

	
	public void setPathMapping(String pathMapping) {
		this.pathMapping = pathMapping;
	}


	
	public String getPathMapping() {
		return pathMapping;
	}


}
