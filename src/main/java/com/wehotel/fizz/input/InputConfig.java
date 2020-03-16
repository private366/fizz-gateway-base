package com.wehotel.fizz.input;

import java.util.Map;

public class InputConfig {

	private InputType type;
	protected Map<String, Object> dataMapping;

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

}
