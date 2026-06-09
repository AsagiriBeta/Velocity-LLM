package com.velocityllm.config;

import java.util.List;

public final class PluginConfig {

    private boolean aiEnabled = true;
    private String apiUrl = "http://127.0.0.1:11434/api/chat";
    private String model = "llama3";
    private String apiKey = "";
    private boolean messagesFormat = true;
    private int timeoutSeconds = 120;

    private boolean triggerEnabled = true;
    private List<String> triggerPrefixes = List.of("@ai", "@bot");
    private int cooldownSeconds = 5;
    private int maxHistory = 3;

    private boolean ragEnabled = true;
    private String docsFolder = "docs";
    private int maxChunks = 3;
    private int chunkSize = 600;
    private int chunkOverlap = 100;

    private int maxResponseLength = 500;
    private String systemPrompt = "";

    private String messagePrefix = "<gradient:#55aaff:#aa55ff>[AI]</gradient> ";
    private String messageThinking = "<gray>正在思考中，请稍候...</gray>";
    private String messageCooldown = "<red>提问太频繁，请等待 {seconds} 秒后再试。</red>";
    private String messageEmptyQuestion = "<red>请输入你的问题。</red>";
    private String messageError = "<red>AI 请求失败：{error}</red>";
    private String messageDisabled = "<red>AI 助手当前已禁用。</red>";

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isMessagesFormat() {
        return messagesFormat;
    }

    public void setMessagesFormat(boolean messagesFormat) {
        this.messagesFormat = messagesFormat;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isTriggerEnabled() {
        return triggerEnabled;
    }

    public void setTriggerEnabled(boolean triggerEnabled) {
        this.triggerEnabled = triggerEnabled;
    }

    public List<String> getTriggerPrefixes() {
        return triggerPrefixes;
    }

    public void setTriggerPrefixes(List<String> triggerPrefixes) {
        this.triggerPrefixes = triggerPrefixes;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public int getMaxHistory() {
        return maxHistory;
    }

    public void setMaxHistory(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    public boolean isRagEnabled() {
        return ragEnabled;
    }

    public void setRagEnabled(boolean ragEnabled) {
        this.ragEnabled = ragEnabled;
    }

    public String getDocsFolder() {
        return docsFolder;
    }

    public void setDocsFolder(String docsFolder) {
        this.docsFolder = docsFolder;
    }

    public int getMaxChunks() {
        return maxChunks;
    }

    public void setMaxChunks(int maxChunks) {
        this.maxChunks = maxChunks;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public int getMaxResponseLength() {
        return maxResponseLength;
    }

    public void setMaxResponseLength(int maxResponseLength) {
        this.maxResponseLength = maxResponseLength;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public String getMessageThinking() {
        return messageThinking;
    }

    public void setMessageThinking(String messageThinking) {
        this.messageThinking = messageThinking;
    }

    public String getMessageCooldown() {
        return messageCooldown;
    }

    public void setMessageCooldown(String messageCooldown) {
        this.messageCooldown = messageCooldown;
    }

    public String getMessageEmptyQuestion() {
        return messageEmptyQuestion;
    }

    public void setMessageEmptyQuestion(String messageEmptyQuestion) {
        this.messageEmptyQuestion = messageEmptyQuestion;
    }

    public String getMessageError() {
        return messageError;
    }

    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }

    public String getMessageDisabled() {
        return messageDisabled;
    }

    public void setMessageDisabled(String messageDisabled) {
        this.messageDisabled = messageDisabled;
    }
}
