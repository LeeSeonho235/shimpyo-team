package com.shimpyo.app.gleo

import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject

object GleoFunctions {
    const val START_REST_MODE = "START_REST_MODE"
    const val END_REST_MODE = "END_REST_MODE"
    const val GET_REST_STATUS = "GET_REST_STATUS"
}

object GleoStatus {
    const val SUCCESS = "success"
    const val ASKING = "asking"
    const val FAIL = "fail"
}

object GleoMode {
    const val VOICE = "voice"
    const val BEEP = "beep"
    const val SILENT = "silent"
}

/**
 * Gleo Callback 전송. 문서 예제(sendGleoBroadcast)에 mode 를 추가한 버전.
 * - message 는 완전한 영어 문장
 * - data 는 JSON 객체 배열 "문자열"
 */
class GleoResponder(private val context: Context) {

    fun send(
        functionName: String,
        status: String,
        message: String,
        mode: String = GleoMode.VOICE,
        data: String? = null,
    ) {
        runCatching {
            val json = JSONObject().apply {
                put("functionName", functionName)
                put("status", status)
                put("message", message)
                put("mode", mode)
                data?.let { put("data", it) }
            }
            Intent(ACTION_RESPONSE).apply {
                putExtra("packageName", context.packageName)
                putExtra("response", json.toString())
            }.also(context::sendBroadcast)
        }.onFailure { Log.e(TAG, "respond failed: $it") }
    }

    private companion object {
        const val ACTION_RESPONSE = "ai.pleos.playground.intent.action.APP_ACTION_RESPONSE"
        const val TAG = "GleoResponder"
    }
}
