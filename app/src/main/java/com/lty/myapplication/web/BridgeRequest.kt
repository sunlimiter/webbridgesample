package com.lty.myapplication.web

import com.google.gson.JsonElement

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:
 */
// 接收 JS 发来的请求结构
data class BridgeRequest(
    val action: String?,
    val params: JsonElement?,
    val callId: String?
)

// 发送给 JS 的响应结构
data class BridgeResponse(
    val code: Int,
    val message: String? = null,
    val data: Any? = null // Any 类型，Gson 序列化时会自动解析
)