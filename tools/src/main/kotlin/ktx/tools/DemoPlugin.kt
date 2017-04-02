package ktx.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

class DemoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("demoSetting", DemoPluginExtension::class.java)
        project.tasks.create("generateKotlinAssetSources", DemoTask::class.java)
    }
}
