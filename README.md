# Velocity LLM

Velocity 群组服 AI 助手插件，支持 Ollama / OpenAI 兼容 API 与 RAG 文档问答。

参考了 [Ollama-Chat](https://github.com/mcraftbbs/Ollama-Chat) 的核心思路，并针对 Velocity 代理端重新实现；可与 [Essential-PlayerInfo](https://github.com/AsagiriBeta/Essential-PlayerInfo) 跨服聊天插件配合使用。

## 功能

- **AI 问答**：通过 `/ask <问题>` 或聊天前缀（`@ai`、`@bot`）向 AI 提问
- **RAG 检索**：从 `plugins/velocity-llm/docs/` 目录加载你手写的 `.md` / `.txt` 玩法文档，自动检索相关片段后交给 AI 回答
- **对话上下文**：保留最近几轮对话，让 AI 理解连续提问
- **群组服兼容**：在 Velocity 端拦截 AI 触发消息，避免与普通聊天逻辑冲突

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

[rag]
enabled = true
docs-folder = "docs"
max-chunks = 3
```

### 添加服务器玩法文档

将你的手写文档放入：

```
plugins/velocity-llm/docs/
├── 新手教程.md
├── 经济系统.txt
└── 常见问题.md
```

修改文档后执行 `/vllm reload`（需要 `velocityllm.admin` 权限）重新加载。

## 命令

| 命令 | 说明 |
|------|------|
| `/ask <问题>` | 向 AI 提问 |
| `@ai <问题>` | 聊天中直接提问（可配置前缀） |
| `/vllm reload` | 重载配置与文档（管理员） |
| `/vllm clear` | 清空自己的对话历史 |
| `/vllm help` | 查看帮助 |

## 与 Essential-PlayerInfo 配合

Essential-PlayerInfo 会在 `PlayerChatEvent` 上将聊天广播到其他子服。为避免 `@ai` 提问被跨服广播，建议在 Essential-PlayerInfo 的 `config.toml` 中设置：

```toml
[message]
enabled = true
command-to-broadcast = true
```

这样只有带 `#` 前缀的消息才会跨服广播，普通聊天和 `@ai` 提问不会泄露到其他子服。玩家跨服聊天时使用：`# 大家好`。

若保持 `command-to-broadcast = false`（默认广播所有消息），`@ai` 内容仍可能被 Essential-PlayerInfo 广播——此时请优先使用 `/ask` 命令。

## RAG 工作原理

1. 启动时扫描 `docs/` 目录，将文档切分为多个片段
2. 玩家提问时，用 TF-IDF 检索最相关的片段（支持中英文）
3. 将检索结果、对话历史与问题一起发送给 AI
4. AI 根据【服务器文档】作答，文档未提及的内容会提示无法确认

## 权限

- `velocityllm.admin` — 允许执行 `/vllm reload`

## 示例

```
玩家: /ask 怎么创建领地？
AI: 使用木斧左键、右键选点后，输入 /res create <名称> 即可创建领地……
```

```
玩家: @ai 怎么赚钱？
AI: 你可以通过出售物品给 NPC、完成每日任务或参与活动来赚取金币……
```
