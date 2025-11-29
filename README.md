# Spring AI Demo

一个使用 Spring Boot 与 Spring AI（OpenAI）构建的最小化聊天示例，提供两套交互方式：
- 非流式（一次性响应）：后端统一封装 `ChatResponse<T>`，前端一次性展示回复
- 流式（SSE）：服务端按分片实时推送回复，前端逐字显示

## 特性
- 基于 `spring-boot-starter-web` 与 `spring-ai-starter-model-openai`
- 统一响应模型 `ChatResponse<T>`（泛型数据承载）
- 提供 `POST /api/chat`（一次性响应）与 `GET /api/chat/stream`（SSE 流式）两种接口
- 两套示例页面：`/chat.html`（一次性）、`/chat-stream.html`（流式）
- 通过环境变量配置 API Key、Base URL、模型

## 快速开始
1. 配置环境变量（建议在系统环境或 IDE Run 配置中设置）：
   - `AI_API_KEY`：OpenAI API Key（必填）
   - `AI_BASE_URL`：OpenAI Base URL，默认 `api.openai.com`
   - `AI_MODEL`：模型名称，默认 `gpt-4o-mini`

2. 本地运行（Windows）：
   - `mvn spring-boot:run`

3. 访问页面：
   - 非流式（一次性）：`http://localhost:8080/chat.html`
   - 流式（SSE）：`http://localhost:8080/chat-stream.html`

应用启动时也会打印上述地址（`src/main/java/com/example/springai/SpringAiDemoApplication.java:64-67`）。

## 配置说明
所有关键参数通过环境变量注入到 Spring AI 配置（`src/main/resources/application.properties:9-16`）：
- `spring.ai.openai.api-key=${AI_API_KEY:YOUR_AI_API_KEY}`
- `spring.ai.openai.base-url=${AI_BASE_URL:api.openai.com}`
- `spring.ai.openai.chat.options.model=${AI_MODEL:gpt-4o-mini}`

可根据需要在 `application.properties` 中调整温度、最大 Tokens 等。

## 接口说明
### 一次性响应：`POST /api/chat`
- 请求体：`{ "message": "你的问题" }`
- 响应体：`ChatResponse<String>`，模型回复在 `data` 字段
- 参考实现：`src/main/java/com/example/springai/ChatController.java:28-39`

示例：

```bash
curl -s -X POST "http://localhost:8080/api/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"你好，介绍一下这个项目"}'
```

典型响应（省略部分字段）：

```json
{
  "errcode": 0,
  "status": 200,
  "message": "OK",
  "timestamp": "2025-11-29 12:00:00",
  "data": "这是一个 Spring AI 与 OpenAI 的演示项目……"
}
```

前端页面：`src/main/resources/static/chat.html` 使用 `fetch('/api/chat')` 渲染 `data`。

### 流式响应（SSE）：`GET /api/chat/stream?message=...`
- 事件：
  - `chunk`：分片文本
  - `done`：结束标记
- 参考实现：`src/main/java/com/example/springai/ChatController.java:42-73`
- 前端页面：`src/main/resources/static/chat-stream.html` 使用 `EventSource` 逐字显示

> 说明：当前 SSE 通道按纯文本分片推送；如需统一为 `ChatResponse<String>` 的结构化分片，可将 `chunk` 的 `data` 改为 JSON 封装。

## 响应模型 `ChatResponse<T>`
- 位置：`src/main/java/com/example/springai/ChatResponse.java:16`
- 字段：`errcode`、`status`、`error`、`message`、`timestamp`、`path`、`data`（泛型）
- 非流式接口返回：`ChatResponse<String>`，回复文本在 `data`

## 依赖与构建
- 主要依赖：
  - Spring Boot 3.2.x
  - Spring AI OpenAI
  - Lombok（已在 `pom.xml` 配置依赖与注解处理器）

构建：

```bash
mvn -q -DskipTests compile
mvn package
```

运行：

```bash
mvn spring-boot:run
# 或
java -jar target/springai-demo-1.0.0.jar
```

## 常见问题
- 未设置 `AI_API_KEY`：后端无法调用模型，页面可能显示降级文案（`ChatController` 有容错分支）。
- 端口占用：修改 `application.properties` 中 `server.port`。
- SSE 在部分代理环境受限：请直接本地访问或关闭反向代理。

## 许可证
Apache-2.0，见 `LICENSE`。
