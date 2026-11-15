/**
 * 시연용 가짜 차량 데이터.
 * 실제 앱에서는 VehicleRepository(Android) 뒤에서 Vehicle SDK의
 * EvBattery, Brake, Seat, HVAC, Light 도메인을 호출한다.
 * 이 모듈은 브라우저 시연만을 위한 것이며 차량과 연결되지 않는다.
 */
import { useEffect, useState } from 'react'

export interface VehicleState {
  charging: boolean
  batteryLevel: number // %
  targetLevel: number // %
  parkingBrakeOn: boolean
  doorLocked: boolean
  cabinTemp: number // ℃
  tripDrivingMinutes: number
}

// 시연에서 충전량이 눈에 보이게 오르도록 실제보다 빠르게 증가시킨다.
const PERCENT_PER_SECOND = 0.12

let state: VehicleState = {
  charging: true,
  batteryLevel: 63,
  targetLevel: 80,
  parkingBrakeOn: true,
  doorLocked: true,
  cabinTemp: 24,
  tripDrivingMinutes: 124,
}

const listeners = new Set<(s: VehicleState) => void>()
let timer: number | undefined

function tick() {
  if (state.charging && state.batteryLevel < 100) {
    state = { ...state, batteryLevel: Math.min(100, state.batteryLevel + PERCENT_PER_SECOND) }
    listeners.forEach((l) => l(state))
  }
}

export const FakeVehicle = {
  get: () => state,
  set(patch: Partial<VehicleState>) {
    state = { ...state, ...patch }
    listeners.forEach((l) => l(state))
  },
  subscribe(l: (s: VehicleState) => void) {
    listeners.add(l)
    if (timer === undefined) timer = window.setInterval(tick, 1000)
    return () => {
      listeners.delete(l)
    }
  },
  /** 목표 충전량까지 남은 시간(분) 추정. 실제 앱도 SDK가 값을 주지 않으면 이렇게 추정할 계획 */
  minutesToTarget(s: VehicleState = state) {
    const remain = Math.max(0, s.targetLevel - s.batteryLevel)
    return Math.round(remain * 1.5) // 예시 값: 1%당 약 1.5분
  },
}

export function useVehicle() {
  const [s, setS] = useState<VehicleState>(FakeVehicle.get())
  useEffect(() => FakeVehicle.subscribe(setS), [])
  return s
}
