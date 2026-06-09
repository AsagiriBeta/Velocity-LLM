package com.velocityllm.config;

import java.util.List;

public final class PluginConfig {

    public enum ApiStyle {
        OLLAMA,
        OPENAI
    }

    public enum RetrievalMode {
        AUTO,
        EMBEDDING,
        TFIDF
    }

    public enum ResponseVisibility {
        PRIVATE,
        SAME_SERVER
    }

    private boolean aiEnabled = true;
    private String baseUrl = "http://127.0.0.1:11434";
    private String model = "llama3";
    private String apiKey = "";
    private ApiStyle apiStyle = ApiStyle.OLLAMA;
    private boolean useChatApi = true;
    private double temperature = 0.7;
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
    private boolean embeddingEnabled = true;
    private String embeddingModel = "nomic-embed-text";
    private RetrievalMode retrievalMode = RetrievalMode.AUTO;
    private double minScore = 0.25;

    private int maxResponseLength = 500;
    private String systemPrompt = "";
    private ResponseVisibility responseVisibility = ResponseVisibility.SAME_SERVER;
    private boolean showPlayerMessages = true;

    private String messagePrefix = "<gradient:#55aaff:#aa55ff>[AI]</gradient> ";
    private String messageThinking = "<gray>正在思考中，请稍候...</gray>";
    private String messageCooldown = "<red>对话太频繁，请等待 {seconds} 秒后再试。</red>";
    private String messageEmptyInput = "<red>请输入你想说的话。</red>";
    private String messageError = "<red>AI 请求失败：{error}</red>";
    private String messageDisabled = "<red>AI 助手当前已禁用。</red>";

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public ApiStyle getApiStyle() {
        return apiStyle;
    }

    public void setApiStyle(ApiStyle apiStyle) {
        this.apiStyle = apiStyle;
    }

    public boolean isUseChatApi() {
        return useChatApi;
    }

    public void setUseChatApi(boolean useChatApi) {
        this.useChatApi = useChatApi;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
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

    public boolean isEmbeddingEnabled() {
        return embeddingEnabled;
    }

    public void setEmbeddingEnabled(boolean embeddingEnabled) {
        this.embeddingEnabled = embeddingEnabled;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public RetrievalMode getRetrievalMode() {
        return retrievalMode;
    }

    public void setRetrievalMode(RetrievalMode retrievalMode) {
        this.retrievalMode = retrievalMode;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
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

    public ResponseVisibility getResponseVisibility() {
        return responseVisibility;
    }

    public void setResponseVisibility(ResponseVisibility responseVisibility) {
        this.responseVisibility = responseVisibility;
    }

    public boolean isShowPlayerMessages() {
        return showPlayerMessages;
    }

    public void setShowPlayerMessages(boolean showPlayerMessages) {
        this.showPlayerMessages = showPlayerMessages;
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

    public String getMessageEmptyInput() {
        return messageEmptyInput;
    }

    public void setMessageEmptyInput(String messageEmptyInput) {
        this.messageEmptyInput = messageEmptyInput;
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
