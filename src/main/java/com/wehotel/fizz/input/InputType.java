package com.wehotel.fizz.input;

public enum InputType {
    REQUEST("REQUEST"),
    MYSQL("MYSQL");
	private final String type;
    private InputType(String aType) {
        this.type = aType;
    }
}