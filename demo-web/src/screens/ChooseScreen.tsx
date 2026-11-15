import { useState } from 'react'
import { Duration, RestType, Session } from '../session'
import { FakeVehicle, useVehicle } from '../vehicle/FakeVehicle'

const TYPES: { key: RestType; title: string; desc: string; icon: JSX.Element }[] = [
  { key: 'nap', title: '낮잠', desc: '좌석을 눕히고 조명을 낮춰\n짧게 잠들어요', icon: <MoonIcon /> },
  { key: 'breathing', title: '호흡', desc: '음성 안내에 맞춰\n천천히 숨을 골라요', icon: <WaveIcon /> },
  { key: 'stretching', title: '스트레칭', desc: '목과 어깨를 푸는\n동작을 안내해요', icon: <StretchIcon /> },
]

/** 2. 휴식 종류 선택 */
export default function ChooseScreen({
  initial,
  onStart,
  onBack,
}: {
  initial: Session
  onStart: (s: Session) => void
  onBack: () => void
}) {
  const v = useVehicle()
  const [type, setType] = useState<RestType>(initial.restType)
  const [dur, setDur] = useState<Duration>(initial.duration)
  const durations: { key: Duration; label: string }[] = [
    { key: 10, label: '10분' },
    { key: 15, label: '15분' },
    { key: 20, label: '20분' },
    { key: 'untilCharged', label: `충전 완료까지 · 약 ${FakeVehicle.minutesToTarget(v)}분` },
  ]
  return (
    <main className="screen">
      <h2 className="title">어떻게 쉴까요?</h2>
      <div className="cards">
        {TYPES.map((t) => (
          <button key={t.key} className={`card ${type === t.key ? 'card--on' : ''}`} onClick={() => setType(t.key)}>
            <span className="card__icon">{t.icon}</span>
            <span className="card__title">{t.title}</span>
            <span className="card__desc">{t.desc}</span>
          </button>
        ))}
      </div>
      <div className="row-between">
        <div className="seg">
          {durations.map((d) => (
            <button key={String(d.key)} className={`seg__item ${dur === d.key ? 'seg__item--on' : ''}`} onClick={() => setDur(d.key)}>
              {d.label}
            </button>
          ))}
        </div>
        <div className="actions">
          <button className="btn btn--ghost" onClick={onBack}>
            뒤로
          </button>
          <button className="btn btn--primary" onClick={() => onStart({ restType: type, duration: dur })}>
            휴식 시작
          </button>
        </div>
      </div>
    </main>
  )
}

function MoonIcon() {
  return (
    <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6">
      <path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z" />
    </svg>
  )
}
function WaveIcon() {
  return (
    <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
      <path d="M2 9c2.5-3 5-3 7.5 0s5 3 7.5 0 3.5-2 5-1" />
      <path d="M2 15c2.5-3 5-3 7.5 0s5 3 7.5 0 3.5-2 5-1" />
    </svg>
  )
}
function StretchIcon() {
  return (
    <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round">
      <circle cx="12" cy="4.5" r="2" />
      <path d="M4 9l8 2 8-2M12 11v5l-3 5M12 16l3 5" />
    </svg>
  )
}
