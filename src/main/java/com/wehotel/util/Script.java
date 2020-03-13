package com.wehotel.util;

/**
 * @author Lancer Hong
 */

public class Script {

    private String type = ScriptUtils.GROOVY;

    private String source;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
