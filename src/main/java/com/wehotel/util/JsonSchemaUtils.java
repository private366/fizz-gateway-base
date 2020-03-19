package com.wehotel.util;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Schema工具类
 * @author zhongjie
 */
public class JsonSchemaUtils {
    private JsonSchemaUtils() {}

    /**
     * 验证JSON字符串是否符合JSON Schema要求
     * @param jsonSchema JSON Schema
     * @param inputJson JSON字符串
     * @return null：验证通过，List：报错信息列表
     */
    public static List<String> validate(String jsonSchema, String inputJson) {
        try {
            Schema schema;
            try {
                JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
                schema = SchemaLoader.load(rawSchema);
            } catch (JSONException e) {
                List<String> errorList = new ArrayList<>(1);
                errorList.add(String.format("JSON Schema格式错误，提示信息[%s]", e.getMessage()));
                return errorList;
            }

            // throws a ValidationException if this object is invalid
            JSONObject json;
            try {
                json = new JSONObject(inputJson);
            } catch (JSONException e) {
                List<String> errorList = new ArrayList<>(1);
                errorList.add(String.format("待验证JSON格式错误，提示信息[%s]", e.getMessage()));
                return errorList;
            }

            schema.validate(json);
        } catch (ValidationException e) {
            return e.getAllMessages();
        }

        return null;
    }
}
