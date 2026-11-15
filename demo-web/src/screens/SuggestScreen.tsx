import { FakeVehicle, useVehicle } from '../vehicle/FakeVehicle'
import { BoltIcon } from '../StatusBar'
import BatteryRing from './BatteryRing'

/** 1. 휴식 제안 — 충전 시작(EvBattery)과 주차 브레이크 체결(Brake)을 감지했을 때 */
export default function SuggestScreen({ onStart, onLater }: { onStart: () => void; onLater: () => void }) {
  const v = useVehicle()
  const minutes = FakeVehicle.minutesToTarget(v)
  return (
    <main className="screen screen--split">
      <section className="suggest">
        <span className="chip chip--accent">
          <BoltIcon /> 충전이 시작됐어요
        </span>
        <h1 className="headline">
          {v.targetLevel}%까지 약 {minutes}분 남았습니다.
          <br />
          휴식 모드를 시작할까요?
        </h1>
        <p className="sub">좌석, 온도, 조명을 한 번에 맞추고 충전이 끝날 때쯤 깨워 드려요.</p>
        <div className="actions">
          <button className="btn btn--primary" onClick={onStart}>
            시작
          </button>
          <button className="btn btn--ghost" onClick={onLater}>
            나중에
          </button>
        </div>
      </section>
      <aside className="ringbox">
        <BatteryRing level={v.batteryLevel} target={v.targetLevel} />
        <div className="ringbox__center">
          <div className="ringbox__value">{Math.floor(v.batteryLevel)}%</div>
          <div className="ringbox__label">목표 {v.targetLevel}%</div>
        </div>
        <div className="ringbox__meta">
          <span>주차 브레이크 체결됨</span>
          <span>문 잠김</span>
        </div>
      </aside>
    </main>
  )
}
