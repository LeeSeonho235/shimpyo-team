package com.shimpyo.app.gleo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * component "service" 인 Gleo function 을 받는다 (END_REST_MODE, GET_REST_STATUS).
 * actionParameters 는 extra "ACTION_PARAM" 에 JSON 문자열로 들어온다(문서 예제 기준).
 *
 * Gleo 는 7초 뒤 Action 을 취소하므로 결과는 바로 돌려준다.
 */
class GleoActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val raw = intent.getStringExtra("ACTION_PARAM") ?: "{}"
        Log.d(TAG, "action=${intent.action} param=$raw")
        val responder = GleoResponder(context)

        when (intent.action) {
            GleoFunctions.END_REST_MODE -> {
                // TODO: RestSessionController.end() 연결. 진행 중인 휴식이 없으면 fail
                responder.send(
                    functionName = GleoFunctions.END_REST_MODE,
                    status = GleoStatus.SUCCESS,
                    message = "Rest mode has ended and the seat is being restored.",
                )
            }
            GleoFunctions.GET_REST_STATUS -> {
                // TODO: 실제 남은 시간과 충전량 연결
                responder.send(
                    functionName = GleoFunctions.GET_REST_STATUS,
                    status = GleoStatus.SUCCESS,
                    message = "The rest session status was retrieved.",
                    data = """[{"remainingMinutes":12,"batteryLevel":74}]""",
                )
            }
        }
    }

    private companion object { const val TAG = "GleoActionReceiver" }
}
