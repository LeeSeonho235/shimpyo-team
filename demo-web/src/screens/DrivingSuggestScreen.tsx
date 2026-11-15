/** 6. 휴식 권유 — 주행 중에는 음성으로만 동작하고, 화면에는 작은 카드만 띄운다 */
export default function DrivingSuggestScreen({ onAccept, onDecline }: { onAccept: () => void; onDecline: () => void }) {
  return (
    <main className="screen drive">
      <svg className="drive__map" viewBox="0 0 1280 650" preserveAspectRatio="xMidYMid slice" aria-hidden>
        <rect width="1280" height="650" fill="#11161D" />
        <g stroke="#1A212B" strokeWidth="2">
          {Array.from({ length: 14 }).map((_, i) => (
            <line key={'v' + i} x1={i * 100} y1="0" x2={i * 100 - 120} y2="650" />
          ))}
          {Array.from({ length: 8 }).map((_, i) => (
            <line key={'h' + i} x1="0" y1={i * 90} x2="1280" y2={i * 90 + 40} />
          ))}
        </g>
        <g transform="translate(0,-160)">
        <path d="M640 700 C 640 520, 700 420, 820 300 S 1000 120, 1060 -20" fill="none" stroke="#2A3340" strokeWidth="46" />
        <path d="M640 700 C 640 520, 700 420, 820 300 S 1000 120, 1060 -20" fill="none" stroke="var(--accent)" strokeWidth="10" strokeLinecap="round" opacity="0.85" />
        <circle cx="905" cy="215" r="16" fill="#0E1116" stroke="var(--accent)" strokeWidth="4" />
        <text x="930" y="210" fill="#C9D2DC" fontSize="20" fontFamily="inherit">졸음쉼터 8km</text>
        <path d="M646 590 l-20 34 20 -10 20 10z" fill="#F2F5F7" />
        </g>
      </svg>

      <div className="voicecard">
        <div className="voicecard__wave" aria-hidden>
          {Array.from({ length: 5 }).map((_, i) => (
            <span key={i} style={{ animationDelay: `${i * 0.12}s` }} />
          ))}
        </div>
        <div className="voicecard__body">
          <div className="voicecard__text">2시간째 운전 중입니다. 8km 앞 졸음쉼터로 안내할까요?</div>
          <div className="voicecard__hint">"응" 또는 "아니"라고 말씀해 주세요</div>
        </div>
        <button className="btn btn--primary btn--sm" onClick={onAccept}>
          안내
        </button>
        <button className="btn btn--ghost btn--sm" onClick={onDecline}>
          괜찮아요
        </button>
      </div>
    </main>
  )
}
