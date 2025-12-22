# apk-dist-plugin

A tiny Gradle plugin that:
1) adjusts per-ABI APK `versionCode` offsets (so split APKs don’t collide), and
2) copies the built APK(s) into AGP’s `build/outputs/apk/…` tree with a predictable filename.

It **does not delete** or replace AGP outputs; it just copies/renames for distribution.

## Requirements
- Android application module (`com.android.application`)
- Uses the Android Components / Artifacts APIs (BuiltArtifacts metadata). 

## Apply

### Kotlin DSL (`app/build.gradle.kts`)
```kotlin
plugins {
  id("io.github.mlm-games.apk-dist") version "0.x.y"
}
```

## Configure (optional)

```kotlin
apkDist {
  // default: true
  enabled.set(true)

  // default: project.name
  artifactNamePrefix.set("app")

  // default: build/outputs/apk (AGP outputs tree)
  // distDirectory.set(layout.buildDirectory.dir("outputs/apk"))
}
```

## Output
When you run `assemble<Variant>`, the plugin creates and runs a task:
- `dist<Variant>Apks`

It copies APKs to:
- `app/build/outputs/apk/<variantName>/`

Filename format:
- `{prefix}-{variant}-{versionName}-{abi}.apk`
- ABI becomes `universal` when there is no ABI filter.
