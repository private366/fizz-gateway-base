package org.everit.json.schema;

import org.everit.json.schema.event.ValidationListener;
import org.json.JSONObject;

/**
 * 自定义JSON验证器
 * @author zhongjie
 */
public class CustomValidator implements Validator {

    private boolean failEarly;

    private final ReadWriteContext readWriteContext;

    private final ValidationListener validationListener;

    CustomValidator(boolean failEarly, ReadWriteContext readWriteContext) {
        this(failEarly, readWriteContext, null);
    }

    CustomValidator(boolean failEarly, ReadWriteContext readWriteContext, ValidationListener validationListener) {
        this.failEarly = failEarly;
        this.readWriteContext = readWriteContext;
        this.validationListener = validationListener;
    }

    @Override public void performValidation(Schema schema, Object input) {
        if (schema instanceof NumberSchema && input instanceof String) {
            // input的是字符串类型，尝试转换成对应的数字类型
            input = JSONObject.stringToValue((String) input);
        }

        ValidationFailureReporter failureReporter = createFailureReporter(schema);
        ReadWriteValidator readWriteValidator = ReadWriteValidator.createForContext(readWriteContext, failureReporter);
        ValidatingVisitor visitor = new CustomValidatingVisitor(input, failureReporter, readWriteValidator, validationListener);
        try {
            visitor.visit(schema);
            visitor.failIfErrorFound();
        } catch (InternalValidationException e) {
            throw e.copy();
        }
    }

    private ValidationFailureReporter createFailureReporter(Schema schema) {
        if (failEarly) {
            return new EarlyFailingFailureReporter(schema);
        }
        return new CollectingFailureReporter(schema);
    }

    public static Validator build() {
        return new CustomValidator(false, null, ValidationListener.NOOP);
    }
}
