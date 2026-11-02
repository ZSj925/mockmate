# mockmate · AI 面试搭子

> 上传简历 → AI 面试官围绕它提问 → 一场**有记忆**的模拟面试。

在线练面试最大的问题不是没有题，而是**没人追问**。mockmate 用本地大模型扮演面试官：一次只问一个问题，记得你前面答了什么，而且知道你的简历里写了哪些项目。

---

## ✨ 功能

| 功能 | 说明 |
|---|---|
| **登录鉴权** | Sa-Token 会话，接口级拦截，未登录一律 401 |
| **多轮对话记忆** | 对话全量落 MySQL，每轮带最近 N 条历史；**重启服务记忆不丢** |
| **会话管理** | 会话列表 / 点击切换 / 历史回放 / 删除，全部按用户隔离 |
| **简历上传** | PDF / DOC / DOCX / TXT，Tika 抽取正文，扫描件会被拦下 |
| **简历摘要** | 上传后**异步**让大模型压成"面试提问素材清单"，状态在列表里可见 |
| **围绕简历面试** | 新建会话时选择简历，摘要注入 system prompt，AI 直接问你简历里的项目 |
| **两页前端** | 原生 HTML/CSS/JS，无框架、无构建，跟着 jar 一起发布 |

---

## 🧱 技术栈

| 层 | 选型 |
|---|---|
| 语言 / 框架 | Java 21 · Spring Boot 4.1.2 |
| AI | Spring AI 2.0.1 + 本地 Ollama（`qwen3:8b`） |
| 鉴权 | Sa-Token 1.46.0 |
| 持久化 | MyBatis + MySQL 8 |
| 简历解析 | Apache Tika 3.3.1（`spring-ai-tika-document-reader`） |
| 前端 | 原生 HTML + CSS + JS（无构建步骤） |

---

## 🏗 架构

```
浏览器（login.html / index.html）
   │  localStorage 存 token + conversationId
   ▼
Sa-Token 拦截器  ── 未登录 → 401
   ▼
Controller        只接参、校验、包 Result
   ▼
Service           会话解析 / 调模型 / 落库
   ├──► Mapper ──► MySQL（user / session / message / resume）
   └──► ChatClient
          └─ Advisor 链
               ├─ ResumeSummary 注入（概要：简历摘要拼进 system）
               └─ InterviewChatMemory（读写 interview_message）
                       ▼
                  Ollama qwen3:8b
```

---

## 🚀 快速开始

### 1. 环境要求

- JDK 21、Maven 3.9+
- MySQL 8
- **Redis ≥ 6.0**（用到 `SET ... KEEPTTL`）
- [Ollama](https://ollama.com/)，并拉取模型：

```bash
ollama pull qwen3:8b
```

> 💡 `num-ctx` 设的是 8192。8G 显存的机器（如 RTX 4060 Laptop）基本是上限，
> 再大就会把层甩到 CPU，回答明显变慢。显存不够就把 `application.yaml` 里的 `num-ctx` 降到 4096。

> ⚠️ **Windows 上装 Redis 的坑**：`tporadowski/redis` 那个最常被搜到的 Windows 版
> 最高只到 **5.0.14**，而 Sa-Token 的 Redis DAO 用了 Redis 6.0 才引入的 `KEEPTTL`，
> 在 5.x 上会报 `ERR syntax error`。Windows 请用
> [redis-windows](https://github.com/redis-windows/redis-windows) 的 7.x/8.x 构建，
> 或 Memurai。装完用 `INFO server` 确认 `redis_version` ≥ 6。


### 2. 建库建表

```bash
mysql -uroot -p < docs/schema.sql
```

### 3. 配置（**不要改代码里的 `application.yaml`**）

配置文件里所有敏感项都是环境变量占位符。二选一：

**方式 A：环境变量**（IDEA → Run Configuration → Environment variables）

```
DB_PASSWORD=你的MySQL密码
REDIS_PASSWORD=你的Redis密码（没设 requirepass 就留空）
JWT_SECRET=（可选，目前登录用 Sa-Token，这块暂未启用）
```

> ⚠️ **IDEA 不会继承你在终端里 `$env:XXX` 设的变量**。在 PowerShell 里设了、IDEA 运行配置里没设，
> 应用照样读不到 —— 典型表现是 MySQL 能连但 Redis 报 `NOAUTH Authentication required`。


**方式 B：本地覆盖文件**

```bash
cp application-local.yaml.example application-local.yaml   # 填真实密码
# 启动时加 --spring.profiles.active=local
```

`application-local.yaml` 已在 `.gitignore` 中，不会被提交。

### 4. 启动

```bash
mvn spring-boot:run
```

打开 <http://localhost:8080/>，会自动跳到登录页。

### 5. 造一个账号

目前没有注册接口，直接插一行：

```sql
insert into user_account (username, password_hash, nickname)
values ('demo', '123456', '演示账号');
```

> ⚠️ `password_hash` 目前存的是**明文**（见下方 Roadmap），只适合本地自测。

---

## 🔌 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/auth/doLogin` | 登录，返回 tokenName / tokenValue |
| `POST` | `/auth/doLogout` | 退出登录（token 过期了也能调） |
| `GET` | `/interview/chat` | 一轮对话。`q` 问题、`conversationId` 会话号、`resumeId` 仅新建时生效 |
| `GET` | `/interview/sessions` | 我的会话列表（含首句预览、消息数、轮次） |
| `GET` | `/interview/sessions/{id}/messages` | 历史回放 |
| `DELETE` | `/interview/sessions/{id}` | 删除会话（连同消息） |
| `POST` | `/resume/resume` | 上传简历（multipart，字段名 `file`） |
| `GET` | `/resume` | 我的简历列表（含摘要状态） |
| `DELETE` | `/resume/{id}` | 删除简历 |

统一响应格式：

```json
{ "code": 1, "msg": null, "data": {} }
```

`code = 1` 成功，`0` 失败（`msg` 是给用户看的提示）。

---

## 🧠 核心设计

这一节是这个项目里最值得看的部分 —— 都是踩过坑才定下来的。

### 1. 上下文记忆：全量存、窗口读

模型的上下文不是"记住"的，而是**每次请求都把前文重新贴给它**。

```
对话全量写入 interview_message（永不删除，用于回放/将来出报告）
                ↓ 每轮请求
按 session_id 取最近 N 条（app.ai.memory.window，一问一答算 2 条）
                ↓
[人设] + [最近 N 条历史] + [本轮问题]  →  模型
```

`conversationId` 就是 `interview_session.id`，它同时是**记忆分区键**和**数据库主键**，
一个号贯穿"存消息"和"读历史"两件事。

### 2. 为什么不用 Spring AI 官方的 `MessageWindowChatMemory`

官方的 `add()` 会把"合并裁剪后的整个窗口"交给仓库，而官方 JDBC 仓库是**先删后插**。
用它们的话，`interview_message` 每轮都会被截断成最近 20 条 —— 历史回放和面试报告就没素材了。

所以这里自己实现 `ChatMemory`：

- `add()` → **只追加**，库里的历史永远是全的
- `get()` → SQL 里 `limit`，窗口在读的时候切，不动库

### 3. `.system()` 是**替换**，不是追加

`ChatClient.prompt().system(x)` 的源码是 `this.systemText = x`。
所以注入简历摘要时必须把人设一起拼上：

```java
spec.system(systemPrompt + "\n\n【候选人简历摘要】\n" + summary);
```

否则"每次只问一个问题"的规则会被整个冲掉。

### 4. 简历摘要异步生成

本地 8B 模型生成一份摘要要 15~20 秒，放在上传请求里用户会等到超时。

```
upload()              入完库立刻返回（实测 < 1 秒）
   └─ 跨 bean 调用 → @Async → 单线程池 → 调模型 → 回写 summary_text + READY
```

⚠️ 必须**跨 bean** 调用。同一个类里 `this.xxx()` 会绕过 Spring 代理，`@Async` 静默失效。

线程池故意只开 1 个线程：本地只有一张显卡，并发推理只会互相抢显存。

### 5. 简历在"建会话"时绑定，之后不可变

`interview_session.resume_id` 一旦写入就不再改。要换简历只能开新会话 ——
这样就不必回答"中途换简历时历史怎么算"这种没有好答案的问题。

---

## 📁 目录结构

```
src/main/java/zsj/mockmate
├── config/       AiConfig（ChatClient）· AsyncConfig（异步线程池）· SaTokenConfigure（拦截器）
├── controller/   AuthController · InterviewController · resumeController · UserController
├── service/      接口 + impl/ 实现（Interview · Resume · ResumeSummary · User · auth）
├── mapper/       MyBatis 接口（XML 在 resources/zsj/mockmate/mapper）
├── memory/       InterviewChatMemory —— 把对话记忆落到 MySQL
├── pojo/         entity / vo / dto / Result / ApiException
├── exception/    GlobalExceptionHandler（统一异常 → Result）
└── utils/        FileNameUtils（文件名清洗，防路径穿越）

src/main/resources
├── application.yaml     配置（敏感项走环境变量）
├── static/              前端：login.html · index.html · css · js
└── zsj/mockmate/mapper/ MyBatis XML

docs/schema.sql          建表脚本
```

---

## ⚠️ 已知限制 / Roadmap

| 项 | 现状 |
|---|---|
| 密码存储 | **明文**（`password_hash` 直接存 123456），应改 BCrypt |
| 面试报告 / 评分 | `interview_report` 表已建好，尚未接入 |
| AI 调用审计 | `ai_call_log` 表已建好，尚未打点 |
| 简历检索 | 目前是"摘要注入"；简历很长时应改成 RAG（切片 + 向量检索） |
| 流式输出 | 只有同步接口，长回答要等整段返回 |
| 简历原件 | 只存解析后的文本，不保留原始文件（`resume` 表暂无 `file_path`） |
| 并发 | 单实例、单线程池，同一会话并发发问会串轮次 |
| 删除简历 | 未处理"被会话引用"的级联 |

---

## 📄 License

仅供学习交流使用。简历数据敏感，部署前请务必处理上面的「密码明文」和「文件下载 XSS」两项。
