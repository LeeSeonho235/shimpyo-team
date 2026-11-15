import { useEffect, useState } from 'react'
import { useVehicle } from './vehicle/FakeVehicle'

export function Logo({ size = 22 }: { size?: number }) {
  return (
    <span className="logo" style={{ fontSize: size }}>
      <span className="logo__mark">,</span>쉼표
    </span>
  )
}

export default function StatusBar({ light, driving }: { light?: boolean; driving?: boolean }) {
  const v = useVehicle()
  const [now, setNow] = useState(new Date())
  useEffect(() => {
    const t = window.setInterval(() => setNow(new Date()), 10_000)
    return () => window.clearInterval(t)
  }, [])
  const time = now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })

  return (
    <div className={`statusbar ${light ? 'statusbar--light' : ''}`}>
      <Logo />
      <div className="statusbar__right">
        {driving ? (
          <span className="pill">주행 중 · 연속 2시간 4분</span>
        ) : (
          <span className="pill pill--accent">
            <BoltIcon /> 충전 중 {Math.floor(v.batteryLevel)}%
          </span>
        )}
        <span className="statusbar__time">{time}</span>
      </div>
    </div>
  )
}

export function BoltIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" aria-hidden>
      <path d="M13 2 4 14h7l-1 8 9-12h-7l1-8z" />
    </svg>
  )
}
