package com.lty.myapplication.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:
 */
// 1. 全局共享一个 Gson 实例，避免频繁创建消耗内存
// 如果你的项目有特定的 Gson 配置（如忽略空值、格式化时间），可以在这里通过 GsonBuilder 统一配置
val globalGson: Gson by lazy {
    GsonBuilder()
        // .serializeNulls() // 可选配置：是否序列化 null 值
        .disableHtmlEscaping() // 可选配置：防止特殊字符被转义
        .create()
}

/**
 * 将任意对象转化为 JSON 字符串
 * 使用示例: val jsonStr = user.toJson()
 */
fun Any?.toJson(): String {
    if (this == null) return ""
    return try {
        globalGson.toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

/**
 * 将 JSON 字符串解析为指定类型的对象
 * 利用 inline 和 reified 完美保留泛型类型，无需手动传递 Class 或 TypeToken
 * 使用示例: val user = jsonStr.fromJson<User>()
 * 使用示例: val userList = jsonStr.fromJson<List<User>>()
 */
inline fun <reified T> String?.fromJson(): T? {
    if (this.isNullOrBlank()) return null
    return try {
        // 获取真实的泛型 Type
        val type = object : TypeToken<T>() {}.type
        globalGson.fromJson(this, type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 针对前面提到的 JsonElement 的特殊解析支持 (由于前面 JSBridge 用到了 JsonElement)
 */
inline fun <reified T> com.google.gson.JsonElement?.fromJson(): T? {
    if (this == null || this.isJsonNull) return null
    return try {
        val type = object : TypeToken<T>() {}.type
        globalGson.fromJson(this, type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}