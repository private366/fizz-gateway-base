package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

public class ClientInputConfig extends InputConfig {
	
	private String path;
	private String method;
	private Map<String, Object> headers = new HashMap<String, Object>();
	private Map<String, Object> bodyDef;
    private Map<String, Object> headersDef;
    private Map<String, Object> paramsDef;
    private Map<String, Object> scriptValidate;

	@SuppressWarnings("unchecked")
	public ClientInputConfig(Map configBody) {
		this.path = (String) configBody.get("path");
		if (configBody.get("headers") != null) {
			setHeaders((Map) configBody.get("headers"));
		}
		if (configBody.get("method") != null) {
			setMethod((String) configBody.get("method"));
		} else {
			setMethod("GET");
		}

		if (configBody.get("bodyDef") != null) {
			bodyDef = ((Map) configBody.get("bodyDef"));
		}
        if (configBody.get("paramsDef") != null) {
            paramsDef = ((Map) configBody.get("paramsDef"));
        }
        if (configBody.get("headersDef") != null) {
            headersDef = ((Map) configBody.get("headersDef"));
        }
        if (configBody.get("scriptValidate") != null) {
            scriptValidate = ((Map) configBody.get("scriptValidate"));
        }
	}
	
	public ClientInputConfig() {
		
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, Object> getBodyDef() {
		return bodyDef;
	}

	public void setBodyDef(Map<String, Object> bodyDef) {
		this.bodyDef = bodyDef;
	}

    public Map<String, Object> getHeadersDef() {
        return headersDef;
    }

    public void setHeadersDef(Map<String, Object> headersDef) {
        this.headersDef = headersDef;
    }

    public Map<String, Object> getParamsDef() {
        return paramsDef;
    }

    public void setParamsDef(Map<String, Object> paramsDef) {
        this.paramsDef = paramsDef;
    }

    public Map<String, Object> getScriptValidate() {
        return scriptValidate;
    }

    public void setScriptValidate(Map<String, Object> scriptValidate) {
        this.scriptValidate = scriptValidate;
    }
}
