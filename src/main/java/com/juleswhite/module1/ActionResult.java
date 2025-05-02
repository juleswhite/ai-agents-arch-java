package com.juleswhite.module1;

import java.util.HashMap;
import java.util.Map;

public class ActionResult {
    private Object result;
    private String error;

    public ActionResult(Object result, String error) {
        this.result = result;
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (error == null) {
            map.put("result", result);
        } else {
            map.put("error", error);
        }
        return map;
    }
}
