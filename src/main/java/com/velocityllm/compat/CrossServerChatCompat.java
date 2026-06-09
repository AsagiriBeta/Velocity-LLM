package com.velocityllm.compat;

import com.velocitypowered.api.event.player.PlayerChatEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 兼容在 {@code PlayerChatEvent} 上直接读取原始消息并跨服广播的插件（如 Essential-PlayerInfo）。
 * 在广播类插件处理之前清空消息文本，避免 @ai 提问内容泄露到其他子服。
 */
public final class CrossServerChatCompat {

    private static final VarHandle MESSAGE_HANDLE = initVarHandle();
    private static final Field MESSAGE_FIELD = initReflectionField();

    private CrossServerChatCompat() {
    }

    public static void suppressBroadcastMessage(PlayerChatEvent event, Logger logger) {
        if (tryVarHandle(event)) {
            return;
        }
        if (tryReflection(event)) {
            return;
        }
        logger.warn("无法清空 @ai 聊天消息内容，跨服聊天插件可能仍会广播该消息");
    }

    private static boolean tryVarHandle(PlayerChatEvent event) {
        if (MESSAGE_HANDLE == null) {
            return false;
        }
        try {
            MESSAGE_HANDLE.set(event, "");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryReflection(PlayerChatEvent event) {
        if (MESSAGE_FIELD == null) {
            return false;
        }
        try {
            MESSAGE_FIELD.set(event, "");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static VarHandle initVarHandle() {
        try {
            return MethodHandles.privateLookupIn(PlayerChatEvent.class, MethodHandles.lookup())
                    .findVarHandle(PlayerChatEvent.class, "message", String.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Field initReflectionField() {
        try {
            Field field = PlayerChatEvent.class.getDeclaredField("message");
            field.setAccessible(true);
            try {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (NoSuchFieldException ignored) {
                // Java 12+ 无 modifiers 字段，部分 JVM 仍允许写入
            }
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
