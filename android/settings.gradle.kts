// 구조 틀 단계. 아직 빌드 검증하지 않았다.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/") }
    }
}
rootProject.name = "shimpyo"
include(":app")
