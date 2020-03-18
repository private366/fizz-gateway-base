package com.wehotel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.StringUtils;

import javax.script.*;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public abstract class ScriptUtils {

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
}
