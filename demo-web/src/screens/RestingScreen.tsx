import { useEffect, useRef, useState } from 'react'
import { DEMO_SPEED } from '../demo'
import { REST_LABEL, Session } from '../session'
import { FakeVehicle, useVehicle } from '../vehicle/FakeVehicle'

/** 4. 휴식 중 — 거의 검은 화면. 남은 시간과 충전량만 표시 */
export default function RestingScreen({ session, onEnd }: { session: Session; onEnd: () => void }) {
  const v = useVehicle()
  const totalSec = useRef(
    (session.duration === 'untilCharged' ? Math.max(1, FakeVehicle.minutesToTarget()) : session.duration) * 60,
  ).current
  const [left, setLeft] = useState(totalSec)

  useEffect(() => {
    const t = window.setInterval(() => setLeft((s) => Math.max(0, s - DEMO_SPEED)), 1000)
    return () => window.clearInterval(t)
  }, [])

  // 타이머 종료, 또는 휴식 중에 목표 충전량에 도달하면 기상 화면으로
  const startedBelowTarget = useRef(FakeVehicle.get().batteryLevel < FakeVehicle.get().targetLevel).current
  useEffect(() => {
    if (left === 0 || (startedBelowTarget && v.batteryLevel >= v.targetLevel)) onEnd()
  }, [left, v.batteryLevel, v.targetLevel, onEnd, startedBelowTarget])

  const mm = String(Math.floor(left / 60)).padStart(2, '0')
  const ss = String(left % 60).padStart(2, '0')

  return (
    <main className="screen rest">
      <div className="rest__label">{REST_LABEL[session.restType]} 중</div>
      <div className="rest__time">
        {mm}:{ss}
      </div>
      <div className="rest__battery">
        충전 {Math.floor(v.batteryLevel)}% <span className="rest__sep">/</span> 목표 {v.targetLevel}%
      </div>
      <div className="rest__bar">
        <div className="rest__fill" style={{ width: `${(v.batteryLevel / v.targetLevel) * 100}%` }} />
      </div>
      <button className="btn btn--dim" onClick={onEnd}>
        휴식 끝내기
      </button>
    </main>
  )
}
