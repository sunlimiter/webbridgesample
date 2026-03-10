package com.lty.myapplication.web

import com.google.gson.JsonElement

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:// 接口现在接收 JsonElement，返回 Any（供 Gson 自动序列化）
 */
interface ActionHandler {
    suspend fun handle(params: JsonElement?): Any?
}