package com.wehotel.fizz.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.transformer.TransformationSpec;
import com.wehotel.util.MapUtil;

public class PathMapping {

	/**
	 * 创建jsonpath transform的规则，格式：<br>
	 * {"pathMappings": [{ "source": "$.step1.requests.request1.response.body",
	 * "target": "$.requestBody2" }] }
	 */
	public static String create(Map<String, String> rules) {

		if (rules.isEmpty()) {
			return null;
		}

		List<Map<String, String>> pathMappings = new ArrayList<>();
		for (Entry<String, String> entry : rules.entrySet()) {
			Map<String, String> m = new HashMap<>();
			m.put("source", "$." + entry.getValue());
			m.put("target", "$." + entry.getKey());
			pathMappings.add(m);
		}

		Map<String, Object> pm = new HashMap<>();
		pm.put("pathMappings", pathMappings);
		return JSON.toJSONString(pm);

	}

	public static Map<String, Object> transform(Map<String, Object> stepContext, Map<String, Object> rules) {
		if (rules.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, String> rs = new HashMap<>();
		Map<String, String> types = new HashMap<>();
		for (Entry<String, Object> entry : rules.entrySet()) {
			if (entry.getValue() instanceof String) {
				String val = (String) entry.getValue();
				String[] vals = val.split(" ");
				if (vals.length > 1) {
					rs.put(entry.getKey(), vals[1]);
					types.put(entry.getKey(), vals[0]);
				} else {
					rs.put(entry.getKey(), val);
				}
			}
		}

		if (rs.isEmpty()) {
			return new HashMap<>();
		}
		ObjectMapper mapper = new ObjectMapper();
		String data;
		try {
			data = mapper.writeValueAsString(stepContext);
			InputStream sourceStream = new ByteArrayInputStream(data.getBytes());
			String specJson = create(rs);
			InputStream transformSpec = new ByteArrayInputStream(specJson.getBytes());
			TransformationSpec spec;
			Object sourceJson;

			// assuming sourceStream, and transformSpec have been initialized
			Configuration configuration = Configuration.builder()
					.options(Option.CREATE_MISSING_PROPERTIES_ON_DEFINITE_PATH).build();
			sourceJson = configuration.jsonProvider().parse(sourceStream, Charset.defaultCharset().name());
			spec = configuration.transformationProvider().spec(transformSpec, configuration);
			Object transformed = configuration.transformationProvider().transform(sourceJson, spec, configuration);
			Map<String, Object> transformedResult = (Map<String, Object>) transformed;

			// convert type
			if (!types.isEmpty() && transformedResult != null && !transformedResult.isEmpty()) {
				for (Entry<String, String> entry : types.entrySet()) {
					if (transformedResult.get(entry.getKey()) != null) {
						Object value = transformedResult.get(entry.getKey());
						switch (entry.getValue()) {
						case "Integer":
						case "int": {
							MapUtil.set(transformedResult, entry.getKey(), Integer.valueOf((String) value));
							break;
						}
						case "Boolean":
						case "boolean": {
							MapUtil.set(transformedResult, entry.getKey(), Boolean.valueOf((String) value));
							break;
						}
						case "Float":
						case "float": {
							MapUtil.set(transformedResult, entry.getKey(), Float.valueOf((String) value));
							break;
						}
						case "Double":
						case "double": {
							MapUtil.set(transformedResult, entry.getKey(), Double.valueOf((String) value));
							break;
						}
						case "String": {
							MapUtil.set(transformedResult, entry.getKey(), String.valueOf(value));
							break;
						}
						case "Long":
						case "long": {
							MapUtil.set(transformedResult, entry.getKey(), Long.valueOf((String) value));
							break;
						}
						}
					}
				}
			}
			return transformedResult;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Map<String, Object> getScriptRules(Map<String, Object> rules) {
		if (rules.isEmpty()) {
			return new HashMap<>();
		}
		Map<String, Object> rs = new HashMap<>();
		for (Entry<String, Object> entry : rules.entrySet()) {
			if (!(entry.getValue() instanceof String)) {
				rs.put(entry.getKey(), entry.getValue());
			}
		}
		return rs;
	}

}
