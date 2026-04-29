# BTELO Coding 完整功能设计清单

> 基于 PRD v1.0 + 现有代码分析 + 产品定位（手机远程操控 Claude Code CLI）
> 日期：2026-04-28
> 最后更新：2026-04-29（同步 6 个 Batch 开发完成状态）

---

## 一、连接与配对层

### 1.1 QR 码扫描连接
- 电脑端启动 `node server/index.js`，终端显示 QR 码
- 手机 App 扫码，解析 `btelo://{IP}:{PORT}/{token}` 格式的 URL
- 调用 `GET /connect?token=` 获取 `ws_token` + 可用会话列表
- 建立 WebSocket 连接 `ws://{IP}:{PORT}/ws?token=`
- **状态：** ✅ 已完成（ScanScreen + CameraX + ML Kit）

### 1.2 手动配对码输入
- 用户手动输入 IP:Port + Token 连接
- 适用于扫码不可用的场景（如屏幕截图扫码）
- **状态：** ⚠️ 有 LoginScreen 支持手动输入服务器地址，但缺少 Token 输入引导

### 1.3 连接状态管理
- 实时显示连接状态：已连接 / 连接中 / 已断开 / 错误
- 断线自动重连（指数退避，最多 3 次）
- 重连后自动恢复会话上下文
- **状态：** ⚠️ WebSocketClientFactory 已实现重连逻辑，但 UI 层 ConnectionState 展示不完整

### 1.4 多设备管理
- 显示已配对设备列表（CLI / Mobile / Web）
- 设备在线/离线状态指示
- 设备名称编辑
- 移除已配对设备
- **状态：** ✅ 已完成 — DevicesScreen + DevicesViewModel 已实现（Batch 4），从 DeviceRepository Flow 加载设备列表

---

## 二、中继服务器层 (Node.js)

### 2.1 服务器核心 (index.js / relay.js)
- Express HTTP 服务器 + WebSocket 服务器
- 两种模式：index.js（一体式）/ relay.js + bridge.js（分离式）
- **状态：** ✅ 两种模式均已实现

### 2.2 REST API
| 端点 | 方法 | 功能 | 状态 |
|---|---|---|---|
| `/connect?token=` | GET | 移动端连接，返回 ws_token + 会话列表 | ✅ |
| `/sessions` | GET | 列出所有 Claude Code 会话 | ✅ |
| `/status` | GET | 服务器健康检查 | ✅ |
| `/restart` | GET/POST | 重启服务器 | ✅ |
| `/bridge/register` | POST | Bridge 注册（relay 模式） | ✅ relay.js |

### 2.3 Claude Code 会话发现
- 扫描 `~/.claude/sessions/` 目录，解析 session JSON 文件
- 过滤 `kind === 'interactive'` 的交互式会话
- 检测进程是否存活（`process.kill(pid, 0)`）
- 按时间倒序排列
- **状态：** ✅ 已完成

### 2.4 JSONL 文件实时同步
- `fs.watch` 监听 JSONL 文件变更
- 增量读取新内容
- 解析后通过 WebSocket 推送 `new_message` 到手机
- **状态：** ✅ 已完成

### 2.5 命令执行引擎
- 接收手机发来的 `command` 消息
- 通过 `claude -p "command" -r sessionId --output-format stream-json` 执行
- 实时解析 stdout 的 stream-json 事件
- 格式化后通过 `output` 消息推送到手机
- 支持命令排队
- **状态：** ✅ 已完成

### 2.6 Bridge 分离模式 (relay.js + bridge.js)
- relay.js：纯消息转发（无 Claude Code 依赖）
- bridge.js：管理 Claude Code 进程，注册到 relay
- 支持 persistent / resume 两种模式
- **状态：** ✅ 已完成

### 2.7 服务器安全加固
- WebSocket 连接 Token 验证
- Token 过期清理
- 闲置会话自动清理（5 分钟无连接）
- 请求频率限制（防刷）
- **状态：** ⚠️ Token 验证已做，频率限制未做，闲置清理仅 relay.js 有

---

## 三、Agents 标签页（AI 对话核心）

### 3.1 顶部导航栏
- **返回按钮：** 进入会话列表
- **会话名称：** 18sp Bold 居中
- **Token 计数：** "287 context tokens" 12sp 灰色，实时更新
- **命令标签：** 输入 "/" 时显示 "/deploy-iphone" 蓝色
- **通知铃铛：** 右上角，点击进入 NotificationSettings
- **头像菜单：** Provider Settings / Devices / Disconnect 下拉菜单
- **状态：** ✅ UI 完成 + Token 计数已从 Session 模型接入（Batch 6）

### 3.2 用户消息气泡
- 背景 #2A2A2A，圆角 16px（左下 4px 尖角）
- 右对齐，最大宽度 85%
- 时间戳 "HH:MM 用户名" + 复制/重试/置顶按钮
- **状态：** ✅ MessageBubble + retry/pin 按钮已实现（Batch 3）

### 3.3 AI 流式输出
- AI 回复实时逐字显示
- 打字机效果，无闪烁
- 自动滚动到底部
- **状态：** ✅ AiStreamingBubble + LaunchedEffect 自动滚动

### 3.4 AI Thinking 状态
- 紫色渐变圆形图标 + 脉冲缩放动画（1.5s 周期）
- "Thinking..." 白色 14px + 说明文字灰色 12px
- **状态：** ✅ ThinkingBubble 组件完成 + AgentsScreen 按 MessageType.THINKING 分流渲染（Batch 2）

### 3.5 工具执行记录
- 按工具类型着色图标：Bash=绿 / Read=蓝 / Edit=蓝 / Write=绿 / Grep=橙
- 命令预览（截断）+ 展开查看完整命令和输出
- **状态：** ✅ ToolExecutionBubble 组件完成 + AgentsScreen 按 MessageType.TOOL 分流渲染（Batch 2）

### 3.6 完成状态条
- "N tool(s) used · Done" 灰色 12sp
- 右侧展开/折叠箭头
- **状态：** ✅ CompletionStatusBar 已接入消息列表（Batch 2）

### 3.7 斜杠命令面板
- 输入 "/" 时自动弹出
- 7 个内置命令：/brainstorming /brand-guidelines /canvas-design /check-commit-push /claude-api /deploy-iphone /fix-and-commit-and-push
- 实时过滤、键盘选中、点击填入
- **状态：** ✅ SlashCommandPanel 完成 + AgentsViewModel 集成

### 3.8 功能标签栏 (Skill Tags)
- 橙色边框标签 #F97316（FEATURE）/ 蓝色边框 #3B82F6（PATH），28px 高，20px 圆角
- 横向滚动 + "+" 添加 + "≡" 菜单
- 点击标签快捷填入输入框
- **状态：** ✅ 已完成 — SkillTagsRow + SkillTag 模型 + AgentsViewModel CRUD（Batch 3）

### 3.9 输入区域
- **文本输入框：** 背景 #1A1A1A，圆角 20px，最小高度 44px，占位符 "Message, / commands, @ history, $ files"
- **工具栏图标：** AI增强 / 附件 / 收藏 / 代码 / 工具 / 语音（灰色，28dp）— 全部已接线
- **发送按钮：** 紫色渐变圆形（#8B5CF6 → #6366F1），36×36dp，空输入时禁用 + loading 状态
- **状态：** ✅ 完成

### 3.10 语音输入模式
- 语音按钮接入 Android SpeechRecognizer API
- 中文识别（zh-CN）
- 识别结果实时填入输入框
- 错误提示（权限不足/网络错误等）
- **状态：** ✅ 已完成 — VoiceInputManager 集成 + AgentsViewModel observeVoiceInput（Batch 2）

### 3.11 会话列表页
- 顶部：Done / Sessions 标题 / Select All
- 会话项：复选框 + 名称 + 时长 + 路径，长按菜单
- **状态：** ⚠️ SessionListScreen 已存在，但缺少设置区（Tab order/Multi-row tabs/Directory switcher）和多选

### 3.12 多会话标签栏
- 横向滚动的会话标签
- 彩色圆点标识 + 选中态高亮
- "+" 新建标签按钮
- **状态：** ✅ SessionTabsRow 已实现

### 3.13 快捷操作栏
- 横向滚动的快捷动作按钮
- "Build feature" "Fix bug" "Run tests" "Deploy"
- 点击填入输入框并发送
- **状态：** ✅ QuickActionsRow 已实现

### 3.14 Provider Control 设置页
- Manage Providers：Claude Code / Codex / Gemini 卡片式选择
- AI Settings：Model 下拉 / Effort 三段选择（Low/Medium/High）
- Provider Details：CLI Version / Settings Scope / Adapter Version
- Usage Statistics：Sessions / Messages / Tokens 统计
- **状态：** ✅ 已完成 — ProviderSettingsScreen + ProviderSettingsViewModel（Batch 4）

---

## 四、Files 标签页（文件浏览器）

### 4.1 顶部导航栏
- 搜索按钮（toggleSearch）、刷新按钮（refreshCurrentPath）、设置按钮
- **状态：** ✅ 搜索和刷新已接入 ViewModel（Batch 1）

### 4.2 Git 仓库卡片网格
- 3 列布局，卡片 120×80dp，圆角 16px
- 蓝色文件夹图标 + 项目名称（14sp Bold）+ 分支名称（12sp 灰色）
- 卡片右上角 ⚡ 图标 → 打开 Git 管理面板
- 搜索过滤
- **状态：** ✅ UI 完成 + Git 入口 + 搜索过滤（Batch 1 + 5）

### 4.3 路径浏览区域
- "Browse from Path" 标题
- 路径输入框：当前路径 + 复制按钮（clipboardManager）
- 目录列表：蓝色文件夹图标 + 目录名
- 搜索过滤目录
- **状态：** ✅ UI 完成 + clipboard + 搜索过滤（Batch 1 + 5）

### 4.4 新建会话按钮
- 蓝色 #3B82F6 按钮，48dp 高，圆角 12px
- "+ New Session Here" 白色文字
- 点击在当前路径创建新 Claude Code 会话
- **状态：** ✅ createSessionAtPath() 已接入 SessionRepository.createSession()（Batch 1）

### 4.5 Git 管理面板
- 顶部 Tab：Changes / Stash / Commits / Diff / Tree
- 分支显示 + Push 按钮（显示 ahead 计数）
- 变更文件列表：M(黄)/A(绿)/D(红) 状态标识 + 行数变化
- Stash 列表卡片
- Commits 时间线（蓝色等宽 hash）
- Diff 代码对比预览（CodeBlockBg 等宽字体 +/-行）
- Tree 缩进文件树（📁📄）
- **状态：** ✅ 已完成 — GitPanelScreen + GitPanelViewModel + GitModels（Batch 5）

### 4.6 文件搜索
- 搜索栏（TextField + Close 按钮），toggleSearch 切换
- 文件名/路径过滤
- getFilteredRepos / getFilteredDirs 实时过滤
- **状态：** ✅ 已完成（Batch 5）

### 4.7 文件操作
- 复制文件路径（clipboardManager）
- 长按文件显示操作菜单
- **状态：** ⚠️ 路径复制已实现，长按操作菜单未做

---

## 五、Browser 标签页（Web Proxy）

### 5.1 页面头部
- "Web Proxy" 标题 20sp Bold + 电脑终端图标
- "Access local dev servers and websites remotely" 说明文字
- **状态：** ✅ UI 完成

### 5.2 添加代理
- **Add Port Proxy：** 紫色插头图标 + 文字，点击弹出端口号输入 AlertDialog
- **Add Website：** 紫色地球图标 + 文字，点击弹出 URL 输入 AlertDialog
- 输入验证 + 添加后插入代理列表
- **状态：** ✅ 已完成 — 卡片 clickable + AlertDialog 弹窗（Batch 1）

### 5.3 Auto-proxy 开关
- "Proxies" 标题 + "Auto-proxy ports" 说明 + Switch 开关
- 蓝色滑块配色
- **状态：** ✅ 开关可切换本地状态

### 5.4 代理条目列表
- **正常状态：** 绿色圆点 + 地址（白色）+ 完整地址（灰色）+ 箭头（clickable→refresh）
- **错误状态：** 红色圆点 + 错误消息（等宽字体 11sp）+ 浏览器工具栏
- **状态：** ✅ UI 完成 + clickable 交互已接线（Batch 1）

### 5.5 浏览器工具栏
- Close / Back / Forward / Refresh / Block / Keyboard
- 灰色图标，18dp
- Close→closeProxy / Refresh→retryProxy / 箭头→refreshProxy
- **状态：** ✅ 图标事件已接线（Batch 1）

### 5.6 代理详情/内嵌浏览器
- 点击代理条目进入 WebView
- 显示代理的网页内容
- **状态：** ❌ 未实现

### 5.7 代理状态监控
- 定时检测代理端口是否可达
- 自动更新状态指示灯
- **状态：** ❌ 未实现

---

## 六、Team 标签页（团队协作）

### 6.1 团队页面
- 邀请码卡片（复制 + 刷新）
- 在线/离线成员列表（头像首字母 + 角色 + 在线绿点）
- 共享会话列表（owner + 成员数 + 相对时间）
- **状态：** ✅ 已完成 — TeamScreen + TeamViewModel（Batch 6）

### 6.2 团队协作高级功能（PRD P2，待设计）
- 多人同时查看同一会话
- 用户在线状态
- 消息已读/未读状态
- 权限控制（只读/读写）
- **状态：** ❌ 未开始

---

## 七、通知系统

### 7.1 本地推送通知
- 任务完成通知
- 通知渠道管理（Android NotificationChannel）
- **状态：** ⚠️ NotificationHelper + NotificationChannelManager 已实现，未接入任务完成事件

### 7.2 Firebase Cloud Messaging (FCM)
- 远程推送通知（App 在后台时）
- FCM Token 管理 + 上报
- **状态：** ✅ FcmTokenManager + BteloFirebaseMessagingService + BootReceiver 已实现

### 7.3 任务完成弹窗
- 蓝色渐变对勾图标（64dp）
- 任务标题（白色 20sp Bold）
- 说明文字（灰色 14sp）
- "· TASK COMPLETED" 标签（金色 #F59E0B）
- "View Session" 按钮（金色渐变）
- "Stop Alarm" 按钮
- **状态：** ✅ 已完成 — TaskCompleteDialog + AgentsViewModel（Batch 3）

### 7.4 通知设置页
- 通知开关总控 + 权限检查（Android 13+ POST_NOTIFICATIONS）
- 声音/震动/呼吸灯独立开关
- 免打扰时段 + TimePicker
- **状态：** ✅ 已完成 — NotificationSettingsScreen（Batch 2）

---

## 八、安全与加密

### 8.1 端到端加密 (E2EE)
- X25519 密钥交换 → HKDF 派生 → ChaCha20-Poly1305 AEAD
- 公钥通过 WebSocket 交换
- **状态：** ✅ CryptoManager + KeyPair + SecureKeyStore 已实现（Android 端）
- **注意：** 服务器端加密被注释/忽略（"we use plaintext"）

### 8.2 密钥轮换
- 每个会话独立密钥版本
- 自动轮换密钥
- **状态：** ✅ KeyRotationManager 已实现

### 8.3 Token 安全存储
- Android Keystore 存储敏感 Token
- SharedPreferences 加密存储
- **状态：** ✅ DataStoreManager + SecureKeyStore 已实现

---

## 九、数据持久化

### 9.1 Room 数据库
- Sessions 表 + Messages 表 + Devices 表
- **状态：** ✅ v3 版本，MIGRATION_2_3 完成

### 9.2 数据同步
- 从服务器同步会话列表
- 从服务器同步消息历史
- **状态：** ⚠️ SyncManager + SyncApi 已实现

---

## 十、基础设施

### 10.1 网络层
- WebSocket 连接管理（OkHttp）
- 自动重连 + 心跳保活
- **状态：** ✅ WebSocketClientFactory + EnhancedWebSocketClient + NetworkMonitor

### 10.2 消息协议
- 类型区分的 JSON 消息
- Gson 序列化/反序列化
- **状态：** ✅ MessageProtocol 已完成

### 10.3 依赖注入
- Hilt DI 框架
- **状态：** ✅ 完成

### 10.4 日志系统
- 分级日志（DEBUG / INFO / WARN / ERROR）
- **状态：** ✅ Logger 工具类

---

## 十一、总结：按优先级的功能完成度

### ✅ 已完整实现 (35 项)
连接流程、WebSocket 协议、消息序列化、会话发现、JSONL 同步、命令执行、Bridge 模式、底部导航、配色主题、领域模型、Room 数据库、依赖注入、消息气泡（含 retry/pin）、AI 流式输出、斜杠命令面板、会话标签栏、快捷操作栏、FCM 推送、端到端加密、密钥轮换、网络监控、Token 存储、**Token 计数接入**、**AI Thinking 气泡渲染**、**工具执行记录渲染**、**完成状态条**、**Skill Tags 标签栏**、**语音输入（SpeechRecognizer）**、**Provider Control 设置页**、**Files 新建会话**、**Git 管理面板**、**文件搜索**、**Browser 添加代理弹窗**、**Browser 工具栏交互**、**DevicesScreen 设备管理**、**TaskCompleteDialog 任务完成弹窗**、**TeamScreen 团队页面**、**NotificationSettings 通知设置**

### ⚠️ UI/逻辑部分完成 (6 项)
手动配对码引导、连接状态 UI 展示、Files 真实文件系统数据、Browser 真实代理数据、通知系统接入任务完成事件、SessionList 设置区/多选

### ❌ 未开始 (5 项)
Browser 内嵌浏览器 WebView、Browser 代理状态监控、Team 多人实时协作、文件长按操作菜单、文件内容预览

---

*文档版本：v2.0 | 2026-04-29（同步 6 个 Batch 开发完成）*
