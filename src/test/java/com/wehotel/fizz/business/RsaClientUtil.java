package com.wehotel.fizz.business;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

@SuppressWarnings("restriction")
public class RsaClientUtil {
	//PUBLICKEY
	public static final String key2 = "<RSAKeyValue><Modulus>sYbLZypg1hW4fyKx7Y5OVEAU1oR7oqZTi9KktaZX+L+TRow88zeRjyjEqE7Ros5NThY34OVyNjjHeEPFKA62iZjclYU7dIvAQyoy6qzWTDs9fLfIbCa36H6QMSSg/8zWeJ7U7u3940KGalup7F9fK4cRfdy+ecIyq9EMPGEQ0lM=</Modulus><Exponent>AQAB</Exponent></RSAKeyValue>";

	private static PublicKey publicKey; // Public Key Class
	private static String publicKeyStr;

	/**
	 * 将公钥转换为XML字符串
	 * 
	 * @param key
	 * @return
	 */
	public static String encodePublicKeyToXml(PublicKey key) {
		if (!RSAPublicKey.class.isInstance(key)) {
			return null;
		}
		RSAPublicKey pubKey = (RSAPublicKey) key;
		  Encoder encoder = Base64.getEncoder();
		StringBuilder sb = new StringBuilder();
		sb.append("<RSAKeyValue>")
				.append("<Modulus>")
				.append(encoder.encodeToString(pubKey.getModulus().toByteArray()))
				.append("</Modulus>")
				.append("<Exponent>")
				.append(encoder.encodeToString(pubKey.getPublicExponent().toByteArray()))
				.append("</Exponent>");
		sb.append("</RSAKeyValue>");
		return sb.toString();

	}
	
	/**
	 * 生成pem文件，提供给譬如调用方使用其他语言：.net、golang等，可以使用此方法生成pem文件供其加密
	 * @throws Exception
	 */
	public static void genPemPublicKey() throws Exception {
		System.out.println("-----BEGIN PUBLIC KEY-----");
		System.out.println(Base64Helper.encode(RsaClientUtil.decodePublicKeyFromXml(key2).getEncoded()));
		System.out.println("-----END PUBLIC KEY-----");
	}
	
	/**
	 * 从XML字符串得到公钥
	 * 
	 * @param xml
	 * @return
	 */
	public static PublicKey decodePublicKeyFromXml(String xml)throws Exception{
		xml = xml.replaceAll("\r", "").replaceAll("\n", "");
		BigInteger modulus = new BigInteger(1, Base64Helper.decode(StringUtils
				.substringBetween(xml, "<Modulus>", "</Modulus>")));
		BigInteger publicExponent = new BigInteger(1,
				Base64Helper.decode(StringUtils.substringBetween(xml,
						"<Exponent>", "</Exponent>")));
		RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulus,
				publicExponent);
		KeyFactory keyf;
		keyf = KeyFactory.getInstance("RSA");
		return keyf.generatePublic(rsaPubKey);
	}


	// 用公钥加密

	public static byte[] encryptData(byte[] data, PublicKey pubKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 得到公钥
	 * 
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws Exception
	 */
	private static PublicKey getPublicKey(String key) throws Exception {
		byte[] keyBytes;
		Decoder encoder = Base64.getDecoder();
		keyBytes = encoder.decode(key);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	// 用公钥加密
	public static String encodeNet(String data) throws Exception {
		if (data == null) {
			return null;
		}
		try {
			if (publicKey == null) {
				String pubKeyXml = key2;
				publicKey = decodePublicKeyFromXml(pubKeyXml);
			}
			if (publicKeyStr == null) {
				publicKeyStr = getKeyString(publicKey);
			}
			return encode(publicKeyStr, data).replaceAll("\r", "").replaceAll("\n", "");
			// return Base64Helper.encode(encryptData(Base64Helper.decode(data),
			// publicKey));
		} catch (Exception e) {
			throw new Exception("加密失败：" + e.getMessage());
		}
	}

	// 用公钥加密
	public static String encode(String pubKey, String data) {
		try {
			if (publicKey == null)
				publicKey = getPublicKey(pubKey);
			if (publicKeyStr == null) {
				publicKeyStr = getKeyString(publicKey);
			}
			return Base64Helper.encode(encryptData(data.getBytes(), publicKey));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 得到密钥字符串（经过base64编码）
	 * 
	 * @return
	 */
	private static String getKeyString(Key key) throws Exception {
		Encoder encoder = Base64.getEncoder();
		byte[] keyBytes = key.getEncoded();
		String s = encoder.encodeToString(keyBytes);
		return s;
	}

}
