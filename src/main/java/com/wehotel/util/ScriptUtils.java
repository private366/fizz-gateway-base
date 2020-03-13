package com.wehotel.util;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public interface ScriptUtils {

    static final String JAVA_SCRIPT = "javascript";

    static final String GROOVY = "groovy";

    static Object execute(Script script) throws ScriptException {
        return execute(script, null);
    }

    static Object execute(Script script, Map<String, Object> context) throws ScriptException {
        ScriptEngineManager engineManger = new ScriptEngineManager();
        ScriptEngine engine = engineManger.getEngineByName(script.getType());
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
