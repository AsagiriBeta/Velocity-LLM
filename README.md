# Velocity LLM

Velocity 群组服 AI 助手插件，基于 **Ollama** 实现 `@ai` 多轮对话与 RAG 文档问答。

## 功能

- `@ai` 多轮对话，同子服玩家可见完整对话
- Ollama `/api/chat` 与 `/api/generate`
- RAG：Ollama Embedding 语义检索本地玩法文档
- 兼容 Essential-PlayerInfo 等跨服聊天插件
- 控制台 `vllm reload` 热重载

## 环境要求

- Velocity **3.5.0-SNAPSHOT+**
- Java **21+**
- Ollama

```bash
ollama pull llama3
ollama pull nomic-embed-text
```

## 构建

```bash
./gradlew shadowJar
```

产物：`build/libs/velocity-llm-1.0.0.jar` → 放入 Velocity `plugins/` 目录。

## 配置

编辑 `plugins/velocity-llm/config.toml`，将玩法文档放入 `plugins/velocity-llm/docs/`。

修改后于 Velocity 控制台执行 `vllm reload`。

## 使用

```
@ai 怎么创建领地？
@ai 那需要多少钱？
```

## 发布前检查清单

1. Ollama 已运行且模型已拉取
2. `docs/` 中已放入服务器玩法文档
3. Velocity 控制台确认日志：`Velocity LLM v1.0.0 已启用，RAG: 就绪`

若 RAG 显示「不可用」，AI 对话仍可使用，但无法检索文档。检查 Ollama 是否运行及 `nomic-embed-text` 是否已安装。
