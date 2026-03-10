package com.lty.myapplication.web

import com.google.gson.JsonElement
import com.lty.myapplication.utils.fromJson
import com.lty.myapplication.utils.toJson
import kotlinx.coroutines.delay

/**
 * @author: lty
 * @date: 2026/03/10
 * @description:
 */
class GetLocationHandler( ) : ActionHandler {

    // 1. 定义专属的入参实体
    data class LocationParams(val accuracy: String = "normal", val timeout: Int = 3000)

    // 2. 定义专属的返回值实体
    data class LocationResult(val lng: String, val lat: String, val accuracy: String)

    override suspend fun handle(params: JsonElement?): Any {
        // 3. 利用 Gson 将笼统的 JsonElement 转化为强类型的 LocationParams
        val requestParams = if (params != null) {
            params.fromJson<LocationParams>()
        } else {
            LocationParams() // 默认参数
        }

        // 模拟耗时定位...
        delay(1000)

        // 4. 直接返回 Kotlin 对象！外层的 sendCallbackToWeb 会自动把它转成 JSON
        return LocationResult(
            lng = "116.405285",
            lat = "39.904989",
            accuracy = requestParams!!.accuracy
        )
    }
}