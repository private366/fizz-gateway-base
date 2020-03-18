package com.wehotel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 */

public class ScriptUtilsTests {

    // 执行groovy脚本的例子
    @Test
    void test4groovyScript() {
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

    // 执行javascript脚本的例子，并有限制说明
    @Test
    void test4javaScript() throws JsonProcessingException, ScriptException {
        Script script = new Script();
        script.setType(ScriptUtils.JAVA_SCRIPT);
        script.setSource(
                // 脚本必须是一个函数，
                // 函数名固定为dyFunc，
                // 函数只有一个参数paramsJsonStr，它是一个json字符串，通过这样完成java数据及类型，到javascript的传递，例如下面的context，
                // 函数必须有返回值，值为javascript的Object类型，含resJsonStr属性，它代表需要传递给java代码的结果的json字符串形式，clazz属性代表resJsonStr对应的java类型
                "function dyFunc(paramsJsonStr) {" +
                "    if (paramsJsonStr) {" +
                "        var person = JSON.parse(paramsJsonStr)['person'];" +
                "        person['age'] = 24;" +
                "        person['sex'] = 'male';" +
                "        var resJsonStr = JSON.stringify(person);" +
                "        return {'clazz':'com.wehotel.util.Person','resJsonStr':resJsonStr};" +
                "    } " +
                "}"
        );
        Person person = new Person();
        person.setName("lancer");
        person.setAge(23);
        Map<String, Object> context = new HashMap<>();
        context.put("person", person);
        Person obj = (Person) ScriptUtils.execute(script, context);
        System.err.println(obj.getName() + ':' + obj.getAge());
    }
}
