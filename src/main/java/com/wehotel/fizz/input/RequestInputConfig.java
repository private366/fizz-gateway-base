package com.wehotel.fizz.input;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class RequestInputConfig extends InputConfig{
	private URL url ;
	public RequestInputConfig(Map configBody) {
		String url = (String) configBody.get("url");
		setUrl(url);
	}

	public String getBaseUrl() {
		return url.getProtocol()+ "://"+ url.getHost() + ":" + url.getPort();
	}

	public String getPath() {
		return url.getPath();
	}

	public void setUrl(String string) {
		try {
			url = new URL(string);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
