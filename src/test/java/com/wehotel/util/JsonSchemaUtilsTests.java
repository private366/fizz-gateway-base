package com.wehotel.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonSchemaUtilsTests {
    private static final String USER_ID_NEED_EXIST_JSON_SCHEMA =
            "{" +
            "   \"type\":\"object\",\n" +
            "   \"properties\":{\n" +
            "       \"userId\":{\n" +
            "           \"type\":\"string\",\n" +
            "           \"title\":\"用户名\",\n" +
            "           \"description\":\"描述\"\n" +
            "       }\n" +
            "   },\n" +
            "   \"required\": [\"userId\"]" +
            "}";

    @Test
    void validateTest() {
        List<String> validateResult1 = JsonSchemaUtils.validate(USER_ID_NEED_EXIST_JSON_SCHEMA, "{\"userId\" : \"aaaaabbbbb\"}");
        assertNull(validateResult1);
        List<String> validateResult2 = JsonSchemaUtils.validate(USER_ID_NEED_EXIST_JSON_SCHEMA, "{\"userId\" : null}");
        assertNotNull(validateResult2);
        List<String> validateResult3 = JsonSchemaUtils.validate(USER_ID_NEED_EXIST_JSON_SCHEMA, "{}");
        assertNotNull(validateResult3);
    }
}
