package com.shimpyo.app.vehicle

import kotlinx.coroutines.flow.Flow

/**
 * 차량 데이터 접근 인터페이스.
 *
 * 화면과 세션 로직은 이 인터페이스만 본다.
 * - 1단계(현재): FakeVehicleRepository — 가짜 데이터
 * - 2단계 이후: PleosVehicleRepository — Vehicle SDK(ai.pleos.playground:Vehicle:2.3.2) 호출
 *
 * 값의 타입(Int, Float)과 단위는 임시다. API Reference 확인 후 실제 타입에 맞춘다.
 */
interface VehicleRepository {

    // ── 연결 ──────────────────────────────────────────────
    /** Vehicle(context).initialize() */
    fun connect()

    /** 진행 중인 비동기 콜백이 모두 끝난 뒤 Vehicle.release() 호출 */
    fun disconnect()

    // ── 기능 지원 여부 ─────────────────────────────────────
    /** Seat / HVAC / Light 의 checkXxxCapabilities → AccessType, AreaBounds */
    suspend fun getCapabilities(): VehicleCapabilities

    // ── 조회 전용 (제어 퍼미션 불필요) ──────────────────────
    /** EvBattery — 충전 상태(EvChargeState) 구독. 퍼미션 CAR_ENERGY */
    val chargeState: Flow<ChargeState>

    /** EvBattery — 현재 충전량(%). 퍼미션 CAR_ENERGY */
    val batteryLevel: Flow<Int?>

    /** Brake — 주차 브레이크 상태. 퍼미션 CAR_DRIVE */
    val parkingBrakeOn: Flow<Boolean?>

    /** Door — 잠금 상태. 퍼미션 CAR_DOORS */
    suspend fun isDoorLocked(): Boolean?

    /** Odometer — 트립 주행 시간(분). TripStatistics. 퍼미션 CAR_DRIVE */
    suspend fun getTripDrivingMinutes(): Int?

    // ── 조회와 제어 (제어 퍼미션 필요, 이름 미확인) ──────────
    /** Seat — 등받이 각도. 퍼미션 CAR_SEATS + 제어 퍼미션 */
    suspend fun getSeatRecline(): Int?
    suspend fun setSeatRecline(value: Int): Result<Unit>

    /** HVAC — 실내 온도. 퍼미션 CAR_CLIMATE + 제어 퍼미션 */
    suspend fun getCabinTemperature(): Float?
    suspend fun setCabinTemperature(value: Float): Result<Unit>

    /** HVAC — 풍량 */
    suspend fun getFanSpeed(): Int?
    suspend fun setFanSpeed(value: Int): Result<Unit>

    /** Light — 앰비언트 라이트 밝기. 퍼미션 CAR_LIGHTS + 제어 퍼미션 */
    suspend fun getAmbientBrightness(): Int?
    suspend fun setAmbientBrightness(value: Int): Result<Unit>
}

/** null 이면 해당 장치는 제어 불가(AccessType 이 READ_WRITE 가 아님) */
data class VehicleCapabilities(
    val seat: ControlRange?,
    val climate: ControlRange?,
    val light: ControlRange?,
)

/** AreaBounds 의 minValue, maxValue */
data class ControlRange(val min: Int, val max: Int) {
    fun clamp(value: Int) = value.coerceIn(min, max)
}

/** 실제 값은 SDK 의 EvChargeState 를 확인한 뒤 매핑한다 */
enum class ChargeState { NOT_CHARGING, CHARGING, COMPLETED, UNKNOWN }
