package com.wehotel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

}
