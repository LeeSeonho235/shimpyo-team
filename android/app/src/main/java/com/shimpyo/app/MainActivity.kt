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
        val parked = true // TODO: VehicleRepository.parkingBrakeOn 값으로 대체 (Brake)
        if (StartRestAction.respond(this, req, parked)) {
            // TODO: RestSessionController.start(...) 연결
        }
    }
}
