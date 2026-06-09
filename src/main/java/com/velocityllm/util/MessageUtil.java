package com.velocityllm.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    public static Component parse(String miniMessage) {
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    public static Component format(String template, String key, String value) {
        return parse(TextUtil.replacePlaceholders(template, key, value));
    }
}
