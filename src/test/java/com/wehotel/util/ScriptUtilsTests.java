package com.wehotel.util;

import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public class ScriptUtilsTests {

    // ScriptUtils 应用的例子
    @Test
    void test() {
        Script script = new Script();
        script.setSource(
                "import com.wehotel.util.DateTimeUtils; " +
                "List<String> datesBetween38and311 = DateTimeUtils.datesBetween(\"2020-03-08\", \"2020-03-11\"); " +
                "System.err.println(datesBetween38and311); " +
                "System.err.println(nowFromJava.toString()); " +
                "def getTime(){return now.getTime();}; " + // 这行是 groovy 代码，其它都是纯 java 代码
                "Map<String, String> hm = new HashMap<>(); " +
                "hm.put(\"1\", \"a\"); " +
                "return hm;"
        );
        try {
            Map<String, Object> context = new HashMap<>();
            Date now = new Date();
            context.put("nowFromJava", now);
            Map<String, String> hmFromGroovy = (Map<String, String>) ScriptUtils.execute(script, context);
            System.err.println("hmFromGroovy: " + hmFromGroovy);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
