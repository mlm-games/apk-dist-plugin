package org.mlm

import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject

abstract class CopyApksTask @Inject constructor() : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkFolder: DirectoryProperty

    /**
     * Should NOT mark this as @OutputDirectory if it points into AGP-managed folders like build/outputs/apk/<variant>.
     * Otherwise Gradle considers the whole directory your task output and will flag AGP tasks touching files there.
     */
    @get:Internal
    abstract val outFolder: DirectoryProperty

    /**
     * Declare only the specific APK files we generate as outputs.
     * This avoids overlapping with AGP metadata/redirect tasks in the same folder.
     */
    @get:OutputFiles
    abstract val outputApks: ListProperty<RegularFile>

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

        val prefix = fileNamePrefix.get()
        val variant = variantName.get()

        builtArtifacts.elements.forEach { artifact ->
            val abi = artifact.filters
                .firstOrNull { it.filterType == FilterConfiguration.FilterType.ABI }
                ?.identifier

            val abiSuffix = abi ?: "universal"
            val versionName = artifact.versionName ?: "no-version"

            // Format: {prefix}-{variant}-{version}-{abi}.apk
            val outName = "$prefix-$variant-$versionName-$abiSuffix.apk"

            val inFile = File(artifact.outputFile)
            val outFile = File(outDir, outName)

            if (inFile.exists()) {
                inFile.copyTo(outFile, overwrite = true)
                logger.lifecycle("Distributed APK: ${outFile.absolutePath}")
            } else {
                logger.warn("Source APK not found at: ${inFile.absolutePath}")
            }
        }
    }
}