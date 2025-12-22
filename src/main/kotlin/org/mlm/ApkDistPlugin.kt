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
            val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                if (extension.enabled.get()) {

                    variant.outputs.forEach { output ->
                        val abi = output.filters.find { it.filterType == FilterConfiguration.FilterType.ABI }?.identifier
                        val offset = when (abi) {
                            "x86" -> -3
                            "x86_64" -> -2
                            "armeabi-v7a" -> -1
                            "arm64-v8a" -> 0
                            else -> 1
                        }
                        output.versionCode.set(output.versionCode.map { it + offset })
                    }

                    val capName = variant.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                    val taskName = "dist${capName}Apks"

                    val copyTaskProvider = project.tasks.register(taskName, CopyApksTask::class.java)

                    copyTaskProvider.configure {
                        outFolder.set(extension.distDirectory.dir(variant.name))
                        apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
                        builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
                        variantName.set(variant.name)
                        fileNamePrefix.set(extension.artifactNamePrefix)
                    }

                    project.tasks.named("assemble$capName").configure {
                        finalizedBy(copyTaskProvider)
                    }
                }
            }
        }
    }
}