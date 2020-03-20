package com.wehotel.fizz.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wehotel.util.MapUtil;
import com.wehotel.util.Script;
import com.wehotel.util.ScriptUtils;

public class ScriptHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptHelper.class);

	public static Object execute(Map<String, Object> scriptCfg, Map<String, Object> stepContext)
			throws ScriptException {
		Script script = new Script();
		script.setType((String) scriptCfg.get("type"));
		script.setSource((String) scriptCfg.get("source"));
		if (script.getSource() == null || script.getSource() == "") {
			return null;
		}

		Map<String, Object> variables = (Map<String, Object>) scriptCfg.get("variables");
		Map<String, Object> ctx = new HashMap<>();
		ctx.put("variables", variables);
		ctx.put("context", stepContext);
		return ScriptUtils.execute(script, ctx);
	}

	public static Map<String, Object> executeScripts(Map<String, Object> scriptRules, Map<String, Object> stepContext) {
		Map<String, Object> result = new HashMap<>();
		if(scriptRules != null && !scriptRules.isEmpty()) {
			for (Entry<String, Object> entry : scriptRules.entrySet()) {
				Map<String, Object> scriptCfg = (Map<String, Object>) entry.getValue();
				try {
					result.put(entry.getKey(), execute(scriptCfg, stepContext));
				} catch (ScriptException e) {
					LOGGER.warn("execute script failed, {}", e);
					throw new RuntimeException("execute script failed");
				}
			}
		}
		return result;
	}

}
