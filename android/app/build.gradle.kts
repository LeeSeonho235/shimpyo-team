// 구조 틀 단계. 플러그인·SDK 버전은 Pleos Connect Emulator 환경 요구사항을 확인한 뒤 맞춘다.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shimpyo.app"
}

dependencies {
    implementation("ai.pleos.playground:Vehicle:2.3.2")
    // TODO: TextToSpeech, SpeechToText, NaviHelper, FusedLocation 의존성 좌표는 각 SDK Importing 페이지에서 확인
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
