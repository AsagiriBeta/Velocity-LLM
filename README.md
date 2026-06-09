# Velocity LLM

Velocity 群组服 AI 助手插件，支持 Ollama / OpenAI 兼容 API 与 RAG 文档问答。

插件运行在 Velocity 代理端（**3.5.0-SNAPSHOT+**，Java 21），可接收任意子服玩家的 `@ai` 聊天消息，并内置与 Essential-PlayerInfo 等跨服聊天插件的兼容处理。

## 功能

- **聊天触发**：`@ai 你的问题`（前缀可配置）
- **Ollama 原生支持**：正确解析 `/api/chat` 与 `/api/generate` 响应
- **RAG 语义检索**：通过 Ollama Embeddings 检索玩法文档，失败时自动回退 TF-IDF
- **多轮对话**：以标准 messages 格式携带上下文
- **控制台管理**：`vllm reload` 重载配置与文档索引

## 构建

```bash
./gradlew shadowJar
```

产物：`build/libs/velocity-llm-1.0.0.jar`

## 环境要求

- Velocity 3.5.0-SNAPSHOT+
- Java 21+
- Ollama（默认 `http://127.0.0.1:11434`）

```bash
ollama pull llama3
ollama pull nomic-embed-text   # RAG 语义检索用
```

## 配置示例

```toml
[ai]
base-url = "http://127.0.0.1:11434"
model = "llama3"
api-style = "ollama"
use-chat-api = true

[rag]
enabled = true
embedding-model = "nomic-embed-text"
retrieval = "auto"
```

将玩法文档放入 `plugins/velocity-llm/docs/`，修改后于 Velocity 控制台执行 `vllm reload`。

## 使用

```
@ai 怎么创建领地？
```

默认同子服所有玩家都能看到 AI 回复（含提问内容），其他子服玩家看不到。可在 `config.toml` 中设置 `response.visibility = "private"` 改为仅提问者可见。

## 控制台命令

```
vllm reload
```

## RAG 检索模式

| 模式 | 说明 |
|------|------|
| `auto` | 优先 Ollama Embedding，不可用时回退 TF-IDF |
| `embedding` | 仅语义向量检索 |
| `tfidf` | 仅关键词 TF-IDF 检索 |
