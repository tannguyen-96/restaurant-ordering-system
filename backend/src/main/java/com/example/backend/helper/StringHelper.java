package com.example.backend.helper;

public final class StringHelper {

    private StringHelper() {
    }

    public static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
