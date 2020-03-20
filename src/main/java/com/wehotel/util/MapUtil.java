package com.wehotel.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.fastjson.JSON;


public class MapUtil {

	public static MultiValueMap<String, String> toMultiValueMap(Map<String, Object> params) {
		MultiValueMap<String, String> mvmap = new LinkedMultiValueMap<>();

		if (params.isEmpty()) {
			return mvmap;
		}

		for (Entry<String, Object> entry : params.entrySet()) {
			Object val = entry.getValue();
			List<String> list = new ArrayList<>();
			if (val instanceof List) {
				List<Object> vals = (List<Object>) val;
				for (Object value : vals) {
					if (value != null) {
						list.add(value.toString());
					}
				}
			} else {
				if (val != null) {
					list.add(val.toString());
				}
			}
			if (list.size() > 0) {
				mvmap.put(entry.getKey(), list);
			}
		}

		return mvmap;
	}
	
	public static Map<String, Object> toHashMap(MultiValueMap<String, String> params) {
		HashMap<String, Object> m = new HashMap<>();

		if (params.isEmpty()) {
			return m;
		}

		for (Entry<String, List<String>> entry : params.entrySet()) {
			List<String> val = entry.getValue();
			if (val != null && val.size() > 0) {
				if (val.size() > 1) {
					m.put(entry.getKey(), val);
				} else {
					m.put(entry.getKey(), val.get(0));
				}
			}
		}

		return m;
	}

	/**
	 * Set value by path，support multiple levels，eg：a.b.c <br>
	 * Do NOT use this method if field name contains a dot <br>
	 * @param data 
	 * @param path
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public static void set(Map<String, Object> data, String path, Object value) {
		String[] fields = path.split("\\.");
		if(fields.length < 2) {
			data.put(path, value);
		}else {
			Map<String, Object> next = data;
			for (int i = 0; i < fields.length - 1; i++) {
				Map<String, Object> val = (Map<String, Object>) next.get(fields[i]);
				if(val == null) {
					val = new HashMap<>();
					next.put(fields[i], val);
				}
				if(i == fields.length - 2) {
					val.put(fields[i+1], value);
					break;
				}
				next = val;
			}
		}
	}
	
	/**
	 * Get value by path, support multiple levels，eg：a.b.c <br>
	 * Do NOT use this method if field name contains a dot <br>
	 * @param data
	 * @param path
	 * @return
	 */
	public static Object get(Map<String, Object> data, String path) {
		String[] fields = path.split("\\.");
		if(fields.length < 2) {
			return data.get(path);
		}else {
			Map<String, Object> next = data;
			for (int i = 0; i < fields.length - 1; i++) {
				if(!(next.get(fields[i]) instanceof Map)) {
					return null;
				}
				Map<String, Object> val = (Map<String, Object>) next.get(fields[i]);
				if(val == null) {
					return null;
				}
				if(i == fields.length - 2) {
					return val.get(fields[i+1]);
				}
				next = val;
			}
		}
		return null;
	}
	
}
