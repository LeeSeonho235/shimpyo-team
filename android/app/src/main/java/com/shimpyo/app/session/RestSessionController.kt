package com.shimpyo.app.session

import com.shimpyo.app.vehicle.ChargeState
import com.shimpyo.app.vehicle.VehicleCapabilities
import com.shimpyo.app.vehicle.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 휴식 세션 상태 관리.
 *
 *   Idle → Suggesting → Preparing → Resting → Waking → Restoring → Idle
 *
 * 어느 상태에서든 아래 상황이면 바로 Restoring 으로 간다.
 * - 주차 브레이크 해제 (Brake)
 * - ERROR_DRIVING_RESTRICTED 수신
 * - 사용자의 종료 버튼 또는 음성 종료 요청 (Gleo END_REST_MODE)
 */
class RestSessionController(
    private val vehicle: VehicleRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<RestState>(RestState.Idle)
    val state: StateFlow<RestState> = _state

    var mode: RestMode = RestMode.BASIC
        private set

    /** 휴식 전 값. 성공적으로 바꾼 항목만 복귀 대상이 된다. 2단계에서 로컬 저장소에도 남긴다 */
    private val saved = SavedSettings()

    fun observeVehicle() {
        // EvBattery — 충전 시작 감지 → 제안, 충전 종료 → 기상
        vehicle.chargeState.onEach { cs ->
            when {
                cs == ChargeState.CHARGING && _state.value == RestState.Idle -> _state.value = RestState.Suggesting
                cs == ChargeState.COMPLETED && _state.value is RestState.Resting -> wake()
            }
        }.launchIn(scope)

        // Brake — 휴식 중 주차 브레이크가 풀리면 즉시 복귀
        vehicle.parkingBrakeOn.onEach { on ->
            if (on == false && _state.value.isActive) restore(reason = EndReason.PARKING_BRAKE_RELEASED)
        }.launchIn(scope)
    }

    /** 휴식 시작. 정차 상태(주차 브레이크 체결)에서만 호출된다는 전제 */
    fun start(type: RestType, durationMinutes: Int?) = scope.launch {
        _state.value = RestState.Preparing
        val caps = vehicle.getCapabilities()
        mode = RestMode.from(caps)

        // 1) 현재 값 저장 (null 이면 제어·복귀 대상에서 제외)
        if (mode == RestMode.FULL) saved.seat = vehicle.getSeatRecline()
        if (mode != RestMode.BASIC) {
            saved.temperature = vehicle.getCabinTemperature()
            saved.fan = vehicle.getFanSpeed()
            saved.light = vehicle.getAmbientBrightness()
        }

        // 2) 좌석 → 공조 → 조명 순서로 설정. 하나가 실패해도 계속 진행
        caps.seat?.let { r -> saved.seat?.let { if (vehicle.setSeatRecline(r.clamp(r.max)).isFailure) saved.seat = null } }
        caps.climate?.let { saved.temperature?.let { if (vehicle.setCabinTemperature(22f).isFailure) saved.temperature = null } }
        caps.light?.let { r -> saved.light?.let { if (vehicle.setAmbientBrightness(r.clamp(r.min + 1)).isFailure) saved.light = null } }

        // 3) 휴식 진행 (TextToSpeech SDK 로 안내 음성 — 의존성 좌표 미확인)
        _state.value = RestState.Resting(type, durationMinutes)
    }

    fun wake() {
        _state.value = RestState.Waking
        // Light — 조명을 단계적으로 밝힘 (밝기 단계 제어 가능 여부 미확인)
        restore(reason = EndReason.COMPLETED)
    }

    fun end() = restore(reason = EndReason.USER_REQUEST)

    fun onDrivingRestricted() = restore(reason = EndReason.DRIVING_RESTRICTED)

    private fun restore(reason: EndReason) = scope.launch {
        _state.value = RestState.Restoring(reason)
        saved.seat?.let { vehicle.setSeatRecline(it) }
        saved.temperature?.let { vehicle.setCabinTemperature(it) }
        saved.fan?.let { vehicle.setFanSpeed(it) }
        saved.light?.let { vehicle.setAmbientBrightness(it) }
        saved.clear()
        _state.value = RestState.Idle
    }
}

sealed interface RestState {
    data object Idle : RestState
    data object Suggesting : RestState
    data object Preparing : RestState
    data class Resting(val type: RestType, val durationMinutes: Int?) : RestState
    data object Waking : RestState
    data class Restoring(val reason: EndReason) : RestState

    val isActive get() = this is Preparing || this is Resting || this is Waking
}

enum class RestType { NAP, BREATHING, STRETCHING }

enum class EndReason { COMPLETED, USER_REQUEST, PARKING_BRAKE_RELEASED, DRIVING_RESTRICTED }

/** capability 결과로 정하는 동작 모드 */
enum class RestMode {
    /** 좌석·공조·조명 모두 READ_WRITE */
    FULL,
    /** 좌석 제어 불가, 공조나 조명은 가능 → "좌석은 직접 조절해 주세요" */
    PARTIAL,
    /** 조회만 가능 → 충전 연동 타이머, 음성 안내, 맞춤 기상 */
    BASIC;

    companion object {
        fun from(c: VehicleCapabilities) = when {
            c.seat != null && c.climate != null && c.light != null -> FULL
            c.climate != null || c.light != null -> PARTIAL
            else -> BASIC
        }
    }
}

private class SavedSettings(
    var seat: Int? = null,
    var temperature: Float? = null,
    var fan: Int? = null,
    var light: Int? = null,
) {
    fun clear() { seat = null; temperature = null; fan = null; light = null }
}
