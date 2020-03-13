package com.wehotel.fizz.input;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.transformer.TransformationSpec;

public class PathMapping {

	/**
	 * 创建jsonpath transform的规则，格式：<br>
	 *  {"pathMappings": [{
			"source": "$.step1.requests.request1.response.body",
			"target": "$.requestBody2"
			}]
		}
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

	public static Map<String, Object> transform(Map<String, Object> stepContext, Map<String, String> rules) {
		ObjectMapper mapper = new ObjectMapper();
		String data;
		try {
			data = mapper.writeValueAsString(stepContext);
			InputStream sourceStream = new ByteArrayInputStream(data.getBytes());
			String specJson = create(rules);
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
			return transformedResult;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
