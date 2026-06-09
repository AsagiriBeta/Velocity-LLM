package com.velocityllm.command;

import com.velocityllm.VelocityLLMPlugin;
import com.velocityllm.ai.ChatService;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Velocity 控制台专用命令，玩家无法执行。
 */
public final class ConsoleCommand implements SimpleCommand {

    private final VelocityLLMPlugin plugin;
    private final ChatService chatService;
    private final Logger logger;

    public ConsoleCommand(VelocityLLMPlugin plugin, ChatService chatService, Logger logger) {
        this.plugin = plugin;
        this.chatService = chatService;
        this.logger = logger;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        CommandSource source = invocation.source();
        if (source instanceof Player) {
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            handleReload(source);
            return;
        }

        source.sendMessage(Component.text("用法: vllm reload — 重新加载 config.toml 与 docs/ 文档"));
    }

    private void handleReload(CommandSource source) {
        try {
            plugin.getConfigManager().load();
            int count = chatService.reload();
            String ragStatus = plugin.getRagService().isIndexReady() ? "RAG 就绪" : "RAG 不可用";
            String message = "Velocity LLM 已重新加载，" + count + " 个文档片段，" + ragStatus + "。";
            logger.info(message);
            source.sendMessage(Component.text(message));
        } catch (Exception e) {
            logger.error("重新加载失败: {}", e.getMessage(), e);
            source.sendMessage(Component.text("重新加载失败: " + e.getMessage()));
        }
    }

    @Override
    public boolean hasPermission(@NotNull Invocation invocation) {
        return !(invocation.source() instanceof Player);
    }

    @Override
    public @NotNull CompletableFuture<List<String>> suggestAsync(@NotNull Invocation invocation) {
        if (invocation.arguments().length == 0) {
            return CompletableFuture.completedFuture(List.of("reload"));
        }
        return CompletableFuture.completedFuture(List.of());
    }

    public static CommandMeta meta(VelocityLLMPlugin plugin) {
        return plugin.getServer().getCommandManager().metaBuilder("vllm")
                .plugin(plugin)
                .build();
    }
}
