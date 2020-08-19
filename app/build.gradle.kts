plugins {
    id(Plugin.application)
    kotlin(Plugin.kotlin_android)
    kotlin(Plugin.kotlin_ext)
}
android {
    compileSdkVersion(Version.compileSdk)
    defaultConfig {
        applicationId = "com.edit.image.sample"
        minSdkVersion(Version.minSdk)
        targetSdkVersion(Version.targetSdk)
        versionCode = Version.versionCode
        versionName = Version.versionName
    }
}
dependencies {
    implementation(Dep.appcompat)
    implementation(Dep.subsampling)
    implementation(Dep.kotlin)
    implementation(Dep.core)
    implementation(Dep.circle)
    implementation(Dep.eraser)
    implementation(Dep.line)
    implementation(Dep.point)
    implementation(Dep.rect)
    implementation(Dep.text)
}