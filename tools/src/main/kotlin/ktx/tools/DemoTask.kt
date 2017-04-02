package ktx.tools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class DemoTask : DefaultTask() {
    @TaskAction
    fun generateKotlinAssetsSources() {
        println("custom generate kotlin asset sources plugin running!")

        var extension: DemoPluginExtension? = project.extensions.findByType(DemoPluginExtension::class.java)
        if (extension == null) {
            println("extension is null")
            extension = DemoPluginExtension()
        }

        println("custom generate kotlin asset sources plugin ran!")
    }
}
