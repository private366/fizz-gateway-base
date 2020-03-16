package com.wehotel.util;

import java.util.Map;
import java.util.Map.Entry;

public class ParamUtil {

	public static String toQueryString(Map<String, Object> params) {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (Entry<String, Object> entry : params.entrySet()) {
			if (entry.getValue() == null || "".equals(entry.getValue())) {
				continue;
			}
			if (i == 0) {
				sb.append(entry.getKey()).append("=").append(entry.getValue());
			} else {
				sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
			}
			i++;
		}
		return sb.toString();
	}

}
