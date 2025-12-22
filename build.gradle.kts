@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.vanniktech.maven.publish)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

configure<GradlePluginDevelopmentExtension> {

    plugins {
        create("apkDistPlugin") {
            id = "io.github.mlm-games.apk-dist"
            implementationClass = "org.mlm.ApkDistPlugin"
            tags.set(listOf("android", "apk", "distribution"))
        }
    }
}