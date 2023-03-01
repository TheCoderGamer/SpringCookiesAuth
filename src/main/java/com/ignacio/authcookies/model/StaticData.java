package com.ignacio.authcookies.model;

import java.util.HashMap;
import java.util.Map;

public class StaticData {
    public static final Map<String, String> users = new HashMap<>();

    static {
        users.put("ignacio", "1234");
        users.put("alumno1", "1234");
        users.put("alumno2", "1234");
        users.put("alumno3", "1234");
        users.put("alumno4", "1234");
    }

    public static Map<String, String> getUsers() {
        return users;
    }
}
