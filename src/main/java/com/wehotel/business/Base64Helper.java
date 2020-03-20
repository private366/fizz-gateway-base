package com.wehotel.business;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Base64.Decoder;

public class Base64Helper {
	@SuppressWarnings("restriction")
	public static String encode(byte[] byteArray) {
		Encoder base64Encoder = Base64.getEncoder();
		return base64Encoder.encodeToString(byteArray);
	}

	@SuppressWarnings("restriction")
	public static byte[] decode(String base64EncodedString) throws Exception {
		Decoder base64Decoder = Base64.getDecoder();
		return base64Decoder.decode(base64EncodedString);
	}
}
