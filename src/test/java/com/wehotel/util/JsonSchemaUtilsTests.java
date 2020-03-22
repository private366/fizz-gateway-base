package com.wehotel.util;

import com.alibaba.fastjson.JSON;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    void customValidTest() {
        Map<String, Object> params = new HashMap<>(10);
        params.put("key1", "value1");
        params.put("key2", Lists.newArrayList("21", "22.123", "23"));
        params.put("key3", "3");

        /*
        {
            "type" : "object",
            "properties" : {
                "key1" : {
                            "type" : "string"
                        },
                "key2" : {
                            "type" : "array",
                            "items" : {
                                "type" : "number"
                            }
                        },
                "key3" : {
                            "type" : "integer"
                        }
            },
            "required" : ["key1" , "key2", "key3"]
        }
        */
        List<String> validateResult = JsonSchemaUtils.validateAllowNumberStr("{\n" +
                "            \"type\" : \"object\",\n" +
                "            \"properties\" : {\n" +
                "                \"key1\" : {\n" +
                "                            \"type\" : \"string\"\n" +
                "                        },\n" +
                "                \"key2\" : {\n" +
                "                            \"type\" : \"array\",\n" +
                "                            \"items\" : {\n" +
                "                                \"type\" : \"number\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                \"key3\" : {\n" +
                "                            \"type\" : \"integer\"\n" +
                "                        }\n" +
                "            },\n" +
                "            \"required\" : [\"key1\" , \"key2\", \"key3\"]\n" +
                "        }", JSON.toJSONString(params));
        assertNull(validateResult);
    }
}
