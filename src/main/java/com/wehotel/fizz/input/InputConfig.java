package com.wehotel.fizz.input;


public class InputConfig {
	
	private InputType type;
	protected DataMapping dataMapping;
	public InputType getType() {
		return type;
	}
	public void setType(InputType typeEnum) {
		this.type = typeEnum;
	}
	
	public DataMapping getDataMapping() {
		return dataMapping;
	}
	
	public void setDataMapping(DataMapping dataMapping) {
		this.dataMapping = dataMapping;
	}

}
