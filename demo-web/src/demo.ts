const params = new URLSearchParams(window.location.search)

/**
 * 시연 배속. ?speed=60 이면 충전과 휴식 타이머가 모두 60배 빠르게 흐른다.
 * 충전량, 예상 시간, 휴식 타이머가 같은 배속을 쓰므로 표시와 실제 진행이 일치한다.
 */
export const DEMO_SPEED = Math.max(1, Number(params.get('speed')) || 1)

/** 주소로 처음 연 화면 번호 (캡처용) */
export const INITIAL_SCREEN = Number(params.get('screen')) || 1
