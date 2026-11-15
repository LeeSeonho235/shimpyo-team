export default function BatteryRing({
  level,
  target,
  size = 300,
  light,
}: {
  level: number
  target: number
  size?: number
  light?: boolean
}) {
  const r = size / 2 - 14
  const c = 2 * Math.PI * r
  const pct = Math.min(100, level) / 100
  const targetAngle = (target / 100) * 360 - 90
  const tx = size / 2 + r * Math.cos((targetAngle * Math.PI) / 180)
  const ty = size / 2 + r * Math.sin((targetAngle * Math.PI) / 180)
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="ring">
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={light ? '#DCE3EA' : '#1E2530'} strokeWidth="16" />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke="var(--accent)"
        strokeWidth="16"
        strokeLinecap="round"
        strokeDasharray={`${c * pct} ${c}`}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
        style={{ transition: 'stroke-dasharray 1s linear' }}
      />
      <circle cx={tx} cy={ty} r="7" fill={light ? '#0E1116' : '#F2F5F7'} />
    </svg>
  )
}
