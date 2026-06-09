package com.velocityllm.command;

import com.velocityllm.VelocityLLMPlugin;
import com.velocityllm.ai.ChatService;
import com.velocityllm.config.PluginConfig;
import com.velocityllm.util.MessageUtil;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class VLLMCommand implements SimpleCommand {

    private final VelocityLLMPlugin plugin;
    private final ChatService chatService;

    public VLLMCommand(VelocityLLMPlugin plugin, ChatService chatService) {
        this.plugin = plugin;
        this.chatService = chatService;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(source);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(source);
            case "clear" -> handleClear(source);
            default -> sendHelp(source);
        }
    }

    private void handleReload(CommandSource source) {
        try {
            plugin.getConfigManager().load();
            int count = chatService.reloadDocs();
            PluginConfig config = plugin.getConfigManager().getConfig();
            source.sendMessage(MessageUtil.format(config.getMessageDocsReloaded(), "count", String.valueOf(count)));
        } catch (Exception e) {
            source.sendMessage(MessageUtil.format(
                    plugin.getConfigManager().getConfig().getMessageError(),
                    "error",
                    e.getMessage()
            ));
        }
    }

    private void handleClear(CommandSource source) {
        if (source instanceof Player player) {
            plugin.getChatHistoryManager().clear(player.getUniqueId());
            source.sendMessage(Component.text("已清空你的 AI 对话历史。"));
        } else {
            source.sendMessage(Component.text("该命令只能由玩家执行。"));
        }
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("=== Velocity LLM 帮助 ==="));
        source.sendMessage(Component.text("/ask <问题> - 向 AI 提问（支持 RAG 文档检索）"));
        source.sendMessage(Component.text("聊天触发 - 以 @ai 或 @bot 开头直接提问"));
        source.sendMessage(Component.text("/vllm reload - 重新加载配置和文档（需要权限）"));
        source.sendMessage(Component.text("/vllm clear - 清空自己的对话历史"));
    }

    @Override
    public boolean hasPermission(@NotNull Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return invocation.source().hasPermission("velocityllm.admin");
        }
        return true;
    }

    @Override
    public @NotNull CompletableFuture<List<String>> suggestAsync(@NotNull Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> options = List.of("help", "reload", "clear");
        if (args.length == 0) {
            return CompletableFuture.completedFuture(options);
        }
        return CompletableFuture.completedFuture(
                options.stream().filter(option -> option.startsWith(args[0])).collect(Collectors.toList())
        );
    }

    public static CommandMeta meta(VelocityLLMPlugin plugin) {
        return plugin.getServer().getCommandManager().metaBuilder("vllm")
                .plugin(plugin)
                .build();
    }

    public static CommandMeta askMeta(VelocityLLMPlugin plugin) {
        return plugin.getServer().getCommandManager().metaBuilder("ask")
                .plugin(plugin)
                .build();
    }
}
