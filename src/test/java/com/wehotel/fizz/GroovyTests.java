package com.wehotel.fizz;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
public class GroovyTests {

	@Test
	void contextLoads() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");
		String sum;
		Map<String, String> config = new HashMap<String, String>();
		config.put("appid","1140");
		config.put("token","859EE0413BC3F88F1F89C162800FB056");
		try {
			engine.put("config", config);
			sum = (String) engine.eval("import com.wehotel.fizz.business.RsaClientUtil; RsaClientUtil.encodeNet(config.get(\"token\")+\",\"+config.get(\"appid\")+\",\"+System.currentTimeMillis() )");
			System.out.print(sum);
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
