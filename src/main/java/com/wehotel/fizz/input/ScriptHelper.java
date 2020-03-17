package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import com.wehotel.util.Script;
import com.wehotel.util.ScriptUtils;

public class ScriptHelper {

	public static Object execute(Map<String, Object> scriptCfg, Map<String, Object> stepContext) throws ScriptException {
		Script script = new Script();
		script.setType((String) scriptCfg.get("type"));
		script.setSource((String) scriptCfg.get("source"));
		Map<String, Object> variables = (Map<String, Object>) scriptCfg.get("variables");
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("variables", variables);
		ctx.put("context", stepContext);
		return ScriptUtils.execute(script, ctx);
	}

}
