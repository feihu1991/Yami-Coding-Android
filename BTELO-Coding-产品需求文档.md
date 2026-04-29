# BTELO Coding 产品需求文档 (PRD)

> 版本：v1.1
> 日期：2026年4月27日
> 最后更新：2026年4月29日（开发完成同步）
> 基于：视频逐帧分析 + UI设计文档 + 知乎文章《我花了两个月用手机 Vibe Coding》

---

## 一、产品概述

### 1.1 产品定位

**BTELO Coding** 是一款专业的移动端 Vibe Coding 工具，让开发者能够通过手机远程操控电脑上运行的 Claude Code，实现：
- 随时随地编写代码、修改功能
- 远程部署、重启服务
- 多会话并行开发
- 内置文件浏览和 Git 管理

### 1.2 核心价值

| 价值点 | 描述 |
|--------|------|
| **解放开发者** | 无需坐在电脑前，手机即可完成开发工作 |
| **高效迭代** | 多会话并行，积累修改点后一键部署 |
| **无缝衔接** | 扫码即用，无需配置 tailscale |
| **移动优先** | 专为手机设计的交互体验 |

### 1.3 目标用户

- 需要随时随地处理代码问题的开发者
- 想要利用碎片时间进行开发的独立开发者
- 对 Vibe Coding 感兴趣的技术爱好者

---

## 二、功能架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        BTELO Coding App                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │   Agents    │ │    Team     │ │    Files    │ │  Browser  │ │
│  │   AI对话    │ │   团队协作   │ │  文件浏览器  │ │ Web代理   │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └───────────┘ │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                      核心功能模块                                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ 会话管理  │ │ 命令面板  │ │ 语音输入  │ │  Git管理  │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │ Skill管理 │ │ Token统计 │ │ 设备配对  │ │ Provider  │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 功能清单

| 模块 | 功能 | 优先级 | 状态 |
|------|------|--------|------|
| Agents | AI 对话界面 | P0 | ✅ 已实现 |
| Agents | Token 实时计数 | P0 | ✅ 已实现（Batch 6 接入 Session.tokenCount） |
| Agents | 语音输入 | P1 | ✅ 已实现（Batch 2 VoiceInputManager 集成） |
| Agents | 命令面板 (Slash Commands) | P0 | ✅ 已实现 |
| Agents | 功能标签栏 (Skill Tags) | P1 | ✅ 已实现（Batch 3 SkillTagsRow + 模型） |
| Agents | 会话列表管理 | P0 | ✅ 已实现 |
| Agents | 多会话标签栏 | P1 | ✅ 已实现 |
| Agents | AI Thinking 气泡 + 工具执行记录 | P0 | ✅ 已实现（Batch 2 MessageType 分流渲染） |
| Agents | 任务完成弹窗 | P1 | ✅ 已实现（Batch 3 TaskCompleteDialog） |
| Agents | Provider Control 设置 | P2 | ✅ 已实现（Batch 4） |
| Files | Git 仓库卡片展示 | P0 | ✅ 已实现 |
| Files | 路径浏览 | P0 | ✅ 已实现 |
| Files | 新建会话入口 | P1 | ✅ 已实现（Batch 1 SessionRepository 接入） |
| Files | 文件搜索 | P1 | ✅ 已实现（Batch 5 搜索栏+过滤） |
| Files | Git 管理面板 | P1 | ✅ 已实现（Batch 5 Changes/Stash/Commits/Diff/Tree） |
| Browser | Web Proxy 端口代理 | P0 | ✅ 已实现 |
| Browser | 添加代理弹窗 | P1 | ✅ 已实现（Batch 1 AlertDialog） |
| Browser | 代理状态指示 + 工具栏 | P1 | ✅ 已实现（Batch 1 clickable 接线） |
| Team | 团队页面 | P2 | ✅ 已实现（Batch 6 成员+共享会话列表） |
| Devices | 设备配对 + 管理 | P0 | ✅ 已实现（Batch 4 DevicesScreen） |
| Devices | 连接状态管理 | P0 | ✅ 已实现 |
| 通知 | 通知设置页 | P1 | ✅ 已实现（Batch 2 权限+开关+DND） |

---

## 三、详细功能需求

（3.1-3.6 各节详细需求规格保持不变，此处省略重复内容。完整功能细节参见原始 PRD v1.0。）

### 关键实现对照表

| PRD 需求 | 实现组件 | 状态 |
|---|---|---|
| AI Thinking 状态（紫色脉冲） | `ThinkingBubble` (ToolExecutionBubble.kt) | ✅ |
| Bash/Read/Edit/Write/Grep 工具记录 | `ToolExecutionBubble` — 按类型着色 | ✅ |
| 完成状态条 "N tool(s) used · Done" | `CompletionStatusBar` | ✅ |
| Skill Tags 功能标签栏 | `SkillTagsRow` — PATH蓝/FEATURE橙 | ✅ |
| 语音输入模式 | `VoiceInputManager` — SpeechRecognizer zh-CN | ✅ |
| 命令面板 (7 个内置命令) | `SlashCommandPanel` | ✅ |
| Provider Control 设置页 | `ProviderSettingsScreen` — 3 Provider + Effort + 统计 | ✅ |
| Git 管理面板 (5 Tab) | `GitPanelScreen` — Changes/Stash/Commits/Diff/Tree | ✅ |
| 文件搜索 | `FilesScreen` 搜索栏 + getFilteredRepos/Dirs | ✅ |
| 添加代理弹窗 | `BrowserScreen` AlertDialog (端口/URL) | ✅ |
| 任务完成弹窗 | `TaskCompleteDialog` — 蓝色对勾+金色标签 | ✅ |
| 设备管理页面 | `DevicesScreen` — 在线/离线+重命名+删除 | ✅ |
| 团队页面 | `TeamScreen` — 邀请码+成员+共享会话 | ✅ |
| Token 计数实时显示 | SessionInfo.tokenCount → AgentsTopBar | ✅ |
| 消息重试/置顶 | MessageBubble onRetry/onPin → AgentsViewModel | ✅ |

---

## 四、非功能性需求

### 4.1 性能要求

| 指标 | 要求 |
|------|------|
| 页面加载时间 | < 2s |
| 消息发送延迟 | < 500ms |
| 列表滚动帧率 | ≥ 60fps |
| 内存占用 | < 200MB |
| 电池消耗 | 正常使用 < 10%/小时 |

### 4.2 安全要求

| 要求 | 说明 |
|------|------|
| 通信加密 | WebSocket 使用 WSS 加密 |
| Token 存储 | 安全存储在 Android Keystore |
| 敏感信息 | 不在日志中记录敏感信息 |
| 会话隔离 | 不同会话数据隔离 |

### 4.3 兼容性要求

| 平台 | 版本要求 |
|------|----------|
| Android | ≥ 10.0 (API 29) |

### 4.4 可用性要求

| 要求 | 说明 |
|------|------|
| 离线缓存 | 缓存最近会话，支持离线查看 |
| 断线重连 | 自动重连，最多重试 3 次 |
| 错误恢复 | 网络恢复后自动同步 |

---

## 五、数据需求

### 5.1 会话数据

```kotlin
data class Session(
    val id: String,
    val name: String,
    val tool: String,
    val path: String = "",
    val createdAt: Long,
    val lastActiveAt: Long,
    val messageCount: Int = 0,
    val tokenCount: Int = 0,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val isConnected: Boolean
)
```

### 5.2 消息数据

```kotlin
data class Message(
    val id: String,
    val sessionId: String,
    val content: String,
    val type: MessageType,  // TEXT, COMMAND, OUTPUT, ERROR, TOOL, THINKING
    val timestamp: Long,
    val isFromUser: Boolean,
    val sender: String = "",
    val tools: List<ToolExecution>? = null
)

data class ToolExecution(
    val type: ToolType,      // BASH, READ, EDIT, WRITE, GREP
    val command: String,
    val output: String? = null,
    val status: ToolStatus   // SUCCESS, ERROR, RUNNING
)
```

### 5.3 设备数据

```kotlin
data class Device(
    val id: String,
    val name: String,
    val publicKey: String,
    val isOnline: Boolean,
    val lastSeen: Long
)
```

---

## 六、API 接口需求

### 6.1 WebSocket 接口

| 接口 | 方向 | 说明 |
|------|------|------|
| `ws://host/ws` | 双向 | 主 WebSocket 连接 |

**消息类型：**
```typescript
// 客户端发送
{ type: 'command' | 'ping'; payload: any; }

// 服务端推送
{ type: 'output' | 'status' | 'new_message' | 'pong'; payload: any; }
```

### 6.2 REST API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/connect?token=` | GET | 移动端连接，返回 ws_token + 会话列表 |
| `/sessions` | GET | 获取会话列表 |
| `/status` | GET | 服务器健康检查 |
| `/restart` | GET/POST | 重启服务器 |

---

## 七、开发优先级（最终状态）

### 7.1 MVP 版本（v1.0）— ✅ 全部完成

| 功能 | 优先级 | 状态 |
|------|--------|------|
| 底部导航栏 | P0 | ✅ |
| AI 对话界面 | P0 | ✅ |
| AI Thinking 气泡 + 工具执行记录 | P0 | ✅ |
| Token 计数显示 | P0 | ✅ |
| 命令面板 (Slash Commands) | P0 | ✅ |
| 会话列表页 | P0 | ✅ |
| Files 文件浏览器 | P0 | ✅ |
| Browser Web Proxy | P0 | ✅ |
| 设备配对功能 | P0 | ✅ |

### 7.2 迭代版本（v1.1）— ✅ 全部完成

| 功能 | 优先级 | 状态 |
|------|--------|------|
| 语音输入功能 | P1 | ✅ |
| 功能标签栏 (Skill Tags) | P1 | ✅ |
| Git 管理面板 | P1 | ✅ |
| 文件搜索 | P1 | ✅ |
| Files 新建会话入口 | P1 | ✅ |
| Browser 添加代理弹窗 | P1 | ✅ |
| Browser 工具栏交互 | P1 | ✅ |
| 任务完成弹窗 | P1 | ✅ |
| 消息操作 (retry/pin) | P1 | ✅ |
| 设备管理界面 | P1 | ✅ |
| 通知设置页 | P1 | ✅ |

### 7.3 未来版本（v2.0）

| 功能 | 优先级 | 状态 |
|------|--------|------|
| Provider Control 设置页 | P2 | ✅ 已提前完成 |
| Team 团队页面 | P2 | ✅ 已提前完成 |
| Browser 内嵌 WebView | P2 | ❌ 未实现 |
| Team 多人实时协作 | P2 | ❌ 未开始 |
| 离线模式支持 | P2 | ⚠️ 部分实现 |
| 文件内容预览 | P2 | ❌ 未实现 |
| 代理状态监控 | P2 | ❌ 未实现 |

---

## 八、验收标准

### 8.1 功能验收 — ✅ 全部通过

- [x] 用户可以通过底部导航切换 4 个 Tab 页面
- [x] 用户可以发送消息并接收 AI 回复
- [x] Token 计数随会话切换更新（从 Session 模型读取）
- [x] AI Thinking 状态正确显示（紫色脉冲动画）
- [x] Bash/Read/Edit/Write/Grep 工具执行记录按类型着色显示
- [x] 完成状态条显示 "N tool(s) used · Done"
- [x] 命令面板可以通过 `/` 触发并过滤
- [x] 会话列表可以查看所有会话
- [x] 文件浏览器可以浏览 Git 仓库（mock 数据）
- [x] Web Proxy 可以添加/查看代理条目
- [x] 设备配对流程正常工作
- [x] Skill Tags 标签栏可点击填入
- [x] 语音输入可启动 SpeechRecognizer 并填入结果
- [x] Provider Control 可选择 Provider/Model/Effort
- [x] Git 管理面板 5 个 Tab 正常切换
- [x] 文件搜索过滤正常
- [x] 任务完成弹窗正常展示
- [x] Team 页面展示成员和共享会话
- [x] 通知设置页权限/开关/DND 正常
- [x] 消息重试和置顶功能正常

### 8.2 性能验收

- [ ] 页面加载时间 < 2s（待真机测试）
- [ ] 消息发送延迟 < 500ms（待真机测试）
- [x] 编译通过 (BUILD SUCCESSFUL)
- [ ] 内存占用 < 200MB（待真机测试）

### 8.3 兼容性验收

- [x] Android 10+ 编译兼容
- [ ] 真机安装运行测试（待进行）

---

## 九、附录

### 9.1 术语表

| 术语 | 说明 |
|------|------|
| Vibe Coding | 通过自然语言描述让 AI 编写代码的开发方式 |
| Claude Code | Anthropic 的 AI 编程助手 |
| Session | 一次完整的开发会话 |
| Skill | 预定义的任务模板，如 `/deploy-iphone` |
| Token | 文本处理的最小单位，影响计费 |
| Web Proxy | 本地端口代理，允许远程访问 |

### 9.2 参考文档

- 开发进度记录：`.claude/DEV_PROGRESS.md`
- 重构计划：`.claude/plans/prancy-skipping-dusk.md`
- API 文档：`./server/` Node.js relay
- 知乎文章：《我花了两个月用手机 Vibe Coding》

### 9.3 颜色规范总结

| 用途 | 色值 | 代码常量 |
|------|------|----------|
| 背景 | #1A1A1A | `AppBackground` |
| 卡片背景 | #2A2A2A | `CardSurface` |
| 选中/高亮 | #3B82F6 | `AccentBlue` |
| 成功/对勾 | #22C55E | `GreenSuccess` |
| Thinking | #8B5CF6 | `ThinkingPurple` |
| Skill Tags边框 | #F97316 | `SkillTagBorder` |
| 文字主色 | #F5F5F7 | `TextPrimary` |
| 文字次要 | #888888 | `TextSecondary` |
| 发送渐变 | #8B5CF6→#6366F1 | `SendGradientStart/End` |
| 金色/完成 | #F59E0B | `WarningAmber` |

---

*文档版本：v1.1*
*创建时间：2026年4月27日*
*最后更新：2026年4月29日（6 个 Batch 开发完成同步）*
