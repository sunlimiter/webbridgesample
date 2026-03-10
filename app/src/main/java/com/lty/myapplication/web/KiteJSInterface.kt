package com.lty.myapplication.web

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.JsonElement
import com.lty.myapplication.utils.fromJson
import com.lty.myapplication.utils.toJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:
 */
class KiteJSInterface(
    private val webView: WebView,
    private val coroutineScope: CoroutineScope
) {
    // 实例化 Gson

    @JavascriptInterface
    fun jsCallAndroidMethod(jsonString: String) {
        coroutineScope.launch(Dispatchers.Default) {
            try {
                // 1. 一行代码完成 JSON 反序列化
                val request = jsonString.fromJson<BridgeRequest>() ?: throw InstantiationException("解析或分发失败")

                val action = request.action
                val callId = request.callId ?: ""
                val params = request.params

                if (action.isNullOrEmpty()) {
                    Timber.w("收到无效的请求: action 为空")
                    return@launch
                }

                Timber.d("收到 JS 请求: action=$action, callId=$callId")
                handleAction(action, params, callId)

            } catch (e: Exception) {
                Timber.e(e, "JSON 解析或分发失败: $jsonString")
                // 如果能解析出 callId，尽可能回传失败信息给 Web
                // 此处为了严谨，实际业务中可根据正则提取 callId 兜底
            }
        }
    }

    private suspend fun handleAction(action: String, params: JsonElement?, callId: String) {
        try {
            // 2. 将 Gson 的 JsonElement 传给分发器
            // 此时 resultData 可以是任意的 Kotlin Data Class
            val resultData = ActionDispatcher.dispatch(action, params)

            sendCallbackToWeb(callId, isSuccess = true, data = resultData)

        } catch (e: Exception) {
            Timber.e(e, "业务执行异常: action=$action")
            // 失败时，构造一个包含错误信息的 map 或专门的 Error Data Class
            val errorData = mapOf("message" to (e.message ?: "未知业务异常"))
            sendCallbackToWeb(callId, isSuccess = false, data = errorData)
        }
    }

    private suspend fun sendCallbackToWeb(callId: String, isSuccess: Boolean, data: Any?) {
        if (callId.isEmpty()) return

        // 3. 构造响应对象
        val response = BridgeResponse(
            code = if (isSuccess) 200 else 500,
            data = data
        )

        // 4. 一行代码序列化为 JSON 字符串
        val responseJson = response.toJson()

        withContext(Dispatchers.Main) {
            val methodName = if (isSuccess) "onSuccess" else "onFail"
            val jsCode = "javascript:window.Kite.$methodName('$callId', $responseJson);"
            webView.evaluateJavascript(jsCode, null)
        }
    }
}