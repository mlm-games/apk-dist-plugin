package org.mlm

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.FilterConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Locale

class ApkDistPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("apkDist", ApkDistExtension::class.java)
        extension.artifactNamePrefix.convention(project.name)

        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                if (!extension.enabled.get()) return@onVariants

                variant.outputs.forEach { output ->
                    val abi = output.filters
                        .find { it.filterType == FilterConfiguration.FilterType.ABI }
                        ?.identifier

                    val offset = when (abi) {
                        "x86" -> -3
                        "x86_64" -> -2
                        "armeabi-v7a" -> -1
                        "arm64-v8a" -> 0
                        else -> 1 // universal / unknown
                    }

                    val base = output.versionCode.orNull
                    if (base != null) {
                        output.versionCode.set(base + offset)
                    }
                }

                val capName = variant.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }

                val taskName = "dist${capName}Apks"
                val assembleTaskName = "assemble$capName"

                val outDirProvider = extension.distDirectory.dir(variant.name)

                val declaredOutputsProvider = project.providers.provider {
                    val prefix = extension.artifactNamePrefix.get()
                    val dir = outDirProvider.get()

                    variant.outputs.map { output ->
                        val abi = output.filters
                            .find { it.filterType == FilterConfiguration.FilterType.ABI }
                            ?.identifier

                        val abiSuffix = abi ?: "universal"
                        val versionName = output.versionName.orNull ?: "no-version"

                        dir.file("$prefix-${variant.name}-$versionName-$abiSuffix.apk")
                    }
                }

                val copyTaskProvider = project.tasks.register(taskName, CopyApksTask::class.java) {
                    outFolder.set(outDirProvider)

                    apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
                    builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())

                    variantName.set(variant.name)
                    fileNamePrefix.set(extension.artifactNamePrefix)

                    outputApks.set(declaredOutputsProvider)

                    group = "distribution"
                    description = "Copies APK(s) for ${variant.name} to a predictable name in ${outDirProvider.get().asFile}"
                }

                // Run after assemble<Variant>
                project.tasks.named(assembleTaskName).configure {
                    finalizedBy(copyTaskProvider)
                }
            }
        }
    }
}