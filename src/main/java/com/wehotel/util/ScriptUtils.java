package com.wehotel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wehotel.fizz.input.PathMapping;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.script.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public abstract class ScriptUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptUtils.class);

    static final String JAVA_SCRIPT = "javascript";

    static final String GROOVY = "groovy";

    private static ScriptEngine groovyEngine;

    private static ScriptEngine javascriptEngine;

    private static final String jsFuncName = "dyFunc";

    private static final String clazz = "clazz";

    private static final String resJsonStr = "resJsonStr";

    static {
        ScriptEngineManager engineManger = new ScriptEngineManager();
        groovyEngine = engineManger.getEngineByName(GROOVY);
        javascriptEngine = engineManger.getEngineByName(JAVA_SCRIPT);
    }

    public static ScriptEngine getScriptEngine(String type) throws ScriptException {
        if (GROOVY.equals(type)) {
            return groovyEngine;
        } else if (JAVA_SCRIPT.equals(type)) {
            return javascriptEngine;
        } else {
            throw new ScriptException("unknown script engine type: " + type);
        }
    }

    public static Object execute(Script script) throws ScriptException {
        return execute(script, null);
    }

    public static Object execute(Script script, Map<String, Object> context) throws ScriptException {
        String type = script.getType();
        ScriptEngine engine = getScriptEngine(type);
        String src = script.getSource();
        if (GROOVY.equals(type)) {
            if (context == null) {
                return engine.eval(src);
            } else {
                Bindings bis = engine.createBindings();
                bis.putAll(context);
                return engine.eval(src, bis);
            }
        } else { // js
            engine.eval(src);
            Invocable invocable = (Invocable) engine;
            String paramsJsonStr = StringUtils.EMPTY;
            try {
                ObjectMapper mapper = JacksonUtils.getObjectMapper();
                if (context != null) {
                    paramsJsonStr = mapper.writeValueAsString(context);
                }
                ScriptObjectMirror som = (ScriptObjectMirror) invocable.invokeFunction(jsFuncName, paramsJsonStr);
                Class<?> clz = Class.forName(som.get(clazz).toString());
                return mapper.readValue(som.get(resJsonStr).toString(), clz);
            } catch (JsonProcessingException | NoSuchMethodException | ClassNotFoundException e) {
                throw new ScriptException(e);
            }
        }
    }



    /**
     * condition.variables中需要从StepContext获取变量值的前缀
     */
    private static final String STEP_CONTEXT_PARAM_PREFIX = "input ";

    @SuppressWarnings("unchecked")
    public static Object execute(Map<String, Object> scriptMap, Map<String, Object> stepContext) {
        Script script = new Script();
        script.setType((String) scriptMap.get("type"));
        script.setSource((String) scriptMap.get("source"));
        Map<String, Object> variables = (Map<String, Object>) scriptMap.get("variables");
        if (!CollectionUtils.isEmpty(variables)) {
            Map<String, Object> transformedVariables = new HashMap<>(variables.size());
            Map<String, Object> rules = new HashMap<>(variables.size());
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String && ((String) value).startsWith(STEP_CONTEXT_PARAM_PREFIX)) {
                    // 需要从Step上下文中获取参数
                    rules.put(key, ((String) value).substring(STEP_CONTEXT_PARAM_PREFIX.length()));
                } else {
                    transformedVariables.put(key, value);
                }
            }
            if (rules.size() > 0) {
                Map<String, Object> result = PathMapping.transform(stepContext, rules);
                if (!CollectionUtils.isEmpty(result)) {
                    // 从Step上下文拿到了值
                    transformedVariables.putAll(result);
                }
            }

            variables = transformedVariables;
        }
        Map<String, Object> ctx = new HashMap<>(4);
        ctx.put("variables", variables);
        ctx.put("context", stepContext);

        try {
            return ScriptUtils.execute(script, ctx);
        } catch (ScriptException e) {
            LOGGER.error("执行脚本异常", e);
            throw new RuntimeException("execute script failed");
        }
    }
}
