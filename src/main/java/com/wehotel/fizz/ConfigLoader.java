package com.wehotel.fizz;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.wehotel.fizz.input.ClientInputConfig;
import com.wehotel.fizz.input.Input;
import com.wehotel.fizz.input.InputType;

public class ConfigLoader {

	private static Map<String, AggregateResource> aggregateResources = null;

	private static DocumentContext parseConfig(File file) throws IOException {
		if (!file.exists()) {
			throw new IOException("File not found");
		}
		String configStr = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
		Configuration conf = Configuration.builder().options(Option.CREATE_MISSING_PROPERTIES_ON_DEFINITE_PATH)
				.options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
		return JsonPath.using(conf).parse(configStr);
	}

	public static Input createInput(File file) throws IOException {
		DocumentContext docCtx = parseConfig(file);

		Input input = new Input();
		input.setName(docCtx.read("$.name"));

		ClientInputConfig clientInputConfig = new ClientInputConfig();
		clientInputConfig.setDataMapping(docCtx.read("$.config.dataMapping"));
		clientInputConfig.setHeaders(docCtx.read("$.config.headers"));
		clientInputConfig.setMethod(docCtx.read("$.config.method"));
		clientInputConfig.setPath(docCtx.read("$.config.path"));
		clientInputConfig.setType(InputType.valueOf(docCtx.read("$.config.type")));
		clientInputConfig.setBodyDef(docCtx.read("$.config.bodyDef"));
		clientInputConfig.setHeadersDef(docCtx.read("$.config.headersDef"));
		clientInputConfig.setParamsDef(docCtx.read("$.config.paramsDef"));
		clientInputConfig.setScriptValidate(docCtx.read("$.config.scriptValidate"));
		clientInputConfig.setValidateResponse(docCtx.read("$.config.validateResponse"));
		input.setConfig(clientInputConfig);
		return input;
	}

	public static Pipeline createPipeline(File file) throws IOException {
		DocumentContext docCtx = parseConfig(file);
		Pipeline pipeline = new Pipeline();

		List<Map<String, Object>> stepConfigs = docCtx.read("$.config.stepConfigs");
		for (Map<String, Object> stepConfig : stepConfigs) {
			Step step = new Step.Builder().read(stepConfig);
			step.setName((String) stepConfig.get("name"));
			if (stepConfig.get("stop") != null) {
				step.setStop((Boolean) stepConfig.get("stop"));
			} else {
				step.setStop(false);
			}
			step.setDataMapping((Map<String, Object>) stepConfig.get("dataMapping"));
			pipeline.addStep(step);
		}

		return pipeline;
	}

	public static synchronized void init() {
		if (aggregateResources == null) {
			aggregateResources = new HashMap<>();
		}

		File dir = new File("json");
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					Input input = null;
					Pipeline pipeline = null;
					try {
						input = createInput(file);
						pipeline = createPipeline(file);
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if (pipeline != null && input != null) {
						ClientInputConfig cfg = (ClientInputConfig) input.getConfig();
						AggregateResource aggregateResource = new AggregateResource(pipeline, input);
						aggregateResources.put(cfg.getMethod().toUpperCase() + ":" + cfg.getPath(), aggregateResource);
					}
				}
			}
		}
	}

	public static AggregateResource matchAggregateResource(String method, String path) {
		if (aggregateResources == null) {
			init();
		}
		return aggregateResources.get(method.toUpperCase() + ":" + path);
	}

}
