# WebBridgeSample（中文说明）

> English documentation: [README.md](README.md)

本项目是一个轻量级 Android 示例，展示了**生产可用的 WebView + JSBridge** 架构。通过 `WebViewAssetLoader` 将 Vite 构建的 Web 前端内嵌进 Android，并借助结构化的桥接层（`KiteJSJsInterface`）实现 JavaScript 与原生 Kotlin 代码之间的类型安全、协程驱动的双向通信。

---

## 核心特性

- 🌉 **结构化 JSBridge** — 采用 `action` + `callId` + `params` 协议，JS 侧可直接封装为 Promise
- ⚡ **协程驱动** — 所有原生 Handler 均运行于 `Dispatchers.Default`，不阻塞 UI 线程
- 🔌 **插件式 Handler** — 实现一个接口、注册一行代码，即可快速扩展任意原生能力
- 🔒 **安全资源加载** — 使用 `WebViewAssetLoader` 替代 `file://`，将本地 HTML/JS/CSS 以虚拟 HTTPS 域名加载
- 🛠️ **Gson 工具扩展** — 通过 reified 内联扩展函数（`toJson` / `fromJson`）消除序列化模板代码

---

## 技术栈

| 层级 | 技术 |
|---|---|
| 开发语言 | Kotlin |
| 最低 SDK | 24（Android 7.0） |
| 目标 / 编译 SDK | 36 |
| 桥接序列化 | Gson 2.13.2 |
| 日志 | Timber 5.0.1 |
| WebView 兼容 | AndroidX WebKit 1.8.0 |
| 前端构建工具 | Vite（产物放入 `assets/`） |

---

## 项目结构

```
app/src/main/
├── assets/
│   └── index.html          # Vite 生产构建入口
├── java/com/lty/myapplication/
│   ├── MainActivity.kt     # WebView 初始化、AssetLoader 配置、桥接挂载
│   ├── utils/
│   │   └── gson.kt         # 全局 Gson 实例 + reified 扩展函数
│   └── web/
│       ├── BridgeRequest.kt     # JS→Native 请求 / Native→JS 响应的数据类
│       ├── ActionHandler.kt     # 所有 Handler 必须实现的接口
│       ├── ActionDispatcher.kt  # 注册表：将 action 字符串映射到对应 Handler
│       ├── KiteJSInterface.kt # @JavascriptInterface 入口，协程调度
│       └── GetLocationHandler.kt # 示例 Handler：getLocation 动作
```

---

## JSBridge 通信协议

### JS → Android

调用 `window.NativeInterface.jsCallAndroidMethod(jsonString)`，payload 格式如下：

```json
{
  "action": "getLocation",
  "callId": "cb_001",
  "params": {
    "accuracy": "high",
    "timeout": 5000
  }
}
```

### Android → JS 回调

原生侧通过 `window.Kite.onSuccess(callId, response)` 或 `window.Kite.onFail(callId, response)` 回调：

```json
{
  "code": 200,
  "data": {
    "lng": "116.405285",
    "lat": "39.904989",
    "accuracy": "high"
  }
}
```

---

## 如何新增原生 Action

1. **创建 Handler**，实现 `ActionHandler` 接口：

```kotlin
class OpenPageHandler : ActionHandler {
    data class Params(val url: String)

    override suspend fun handle(params: JsonElement?): Any? {
        val p = params.fromJson<Params>() ?: return null
        // ... 打开页面的逻辑
        return mapOf("opened" to true)
    }
}
```

2. **在 `ActionDispatcher.init {}` 中注册**：

```kotlin
handlers["open"] = OpenPageHandler()
```

仅需以上两步，桥接层会自动将 `action: "open"` 的 JS 调用路由到新 Handler。

---

## 快速上手

1. 克隆仓库
2. 用 Android Studio 打开项目
3. 将 Vite 生产构建产物放入 `app/src/main/assets/`（确保 `index.html` 在根目录）
4. 在 API 24+ 的设备或模拟器上运行

> **注意：** Web 前端通过 `https://appassets.androidplatform.net/assets/index.html` 加载。JS 代码调用 `window.NativeInterface.jsCallAndroidMethod(...)` 即可与原生通信。

---

## 开源协议

MIT
