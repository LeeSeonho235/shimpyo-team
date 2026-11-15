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

        // 1단계(UI 프로토타입): 휴식 세션과 차량이 아직 연결되지 않았다.
        // 실제로 실행하지 않은 작업을 success 로 응답하지 않는다.
        // TODO(2단계): RestSessionController 연결 후 end() / 상태 조회 결과로 응답
        when (intent.action) {
            GleoFunctions.END_REST_MODE -> responder.send(
                functionName = GleoFunctions.END_REST_MODE,
                status = GleoStatus.FAIL,
                message = "Rest mode control is not connected to the vehicle yet.",
            )
            GleoFunctions.GET_REST_STATUS -> responder.send(
                functionName = GleoFunctions.GET_REST_STATUS,
                status = GleoStatus.FAIL,
                message = "Rest session status is not available yet.",
            )
        }
    }

    private companion object { const val TAG = "GleoActionReceiver" }
}
