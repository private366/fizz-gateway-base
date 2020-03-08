package com.wehotel.fizz.input;

import java.util.Map;

public class InputFactory {
	public static InputConfig createInputConfig(Map config) {
		String type = (String) config.get("type");
		Map configBody = (Map)config.get("config");
		InputType typeEnum = InputType.valueOf(type.toUpperCase());
		InputConfig inputConfig = null;
		switch(typeEnum) {
			case REQUEST:
				inputConfig = new RequestInputConfig(configBody);
				
				break;
			case MYSQL:
				inputConfig = new MySQLInputConfig(configBody);
				break;
		}
		inputConfig.setType(typeEnum);
		inputConfig.setPathMapping((String)configBody.get("pathMapping"));
		
		return inputConfig;
	}
	
	public static Input createInput(String type) {
		InputType typeEnum = InputType.valueOf(type.toUpperCase());
		Input input = null;
		switch(typeEnum) {
			case REQUEST:
				input = new RequestInput();
				break;
			case MYSQL:
				input = new MySQLInput();
				break;
		}
		
		return input;
	}

}
