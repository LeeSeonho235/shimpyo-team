package com.shimpyo.app.vehicle

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * 1단계(UI 프로토타입)용 가짜 구현.
 * 충전 중 상태에서 충전량이 시간에 따라 오른다. 차량과 연결되지 않는다.
 */
class FakeVehicleRepository(
    private val capabilities: VehicleCapabilities = VehicleCapabilities(
        seat = ControlRange(0, 100),
        climate = ControlRange(16, 30),
        light = ControlRange(0, 10),
    ),
) : VehicleRepository {

    private var seat = 20
    private var temp = 24f
    private var fan = 3
    private var light = 7

    override fun connect() = Unit
    override fun disconnect() = Unit

    override suspend fun getCapabilities() = capabilities

    override val chargeState: Flow<ChargeState> = MutableStateFlow(ChargeState.CHARGING)

    override val batteryLevel: Flow<Int?> = flow {
        var level = 63
        while (level < 100) {
            emit(level)
            delay(10_000)
            level++
        }
        emit(100)
    }

    override val parkingBrakeOn: Flow<Boolean?> = MutableStateFlow(true)

    override suspend fun isDoorLocked(): Boolean? = true
    override suspend fun getTripDrivingMinutes(): Int? = 124

    override suspend fun getSeatRecline(): Int? = seat
    override suspend fun setSeatRecline(value: Int) = runCatching { seat = value }

    override suspend fun getCabinTemperature(): Float? = temp
    override suspend fun setCabinTemperature(value: Float) = runCatching { temp = value }

    override suspend fun getFanSpeed(): Int? = fan
    override suspend fun setFanSpeed(value: Int) = runCatching { fan = value }

    override suspend fun getAmbientBrightness(): Int? = light
    override suspend fun setAmbientBrightness(value: Int) = runCatching { light = value }
}
