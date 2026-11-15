package com.shimpyo.app.ui

/**
 * 화면 6개. 디자인과 흐름은 demo-web/ 의 시연 화면을 따른다.
 * 1단계에서는 브라우저 시연만 있고, Compose 구현은 2단계에서 한다.
 */
enum class Screen(val title: String) {
    SUGGEST("휴식 제안"),
    CHOOSE("휴식 종류 선택"),
    PREPARING("준비 중"),
    RESTING("휴식 중"),
    WAKE("기상과 복귀"),
    DRIVING_SUGGEST("휴식 권유"),
}
