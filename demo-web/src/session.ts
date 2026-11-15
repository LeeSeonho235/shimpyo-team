export type RestType = 'nap' | 'breathing' | 'stretching'
export type Duration = 10 | 15 | 20 | 'untilCharged'

export interface Session {
  restType: RestType
  duration: Duration
}

export const REST_LABEL: Record<RestType, string> = {
  nap: '낮잠',
  breathing: '호흡',
  stretching: '스트레칭',
}
