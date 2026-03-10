# WebBridgeSample

> 中文文档请查看 [README_CN.md](README_CN.md)

A lightweight Android sample project demonstrating a **production-ready WebView + JSBridge** architecture. A Vite-built web frontend is embedded inside Android via `WebViewAssetLoader`, and a structured bridge (`KiteJSJsInterface`) provides type-safe, coroutine-based communication between JavaScript and native Kotlin code.

---

## Features

- 🌉 **Structured JSBridge** — `action` + `callId` + `params` protocol, fully Promise-compatible on the JS side
- ⚡ **Coroutine-driven** — every native handler runs on `Dispatchers.Default`, no UI-thread blocking
- 🔌 **Plugin-style handlers** — add a new native capability by implementing one interface and registering one line
- 🔒 **Secure asset loading** — uses `WebViewAssetLoader` instead of `file://` to load bundled HTML/JS/CSS under a virtual HTTPS domain
- 🛠️ **Gson utilities** — reified inline extensions (`toJson` / `fromJson`) eliminate boilerplate serialization code

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target / Compile SDK | 36 |
| Bridge serialization | Gson 2.13.2 |
| Logging | Timber 5.0.1 |
| WebView compat | AndroidX WebKit 1.8.0 |
| Frontend bundler | Vite (output placed in `assets/`) |

---

## Project Structure

```
app/src/main/
├── assets/
│   └── index.html          # Vite production build entry point
├── java/com/lty/myapplication/
│   ├── MainActivity.kt     # WebView setup, AssetLoader, bridge mounting
│   ├── utils/
│   │   └── gson.kt         # Global Gson instance + reified extension functions
│   └── web/
│       ├── BridgeRequest.kt     # JS → Native request / Native → JS response DTOs
│       ├── ActionHandler.kt     # Interface all native handlers must implement
│       ├── ActionDispatcher.kt  # Registry: maps action strings to handlers
│       ├── KiteJSInterface.kt # @JavascriptInterface entry point, coroutine orchestration
│       └── GetLocationHandler.kt # Example handler: "getLocation" action
```

---

## JSBridge Protocol

### JS → Android

Call `window.NativeInterface.jsCallAndroidMethod(jsonString)` with the following payload:

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

### Android → JS callback

The native side calls back via `window.Kite.onSuccess(callId, response)` or `window.Kite.onFail(callId, response)`:

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

## How to Add a New Native Action

1. **Create a handler** that implements `ActionHandler`:

```kotlin
class OpenPageHandler : ActionHandler {
    data class Params(val url: String)

    override suspend fun handle(params: JsonElement?): Any? {
        val p = params.fromJson<Params>() ?: return null
        // ... open page logic
        return mapOf("opened" to true)
    }
}
```

2. **Register it** in `ActionDispatcher.init {}`:

```kotlin
handlers["open"] = OpenPageHandler()
```

That's it — the bridge automatically routes JS calls with `action: "open"` to your new handler.

---

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Place your Vite production build output inside `app/src/main/assets/` (ensure `index.html` is at the root)
4. Run on a device or emulator (API 24+)

> **Note:** The web frontend is loaded from `https://appassets.androidplatform.net/assets/index.html`. Your JS code should call `window.NativeInterface.jsCallAndroidMethod(...)` to communicate with native.

---

## License

MIT
