import { useEffect, useState } from 'react'
import { Session } from './session'
import StatusBar from './StatusBar'
import SuggestScreen from './screens/SuggestScreen'
import ChooseScreen from './screens/ChooseScreen'
import PreparingScreen from './screens/PreparingScreen'
import RestingScreen from './screens/RestingScreen'
import WakeScreen from './screens/WakeScreen'
import DrivingSuggestScreen from './screens/DrivingSuggestScreen'

export type ScreenNo = 1 | 2 | 3 | 4 | 5 | 6
/** 완전/부분/기본 모드. 실제 앱에서는 capability 확인 결과로 정해진다 */
export type Mode = 'full' | 'partial' | 'basic'

const params = new URLSearchParams(window.location.search)

function readScreen(): ScreenNo {
  const n = Number(params.get('screen'))
  return n >= 1 && n <= 6 ? (n as ScreenNo) : 1
}

function readMode(): Mode {
  const m = params.get('mode')
  return m === 'partial' || m === 'basic' ? m : 'full'
}

/** 시연 배속. ?speed=60 이면 휴식 타이머가 60배 빠르게 흐른다 */
export const DEMO_SPEED = Math.max(1, Number(params.get('speed')) || 1)

function useFrameScale() {
  const calc = () => Math.min(1, window.innerWidth / 1280, window.innerHeight / 720)
  const [scale, setScale] = useState(calc)
  useEffect(() => {
    const onResize = () => setScale(calc())
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [])
  return scale
}

export default function App() {
  const [screen, setScreen] = useState<ScreenNo>(readScreen)
  const [session, setSession] = useState<Session>({ restType: 'nap', duration: 20 })
  const mode = readMode()
  const scale = useFrameScale()

  const go = (n: ScreenNo) => {
    setScreen(n)
    const url = new URL(window.location.href)
    url.searchParams.set('screen', String(n))
    window.history.replaceState(null, '', url)
  }

  const light = screen === 5

  return (
    <div className="stage">
      <div className={`frame ${light ? 'frame--light' : ''} ${screen === 4 ? 'frame--rest' : ''}`} style={{ transform: `scale(${scale})` }}>
        {screen !== 4 && <StatusBar light={light} driving={screen === 6} />}
        {screen === 1 && <SuggestScreen onStart={() => go(2)} onLater={() => go(6)} />}
        {screen === 2 && (
          <ChooseScreen
            initial={session}
            onBack={() => go(1)}
            onStart={(s) => {
              setSession(s)
              go(3)
            }}
          />
        )}
        {screen === 3 && <PreparingScreen mode={mode} onDone={() => go(4)} onCancel={() => go(1)} />}
        {screen === 4 && <RestingScreen session={session} onEnd={() => go(5)} />}
        {screen === 5 && <WakeScreen mode={mode} onDone={() => go(6)} />}
        {screen === 6 && <DrivingSuggestScreen onAccept={() => go(1)} onDecline={() => go(1)} />}
      </div>
    </div>
  )
}
