package com.wehotel.fizz;

import java.net.MalformedURLException;
import java.net.URL;

public class RequestConfig {
	private URL url ;
	public String getBaseUrl() {
		return url.getProtocol()+ "://"+ url.getHost() + ":" + url.getPort();
	}

	public String getPath() {
		return url.getPath();
	}

	public void setUrl(String string) {
		// TODO Auto-generated method stub
		try {
			url = new URL(string);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
