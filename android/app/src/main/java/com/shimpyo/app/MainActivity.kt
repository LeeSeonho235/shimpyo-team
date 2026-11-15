package com.shimpyo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.shimpyo.app.gleo.StartRestAction

/**
 * 진입 화면. UI(Compose 화면 6개)는 demo-web 시안을 기준으로 2단계에서 옮긴다.
 * ui/ 패키지의 Screens.kt 에 화면 목록만 정의해 두었다.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleGleo(intent)
        // TODO: setContent { ShimpyoApp(...) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleGleo(intent)
    }

    private fun handleGleo(intent: Intent?) {
        val req = StartRestAction.parse(intent) ?: return
        // 1단계: 차량(Brake)과 세션이 아직 연결되지 않아 주차 여부를 알 수 없다 → null 로 넘겨 실패 응답
        // TODO(2단계): VehicleRepository.parkingBrakeOn 의 최신 값 사용
        val parked: Boolean? = null
        if (StartRestAction.respond(this, req, parked)) {
            // TODO(2단계): RestSessionController.start(...) 연결. 연결 전에는 여기에 도달하지 않는다
        }
    }
}
