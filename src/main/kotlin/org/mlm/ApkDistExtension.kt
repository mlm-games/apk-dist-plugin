package org.mlm

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ApkDistExtension @Inject constructor(layout: ProjectLayout) {
    
    /**
     * The directory where the renamed APKs will be copied.
     * Default: {buildDir}/dist/apk
     */
    abstract val distDirectory: DirectoryProperty

    /**
     * Helper to enable or disable the plugin logic per variant if needed.
     * Default: true
     */
    abstract val enabled: Property<Boolean>

    /**
     * Prefix for the generated APK file name.
     * Default: The project name (e.g., "app")
     */
    abstract val artifactNamePrefix: Property<String>

    init {
        distDirectory.convention(layout.buildDirectory.dir("dist/apk"))
        enabled.convention(true)
        artifactNamePrefix.convention("app")
    }
}