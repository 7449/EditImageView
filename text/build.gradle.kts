plugins {
    id(Plugin.library)
    kotlin(Plugin.kotlin_android)
}
apply(from = "../gradle/UPLOAD.gradle")
android {
    compileSdkVersion(Version.compileSdk)
    defaultConfig {
        minSdkVersion(Version.minSdk)
        targetSdkVersion(Version.targetSdk)
        versionCode = Version.versionCode
        versionName = Version.versionName
    }
    compileOptions { kotlinOptions.freeCompilerArgs += listOf("-module-name", "com.ydevelop.text") }
}
dependencies {
    compileOnly(project(":core"))
//    compileOnly(Dep.core)
    compileOnly(Dep.kotlin)
}