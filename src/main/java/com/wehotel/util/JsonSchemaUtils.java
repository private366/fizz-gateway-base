package com.wehotel.util;

import org.everit.json.schema.CustomValidator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
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
            CheckJsonResult checkJsonResult = checkJson(jsonSchema, inputJson);
            if (checkJsonResult.errorList != null) {
                return checkJsonResult.errorList;
            }
            checkJsonResult.schema.validate(checkJsonResult.json);
        } catch (ValidationException e) {
            return e.getAllMessages();
        }

        return null;
    }

    /**
     * 验证JSON字符串是否符合JSON Schema要求，允许数字\布尔类型 是字符串格式
     * @param jsonSchema JSON Schema
     * @param inputJson JSON字符串
     * @return null：验证通过，List：报错信息列表
     */
    public static List<String> validateAllowValueStr(String jsonSchema, String inputJson) {
        try {
            CheckJsonResult checkJsonResult = checkJson(jsonSchema, inputJson);
            if (checkJsonResult.errorList != null) {
                return checkJsonResult.errorList;
            }
            CustomValidator.build().performValidation(checkJsonResult.schema, checkJsonResult.json);
        } catch (ValidationException e) {
            return e.getAllMessages();
        }

        return null;
    }

    private static CheckJsonResult checkJson(String jsonSchema, String inputJson) {
        CheckJsonResult checkJsonResult = new CheckJsonResult();
        try {
            JSONObject rawSchema = new JSONObject(new JSONTokener(jsonSchema));
            checkJsonResult.schema = SchemaLoader.load(rawSchema);
        } catch (Exception e) {
            checkJsonResult.errorList = new ArrayList<>(1);
            checkJsonResult.errorList.add(String.format("JSON Schema格式错误，提示信息[%s]", e.getMessage()));
            return checkJsonResult;
        }

        // throws a ValidationException if this object is invalid
        try {
            checkJsonResult.json = new JSONObject(inputJson);
        } catch (Exception e) {
            List<String> errorList = new ArrayList<>(1);
            checkJsonResult.errorList.add(String.format("待验证JSON格式错误，提示信息[%s]", e.getMessage()));
            return checkJsonResult;
        }

        return checkJsonResult;
    }

    private static class CheckJsonResult {
        Schema schema;
        JSONObject json;
        List<String> errorList;
    }
}
