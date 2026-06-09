# Velocity LLM

Velocity 群组服 AI 助手插件，支持 Ollama / OpenAI 兼容 API 与 RAG 文档问答。

插件运行在 Velocity 代理端，可接收**任意子服**玩家发送的 `@ai` 聊天消息。参考了 [Ollama-Chat](https://github.com/mcraftbbs/Ollama-Chat) 的核心思路；可与 [Essential-PlayerInfo](https://github.com/AsagiriBeta/Essential-PlayerInfo) 跨服聊天插件配合使用。

## 功能

- **聊天触发**：玩家在任意子服聊天中发送 `@ai 你的问题` 即可提问（前缀可配置）
- **RAG 检索**：从 `plugins/velocity-llm/docs/` 加载手写的 `.md` / `.txt` 玩法文档，检索相关片段后交给 AI 回答
- **对话上下文**：保留最近几轮对话，支持连续追问
- **纯配置驱动**：所有设置通过 `config.toml` 管理，无游戏内管理命令

## 构建

```bash
./gradlew shadowJar
```

产物位于 `build/libs/velocity-llm-1.0.0.jar`，放入 Velocity 的 `plugins/` 目录即可。

## 环境要求

- Velocity 3.3+
- Java 17+
- 已运行的 Ollama 或其他兼容 API（默认 `http://127.0.0.1:11434/api/chat`）

## 配置

首次启动后编辑 `plugins/velocity-llm/config.toml`：

```toml
[ai]
enabled = true
api-url = "http://127.0.0.1:11434/api/chat"
model = "llama3"
messages-format = true

[chat]
trigger-prefixes = ["@ai", "@bot"]

[rag]
enabled = true
docs-folder = "docs"
max-chunks = 3
```

### 添加服务器玩法文档

```
plugins/velocity-llm/docs/
├── 新手教程.md
├── 经济系统.txt
└── 常见问题.md
```

修改配置或文档后，在 **Velocity 控制台**执行：

```
vllm reload
```

## 使用方式

玩家在任意子服的聊天框输入：

```
@ai 怎么创建领地？
@bot 服务器有哪些指令？
```

AI 回复仅发送给提问的玩家，不会作为普通聊天广播。

## 控制台命令

| 命令 | 说明 |
|------|------|
| `vllm reload` | 重新加载 config.toml 与 docs/ 文档 |

仅在 Velocity 控制台可用，玩家无法执行。

## 与 Essential-PlayerInfo 配合

Essential-PlayerInfo 会在 `PlayerChatEvent` 上将聊天广播到其他子服。为避免 `@ai` 提问被跨服广播，建议在 Essential-PlayerInfo 的 `config.toml` 中设置：

```toml
[message]
enabled = true
command-to-broadcast = true
```

这样只有带 `#` 前缀的消息才会跨服广播。玩家跨服聊天时使用：`# 大家好`。

本插件在 `PlayerChatEvent` 的最早阶段拦截 `@ai` 消息并拒绝其作为普通聊天发送，配合上述配置可避免 AI 提问泄露到其他子服。

## RAG 工作原理

1. 启动时扫描 `docs/` 目录，将文档切分为多个片段
2. 玩家提问时，用 TF-IDF 检索最相关的片段（支持中英文）
3. 将检索结果、对话历史与问题一起发送给 AI
4. AI 根据【服务器文档】作答，文档未提及的内容会提示无法确认
