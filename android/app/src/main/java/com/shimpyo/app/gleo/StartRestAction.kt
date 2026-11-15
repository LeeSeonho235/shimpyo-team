package com.shimpyo.app.gleo

import android.content.Context
import android.content.Intent
import org.json.JSONObject

/**
 * START_REST_MODE (component "activity") 처리.
 * MainActivity 의 onCreate 와 onNewIntent 양쪽에서 호출한다
 * (이미 실행 중이면 onNewIntent 로 들어올 수 있음).
 *
 * 좌석 이동은 7초보다 오래 걸리므로 모드 시작 직후 바로 Callback 을 보내고,
 * 좌석·공조·조명 제어는 그 뒤에 진행한다.
 */
object StartRestAction {

    data class Request(val restType: String?, val durationMinutes: Int?)

    fun parse(intent: Intent?): Request? {
        if (intent?.action != GleoFunctions.START_REST_MODE) return null
        val json = runCatching { JSONObject(intent.getStringExtra("ACTION_PARAM") ?: "{}") }.getOrNull() ?: return null
        return Request(
            restType = json.optString("restType").ifBlank { null },
            durationMinutes = if (json.has("durationMinutes")) json.optInt("durationMinutes") else null,
        )
    }

    /**
     * @param isParked 주차 브레이크 상태. null 이면 확인 불가로 보고 시작하지 않는다
     * @return 휴식을 시작해도 되면 true. 호출한 쪽은 true 일 때만 세션을 시작한다
     */
    fun respond(context: Context, req: Request, isParked: Boolean?): Boolean {
        val responder = GleoResponder(context)
        return when {
            req.restType == null -> {
                responder.send(
                    GleoFunctions.START_REST_MODE, GleoStatus.ASKING,
                    "The rest type is required to start rest mode.",
                    data = """[{"missingField":"restType"}]""",
                )
                false
            }
            isParked == null -> {
                // 차량 상태를 읽을 수 없으면 시작했다고 응답하지 않는다
                responder.send(
                    GleoFunctions.START_REST_MODE, GleoStatus.FAIL,
                    "The parking state cannot be checked, so rest mode was not started.",
                )
                false
            }
            !isParked -> {
                responder.send(
                    GleoFunctions.START_REST_MODE, GleoStatus.FAIL,
                    "The vehicle must be parked to start rest mode.",
                )
                false
            }
            else -> {
                // 쉼표가 직접 호흡 안내 음성을 재생하므로 silent
                responder.send(
                    GleoFunctions.START_REST_MODE, GleoStatus.SUCCESS,
                    "Rest mode has started.", mode = GleoMode.SILENT,
                    data = """[{"restType":"${req.restType}","durationMinutes":${req.durationMinutes ?: "null"}}]""",
                )
                true
            }
        }
    }
}
