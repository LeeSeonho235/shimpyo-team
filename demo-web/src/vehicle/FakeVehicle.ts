/**
 * 시연용 가짜 차량 데이터.
 * 실제 앱에서는 VehicleRepository(Android) 뒤에서 Vehicle SDK의
 * EvBattery, Brake, Seat, HVAC, Light 도메인을 호출한다.
 * 이 모듈은 브라우저 시연만을 위한 것이며 차량과 연결되지 않는다.
 */
import { useEffect, useState } from 'react'
import { DEMO_SPEED, INITIAL_SCREEN } from '../demo'

export interface VehicleState {
  charging: boolean
  batteryLevel: number // %
  targetLevel: number // %
  parkingBrakeOn: boolean
  doorLocked: boolean
  cabinTemp: number // ℃
  tripDrivingMinutes: number
}

/** 시연용 충전 속도: 1%당 1.5분. 예상 시간 계산과 실제 진행이 같은 값을 쓴다 */
const MINUTES_PER_PERCENT = 1.5
const PERCENT_PER_SECOND = 1 / (MINUTES_PER_PERCENT * 60)

const START: VehicleState = {
  charging: true,
  batteryLevel: 63,
  targetLevel: 80,
  parkingBrakeOn: true,
  doorLocked: true,
  cabinTemp: 24,
  tripDrivingMinutes: 124,
}

// 기상 화면(5번)을 주소로 바로 열어 캡처할 때만 '목표 직전' 값으로 시작한다.
// 시연 흐름(1→5) 에서는 이 값을 쓰지 않는다.
let state: VehicleState = INITIAL_SCREEN === 5 ? { ...START, batteryLevel: START.targetLevel - 2 } : { ...START }

const listeners = new Set<(s: VehicleState) => void>()
let timer: number | undefined

function tick() {
  if (state.charging && state.batteryLevel < 100) {
    state = { ...state, batteryLevel: Math.min(100, state.batteryLevel + PERCENT_PER_SECOND * DEMO_SPEED) }
    listeners.forEach((l) => l(state))
  }
}

export const FakeVehicle = {
  get: () => state,
  set(patch: Partial<VehicleState>) {
    state = { ...state, ...patch }
    listeners.forEach((l) => l(state))
  },
  /** 반복 시연을 위해 시작 상태(충전 63%)로 되돌린다 */
  reset() {
    FakeVehicle.set({ ...START })
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
    return Math.ceil(remain * MINUTES_PER_PERCENT)
  },
}

export function useVehicle() {
  const [s, setS] = useState<VehicleState>(FakeVehicle.get())
  useEffect(() => FakeVehicle.subscribe(setS), [])
  return s
}
