package com.velocityllm.command;

import com.velocityllm.ai.ChatService;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AskCommand implements SimpleCommand {

    private final ChatService chatService;

    public AskCommand(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(net.kyori.adventure.text.Component.text("该命令只能由玩家执行。"));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            source.sendMessage(net.kyori.adventure.text.Component.text("用法: /ask <问题>"));
            return;
        }

        String question = String.join(" ", args);
        chatService.ask(player, question);
    }

    @Override
    public boolean hasPermission(@NotNull Invocation invocation) {
        return true;
    }

    @Override
    public @NotNull CompletableFuture<List<String>> suggestAsync(@NotNull Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}
