package com.lty.myapplication.web

import com.google.gson.JsonElement

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:
 */
object ActionDispatcher {
    private val handlers = mutableMapOf<String, ActionHandler>()

    init {
        // 在这里注册你的业务模块
        handlers["getLocation"] = GetLocationHandler()
//        handlers["open"] = OpenPageHandler()
//        handlers["wxpay"] = WxPayHandler()
    }

    // 分发并执行
    suspend fun dispatch(action: String, params: JsonElement?): Any? {
        val handler = handlers[action]
            ?: throw IllegalArgumentException("Android 端未实现该 Action: $action")
        return handler.handle(params)
    }
}