package com.shimpyo.app.session

import com.shimpyo.app.vehicle.ChargeState
import com.shimpyo.app.vehicle.VehicleCapabilities
import com.shimpyo.app.vehicle.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 휴식 세션 상태 관리. (구조 틀 단계 — 아직 빌드·에뮬레이터 검증 전)
 *
 *   Idle → Suggesting → Preparing → Resting → Waking → Restoring → Idle
 *                                                         └→ RestoreFailed (재시도 대기)
 *
 * 어느 상태에서든 아래 상황이면 진행 중인 준비·타이머를 취소하고 Restoring 으로 간다.
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

    /** 휴식 전 값. 바꾸는 데 성공한 항목만 남는다. 2단계에서 로컬 저장소에도 남긴다 */
    private val saved = SavedSettings()

    /** 준비·휴식 타이머 작업. 종료 시 반드시 취소한다 */
    private var sessionJob: Job? = null
    private var restoreJob: Job? = null

    fun observeVehicle() {
        // EvBattery — 충전 시작 감지 → 제안, 휴식 중 충전 완료 → 기상
        vehicle.chargeState.onEach { cs ->
            when {
                cs == ChargeState.CHARGING && _state.value == RestState.Idle -> _state.value = RestState.Suggesting
                cs == ChargeState.COMPLETED && _state.value is RestState.Resting -> wake()
            }
        }.launchIn(scope)

        // Brake — 세션 중 주차 브레이크가 풀리면 즉시 복귀
        vehicle.parkingBrakeOn.onEach { on ->
            if (on == false && _state.value.isActive) restore(EndReason.PARKING_BRAKE_RELEASED)
        }.launchIn(scope)
    }

    /** 휴식 시작. 호출하는 쪽에서 정차 상태(주차 브레이크 체결)를 먼저 확인한다 */
    fun start(type: RestType, durationMinutes: Int?) {
        if (_state.value.isActive || _state.value is RestState.Restoring) return
        sessionJob = scope.launch {
            _state.value = RestState.Preparing
            val caps = vehicle.getCapabilities()
            mode = RestMode.from(caps)

            // 1) 현재 값 저장 (null 이면 제어·복귀 대상에서 제외)
            val seat0 = if (caps.seat != null) vehicle.getSeatRecline() else null
            val temp0 = if (caps.climate != null) vehicle.getCabinTemperature() else null
            val fan0 = if (caps.climate != null) vehicle.getFanSpeed() else null
            val light0 = if (caps.light != null) vehicle.getAmbientBrightness() else null

            // 2) 좌석 → 공조 → 조명 순서로 설정. 성공한 항목만 복귀 대상으로 기록한다
            caps.seat?.let { r -> seat0?.let { if (vehicle.setSeatRecline(r.clamp(r.max)).isSuccess) saved.seat = it } }
            caps.climate?.let { temp0?.let { if (vehicle.setCabinTemperature(22f).isSuccess) saved.temperature = it } }
            caps.climate?.let { fan0?.let { if (vehicle.setFanSpeed(1).isSuccess) saved.fan = it } }
            caps.light?.let { r -> light0?.let { if (vehicle.setAmbientBrightness(r.clamp(r.min + 1)).isSuccess) saved.light = it } }

            // 준비 도중 취소되었으면 Resting 으로 넘어가지 않는다
            if (!isActive) return@launch
            _state.value = RestState.Resting(type, durationMinutes)
            // TODO: TextToSpeech SDK 로 안내 음성 (의존성 좌표 미확인)

            // 3) 휴식 타이머. 시간을 정하지 않았으면 충전 완료(EvBattery) 이벤트로만 깨운다
            if (durationMinutes != null) {
                delay(durationMinutes * 60_000L)
                if (_state.value is RestState.Resting) wake()
            }
        }
    }

    fun wake() {
        _state.value = RestState.Waking
        // Light — 조명을 단계적으로 밝힘 (밝기 단계 제어 가능 여부 미확인)
        restore(EndReason.COMPLETED)
    }

    fun end() = restore(EndReason.USER_REQUEST)

    fun onDrivingRestricted() = restore(EndReason.DRIVING_RESTRICTED)

    /** 실패한 복귀를 다시 시도한다 (RestoreFailed 상태에서 호출) */
    fun retryRestore() = restore(EndReason.RETRY)

    private fun restore(reason: EndReason) {
        if (restoreJob?.isActive == true) return
        // 진행 중인 준비·타이머를 먼저 멈춘다. 늦게 끝난 준비가 Resting 으로 되돌리지 않도록
        sessionJob?.cancel()
        sessionJob = null

        restoreJob = scope.launch {
            _state.value = RestState.Restoring(reason)
            // 성공한 항목만 지운다. 실패한 항목은 원래 값을 남겨 재시도할 수 있게 한다
            saved.seat?.let { if (vehicle.setSeatRecline(it).isSuccess) saved.seat = null }
            saved.temperature?.let { if (vehicle.setCabinTemperature(it).isSuccess) saved.temperature = null }
            saved.fan?.let { if (vehicle.setFanSpeed(it).isSuccess) saved.fan = null }
            saved.light?.let { if (vehicle.setAmbientBrightness(it).isSuccess) saved.light = null }
            // Set 은 완료 콜백이 없으므로 실제 완료 확인은 Get/Callback 으로 해야 한다 (2단계)

            _state.value = if (saved.isEmpty()) RestState.Idle else RestState.RestoreFailed(saved.pendingItems())
        }
    }
}

sealed interface RestState {
    data object Idle : RestState
    data object Suggesting : RestState
    data object Preparing : RestState
    data class Resting(val type: RestType, val durationMinutes: Int?) : RestState
    data object Waking : RestState
    data class Restoring(val reason: EndReason) : RestState
    /** 일부 항목 복귀 실패. 화면에 "직접 조절" 안내와 재시도 버튼을 띄운다 */
    data class RestoreFailed(val items: List<String>) : RestState

    val isActive get() = this is Preparing || this is Resting || this is Waking
}

enum class RestType { NAP, BREATHING, STRETCHING }

enum class EndReason { COMPLETED, USER_REQUEST, PARKING_BRAKE_RELEASED, DRIVING_RESTRICTED, RETRY }

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
    fun isEmpty() = seat == null && temperature == null && fan == null && light == null
    fun pendingItems() = buildList {
        if (seat != null) add("seat")
        if (temperature != null || fan != null) add("climate")
        if (light != null) add("light")
    }
}
