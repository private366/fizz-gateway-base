package com.wehotel.util;

/**
 * @author Lancer Hong
 */

public final class Constants {

	public static final class Symbol {
		public static final String EMPTY = "";
		public static final String SPACE_STR = " ";
		public static final char COMMA = ',';
		public static final char COLON = ':';
		public static final char BLANK = ' ';
		public static final char SPACE = BLANK;
		public static final char FORWARD_SLASH = '/';
		public static final char BACK_SLASH = '\\';
		public static final char DOT = '.';
		public static final char SEMICOLON = ';';
		public static final char DOUBLE_QUOTE = '"';
		public static final char SINGLE_QUOTE = '\'';
		public static final char ASTERISK = '*';
		public static final char DASH = '-';
		public static final char UNDERLINE = '_';
		public static final char EQUAL = '=';
		public static final char AT = '@';
		public static final char LEFT_SQUARE_BRACKET = '[';
		public static final char RIGHT_SQUARE_BRACKET = ']';
		public static final char LEFT_BRACE = '{';
		public static final char RIGHT_BRACE = '}';
		public static final String LINE_SEPARATOR = System.lineSeparator();

		public static final String COMMA_SPACE = ", ";
		public static final char LF = '\n';
		public static final char TAB = '\t';
		public static final char NUL = '\u0000';
	}

	public static final class Charset {
		public static final String UTF8 = "UTF-8";
		public static final String GBK = "GBK";
		public static final String ISO88591 = "ISO8859-1";
	}

	public static final class DatetimePattern {
		public static final String DP10 = "yyyy-MM-dd";
		public static final String DP14 = "yyyyMMddHHmmss";
		public static final String DP19 = "yyyy-MM-dd HH:mm:ss";
		public static final String DP23 = "yyyy-MM-dd HH:mm:ss.SSS";
		public static final byte MILLS_LEN = 13;
	}

	public static final class Profiles {
		public static final String LOCAL = "local";
		public static final String DEV = "dev";
		public static final String TEST = "test";
		public static final String PREPROD = "preprod";
		public static final String PROD = "prod";

		public static final String HTTP_SERVER = "http_server";
		public static final String HTTP_CLIENT = "http_client";
		public static final String MYSQL = "mysql";
		public static final String REDIS = "redis";
		public static final String CODIS = "codis";
		public static final String MONGO = "mongo";
		public static final String ACTIVEMQ = "activemq";
		public static final String KAFKA = "kafka";
		public static final String ELASTICSEARCH = "elasticsearch";
		public static final String SCHED = "sched";
	}

	public static final String BIZ_ID = "bizId";
}
