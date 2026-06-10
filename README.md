# Velocity LLM

Velocity 群组服 AI 助手插件，基于 **Ollama** 实现 `@ai` 多轮对话与 RAG 文档问答。

插件运行在 Velocity 代理端（**3.5.0-SNAPSHOT+**，Java 21），可接收任意子服玩家的 `@ai` 聊天消息，并内置与 Essential-PlayerInfo 等跨服聊天插件的兼容处理。

## 功能

- **聊天触发**：`@ai 你的问题`（前缀可配置）
- **同子服可见**：默认同子服玩家可见完整 AI 对话（提问与回复）
- **Ollama 原生支持**：正确解析 `/api/chat` 与 `/api/generate` 响应
- **RAG 语义检索**：通过 Ollama Embedding 检索本地玩法文档
- **多轮对话**：以标准 messages 格式携带上下文
- **控制台管理**：`vllm reload` 重载配置与文档索引

## 环境要求

- Velocity **3.5.0-SNAPSHOT+**
- Java **21+**
- Ollama（默认 `http://127.0.0.1:11434`）

```bash
ollama pull llama3
ollama pull nomic-embed-text
```

## 构建

```bash
./gradlew shadowJar
```

产物：`build/libs/velocity-llm-1.0.0.jar` → 放入 Velocity `plugins/` 目录。

合并到 `main` 分支后，GitHub Actions 会自动构建并在 [Releases](https://github.com/AsagiriBeta/Velocity-LLM/releases) 发布 JAR。发布新版本前请在 `build.gradle` 中更新 `version`。

## 配置

编辑 `plugins/velocity-llm/config.toml`，将玩法文档放入 `plugins/velocity-llm/docs/`。

```toml
[ai]
base-url = "http://127.0.0.1:11434"
model = "llama3"
use-chat-api = true

[rag]
enabled = true
embedding-model = "nomic-embed-text"
min-score = 0.25

[response]
visibility = "same-server"
show-player-messages = true
```

修改后于 Velocity 控制台执行 `vllm reload`。

## 使用

```
@ai 怎么创建领地？
@ai 那需要多少钱？
```

## 控制台命令

```
vllm reload
```

## 发布前检查清单

1. Ollama 已运行且模型已拉取
2. `docs/` 中已放入服务器玩法文档
3. Velocity 控制台确认日志：`Velocity LLM v1.0.0 已启用，RAG: 就绪`

若 RAG 显示「不可用」，AI 对话仍可使用，但无法检索文档。请检查 Ollama 是否运行，以及 `nomic-embed-text` 是否已安装。
