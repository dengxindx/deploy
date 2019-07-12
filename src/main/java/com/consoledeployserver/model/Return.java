package com.consoledeployserver.model;

import java.util.HashMap;

public class Return extends HashMap<String, Object> {

    public enum Result_Fields {
        success, code, note
    }

    /////////////////////////////////////////// SUCCESS/////////////////////////
    public static Return SUCCESS(ProcessCode code) {
        return SUCCESS(code.name(), code.getMessage());
    }

    public static Return SUCCESS(String code, String note) {
        Return jo = new Return();
        jo.put(Result_Fields.success.name(), true);
        jo.put(Result_Fields.code.name(), code);
        jo.put(Result_Fields.note.name(), note);
        return jo;
    }


    /////////////////////////////////////////// Fail/////////////////////////
    public static Return FAIL(String code, String note) {
        Return jo = new Return();
        jo.put(Result_Fields.success.name(), false);
        jo.put(Result_Fields.code.name(), code);
        jo.put(Result_Fields.note.name(), note);
        return jo;
    }


    public static Return FAIL(ProcessCode code) {
        return FAIL(code.name(), code.getMessage());
    }

    @Override
    public Return put(String key, Object value) {
        super.put(key, value);
        return this;
    }

}
