package com.shimpyo.app.vehicle

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * 2단계 이후 구현할 실제 Vehicle SDK 연결부. 지금은 자리만 잡아 둔다.
 *
 * 알려진 사실(공식 문서 기준)
 * - 진입점: ai.pleos.playground.vehicle.Vehicle(context), initialize() / release()
 * - 도메인 객체는 vehicle.getEvBattery(), getBrake(), getSeat(), getHVAC(), getLight() 등으로 얻는다.
 * - 모든 호출은 비동기(onComplete / onFailed). 콜백이 끝나기 전에 release() 하면 안 된다.
 * - Set 에는 onComplete 가 없다. 완료는 Callback 리스너나 Get 으로 확인한다.
 *
 * 도메인별 함수 이름은 API Reference 에서 확인한 뒤 채운다. 추측해서 쓰지 않는다.
 * 콜백은 suspendCancellableCoroutine 으로 감싸 suspend 함수로 바꿀 계획이다.
 */
@Suppress("UNUSED_PARAMETER")
class PleosVehicleRepository(context: Context) : VehicleRepository {

    // Vehicle
    override fun connect() = TODO("API Reference 확인 후 연결")
    override fun disconnect() = TODO("API Reference 확인 후 연결")

    // Seat, HVAC, Light — checkXxxCapabilities
    override suspend fun getCapabilities(): VehicleCapabilities = TODO("API Reference 확인 후 연결")

    // EvBattery — EvChargeState 리스너
    override val chargeState: Flow<ChargeState> get() = TODO("API Reference 확인 후 연결")

    // EvBattery — 충전량
    override val batteryLevel: Flow<Int?> get() = TODO("API Reference 확인 후 연결")

    // Brake — 주차 브레이크
    override val parkingBrakeOn: Flow<Boolean?> get() = TODO("API Reference 확인 후 연결")

    // Door — 잠금 상태
    override suspend fun isDoorLocked(): Boolean? = TODO("API Reference 확인 후 연결")

    // Odometer — TripStatistics
    override suspend fun getTripDrivingMinutes(): Int? = TODO("API Reference 확인 후 연결")

    // Seat
    override suspend fun getSeatRecline(): Int? = TODO("API Reference 확인 후 연결")
    override suspend fun setSeatRecline(value: Int): Result<Unit> = TODO("API Reference 확인 후 연결")

    // HVAC
    override suspend fun getCabinTemperature(): Float? = TODO("API Reference 확인 후 연결")
    override suspend fun setCabinTemperature(value: Float): Result<Unit> = TODO("API Reference 확인 후 연결")
    override suspend fun getFanSpeed(): Int? = TODO("API Reference 확인 후 연결")
    override suspend fun setFanSpeed(value: Int): Result<Unit> = TODO("API Reference 확인 후 연결")

    // Light
    override suspend fun getAmbientBrightness(): Int? = TODO("API Reference 확인 후 연결")
    override suspend fun setAmbientBrightness(value: Int): Result<Unit> = TODO("API Reference 확인 후 연결")
}
