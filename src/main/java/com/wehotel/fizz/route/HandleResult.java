package com.wehotel.fizz.route;

import java.util.Map;

/**
 * @author Lancer Hong
 */

public class HandleResult {

    public boolean success;

    // public int code ?

    public Throwable cause;

    public Map<String, ?> data;

    public static final HandleResult SUCCESS() {
        HandleResult r = new HandleResult();
        r.success = true;
        return r;
    }

    public static final HandleResult SUCCESS_WITH(Map<String, ?> data) {
        HandleResult r = new HandleResult();
        r.success = true;
        r.data = data;
        return r;
    }

    public static final HandleResult FAIL() {
        HandleResult r = new HandleResult();
        r.success = false;
        return r;
    }

    public static final HandleResult FAIL_WITH(Throwable cause) {
        HandleResult r = new HandleResult();
        r.success = false;
        r.cause = cause;
        return r;
    }
}
