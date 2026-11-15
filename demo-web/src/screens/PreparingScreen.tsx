import { useEffect, useState } from 'react'
import type { Mode } from '../App'

type Item = { key: string; label: string; done: string; unsupported?: boolean }

/**
 * 3. 준비 중 — 현재 값 저장 후 좌석(Seat) → 공조(HVAC) → 조명(Light) 순서로 설정.
 * 부분/기본 모드에서는 지원되지 않는 항목을 "직접 조절" 안내로 바꾼다.
 */
export default function PreparingScreen({ mode, onDone, onCancel }: { mode: Mode; onDone: () => void; onCancel: () => void }) {
  const items: Item[] = [
    { key: 'save', label: '지금 설정 저장', done: '좌석 위치와 공조 설정을 저장했어요' },
    { key: 'seat', label: '좌석 눕히는 중', done: '좌석을 눕혔어요', unsupported: mode !== 'full' },
    { key: 'hvac', label: '온도 맞추는 중', done: '실내 온도 22℃ · 외기 유입 유지', unsupported: mode === 'basic' },
    { key: 'light', label: '조명 낮추는 중', done: '조명을 은은하게 낮췄어요', unsupported: mode === 'basic' },
  ]
  const [step, setStep] = useState(0)

  useEffect(() => {
    if (step >= items.length) return
    const t = window.setTimeout(() => setStep((s) => s + 1), step === 0 ? 900 : 1400)
    return () => window.clearTimeout(t)
  }, [step, items.length])

  const finished = step >= items.length

  return (
    <main className="screen screen--split">
      <section>
        <h2 className="title">{finished ? '준비가 끝났어요' : '휴식 모드를 준비하고 있어요'}</h2>
        <p className="sub">휴식이 끝나면 저장한 설정으로 그대로 되돌려 드려요.</p>
        <div className="actions" style={{ marginTop: 48 }}>
          <button className="btn btn--primary" disabled={!finished} onClick={onDone}>
            휴식 시작
          </button>
          <button className="btn btn--ghost" onClick={onCancel}>
            취소
          </button>
        </div>
      </section>
      <ul className="steps">
        {items.map((it, i) => {
          const state = i < step ? 'done' : i === step ? 'active' : 'wait'
          return (
            <li key={it.key} className={`step step--${state} ${it.unsupported && state === 'done' ? 'step--manual' : ''}`}>
              <span className="step__dot">{state === 'done' ? (it.unsupported ? '!' : '✓') : state === 'active' ? <span className="spinner" /> : ''}</span>
              <span className="step__text">
                {state === 'done' ? (it.unsupported ? manualText(it.key) : it.done) : it.label}
              </span>
            </li>
          )
        })}
      </ul>
    </main>
  )
}

function manualText(key: string) {
  if (key === 'seat') return '좌석은 직접 조절해 주세요'
  if (key === 'hvac') return '온도는 직접 조절해 주세요'
  return '조명은 직접 조절해 주세요'
}
