@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension // <--- THIS WAS MISSING

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.vanniktech.maven.publish)
}

val groupProp = project.findProperty("GROUP") as String
val versionProp = project.findProperty("VERSION_NAME") as String

group = groupProp
version = versionProp

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
    website.set(project.findProperty("POM_URL") as String)
    vcsUrl.set(project.findProperty("POM_SCM_URL") as String)

    plugins {
        create("apkDistPlugin") {
            id = "io.github.mlm-games.apk-dist"
            implementationClass = "org.mlm.ApkDistPlugin"
            displayName = project.findProperty("POM_NAME") as String
            description = project.findProperty("POM_DESCRIPTION") as String
            tags.set(listOf("android", "apk", "distribution"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(groupProp, "apk-dist-plugin", versionProp)
}