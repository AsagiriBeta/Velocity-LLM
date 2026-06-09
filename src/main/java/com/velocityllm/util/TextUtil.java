package com.velocityllm.util;

public final class TextUtil {

    private TextUtil() {
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static String replacePlaceholders(String template, String key, String value) {
        return template.replace("{" + key + "}", value);
    }
}
