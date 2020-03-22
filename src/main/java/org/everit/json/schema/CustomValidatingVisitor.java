package org.everit.json.schema;

import org.everit.json.schema.event.ValidationListener;
import org.json.JSONObject;

/**
 * 自定义验证访问器
 * @author zhongjie
 */
public class CustomValidatingVisitor extends ValidatingVisitor {
    CustomValidatingVisitor(Object subject, ValidationFailureReporter failureReporter, ReadWriteValidator readWriteValidator, ValidationListener validationListener) {
        super(subject, failureReporter, readWriteValidator, validationListener);
    }

    @Override
    void visitNumberSchema(NumberSchema numberSchema) {
        if (subject instanceof String) {
            // input的是字符串类型，尝试转换成对应的数字类型
            subject = JSONObject.stringToValue((String) subject);
        }
        numberSchema.accept(new NumberSchemaValidatingVisitor(subject, this));
    }
}
