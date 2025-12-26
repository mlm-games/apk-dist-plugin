package org.mlm

import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

abstract class CopyApksTask @Inject constructor() : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val outFolder: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val fileNamePrefix: Property<String>

    @TaskAction
    fun run() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
        
        if (builtArtifacts == null) {
            logger.warn("No artifacts found to distribute for ${variantName.get()}")
            return
        }

        val outDir = outFolder.get().asFile
        if (!outDir.exists()) outDir.mkdirs()

        builtArtifacts.elements.forEach { artifact ->
            val abi = artifact.filters
                .firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier
            
            val abiSuffix = abi ?: "universal"
            val versionName = artifact.versionName ?: "no-version"
            val prefix = fileNamePrefix.get()
            
            // Format: {prefix}-{variant}-{version}-{abi}.apk
            // Example: appp-release-1.0.0-arm64-v8a.apk
            val outName = "$prefix-${variantName.get()}-$versionName-$abiSuffix.apk"
            
            val inFile = File(artifact.outputFile)
            val outFile = File(outDir, outName)

            if (inFile.exists()) {
                inFile.copyTo(outFile, overwrite = true)
                logger.lifecycle("Distributed APK: ${outFile.absolutePath}")
            } else {
                logger.warn("Warning: Source APK not found at: ${inFile.absolutePath}")
            }
        }
    }
}