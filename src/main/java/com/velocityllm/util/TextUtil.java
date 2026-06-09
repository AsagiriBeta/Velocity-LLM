package com.velocityllm.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Pattern CJK = Pattern.compile("\\p{Script=Han}");
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");

    private TextUtil() {
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFKC);
        Set<String> tokens = new HashSet<>();
        String[] parts = TOKEN_SPLIT.split(normalized);
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (containsCjk(part)) {
                addCjkTokens(part, tokens);
            } else if (part.length() > 1) {
                tokens.add(part);
            }
        }
        return new ArrayList<>(tokens);
    }

    private static boolean containsCjk(String text) {
        return CJK.matcher(text).find();
    }

    private static void addCjkTokens(String text, Set<String> tokens) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            tokens.add(String.valueOf(c));
            if (i + 1 < text.length()) {
                tokens.add(text.substring(i, i + 2));
            }
        }
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
