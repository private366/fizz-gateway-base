package com.wehotel.util;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public abstract class ScriptUtils {

    static final String JAVA_SCRIPT = "javascript"; // 不打算支持

    static final String GROOVY = "groovy";

    private static ScriptEngine groovyEngine;

    private static ScriptEngine javascriptEngine;

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
        ScriptEngine engine = getScriptEngine(script.getType());
        String src = script.getSource();
        if (context == null) {
            return engine.eval(src);
        } else {
            Bindings bis = engine.createBindings();
            bis.putAll(context);
            return engine.eval(src, bis);
        }
    }
}
