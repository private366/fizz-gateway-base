package com.wehotel.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wehotel.util.Constants.DatetimePattern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Lancer Hong
 */

public abstract class JacksonUtils {

	private static ObjectMapper m;

	static {
		JsonFactory f = new JsonFactory();
		f.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		f.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

		m = new ObjectMapper(f);

		m.setSerializationInclusion(Include.NON_EMPTY);
		m.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		m.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		m.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
		m.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true); // FIXME
		m.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
		m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		m.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

		SimpleModule m0 = new SimpleModule();
		m0.addDeserializer(Date.class, new DateDeseralizer());
		m.registerModule(m0);

		SimpleModule m1 = new SimpleModule();
		m1.addDeserializer(LocalDate.class, new LocalDateDeseralizer());
		m.registerModule(m1);

		SimpleModule m2 = new SimpleModule();
		m2.addDeserializer(LocalDateTime.class, new LocalDateTimeDeseralizer());
		m.registerModule(m2);

		SimpleModule m3 = new SimpleModule();
		m3.addSerializer(LocalDateTime.class, new LocalDateTimeSeralizer());
		m.registerModule(m3);
	}

	public static ObjectMapper getObjectMapper() {
		return m;
	}
}

class DateDeseralizer extends JsonDeserializer<Date> {

	public Date deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {

		String s = jp.getText();
		int sl = s.length();
		if (sl == DatetimePattern.MILLS_LEN) {
			return new Date(Long.parseLong(s));
		} else {
			String dtp = DatetimePattern.DP10;
			DateTimeFormatter dtf = null;
			if (sl == DatetimePattern.DP10.length()) {
			} else if (sl == DatetimePattern.DP14.length()) {
				dtp = DatetimePattern.DP14;
			} else if (sl == DatetimePattern.DP19.length()) {
				dtp = DatetimePattern.DP19;
			} else if (sl == DatetimePattern.DP23.length()) {
				dtp = DatetimePattern.DP23;
			} else {
				throw new IOException("invalid datetime pattern: " + s);
			}
			dtf = DateTimeUtils.getDateTimeFormatter(dtp);
			LocalDateTime ldt = LocalDateTime.parse(s, dtf);
			return DateTimeUtils.from(ldt);
		}
	}
}

class LocalDateDeseralizer extends JsonDeserializer<LocalDate> {

	public LocalDate deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {

		String s = jp.getText();
		if (s.length() == DatetimePattern.DP10.length()) {
			DateTimeFormatter dtf = DateTimeUtils.getDateTimeFormatter(DatetimePattern.DP10);
			return LocalDate.parse(s, dtf);
		} else {
			throw new IOException("invalid datetime pattern: " + s);
		}
	}
}

class LocalDateTimeDeseralizer extends JsonDeserializer<LocalDateTime> {

	public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {

		String s = jp.getText();
		int sl = s.length();
		if (sl == DatetimePattern.MILLS_LEN) {
			return DateTimeUtils.from(Long.parseLong(s));
		} else {
			String dtp = DatetimePattern.DP10;
			DateTimeFormatter dtf = null;
			if (sl == DatetimePattern.DP10.length()) {
			} else if (sl == DatetimePattern.DP14.length()) {
				dtp = DatetimePattern.DP14;
			} else if (sl == DatetimePattern.DP19.length()) {
				dtp = DatetimePattern.DP19;
			} else if (sl == DatetimePattern.DP23.length()) {
				dtp = DatetimePattern.DP23;
			} else {
				throw new IOException("invalid datetime pattern: " + s);
			}
			dtf = DateTimeUtils.getDateTimeFormatter(dtp);
			return LocalDateTime.parse(s, dtf);
		}
	}
}

class LocalDateTimeSeralizer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime ldt, JsonGenerator jg, SerializerProvider sp) throws IOException {
		jg.writeNumber(DateTimeUtils.toMillis(ldt));
	}
}
