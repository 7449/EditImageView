buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(ClassPath.gradle)
        classpath(ClassPath.bintray)
        classpath(ClassPath.kotlin)
    }
}
allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}