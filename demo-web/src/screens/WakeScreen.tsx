import { useEffect, useState } from 'react'
import type { Mode } from '../App'
import { useVehicle } from '../vehicle/FakeVehicle'

/** 5. 기상과 복귀 — 조명을 밝히고 저장해 둔 좌석·공조 값으로 되돌린다 */
export default function WakeScreen({ mode, onDone }: { mode: Mode; onDone: () => void }) {
  const v = useVehicle()
  const items = [
    { label: '좌석을 운전 위치로 되돌렸어요', skip: mode !== 'full' },
    { label: '공조를 휴식 전 설정으로 되돌렸어요', skip: mode === 'basic' },
    { label: '조명을 원래 밝기로 켰어요', skip: mode === 'basic' },
  ].filter((i) => !i.skip)
  const [done, setDone] = useState(0)
  useEffect(() => {
    if (done >= items.length) return
    const t = window.setTimeout(() => setDone((d) => d + 1), 1100)
    return () => window.clearTimeout(t)
  }, [done, items.length])

  const restored = done >= items.length
  const nearTarget = v.batteryLevel >= v.targetLevel - 3

  return (
    <main className="screen screen--split wake">
      <section>
        <span className="chip chip--dark">잘 쉬셨어요?</span>
        <h1 className="headline headline--dark">{nearTarget ? '충전이 곧 끝납니다' : '휴식을 마쳤어요'}</h1>
        <p className="sub sub--dark">
          현재 충전량 {Math.floor(v.batteryLevel)}% · 목표 {v.targetLevel}%
        </p>
        <div className="actions" style={{ marginTop: 44 }}>
          <button className="btn btn--dark" disabled={!restored} onClick={onDone}>
            {restored ? '출발 준비 완료' : '복귀하는 중…'}
          </button>
        </div>
      </section>
      <ul className="steps steps--light">
        {items.map((it, i) => (
          <li key={it.label} className={`step ${i < done ? 'step--done' : 'step--active'}`}>
            <span className="step__dot">{i < done ? '✓' : <span className="spinner spinner--dark" />}</span>
            <span className="step__text">{i < done ? it.label : it.label.replace('되돌렸어요', '되돌리는 중').replace('켰어요', '켜는 중')}</span>
          </li>
        ))}
        {mode !== 'full' && (
          <li className="step step--done step--manual">
            <span className="step__dot">!</span>
            <span className="step__text">{mode === 'basic' ? '좌석과 공조는 직접 확인해 주세요' : '좌석 위치는 직접 확인해 주세요'}</span>
          </li>
        )}
        <li className="step step--done">
          <span className="step__dot">✓</span>
          <span className="step__text">문 잠금 상태 확인 완료</span>
        </li>
      </ul>
    </main>
  )
}
